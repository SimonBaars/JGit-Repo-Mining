package nl.simonbaars.bugcommit;

import java.io.FileReader;
import java.io.IOException;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.opencsv.CSVReader;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	String fileName = "/home/simon/Downloads/HACKATHON/NovaGoldenSet.csv";
    	CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(fileName), ';');
			reader.readNext();
			JsonObject array = new JsonObject();
			reader.iterator().forEachRemaining(line -> {
				if(!line[0].equals("")) {
				JsonObject obj = new JsonObject();
				array.put(line[0], obj);
				obj.put("creationdate", "2019-06-13 21:54:46 +0000");
				obj.put("resolutiondate", "2019-06-13 21:54:46 +0000");
				obj.put("hash", line[1]);
				obj.put("commitdate", "2019-06-13 21:54:46 +0000");
				}
	    	});
			System.out.println(array.toJson());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    	// if the first line is the header
    	//String[] header = reader.readNext();

    	// iterate over reader.readNext until it returns null
    	
    	


    }
}
