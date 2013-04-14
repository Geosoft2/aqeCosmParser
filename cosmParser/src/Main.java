import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.security.auth.login.Configuration;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
		
		
		//logging stuff
		Logger logger = Logger.getRootLogger();
		PropertyConfigurator.configure("log4j.properties");
		
		
		try {
			
			logger.info("cosmParser started");
			//create new seeker object	
			eggSeeker seeker = new eggSeeker();
			
			
			//prepare the database
			logger.info("prepare database");
			seeker.addEggPhenomena();
			seeker.addEggProcedures();
			seeker.addEggOfferings();
			seeker.linkPhenomenonToOffering();
			seeker.linkProcedureToOffering();
			seeker.linkProcedureToPhenomenon();
			
			logger.info("retreive AQE with tag 'munster egg' from cosm");
			//Workflow for retrieving all AQE from with tag "munster egg" from cosm and save them to the database.
			ArrayList<String> feedIdList = seeker.getEggsByTag("munster+egg");
			logger.info("AQE's found: "+feedIdList);
			//Iterate over feedID collection
			Iterator<String> feedIdIter = feedIdList.iterator();
			while (feedIdIter.hasNext()){
				//save to database
				seeker.addNewEgg(feedIdIter.next().toString());
			}
			//get all eggs in database
			databaseCon dbCon = new databaseCon();
			//eggList is now the collection of AQE in database
			feedIdList = dbCon.getAllFeatures();
			logger.info("AQE in Database: "+feedIdList);
			feedIdIter = feedIdList.iterator();
			
			//array list of air quality eggs
			ArrayList<AirQualityEgg> aqeList = new ArrayList<AirQualityEgg>();
			while(feedIdIter.hasNext()){
				//add a new air quality egg object to list
				aqeList.add(new AirQualityEgg(feedIdIter.next().toString()));
			}
			
			logger.info("Update measurements...");
			//update and store measurements
			Iterator<AirQualityEgg> aqeIterator = aqeList.iterator();
			//iterate over eggs
			while(aqeIterator.hasNext()){
				AirQualityEgg aqe = (AirQualityEgg) aqeIterator.next();
				logger.info("...of AQE "+ aqe.getFeedID());
				aqe.updateAllMeasurements();
				//aqe.logMeasurements();
				aqe.writeAllToDatabase();
			}
			
			logger.info("cosmParser finished");
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		logger.info("Generate test data...");
		testDataGenerator gen = new testDataGenerator();
		//generate test data for two days
		logger.info("...for first egg");
		gen.addTestEgg("00000", "AQE Test Unit 00", "Artificial AQE", "51.961298", "7.590008");
		logger.info("...for second egg");
		gen.addTestEgg("00001", "AQE Test Unit 01", "Artificial AQE", "51.970288", "7.565374");
		logger.info("...for third egg");
		gen.addTestEgg("00002", "AQE Test Unit 02", "Artificial AQE", "51.973504", "7.631888");
		//gen.generateAirHumidityData(576);
		logger.info("Test data generated!");
		*/
		 
		
	}

}
