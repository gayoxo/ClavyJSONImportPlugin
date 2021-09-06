package fdi.ucm.server.importparser.json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class mainProcesso_ex2 extends CollectionJSON{

	public static void main(String[] args) {
		CollectionJSON C=new CollectionJSON();
		ArrayList<String> log = new ArrayList<String>();
		C.debugfile=true;
		C.procesaJSONFolder("files/ex2", log);
		
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
	
}
