import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.security.auth.login.Configuration;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Useful links
 * http://stackoverflow.com/questions/1395551/convert-a-json-string-to-object-in-java
 * 
 * @author sven
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		
		String feedID = ""+75759;
		String[] params = {"CO", "humidity", "NO2", "O3", "temperature"};
		Utilities utils = new Utilities();
		
		cosmParser parser = new cosmParser(feedID);
		JSONObject obj01;

		try {
			ArrayList<String> result = parser.parseToStringCollection(params[0], "json");
			
			System.out.println(result.size());
			Iterator resultIter = result.listIterator();
			while (resultIter.hasNext()){
				obj01 = (JSONObject) new JSONParser().parse(resultIter.next().toString());
				parser.jsonToDatabase(obj01);
				//System.out.println(obj01.toString());				
				//System.out.println(resultIter.next());
			}
			
			
			/*
			ArrayList<String> intervalCollection = utils.splitInterval(utils.getLastUpdateTime("CO"), utils.getCurrentTime(), 12);
			Iterator icIterator = intervalCollection.listIterator();
			while (icIterator.hasNext()){
				System.out.println(icIterator.next());
			}
			System.out.println(intervalCollection.size());
			*/
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
