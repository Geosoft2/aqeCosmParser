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

		try {
			
			//create new seeker object	
			eggSeeker seeker = new eggSeeker();

			//prepare the database
			seeker.addEggPhenomena();
			seeker.addEggProcedures();
			seeker.addEggOfferings();
			seeker.linkPhenomenonToOffering();
			seeker.linkProcedureToOffering();
			seeker.linkProcedureToPhenomenon();
			
			
			//Workflow for retrieving all AQE from with tag "munster egg" from cosm and save them to the database.
			ArrayList<String> feedIdList = seeker.getEggsByTag("munster+egg");
			//Iterate over feedID collection
			Iterator feedIdIter = feedIdList.iterator();
			while (feedIdIter.hasNext()){
				//save to database
				seeker.addNewEgg(feedIdIter.next().toString());
			}
			//get all eggs in database
			databaseCon dbCon = new databaseCon();
			//eggList is now the collection of AQE in database
			feedIdList = dbCon.getAllFeatures();
			feedIdIter = feedIdList.iterator();
			
			//array list of air quality eggs
			ArrayList<AirQualityEgg> aqeList = new ArrayList<AirQualityEgg>();
			while(feedIdIter.hasNext()){
				aqeList.add(new AirQualityEgg(feedIdIter.next().toString()));
			}
			
			//update and store measurements
			Iterator aqeIterator = aqeList.iterator();
			while(aqeIterator.hasNext()){
				AirQualityEgg aqe = (AirQualityEgg) aqeIterator.next();
				aqe.updateAllMeasurements();
				//aqe.logMeasurements();
				aqe.writeAllToDatabase();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
