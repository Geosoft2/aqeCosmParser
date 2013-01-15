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
		//String[] params = {"CO", "humidity", "NO2", "O3", "temperature"};

		try {
			
			AirQualityEgg aqe = new AirQualityEgg(feedID);
			aqe.updateAllMeasurements();
			//aqe.updateMeasurement("CO");
			aqe.writeToDatabase("CO");
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
