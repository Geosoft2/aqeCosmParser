import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class that generates test data for the air quality eggs
 * @author sven
 *
 */
public class testDataGenerator {
	
	databaseCon dbCon;
	eggSeeker seeker;
	int winWidth;
	Utilities utils = new Utilities();
	
	testDataGenerator(int winWidth){
		try {
			this.winWidth = winWidth;
			this.dbCon = new databaseCon();
			this.seeker = new eggSeeker();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addTestEgg(){
		//feedID, title, description, lat, lon
		ArrayList<String> eggMetadata = new ArrayList<String>();
		String[] first = {"00000", "AQE Test Unit", "Artificial AQE", "51.961298", "7.590008"};
		for (String s : first){
			eggMetadata.add(s);
		}
		String feedID = eggMetadata.get(0);
		
		int number = 576;
		
		if (!dbCon.isEggInDatabase(feedID)) {
			// Step 2. see new method: getEggMetadata
			//logger.info(this.getEggMetadata(feedID).toString());
			dbCon.addNewFeature(eggMetadata);
			//establish link between feature_of_interest table and offering table
			seeker.linkFeatureOfInterestToOfferings(feedID);
			//establish link between procedure and feature_of_interest table
			seeker.linkProceduresToFeatureOfInterest(feedID);
		}
		
		try {
			// create new air quality egg
			AirQualityEgg aqe = new AirQualityEgg(feedID);
			//[{"at":"2013-03-22T10:34:36.392532Z","value":"23.80"},{"at":"2013-03-22T10:36:30.655278Z","value":"23.70"}]
			
			//humidity -> Check!
			aqe.setValuesHumidity(this.generateData(number, 60, 0, 100, 10, 10));
			
			//Kohlenstoffmonoxid -> Check!
			//http://de.wikipedia.org/wiki/Kohlenstoffmonoxid
			aqe.setValuesCO(this.generateData(number, 150, 0, 7000, 10, 50));
			
			// nitrogen oxide -> Check!
			//http://cfpub.epa.gov/eroe/index.cfm?fuseaction=detail.viewInd&lv=list.listbyalpha&r=231330&subtop=341
			aqe.setValuesNO2(this.generateData(number, 0.03, 0, 1, 10, 0.01));
			
			// ozone
			//http://www.umwelt.nrw.de/umwelt/luftqualitaet/ozon/belastung.php
			//http://www.lenntech.com/calculators/ppm/converter-parts-per-million.htm
			// average nrw ca. 35 yg/m³ -> 0.035 mg/m³ -> 0.0173 ppm, 240 yg/m³ -> 0.24 mg/m³ -> 0.118 ppm,
			aqe.setValuesO3(this.generateData(number, 0.0173, 0, 0.118, 10, 0.01));
			
			// temperature -> Check!
			aqe.setValuesTemp(this.generateData(number, 9.8, -10, 40, 10, 5));
			
			aqe.writeAllToDatabase();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
	
	/**
	 * Method generateds random data for presentation and testing purposes
	 * @param n the number of generated values 
	 * @param mean the expected value around the other values are generated
	 * @param from lower border -> potential minimal value
	 * @param to upper border -> potential maximal value
	 * @param outlierProbability: probability that a outlier occurs
	 * @param multiplicator interval in which the generated values are 
	 * @return JSONArray containing artificial timestamps and values
	 */
	public JSONArray generateData(int n, double mean, double from, double to, int outlierProbability, double multiplicator){
		
		// empty jsonarray
		JSONArray array = new JSONArray();
		
		// generate time stamp
		String timeStamp = utils.getCurrentTimeAsString();
		timeStamp = timeStamp.substring(0, timeStamp.length()-1)+".000000Z";
		
		// loop that generates values
		for (int i=0; i<n; i++){
			
			//Is value an outlier?
			boolean isOutlier = Math.random()*100 <= outlierProbability;
			
			
			double value = 0;
			if (isOutlier){
				// low or high value outlier
				boolean high = Math.random() >= 0.5;
				if (high) {
					// decrease upper border
					value = to-Math.random()*multiplicator;
				} else {
					// increase lower border
					value = from+Math.random()*multiplicator;
				}
					
			} else {
				value = mean+Math.random()*multiplicator;
			}
			
			//System.out.println(value);
			
			// generate jsonobject and add to jsonarray
			try {
				JSONObject obj = new JSONObject();
				obj.put("value", value);
				// add five seconds to timestamp
				Date date = utils.overlongTimestampToDate(timeStamp);
				date = utils.addMinutes(date, 5);
				timeStamp = utils.sqlDateToCosmString(date);
				obj.put("at", timeStamp);
				
				array.add(obj);
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//System.out.println(array.toJSONString());
		return array;
		
	}
	
	
	
	
	

}
