package fdi.ucm.server.importparser.json;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
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
		
		 try {
				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
				
				System.out.println(FileIO);
				
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));

				oos.writeObject(C.getCollection());

				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}


	public CompleteCollection getCollection() {
		return C;
	}
	
	
	
	private void init(ArrayList<String> log, String collection) {
		C=new CompleteCollection(File.separator+System.currentTimeMillis()+"", collection);
		
		PathFinder=new HashMap<String, CompleteElementType>();
		MultivaluedList=new HashMap<CompleteElementType, HashMap<CompleteElementType,List<CompleteElementType>>>();
		
		this.log=log;
		
	}
	

	public void procesaJSONArrayFolder(JsonArray JSONArray, ArrayList<String> log, String Collection, Properties prop) {
		init(log,Collection);
		
		CG=new CompleteGrammar(Collection, Collection, C);
		C.getMetamodelGrammar().add(CG);
		
		
		
		
		String PathDes=prop.getProperty("desc");
		if (PathDes==null)
			PathDes="";
		
		PathDes=PathDes.toLowerCase();
		
		for (JsonElement jsonobject : JSONArray)

				procesaJSON(jsonobject,PathDes,"");
		
			
	}

	


	public void procesaJSONFolder(String JSONFolder, ArrayList<String> log) {
		init(log,JSONFolder);
		
		
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
		if (PathDes==null)
			PathDes="";
		
		PathDes=PathDes.toLowerCase();
		
		for (File file : JSONFilesIN)
			if (file.getName().toLowerCase().endsWith(".json"))
			{
			try {
				JsonElement JSONELEM = new JsonParser().parse(new FileReader(file));
				
				procesaJSON(JSONELEM,PathDes,file.getName().split("\\.")[0]);
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


	private void procesaJSON(JsonElement JSONELEM, String pathDes, String description) {
	
		
		
		CompleteDocuments CD=new CompleteDocuments(C,description,"");
		C.getEstructuras().add(CD);
		
		
		//parece igual pero el comportamiento es diferente al ser el padre directamente la gramatica
		
		
		if (JSONELEM.isJsonArray())
			{
			
			CompleteElementType CETY=PathFinder.get("/");
			
			if (CETY==null) {
				
			
				
			CETY=new CompleteTextElementType("entry", CG);
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
				CompleteElementType este= LL.get(i);
				if (Jolem.isJsonPrimitive())
				{
					
					if (pathDes.equals("/"))
						CD.setDescriptionText(CD.getDescriptionText()+" "+Jolem.getAsString());
					
					if (este instanceof CompleteTextElementType)
					{
						CompleteTextElement EsteElem=new CompleteTextElement((CompleteTextElementType) este, Jolem.getAsString());
						CD.getDescription().add(EsteElem);
					}
					
					//AQUI INSERTAR
					
				}
				else
					procesaObjeto(Jolem,"entry/",este,CD,pathDes);
			}	
				
				
			MultivaluedList.put(null, listaHermanos);	
			}
		else if (JSONELEM instanceof JsonObject)
		{
			
			JsonObject JobElemElem = JSONELEM.getAsJsonObject();
			for (Entry<String, JsonElement> JSonElemProcc : JobElemElem.entrySet()) {
				
				CompleteElementType CETY=PathFinder.get(JSonElemProcc.getKey());
				
				if (CETY==null) {
				CETY=new CompleteTextElementType(JSonElemProcc.getKey(), CG);
				CETY.setClassOfIterator(CETY);
				CG.getSons().add(CETY);
				
				PathFinder.put(JSonElemProcc.getKey(),CETY);
				}
				
				if (JSonElemProcc.getValue().isJsonPrimitive())
				{
					
					if (pathDes.equals(JSonElemProcc.getKey().toLowerCase()))
						CD.setDescriptionText(CD.getDescriptionText()+" "+JSonElemProcc.getValue().getAsString());
					
					
					if (CETY instanceof CompleteTextElementType)
					{
						CompleteTextElement EsteElem=new CompleteTextElement((CompleteTextElementType) CETY, JSonElemProcc.getValue().getAsString());
						CD.getDescription().add(EsteElem);
					}
				}else
					procesaObjeto(JSonElemProcc.getValue(),JSonElemProcc.getKey()+"/",
							CETY,CD,pathDes);
			}
			
		}
		
	}


	private void procesaObjeto(JsonElement JSONELEM, String stringPadre,
			CompleteElementType PadreEleme, CompleteDocuments cD, String pathDes) {
		if (JSONELEM.isJsonArray()) {
				CompleteElementType CETY=PathFinder.get(stringPadre+"entry");
				
				if (CETY==null) {
					
				
					
				CETY=new CompleteTextElementType("entry",PadreEleme, CG);
				CETY.setClassOfIterator(CETY);
				PadreEleme.getSons().add(CETY);
				
				PathFinder.put(stringPadre+"entry",CETY);
				}
				
				
				if (!CETY.isMultivalued())
					CETY.setMultivalued(true);
				
				HashMap<CompleteElementType, List<CompleteElementType>> listaHermanos=MultivaluedList.get(PadreEleme);
				
				JsonArray ArrayElem = JSONELEM.getAsJsonArray();
				
				if (listaHermanos==null)
					listaHermanos=new HashMap<CompleteElementType, List<CompleteElementType>>();

				
				List<CompleteElementType> LL=listaHermanos.get(CETY);
				if (LL==null)
				{
					LL=new LinkedList<CompleteElementType>();
					LL.add(CETY);
					
					if (CETY.getFather()==PadreEleme)
						listaHermanos.put(CETY, LL);
				}
				
				while (ArrayElem.size()>LL.size())
					produceHermano(LL,CETY,PadreEleme);
				
				for (int i = 0; i < ArrayElem.size(); i++) {
					JsonElement Jolem = ArrayElem.get(i);
					CompleteElementType este= LL.get(i);
					if (Jolem.isJsonPrimitive())
					{
						if (pathDes.equals((stringPadre+"entry").toLowerCase()))
							cD.setDescriptionText(cD.getDescriptionText()+" "+Jolem.getAsString());
						
						
						if (este instanceof CompleteTextElementType)
						{
							CompleteTextElement EsteElem=new CompleteTextElement((CompleteTextElementType) este, Jolem.getAsString());
							cD.getDescription().add(EsteElem);
						}
						
						//AQUI INSERTAR
						
					}
					else
						procesaObjeto(Jolem,stringPadre+"entry/",este,cD,pathDes);
				}	
					
					
				MultivaluedList.put(PadreEleme, listaHermanos);	
		}
		else if (JSONELEM instanceof JsonObject)
		{
			
			
			//Que Ocurre si mi padre es un multievalado, se me va
			
			JsonObject JobElemElem = JSONELEM.getAsJsonObject();
			for (Entry<String, JsonElement> JSonElemProcc : JobElemElem.entrySet()) {
				
				CompleteElementType CETY=PathFinder.get(stringPadre+JSonElemProcc.getKey());
				
				if (CETY==null) {
					CETY=new CompleteTextElementType(JSonElemProcc.getKey(),PadreEleme, CG);
					CETY.setClassOfIterator(CETY);
					PadreEleme.getSons().add(CETY);
					PathFinder.put(stringPadre+JSonElemProcc.getKey(),CETY);
				}
				

				//aqui parece que lo estoy intentando controlar, pero no se lo asigno bien a los hermanos o no se
				if (CETY.getFather()!=PadreEleme)
				{
					CompleteTextElementType CETY2=new CompleteTextElementType(JSonElemProcc.getKey(),PadreEleme, CG);
					CETY2.setClassOfIterator(CETY);
					PadreEleme.getSons().add(CETY2);
					CETY=CETY2;
				}
				
				
				if (JSonElemProcc.getValue().isJsonPrimitive())
				{
					
					if (pathDes.equals((stringPadre+JSonElemProcc.getKey()).toLowerCase()))
						cD.setDescriptionText(cD.getDescriptionText()+" "+JSonElemProcc.getValue().getAsString());
					
	
					if (CETY instanceof CompleteTextElementType) {
						CompleteTextElement EsteElem=new CompleteTextElement((CompleteTextElementType) CETY, JSonElemProcc.getValue().getAsString());
						cD.getDescription().add(EsteElem);
					}
						
				}else
					procesaObjeto(JSonElemProcc.getValue(),stringPadre+JSonElemProcc.getKey()+"/",
								CETY,cD,pathDes);
				
		
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
	
	public HashMap<String, CompleteElementType> getPathFinder() {
		return PathFinder;
	}
	
	public HashMap<CompleteElementType, HashMap<CompleteElementType, List<CompleteElementType>>> getMultivaluedList() {
		return MultivaluedList;
	}
	

}
