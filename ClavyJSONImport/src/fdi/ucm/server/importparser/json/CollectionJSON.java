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
import java.util.Hashtable;
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
import fdi.ucm.server.modelComplete.collection.grammar.CompleteElementType;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteGrammar;
import fdi.ucm.server.modelComplete.collection.grammar.CompleteTextElementType;


public class CollectionJSON {
	
	public boolean debugfile=false;
	public CompleteCollection C=new CompleteCollection();
	public ArrayList<String> log;
	private CompleteGrammar CG;
	private HashMap<String, CompleteElementType> PathFinder;
	private HashMap<CompleteElementType, HashMap<CompleteElementType, List<CompleteElementType>>> MultivaluedList;
	
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
		
		PathFinder=new HashMap<String, CompleteElementType>();
		MultivaluedList=new HashMap<CompleteElementType, HashMap<CompleteElementType,List<CompleteElementType>>>();
		
		this.log=log;
		File RootFolder=new File(JSONFolder);
		if (!RootFolder.isDirectory())
			return;
		
		while (RootFolder.listFiles().length==1&&RootFolder.listFiles()[0].isDirectory())
			RootFolder=RootFolder.listFiles()[0];
			
		
		
		CG=new CompleteGrammar(RootFolder.getName(), RootFolder.getName(), C);
		C.getMetamodelGrammar().add(CG);
		
		File[] JSONFilesIN = RootFolder.listFiles();
		
		Properties prop = new Properties();
		
		try (InputStream input = new FileInputStream(JSONFolder+File.separator+".properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("no .properties Load");
        }
		
		String PathDes=prop.getProperty("desc");
		
		for (File file : JSONFilesIN)
			if (file.getName().toLowerCase().endsWith(".json"))
			{
			try {
				procesaJSON(file,PathDes, log);
			} catch (JsonIOException e) {
				System.err.println("Error input file "+ file.getName());
				log.add("Error input file "+ file.getName());
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				System.err.println("Error syntax file "+ file.getName());
				log.add("Error syntax file "+ file.getName());
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				System.err.println("Error not found file "+ file.getName());
				log.add("Error not found file "+ file.getName());
				e.printStackTrace();
			}
			}
		
	}


	private void procesaJSON(File JSonFile, String pathDes, ArrayList<String> log2) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
	
		JsonElement JSONELEM = new JsonParser().parse(new FileReader(JSonFile));
		
		CompleteDocuments CD=new CompleteDocuments(C,JSonFile.getName().split("\\.")[0],"");
		C.getEstructuras().add(CD);
		
		if (JSONELEM.isJsonArray())
			{
			
			CompleteElementType CETY=PathFinder.get("/");
			
			if (CETY==null) {
			CETY=new CompleteElementType("entry", CG);
			CETY.setClassOfIterator(CETY);
			CG.getSons().add(CETY);
			
			PathFinder.put("/",CETY);
			}
			
			if (!CETY.isMultivalued())
				CETY.setMultivalued(true);
			
			HashMap<CompleteElementType, List<CompleteElementType>> listaHermanos=MultivaluedList.get(null);
			
			JsonArray ArrayElem = JSONELEM.getAsJsonArray();
			
			if (listaHermanos==null)
				listaHermanos=new HashMap<CompleteElementType, List<CompleteElementType>>();

			
			List<CompleteElementType> LL=listaHermanos.get(CETY);
			if (LL==null)
			{
				LL=new LinkedList<CompleteElementType>();
				LL.add(CETY);
				listaHermanos.put(CETY, LL);
			}
			
			while (ArrayElem.size()>LL.size())
				produceHermano(LL,CETY,null);
			
			for (int i = 0; i < ArrayElem.size(); i++) {
				JsonElement Jolem = ArrayElem.get(i);
				if (Jolem.isJsonPrimitive())
				{
					CompleteElementType este= LL.get(i);
					
					//AQUI INSERTAR
					
				}
			}	
				
				
			MultivaluedList.put(null, listaHermanos);	
			}
		else
		{
			
			JsonObject JobElemElem = JSONELEM.getAsJsonObject();
			for (Entry<String, JsonElement> JSonElemProcc : JobElemElem.entrySet()) {
				if (JSonElemProcc.getValue().isJsonPrimitive())
				{
					CompleteElementType CETY=PathFinder.get(JSonElemProcc.getKey());
					
					if (CETY==null) {
					CETY=new CompleteTextElementType("entry", CG);
					CETY.setClassOfIterator(CETY);
					CG.getSons().add(CETY);
					
					PathFinder.put(JSonElemProcc.getKey(),CETY);
					}
					
					if (CETY instanceof CompleteTextElementType)
					{
					//AQUI INSERTAR EN DOCUMENTO
					}
				}
			}
			
		}
		
	}


	private void produceHermano(List<CompleteElementType> lL, CompleteElementType cETY, CompleteElementType Padre) {
		CompleteElementType creado;
		if (cETY instanceof CompleteTextElementType)
			creado=new CompleteTextElementType(cETY.getName(),cETY.getFather(), CG);
		else
			creado=new CompleteElementType(cETY.getName(),cETY.getFather(), CG);
		
		creado.setClassOfIterator(cETY);
		
		if (Padre ==null)
		{
			boolean found=false;
			int ilast=-1;
			for (int i = 0; i < CG.getSons().size(); i++) {
				CompleteElementType hijogram =CG.getSons().get(i);
				if (hijogram.getClassOfIterator() == cETY)
					if (!found)
						found=true;
					else
					{
						ilast=i+1;
						break;
					}
						
			}
			
			if (ilast<0)
				ilast=CG.getSons().size();
			
			CG.getSons().add(ilast, creado);
			
		}else
		{
			boolean found=false;
			int ilast=-1;
			for (int i = 0; i < Padre.getSons().size(); i++) {
				CompleteElementType hijogram =Padre.getSons().get(i);
				if (hijogram.getClassOfIterator() == cETY)
					if (!found)
						found=true;
					else
					{
						ilast=i+1;
						break;
					}
						
			}
			
			if (ilast<0)
				ilast=Padre.getSons().size();
			
			Padre.getSons().add(ilast, creado);
		}
		
		lL.add(creado);
		
		
	}

}
