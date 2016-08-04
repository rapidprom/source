package org.rapidprom.operators.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.processmining.openslex.metamodel.SLEXMMAttribute;
import org.processmining.openslex.metamodel.SLEXMMClass;
import org.processmining.openslex.metamodel.SLEXMMDataModel;
import org.processmining.openslex.metamodel.SLEXMMRelationship;
import org.processmining.openslex.metamodel.SLEXMMStorageMetaModel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

public class DataModelMM {

	public static final String AT_DATAMODEL = "datamodel";
	public static final String AT_CLASS = "class";
	public static final String AT_ATNAME = "attribute_name";
	public static final String AT_RS_NAME = "relationship_name";
	public static final String AT_RS_CLASS_SOURCE = "class_source";
	public static final String AT_RS_CLASS_TARGET = "class_target";
	public static final String AT_RS_ATNAME_SOURCE = "attribute_source";
	public static final String AT_RS_ATNAME_TARGET = "attribute_target";
	
	private HashMap<String,SLEXMMDataModel> slxdmmap = new HashMap<>();
	
	private HashMap<String, // datamodel
		HashMap<String, // class
			HashSet<String> // attribute names
		>
	> dmmap = new HashMap<>();
	
	private HashMap<String, // datamodel
		HashMap<String, // class
			SLEXMMClass // slx Class
		>
	> slxclassmap = new HashMap<>();
	
	private HashMap<Integer, // slx Class Id
		HashMap<String, // att name
			SLEXMMAttribute // slx Attribute
		>
	> attsPerClass = new HashMap<>();
	
	private HashMap<Integer, // slx Class Id
		ArrayList<DataModelMMKey> // List of Keys
	> keysPerClass = new HashMap<>();
	
	private HashMap<Integer, // slx Class Id
		DataModelMMKey // PK Key
	> pkPerClass = new HashMap<>();
	
	private HashMap<Integer, // slx Class Id
		ArrayList<DataModelMMKey> // List of FK Keys
	> fksPerClass = new HashMap<>();
	
	private SLEXMMStorageMetaModel mm = null;
	
	public DataModelMM(SLEXMMStorageMetaModel mm, ExampleSet esclasses, ExampleSet eskeys) throws Exception {
		this.mm = mm;
		init(esclasses, eskeys);
	}
	
	private void init(ExampleSet esclasses, ExampleSet eskeys) throws Exception {
		parseClasses(esclasses);
		parseKeys(eskeys);
	}
	
	public SLEXMMClass getClassForName(String datamodel, String name) {
		if (slxclassmap.containsKey(datamodel)) {
			if (slxclassmap.get(datamodel).containsKey(name)) {
				return slxclassmap.get(datamodel).get(name);
			}
		}
		return null;
	}
	
	public SLEXMMAttribute getAttributeForName(SLEXMMClass c, String name) {
		if (attsPerClass.containsKey(c.getId())) {
			return attsPerClass.get(c.getId()).get(name);
		}
		return null;
	}
	
	public List<DataModelMMKey> getKeysForClass(SLEXMMClass c) {
		if (keysPerClass.containsKey(c.getId())) {
			return keysPerClass.get(c.getId());
		}
		return null;
	}
	
	public DataModelMMKey getPKForClass(SLEXMMClass c) {
		if (pkPerClass.containsKey(c.getId())) {
			return pkPerClass.get(c.getId());
		}
		return null;
	}
	
	public List<DataModelMMKey> getFKsForClass(SLEXMMClass c) {
		if (fksPerClass.containsKey(c.getId())) {
			return fksPerClass.get(c.getId());
		}
		return null;
	}
	
	private void parseKeys(ExampleSet eskeys) throws Exception {
		
		HashMap<String, // datamodel
			HashMap<String, // key name
				DataModelMMKey // key
			>
		> kymap = new HashMap<>();
	
//		HashMap<String, // datamodel
//			HashMap<String, // source class
//				HashSet<DataModelMMKey> // set of keys
//			>
//		> kyPerSourceMap = new HashMap<>();
		
		Attributes ats = eskeys.getAttributes();
		Attribute kydm = ats.get(AT_DATAMODEL, false);
		Attribute kynm = ats.get(AT_RS_NAME, false);
		Attribute kycls = ats.get(AT_RS_CLASS_SOURCE, false);
		Attribute kyclt = ats.get(AT_RS_CLASS_TARGET, false);
		Attribute kyats = ats.get(AT_RS_ATNAME_SOURCE, false);
		Attribute kyatt = ats.get(AT_RS_ATNAME_TARGET, false);
		
		if (kydm == null || kynm == null || kycls == null ||
				kyclt == null || kyats == null || kyatt == null) {
			throw new Exception("Required attributes not present");
		}
		
		Iterator<Example> it = eskeys.iterator();
		
		while (it.hasNext()) {
			
			Example e = it.next();
			String str_dm = e.getValueAsString(kydm);
			String str_nm = e.getValueAsString(kynm);
			String str_cls = e.getValueAsString(kycls);
			String str_clt = e.getValueAsString(kyclt);
			String str_ats = e.getValueAsString(kyats);
			String str_att = e.getValueAsString(kyatt);
			
			if (!kymap.containsKey(str_dm)) {
				kymap.put(str_dm, new HashMap<String,DataModelMMKey>());
			}
			HashMap<String, DataModelMMKey> rsmap = kymap.get(str_dm);
						
			if (!rsmap.containsKey(str_nm)) {
				
				Integer sourceClassId = null;
				
				if (slxclassmap.containsKey(str_dm)) {
					if (slxclassmap.get(str_dm).containsKey(str_cls)) {
						sourceClassId = slxclassmap.get(str_dm).get(str_cls).getId();
					}
				}
				
				Integer targetClassId = null;
				
				if (slxclassmap.containsKey(str_dm)) {
					if (slxclassmap.get(str_dm).containsKey(str_clt)) {
						targetClassId = slxclassmap.get(str_dm).get(str_clt).getId();
					}
				}
				
				if (targetClassId == null) {
					targetClassId = -1;
				}
				
				if (sourceClassId != null) {
					SLEXMMRelationship slxrs = mm.createRelationship(str_nm,
							sourceClassId, targetClassId);

					DataModelMMKey k = new DataModelMMKey(str_nm, str_cls,
							str_clt, slxrs.getId());
					rsmap.put(str_nm, k);

					if (!keysPerClass.containsKey(sourceClassId)) {
						keysPerClass.put(sourceClassId,
								new ArrayList<DataModelMMKey>());
					}
					keysPerClass.get(sourceClassId).add(k);

					if (targetClassId == -1) {
						pkPerClass.put(sourceClassId, k);
					} else {
						if (!fksPerClass.containsKey(sourceClassId)) {
							fksPerClass.put(sourceClassId,
									new ArrayList<DataModelMMKey>());
						}
						fksPerClass.get(sourceClassId).add(k);
					}
				} else {
					throw new Exception(
							"Source class name: '" + str_cls + "' not found");
				}
			}
			
			DataModelMMKey k = rsmap.get(str_nm);
			k.getAttributesMap().put(str_ats,str_att);
			
//			if (!kyPerSourceMap.containsKey(str_dm)) {
//				kyPerSourceMap.put(str_dm, new HashMap<String,HashSet<DataModelMMKey>>());
//			}
//			HashMap<String,HashSet<DataModelMMKey>> sclmap = kyPerSourceMap.get(str_dm);
//			
//			if (!sclmap.containsKey(str_cls)) {
//				sclmap.put(str_cls, new HashSet<DataModelMMKey>());
//			}
//			HashSet<DataModelMMKey> kset = sclmap.get(str_cls);
//			kset.add(k);
		}
		
		
	}

	private void parseClasses(
			ExampleSet esclasses) throws Exception {

		Attributes ats = esclasses.getAttributes();
		Attribute atdm = ats.get(AT_DATAMODEL, false);
		Attribute atcl = ats.get(AT_CLASS, false);
		Attribute atnm = ats.get(AT_ATNAME, false);

		if (atdm == null || atcl == null || atnm == null) {
			throw new Exception("Required attributes not present");
		}

		Iterator<Example> it = esclasses.iterator();

		while (it.hasNext()) {
			Example e = it.next();
			String str_dm = e.getValueAsString(atdm);
			String str_cl = e.getValueAsString(atcl);
			String str_at = e.getValueAsString(atnm);

			if (!dmmap.containsKey(str_dm)) {
				dmmap.put(str_dm, new HashMap<String, HashSet<String>>());
				slxclassmap.put(str_dm, new HashMap<String,SLEXMMClass>());
				slxdmmap.put(str_dm, mm.createDataModel(str_dm));
			}
			HashMap<String, HashSet<String>> clmap = dmmap.get(str_dm);
			SLEXMMDataModel slxdm = slxdmmap.get(str_dm);

			if (!clmap.containsKey(str_cl)) {
				clmap.put(str_cl, new HashSet<String>());
				SLEXMMClass slxcl = mm.createClass(slxdm.getId(), str_cl);
				slxclassmap.get(str_dm).put(str_cl,slxcl);
				attsPerClass.put(slxcl.getId(), new HashMap<String,SLEXMMAttribute>());
			}
			SLEXMMClass slxcl = slxclassmap.get(str_dm).get(str_cl);

			HashSet<String> atset = clmap.get(str_cl);
			atset.add(str_at);
			SLEXMMAttribute at = mm.createAttribute(slxcl.getId(), str_at);
			attsPerClass.get(slxcl.getId()).put(str_at, at);
		}

	}
	
}
