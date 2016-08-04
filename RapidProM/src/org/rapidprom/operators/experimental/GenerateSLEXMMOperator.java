package org.rapidprom.operators.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.processmining.openslex.metamodel.SLEXMMActivity;
import org.processmining.openslex.metamodel.SLEXMMActivityInstance;
import org.processmining.openslex.metamodel.SLEXMMAttribute;
import org.processmining.openslex.metamodel.SLEXMMAttributeValue;
import org.processmining.openslex.metamodel.SLEXMMCase;
import org.processmining.openslex.metamodel.SLEXMMClass;
import org.processmining.openslex.metamodel.SLEXMMEvent;
import org.processmining.openslex.metamodel.SLEXMMEventAttribute;
import org.processmining.openslex.metamodel.SLEXMMLog;
import org.processmining.openslex.metamodel.SLEXMMObject;
import org.processmining.openslex.metamodel.SLEXMMObjectVersion;
import org.processmining.openslex.metamodel.SLEXMMProcess;
import org.processmining.openslex.metamodel.SLEXMMRelation;
import org.processmining.openslex.metamodel.SLEXMMStorageMetaModel;
import org.processmining.openslex.metamodel.SLEXMMStorageMetaModelImpl;
import org.rapidprom.ioobjectrenderers.SLEXMMIOObjectVisualizationType;
import org.rapidprom.ioobjects.SLEXMMIOObject;

import com.google.gwt.dev.util.collect.HashSet;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NumericalAttribute;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.preprocessing.filter.attributes.NumericalAttributeFilter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;

public class GenerateSLEXMMOperator extends Operator {

	private final static String[] SUPPORTED_FILE_FORMATS = new String[] { "slexmm" };
	private final static String PARAMETER_KEY = "file";
	private final static String PARAMETER_DESC = "Select where to save the generated Meta Model.";
	
	private final static String EV_EXSET_ANNOTATION_FILE_NAME = "file_name";
	private final static String EV_EXSET_ANNOTATION_TIMESTAMP = "timestamp";
	private final static String EV_EXSET_ANNOTATION_TIMESTAMP_FORMAT = "timestamp_format";
	private final static String EV_EXSET_ANNOTATION_ACTIVITY = "activity";
	private final static String EV_EXSET_ANNOTATION_ACTIVITY_INSTANCE = "activity_instance";
	private final static String EV_EXSET_ANNOTATION_LIFECYCLE = "lifecycle";
	private final static String EV_EXSET_ANNOTATION_RESOURCE = "resource";
	
	private final static String VER_EXSET_ANNOTATION_FILE_NAME = "file_name";
	private final static String VER_EXSET_ANNOTATION_DATAMODEL_NAME = "datamodel_name";
	private final static String VER_EXSET_ANNOTATION_CLASS_NAME = "class_name";
	private final static String VER_EXSET_ANNOTATION_START_TIMESTAMP = "start_timestamp";
	private final static String VER_EXSET_ANNOTATION_END_TIMESTAMP = "end_timestamp";
	private final static String VER_EXSET_ANNOTATION_TIMESTAMP_FORMAT = "timestamp_format";
	
	private final static String EV_TO_VER_MAPPING_EVID_FIELD = "eventId";
	private final static String EV_TO_VER_MAPPING_VERID_FIELD = "versionId";
	private final static String EV_TO_VER_MAPPING_LABEL_FIELD = "label";
	
	private final static String LOGS_PROC_ID_FIELD = "processId";
	private final static String LOGS_LOG_ID_FIELD = "logId";
	private final static String LOGS_TRACE_ID_FIELD = "traceId";
	private final static String LOGS_EVENT_ID_FIELD = "eventId";

	private InputPort inputDMClasses = getInputPorts().createPort (
			"Classes definition", ExampleSet.class);
	
	private InputPort inputDMKeys = getInputPorts().createPort(
			"Keys definition", ExampleSet.class);

	private InputPort inputVersionsCollection = getInputPorts().createPort(
			"Versions collection", new IOObjectCollection<ExampleSet>().getClass());

	private InputPort inputEventsCollection = getInputPorts().createPort(
			"Events collection", new IOObjectCollection<ExampleSet>().getClass());
	
	private InputPort inputEventsToObjectV = getInputPorts().createPort(
			"EvToObjV mapping", ExampleSet.class);
	
	private InputPort inputLogs = getInputPorts().createPort(
			"Logs table", ExampleSet.class);
	
	private OutputPort outputMM = getOutputPorts().createPort(
			"OpenSLEX Meta Model");
	
	private MetaData outputMD = new MetaData(SLEXMMIOObject.class);
	
	public GenerateSLEXMMOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(outputMM, SLEXMMIOObject.class));
		//outputMMSet.deliverMD(outputMD);
	}
	
	@Override
	public void doWork() throws OperatorException {		
		Logger logger = LogService.getRoot();
		logger.log(Level.INFO, "Start: Generate MM");
		long time = System.currentTimeMillis();
		
		boolean failed = true;
		String msgFailure = "Generation of MM failed";
		SLEXMMStorageMetaModelImpl mm = null;
		//MetaData md = inputMM.getMetaData();
		
		//SLEXMMIOObject mmIO = inputMM.getData(SLEXMMIOObject.class);
		//SLEXMMStorageMetaModelImpl slxmm = mmIO.getArtifact();
		
//		String query = getParameterAsString(PARAMETER_1);

		ExampleSet exsDMClasses = inputDMClasses.getData(ExampleSet.class);
		ExampleSet exsDMKeys = inputDMKeys.getData(ExampleSet.class);
		
		try {			
			String fileName = getParameter(PARAMETER_KEY);
			if (fileName == null) {
				throw new Exception("File parameter not set");
			} else {
				File f = new File(fileName);
				f.delete();
				f.createNewFile();
				mm = new SLEXMMStorageMetaModelImpl(f.getParent(), f.getName());
				mm.setAutoCommit(false);
			}
			
			DataModelMM dmm = new DataModelMM(mm,exsDMClasses,exsDMKeys);
			
			HashMap<Double,Integer> evIdMap = new HashMap<>();
			HashMap<Double,Integer> verIdMap = new HashMap<>();
			HashMap<Integer,SLEXMMActivityInstance> evToAIMap = new HashMap<>();
			
			mm.commit();
			
			if (inputEventsCollection.isConnected()) {
				IOObjectCollection<ExampleSet> evCol = inputEventsCollection.getData(IOObjectCollection.class);
				if (evCol != null) {
					importEvents(evCol,mm,evIdMap,evToAIMap);
				}
			}
			
			if (inputVersionsCollection.isConnected()) {
				IOObjectCollection<ExampleSet> verCol = inputVersionsCollection.getData(IOObjectCollection.class);
				if (verCol != null) {
					importVersions(verCol,dmm,mm,verIdMap);
				}
			}
			
			if (inputEventsToObjectV.isConnected()) {
				ExampleSet exsEvToObjV = inputEventsToObjectV.getData(ExampleSet.class);
				if (exsEvToObjV != null) {
					importMappingEvsToObjVersions(exsEvToObjV,mm,evIdMap,verIdMap);
				}
			}
			
			if (inputLogs.isConnected()) {
				ExampleSet exsLogs = inputLogs.getData(ExampleSet.class);
				if (exsLogs != null) {
					importLogs(exsLogs,mm,evIdMap,evToAIMap);
				}
			}
			
			mm.commit();
			failed = false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		
		if (!failed) {
			SLEXMMIOObject slxmmIOObject = new SLEXMMIOObject(mm);
			slxmmIOObject
					.setVisualizationType(SLEXMMIOObjectVisualizationType.DEFAULT);
			outputMM.deliverMD(outputMD);
			outputMM.deliver(slxmmIOObject);
		} else {
			outputMM.deliverMD(outputMD);
			outputMM.deliver(null);
			throw new OperatorException(msgFailure);
		}
		logger.log(Level.INFO,
				"End: Generate MM ("
						+ (System.currentTimeMillis() - time) / 1000 + " sec)");
	}

	private void importLogs(ExampleSet exsLogs, SLEXMMStorageMetaModel mm,
			HashMap<Double,Integer> evIdMap,
			HashMap<Integer,SLEXMMActivityInstance> eventsToAIMap) throws Exception {
		
		HashMap<String,Integer> procsMap = new HashMap<>();
		HashMap<String,Integer> logsMap = new HashMap<>();
		HashMap<String,SLEXMMCase> tracesMap = new HashMap<>();
		HashMap<Integer,HashSet<Integer>> actsToProc = new HashMap<>();
		
		Attributes atts = exsLogs.getAttributes();
		Attribute procIdAt = atts.get(LOGS_PROC_ID_FIELD, false);
		Attribute logIdAt = atts.get(LOGS_LOG_ID_FIELD, false);
		Attribute traceIdAt = atts.get(LOGS_TRACE_ID_FIELD, false);
		Attribute eventIdAt = atts.get(LOGS_EVENT_ID_FIELD, false);
		
		long i = 0;
		
		if (procIdAt != null && logIdAt != null && traceIdAt != null && 
				eventIdAt != null) {
			for (Example example : exsLogs) {
				
				String procName = example.getValueAsString(procIdAt);
				String logName = example.getValueAsString(logIdAt);
				String traceName = example.getValueAsString(traceIdAt);
				double evId = example.getNumericalValue(eventIdAt);
				
				Integer evMMId = evIdMap.get(evId);
				
				if (evMMId != null) {
					Integer procId = procsMap.get(procName);
					
					if (procId == null) {
						SLEXMMProcess proc = mm.createProcess(procName);
						procId = proc.getId();
						procsMap.put(procName, procId);
						i++;
					}
					
					Integer logId = logsMap.get(logName);
					
					if (logId == null) {
						SLEXMMLog log = mm.createLog(procId, logName);
						logId = log.getId();
						logsMap.put(logName, logId);
						i++;
					}
					
					SLEXMMCase trace = tracesMap.get(traceName);
					
					if (trace == null) {
						trace = mm.createCase(traceName);
						mm.addCaseToLog(logId, trace.getId());
						tracesMap.put(traceName, trace);
						i++;
					}
					
					SLEXMMActivityInstance aiMM = eventsToAIMap.get(evMMId);
					
					if (aiMM != null) {
						try {
							trace.add(aiMM.getId());
							HashSet<Integer> procsSet = actsToProc.get(aiMM.getActivityId());
							if (procsSet == null) {
								procsSet = new HashSet<Integer>();
								actsToProc.put(aiMM.getActivityId(),procsSet);
							}
							if (!procsSet.contains(procId)) {
								mm.addActivityToProcess(procId, aiMM.getActivityId());
								procsSet.add(procId);
								i++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
										
					i++;
				}
				
				if (i >= 100) {
					mm.commit();
					i = 0L;
				} else {
					i++;
				}
			}
		} else {
			throw new Exception("Attributes missing in logs table");
		}
	}
	
	private void importMappingEvsToObjVersions(ExampleSet exsEvToObjV, SLEXMMStorageMetaModel mm,
			HashMap<Double,Integer> evIdMap, HashMap<Double,Integer> verIdMap) throws Exception {
		
		Attributes atts = exsEvToObjV.getAttributes();
		Attribute evIdAt = atts.get(EV_TO_VER_MAPPING_EVID_FIELD, false);
		Attribute verIdAt = atts.get(EV_TO_VER_MAPPING_VERID_FIELD, false);
		Attribute labelAt = atts.get(EV_TO_VER_MAPPING_LABEL_FIELD, false);
		
		long i = 0;
		
		if (evIdAt != null && verIdAt != null) {
			for (Example example : exsEvToObjV) {
				
				double evId = example.getNumericalValue(evIdAt);
				double verId = example.getNumericalValue(verIdAt);
				
				String label = "";
				if (labelAt != null) {
					label = example.getValueAsString(labelAt);
				}
				
				Integer evMMId = evIdMap.get(evId);
				Integer verMMId = verIdMap.get(verId);
				
				if (evMMId != null && verMMId != null) {
					try {
						mm.addEventToObjectVersion(verMMId, evMMId, label);
						i++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				if (i >= 100) {
					mm.commit();
					i = 0L;
				} else {
					i++;
				}
			}
		} else {
			throw new Exception("Attributes missing for mapping events to versions");
		}
	}
	
	private void computeEndTimestampsVersions(ExampleSet verExSet, List<Attribute> pkListAtts,
			SLEXMMClass clmm, SimpleDateFormat dateFormatter,
			Attribute startTimestampAtt, Attribute endTimestampAtt,
			HashMap<Double,Long> startTimePerVersion, HashMap<Double,Long> endTimePerVersion) {
		
		HashMap<Integer,HashMap<String,ArrayList<Double>>> versionsPerClassMap = new HashMap<>();
		
		for (Example example : verExSet) {
			try {
				
				StringBuilder objIdStr = new StringBuilder();
				for (Attribute at: pkListAtts) {
					objIdStr.append(example.getValueAsString(at));
					objIdStr.append('#');
				}
				
				long startTimestampL = -2L;
				if (startTimestampAtt != null) {
					startTimestampL = dateFormatter
							.parse(example.getValueAsString(startTimestampAtt))
							.getTime();
				}
				
				long endTimestampL = -1L;
				if (endTimestampAtt != null) {
					endTimestampL = dateFormatter
							.parse(example.getValueAsString(endTimestampAtt))
							.getTime();
				}
				
				startTimePerVersion.put(example.getId(), startTimestampL);
				endTimePerVersion.put(example.getId(), endTimestampL);
				
				if (!versionsPerClassMap.containsKey(clmm.getId())) {
					versionsPerClassMap.put(clmm.getId(), new HashMap<String,ArrayList<Double>>());
				}
				HashMap<String,ArrayList<Double>> objsClassMap = versionsPerClassMap.get(clmm.getId());
				
				if (!objsClassMap.containsKey(objIdStr.toString())) {
					objsClassMap.put(objIdStr.toString(), new ArrayList<Double>());
				}
				
				ArrayList<Double> versOfObj = objsClassMap.get(objIdStr.toString());
				if (endTimestampAtt == null) {
					if (versOfObj.size() > 0) {
						Double lastv = versOfObj.get(versOfObj.size() - 1);
						endTimePerVersion.put(lastv, startTimestampL);
					}
				}
				versOfObj.add(example.getId());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void parseRows(ExampleSet verExSet, List<Attribute> pkListAtts,
			HashMap<String,Integer> objMap, Long i, SLEXMMClass clmm,
			SLEXMMStorageMetaModel mm,
			HashMap<Integer,HashMap<String,ArrayList<Integer>>> versionsPerClassMap,
			HashMap<Double,Long> startTimePerExample, HashMap<Integer,Long> startTimePerVersion,
			HashMap<Double,Long> endTimePerExample, HashMap<Integer,Long> endTimePerVersion,
			HashMap<Attribute,SLEXMMAttribute> attsToVerAttsMap,
			HashMap<Integer,HashMap<String,HashMap<Integer,HashMap<Integer,String>>>> relsPerClassMap,
			List<DataModelMMKey> fks,Attributes atts, HashMap<Double,Integer> verIdMap) {
		
		for (Example example : verExSet) {
			try {
				
				Integer objId = null;
				StringBuilder objIdStr = new StringBuilder();
				for (Attribute at: pkListAtts) {
					objIdStr.append(example.getValueAsString(at));
					objIdStr.append('#');
				}
				
				objId = objMap.get(objIdStr.toString());
				if (objId == null) {
					SLEXMMObject objmm = mm.createObject(clmm.getId());
					objId = objmm.getId();
					objMap.put(objIdStr.toString(),objId);
				}
				
				long startTimestampL = startTimePerExample.get(example.getId());
				
				long endTimestampL = endTimePerExample.get(example.getId());

				SLEXMMObjectVersion ver = mm.createObjectVersion(objId,
						startTimestampL, endTimestampL);
				verIdMap.put(example.getId(), ver.getId());
				
				startTimePerVersion.put(ver.getId(), startTimestampL);
				endTimePerVersion.put(ver.getId(), endTimestampL);
				
				if (!versionsPerClassMap.containsKey(clmm.getId())) {
					versionsPerClassMap.put(clmm.getId(), new HashMap<String,ArrayList<Integer>>());
				}
				HashMap<String,ArrayList<Integer>> objsClassMap = versionsPerClassMap.get(clmm.getId());
				
				if (!objsClassMap.containsKey(objIdStr.toString())) {
					objsClassMap.put(objIdStr.toString(), new ArrayList<Integer>());
				}
				
				ArrayList<Integer> versOfObj = objsClassMap.get(objIdStr.toString());
				versOfObj.add(ver.getId());
				
				for (Attribute attribute : attsToVerAttsMap.keySet()) {
					String v = example.getValueAsString(attribute);
					SLEXMMAttribute attmm = attsToVerAttsMap.get(attribute);
					SLEXMMAttributeValue attv = mm.createAttributeValue(
							attmm.getId(), ver.getId(), v,
							String.valueOf(attribute.getValueType()));
				}
				
				if (!relsPerClassMap.containsKey(clmm.getId())) {
					relsPerClassMap.put(clmm.getId(),
							new HashMap<String, HashMap<Integer, HashMap<Integer, String>>>());
				}
				
				HashMap<String, HashMap<Integer, HashMap<Integer, String>>> relsAux1 = 
						relsPerClassMap.get(clmm.getId());
				
				if (!relsAux1.containsKey(objIdStr.toString())) {
					relsAux1.put(objIdStr.toString(), new HashMap<Integer,HashMap<Integer,String>>());
				}
				
				HashMap<Integer,HashMap<Integer,String>> relsAux2 =
						relsAux1.get(objIdStr.toString());
				
				if (!relsAux2.containsKey(ver.getId())) {
					relsAux2.put(ver.getId(), new HashMap<Integer,String>());
				}
				
				HashMap<Integer, String> relsAux3 = relsAux2.get(ver.getId());
				
				if (fks != null) {
					for (DataModelMMKey fk : fks) {
						StringBuilder targetObjId = new StringBuilder();
						for (String atStr : fk.getAttributesMap().keySet()) {
							Attribute at = atts.get(atStr);
							targetObjId.append(example.getValueAsString(at));
							targetObjId.append('#');
						}
						relsAux3.put(fk.getRelationshipId(),
								targetObjId.toString());
					}
				}
				
				if (i >= 100) {
					mm.commit();
					i = 0L;
				} else {
					i++;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void importVersions(IOObjectCollection<ExampleSet> verCol,
			DataModelMM dm, SLEXMMStorageMetaModel mm, HashMap<Double,Integer> verIdMap) throws Exception {
		
		List<ExampleSet> verColList = verCol.getObjects();
		
		HashMap<Integer, // ClassID
			HashMap<String, // ObjIDStr
				Integer // ObjID
			>
		> objPerClassMap = new HashMap<>();
		
		HashMap<Integer, // Class ID
			HashMap<String, // ObjIDStr
				HashMap<Integer, // Version ID
					HashMap<Integer, // Relationship ID
						String> // ObjIDStr
				>
			>
		> relsPerClassMap = new HashMap<>();
		
		HashMap<Integer, // Class ID
			HashMap<String, // objIDStr
				ArrayList<Integer>  // version ID
			>
		> versionsPerClassMap = new HashMap<>();
		
		HashMap<Double,Long> startTimePerExample = new HashMap<>(); // start timestamp per Example ID
		HashMap<Double,Long> endTimePerExample = new HashMap<>(); // end timestamp per Example ID
		
		HashMap<Integer,Long> startTimePerVersion = new HashMap<>(); // start timestamp per version ID
		HashMap<Integer,Long> endTimePerVersion = new HashMap<>(); // end timestamp per version ID
		
		HashMap<Integer,Integer> targetClassPerRelationship = new HashMap<>(); // class Id per Relationship ID
		
		Long i = 0L;
				
		for (ExampleSet verExSet : verColList) {
			
			Annotations an = verExSet.getAnnotations();
			String file_name = an.getAnnotation(VER_EXSET_ANNOTATION_FILE_NAME);
			String datamodel_name = an.getAnnotation(VER_EXSET_ANNOTATION_DATAMODEL_NAME);
			String class_name = an.getAnnotation(VER_EXSET_ANNOTATION_CLASS_NAME);
			String startTimestamp = an.getAnnotation(VER_EXSET_ANNOTATION_START_TIMESTAMP);
			String endTimestamp = an.getAnnotation(VER_EXSET_ANNOTATION_END_TIMESTAMP);
			String timestampFormat = an.getAnnotation(VER_EXSET_ANNOTATION_TIMESTAMP_FORMAT);
			Attribute attId = verExSet.getAttributes().getId();
			if (attId == null) {
				throw new Exception("no Id attribute set in ExampleSet "+verExSet.getName()); 
			}
			
			System.out.println("FileName: "+file_name);
			
			SimpleDateFormat dateFormatter = null;
			if (timestampFormat != null) {
				dateFormatter = new SimpleDateFormat(timestampFormat);
			} else {
				dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			
			SLEXMMClass clmm = dm.getClassForName(datamodel_name, class_name);
			
			if (!objPerClassMap.containsKey(clmm.getId())) {
				objPerClassMap.put(clmm.getId(), new HashMap<String,Integer>());
			}
			
			HashMap<String,Integer> objMap = objPerClassMap.get(clmm.getId());
			
			HashMap<Attribute,SLEXMMAttribute> attsToVerAttsMap = new HashMap<>();
			Attributes atts = verExSet.getAttributes();
						
			for (Attribute at : atts) {
				SLEXMMAttribute vAt = dm.getAttributeForName(clmm, at.getName());
				if (vAt != null) {
					attsToVerAttsMap.put(at, vAt);
				}
			}
			
			Attribute startTimestampAtt = atts.get(startTimestamp, false);
			Attribute endTimestampAtt = null;
			if (endTimestamp != null) {
				endTimestampAtt = atts.get(endTimestamp, false);
			}
						
			List<Attribute> pkListAtts = new ArrayList<>();
			DataModelMMKey pk = dm.getPKForClass(clmm);
			for (String pkAt : pk.getAttributesMap().keySet()) {
				Attribute at = atts.get(pkAt,false);
				pkListAtts.add(at);
			}
			
			HashMap<DataModelMMKey,List<Attribute>> fkAttrMap = new HashMap<>();
			List<DataModelMMKey> fks = dm.getFKsForClass(clmm);
			if (fks != null) {
				for (DataModelMMKey fk : fks) {
					SLEXMMClass targetClass = dm.getClassForName(datamodel_name, fk.getTargetClass());
					if (targetClass != null) {
						Integer targetClassId = targetClass.getId();
						targetClassPerRelationship.put(fk.getRelationshipId(),targetClassId);
					}
					List<Attribute> fkListAtts = new ArrayList<>();
					fkAttrMap.put(fk, fkListAtts);
					for (String fkAt : fk.getAttributesMap().keySet()) {
						Attribute at = atts.get(fkAt, false);
						fkListAtts.add(at);
					}
				}
			}
			
			computeEndTimestampsVersions(verExSet, pkListAtts, clmm, dateFormatter,
					 startTimestampAtt, endTimestampAtt, startTimePerExample, endTimePerExample);
			
			parseRows(verExSet, pkListAtts, objMap, i, clmm, mm, versionsPerClassMap,
					 startTimePerExample, startTimePerVersion, endTimePerExample, 
					 endTimePerVersion, attsToVerAttsMap,
					 relsPerClassMap, fks, atts, verIdMap);
		}
		
		mm.commit();
		
		i = 0L;
		
		// Go through versions and create relations
		for (Integer clId : relsPerClassMap.keySet()) {
			for (String objId : relsPerClassMap.get(clId).keySet()) {
				for (Integer vId : relsPerClassMap.get(clId).get(objId)
						.keySet()) {

					long sStartTimestamp = startTimePerVersion.get(vId);
					long sEndTimestamp = endTimePerVersion.get(vId);

					for (Integer rsId : relsPerClassMap.get(clId).get(objId)
							.get(vId).keySet()) {
						String targetObjId = relsPerClassMap.get(clId)
								.get(objId).get(vId).get(rsId);
						Integer tclId = targetClassPerRelationship.get(rsId);
						ArrayList<Integer> tvlist = versionsPerClassMap
								.get(tclId).get(targetObjId);
						if (tvlist != null) {
							for (Integer tvid : tvlist) {
								long tStartTimestamp = startTimePerVersion
										.get(tvid);
								long tEndTimestamp = endTimePerVersion
										.get(tvid);

								if (beforeOrEqual(tStartTimestamp,sEndTimestamp)
										&& afterOrEqual(tEndTimestamp,sStartTimestamp)) {
									long relationStartTimestamp = latest(
											sStartTimestamp, tStartTimestamp);
									long relationEndTimestamp = earliest(
											sEndTimestamp, tEndTimestamp);

									SLEXMMRelation rel = mm.createRelation(vId,
											tvid, rsId, relationStartTimestamp,
											relationEndTimestamp);

									if (i >= 100) {
										mm.commit();
										i = 0L;
									} else {
										i++;
									}
								}
							}
						}
					}
				}
			}
		}
		
		mm.commit();
	}
	
	private boolean beforeOrEqual(long a, long b) {
		return (a <= b || a == -2 || b == -1);
	}
	
	private boolean afterOrEqual(long a, long b) {
		return (a >= b || a == -1 || b == -2);
	}
	
	private long latest(long a, long b) {
		if (a == -1 || b == -1) {
			return -1;
		} else {
			return Math.max(a,b);
		}
	}
	
	private long earliest(long a, long b) {
		if (a == -2 || b == -2) {
			return -2;
		} else if (a == -1 || b == -1) {
			return Math.max(a,b);
		} else {
			return Math.min(a,b);
		}
	}
	
	private boolean isEmptyAnnotation(String v) {
		return (v == null || v.isEmpty() || v.equalsIgnoreCase("?"));
	}
	
	private void importEvents(IOObjectCollection<ExampleSet> evCol, SLEXMMStorageMetaModel mm,
			HashMap<Double,Integer> verIdMap,
			HashMap<Integer,SLEXMMActivityInstance> eventsToAIMap) {
		
		List<ExampleSet> evColList = evCol.getObjects();
		int order = 1;
		int lastCommitOrder = 0;
		for (ExampleSet evExSet : evColList) {
			
			HashMap<String,Integer> actInsMap = new HashMap<>();
			HashMap<String,SLEXMMActivity> actMap = new HashMap<>();
			
			Annotations an = evExSet.getAnnotations();
			String file_name = an.getAnnotation(EV_EXSET_ANNOTATION_FILE_NAME);
			String timestamp = an.getAnnotation(EV_EXSET_ANNOTATION_TIMESTAMP);
			String timestampFormat = an.getAnnotation(EV_EXSET_ANNOTATION_TIMESTAMP_FORMAT);
			String activity = an.getAnnotation(EV_EXSET_ANNOTATION_ACTIVITY);
			String activity_ins = an.getAnnotation(EV_EXSET_ANNOTATION_ACTIVITY_INSTANCE);
			String lifecycle = an.getAnnotation(EV_EXSET_ANNOTATION_LIFECYCLE);
			String resource = an.getAnnotation(EV_EXSET_ANNOTATION_RESOURCE);
			
			SimpleDateFormat dateFormatter = null;
			if (timestampFormat != null) {
				dateFormatter = new SimpleDateFormat(timestampFormat);
			} else {
				dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			
			boolean hasTimestamp = true;
			boolean hasActivity = true;
			boolean hasActivityInstance = true;
			boolean hasLifecycle = true;
			boolean hasResource = true;
			
			if (isEmptyAnnotation(timestamp)) {
				hasTimestamp = false;
			}
			
			if (isEmptyAnnotation(activity)) {
				hasActivity = false;
			}
			
			if (isEmptyAnnotation(activity_ins)) {
				hasActivityInstance = false;
			}
			
			if (isEmptyAnnotation(lifecycle)) {
				hasLifecycle = false;
			}
			
			if (isEmptyAnnotation(resource)) {
				hasResource = false;
			}
			
			HashMap<Attribute,SLEXMMEventAttribute> attsToEvAttsMap = new HashMap<>();
			Attributes atts = evExSet.getAttributes();
			for (Attribute at : atts) {
				SLEXMMEventAttribute evAt = mm.createEventAttribute(at.getName());
				attsToEvAttsMap.put(at, evAt);
			}
			
			Attribute timestampAtt = null;
			Attribute activityAtt = null;
			Attribute activity_insAtt = null;
			Attribute lifecycleAtt = null;
			Attribute resourceAtt = null;
			
			if (hasTimestamp) {
				timestampAtt = atts.get(timestamp, false);
			}
			
			if (hasActivity) {
				activityAtt = atts.get(activity, false);
			}
			
			if (hasActivityInstance) {
				activity_insAtt = atts.get(activity_ins, false);
			}
			
			if (hasLifecycle) {
				lifecycleAtt = atts.get(lifecycle, false);
			}
			
			if (hasResource) {
				resourceAtt = atts.get(resource, false);
			}
			
			for (Example example : evExSet) {
				
				SLEXMMActivity activitymm = null;
				String activityV = null;
				if (hasActivity) {
					activityV = example.getValueAsString(activityAtt);
					activitymm = actMap.get(activityV);
					if (activitymm == null) {
						activitymm = mm.createActivity(activityV);
						actMap.put(activityV, activitymm);
					}
				}
				
				SLEXMMActivityInstance ai = null;
				Integer actInsId = null;
				if (hasActivityInstance) {
					String activityInsV = example.getValueAsString(activity_insAtt);
					if (activityInsV != null) {
						actInsId = actInsMap.get(activityInsV);
						if (actInsId == null) {
							if (activitymm != null) {
								ai = mm.createActivityInstance(activitymm);
								actInsId = ai.getId();
								actInsMap.put(activityInsV, actInsId);
							}
						}
					}
				}
				if (actInsId == null) {
					if (activitymm != null) {
						ai = mm.createActivityInstance(activitymm);
						actInsId = ai.getId();
					} else {
						actInsId = -1;
					}
				}
								
				String timestampV = null;
				long timestampL = 0L;
				if (hasTimestamp) {
					timestampV = example.getValueAsString(timestampAtt);
					if (timestampV != null) {
						try {
							timestampL = dateFormatter.parse(timestampV).getTime();
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
					}
				}
				
				String lifecycleV = null;
				if (hasLifecycle) {
					lifecycleV = example.getValueAsString(lifecycleAtt);
				}
				
				String resourceV = null;
				if (hasResource) {
					resourceV = example.getValueAsString(resourceAtt);
				}
				
				SLEXMMEvent e = mm.createEvent(order, actInsId, lifecycleV, resourceV, timestampL);
				verIdMap.put(example.getId(), e.getId());
				
				if (ai != null) {
					eventsToAIMap.put(e.getId(), ai);
				}
				
				for (Attribute at : atts) {
					String v = example.getValueAsString(at);
					mm.createEventAttributeValue(
							attsToEvAttsMap.get(at).getId(), e.getId(), v,
							String.valueOf(at.getValueType()));
				}
				
				if (order - lastCommitOrder >= 100) {
					mm.commit();
					lastCommitOrder = order;
				}
			}
			
			mm.commit();
		}
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_KEY, PARAMETER_DESC,
				false, SUPPORTED_FILE_FORMATS));
//		ParameterType queryType = new ParameterTypeString(PARAMETER_1,PARAMETER_1,"",true);
//		parameterTypes.add(queryType);
//		ParameterType type =
//				new ParameterTypeConfiguration(DAPOQLangDialogCreator.class, this);
//        type.setExpert(false);
//        parameterTypes.add(type);
		
		return types;
	}
	
}
