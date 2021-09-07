package fdi.ucm.server.importparser.json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import fdi.ucm.server.modelComplete.collection.document.CompleteDocuments;
import fdi.ucm.server.modelComplete.collection.document.CompleteElement;
import fdi.ucm.server.modelComplete.collection.document.CompleteTextElement;

public class mainProcesso_ex2_Observation extends CollectionJSON{

	public static void main(String[] args) {
		CollectionJSON C=new CollectionJSON();
		ArrayList<String> log = new ArrayList<String>();
		C.debugfile=true;
		C.procesaJSONFolder("files/ex2/Observation", log);
		
		 try {
				String FileIO = System.getProperty("user.home")+File.separator+System.currentTimeMillis()+".clavy";
				
				System.out.println(FileIO);
				
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FileIO));

				oos.writeObject(C.getCollection());

				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		 
		 System.out.println("Document Debug");
		 for (CompleteDocuments documento : C.getCollection().getEstructuras()) {
			System.out.println(documento.getDescriptionText());
			for (CompleteElement complete_elemento : documento.getDescription()) {
				System.out.print(complete_elemento.getHastype().getName()+"^^"+
						complete_elemento.getHastype().toString()+"-<>-"+complete_elemento.getHastype().getClassOfIterator().toString());
				
				if (complete_elemento instanceof CompleteTextElement && !((CompleteTextElement) complete_elemento).getValue().trim().isEmpty())
					System.out.print("-$$-"+((CompleteTextElement) complete_elemento).getValue());
				
				
				System.out.println();
				
			}
		}
	}
	
}
