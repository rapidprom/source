package com.rapidminer.util;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.ioobjects.ProMIOObject;

public class ProMIOObjectList {
	
	private static ProMIOObjectList singleton = new ProMIOObjectList();
	
	private List<ProMIOObject> list = new ArrayList<ProMIOObject>();
	
	private ProMIOObjectList () {
		
	}
	
	public static ProMIOObjectList getInstance() {
		return singleton;
	}
	
	public void addToList(ProMIOObject item) {
		list.add(item);
	}
	
	public void clear() {
		for (ProMIOObject item : list) {
			item.clear();
		}
		list.clear();
	}

}
