package fdi.ucm.server.importparser.json;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import fdi.ucm.server.modelComplete.collection.CompleteCollection;
import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionJSON {
	
	public boolean debugfile=false;
	public CompleteCollection C=new CompleteCollection();
	public ArrayList<String> log;
	
	public static void main(String[] args) {
		CollectionJSON C=new CollectionJSON();
		ArrayList<String> log = new ArrayList<String>();
		C.debugfile=true;
		C.procesaJSONFolder("files/ex1/Patient", log);
		
//		 try {
				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
				
				System.out.println(FileIO);
//				
//				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));
//
//				oos.writeObject(C.getColeccion());
//
//				oos.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
	}


	private void procesaJSONFolder(String JSONFolder, ArrayList<String> log) {
		C=new CompleteCollection(File.separator+System.currentTimeMillis()+"", JSONFolder);
		this.log=log;
		File RootFolder=new File(JSONFolder);
		if (!RootFolder.isDirectory())
			return;
		
		File[] JSONFilesIN = RootFolder.listFiles();
		
		Properties prop = new Properties();
		
		try (InputStream input = new FileInputStream(JSONFolder+".properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("no .properties Load");
        }
		
		String PathDes=prop.getProperty("desc");
		
		for (File file : JSONFilesIN)
			try {
				procesaJSON(file,PathDes, log);
			} catch (JsonIOException e) {
				log.add(e);
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}


	private void procesaJSON(File JSonFile, String pathDes, ArrayList<String> log2) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		JsonElement JSONELEM = new JsonParser().parse(new FileReader(JSonFile));
		
		
		
	}

}
