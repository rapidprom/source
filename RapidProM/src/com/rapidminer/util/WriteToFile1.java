package com.rapidminer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class WriteToFile1 {
	
	private static WriteToFile1 instance = null;
	
	private File f;
	private BufferedWriter writer;
	private FileOutputStream fileOutputStream;
	private OutputStreamWriter outputStreamWriter;
	
	private WriteToFile1 (String path) {
//		f = new File(path);
//		try {
//			fileOutputStream = new FileOutputStream(path);
//			outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
//			writer = new BufferedWriter(outputStreamWriter);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static WriteToFile1 getInstance() {
		if (instance == null) {
			instance = new WriteToFile1("d:\\testcoding1.txt");
		}
		return instance;
	}
	
	public void write (String txt) {
//		try {
//			writer.write(txt + "\n");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void close () {
//		try {
//			writer.close();
//			outputStreamWriter.close();
//			fileOutputStream.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
