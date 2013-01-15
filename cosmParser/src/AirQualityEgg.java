import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class represents a Air Quality Egg to encapsulate data and methods.
 * (Better than thousands of array lists...)
 * @author sven
 *
 */
public class AirQualityEgg {
	
	//more technical attributes
	private String feedID;
	private String API_KEY;
	private HttpClient httpClient;
	private HttpGet httpGet;
	private HttpResponse httpResponse;
	//standard values for 
	private int interval = 60; //one datapoint every 60 seconds
	private int limit = 1000; //max. 100 results per request, up to 1000 possible -> poor performance 
	private int splitIntervalDuration = 12; //hours
	private Utilities utils = new Utilities();
	private Properties properties = new Properties(); //Some values are read from properties file
	
	private Logger logger;
	
	private JSONArray valuesCO;
	private JSONArray valuesHumidity;
	private JSONArray valuesNO2;
	private JSONArray valuesO3;
	private JSONArray valuesTemp;
	
	/**
	 * Well, it's just a constructor
	 * @param feedID in Cosm of Air Quality Egg
	 * @throws FileNotFoundException if the config.properties was not found
	 * @throws IOException
	 */
	public AirQualityEgg(String feedID) throws FileNotFoundException, IOException{
		
		this.feedID = feedID;
		//read values for parametrisation from properties file
		
		//load data from properties file into attributes
		properties.load(new FileInputStream("config.properties"));
		this.interval = Integer.valueOf((String)properties.get("interval"));
		this.limit = Integer.valueOf((String) properties.get("limit"));
		this.splitIntervalDuration = Integer.valueOf((String) properties.get("splitIntervalDuration"));
		this.API_KEY = (String) properties.getProperty("apikey");
		
		//create http client
		this.httpClient = new DefaultHttpClient();
		
		logger = Logger.getLogger(this.getClass());
		PropertyConfigurator.configure("log4j.properties");
		
		//initialize jsonarrays
		valuesCO = new JSONArray();
		valuesHumidity = new JSONArray();
		valuesNO2 = new JSONArray();
		valuesO3 = new JSONArray();
		valuesTemp = new JSONArray();
		
	}
	
	
	/**
	 * Updates all the measurements by calling the different update functions in a sequence
	 * @throws Exception 
	 */
	public void updateAllMeasurements() throws Exception{
		updateMeasurement("CO");
		updateMeasurement("humidity");
		updateMeasurement("NO2");
		updateMeasurement("temperature");
		updateMeasurement("O3");	
	}
	
	
	/**
	 * Method that updates the retrieves the values from cosm for a single parameter
	 * @param param is the measured parameter to be updated
	 * @throws Exception
	 */
	public void updateMeasurement(String param) throws Exception{
		
		String result = "";
		//filter wrong parameters
		if ((param != "CO") && (param != "humidity") && (param != "NO2") && (param != "temperature") && (param != "O3")) throw new Exception("Unsupported parameter");
		else {
			//get the time interval for that the request has to be constructed
			String end = utils.getCurrentTime();
			String start = utils.getLastUpdateTime(param);
			
			//get the collection of intervals like: 2012-12-02T11:00:01Z, 2012-12-02T23:00:00Z, 2012-12-02T23:00:01Z, 2012-12-03T11:00:00Z
			ArrayList<String> intervalCollection = utils.splitInterval(start, end, splitIntervalDuration);
			Iterator icIterator = intervalCollection.listIterator();
			
			//iterate over intervals and make cosm api requests
			while (icIterator.hasNext()){
				
				//TODO: make limit adjustment possible
				//Create http GET request with parameter, format, start- and end time
				String getString ="http://api.cosm.com/v2/feeds/"+feedID+"/datastreams/"+param+".json?start="+icIterator.next()+"&end="+icIterator.next()+"&interval="+interval+"&limit="+limit;
				logger.info(getString);
				httpGet = new HttpGet(getString);
				httpGet.addHeader("X-ApiKey", API_KEY);

				//Response object, used later
				HttpResponse httpResponse;
				
				try {
					//Execute request
					httpResponse = httpClient.execute(httpGet);
					/* Checking response */
					if (httpResponse != null) {
						// In case of success the status code is 2xx
						if (httpResponse.getStatusLine().getStatusCode() / 100 == 2 ) {
							//Get the data in the entity
						    BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
						    String line = "";
						    while ((line = rd.readLine()) != null) {
						        result += line;
						      }
						    //fill the local json arrays with values
						    JSONObject obj = (JSONObject) new JSONParser().parse(result);
						    JSONArray tempArray = (JSONArray) obj.get("datapoints");
						    if(param == "CO") valuesCO.addAll(tempArray);
						    else if (param == "humidity") valuesHumidity.addAll(tempArray);
						    else if (param == "NO2") valuesNO2.addAll(tempArray);
						    else if (param == "temperature") valuesTemp.addAll(tempArray);
						    else if (param == "O3") valuesO3.addAll(tempArray);
						    //clear the result string!
						    result = "";
						}
						else {
							//TODO: Do something that makes sense: Just missing values
							System.out.println("Ooops");
						}
						
					}

				} catch (Exception e) {
					System.out.println("Something went wrong");
					e.printStackTrace();
				}
			}
			
			
			}

		
	}
	
	/**
	 * Method that logs all the values as string, for debugging purposes
	 */
	public void logMeasurements(){
		logger.info(valuesCO);
		logger.info(valuesHumidity);
		logger.info(valuesNO2);
		logger.info(valuesO3);
		logger.info(valuesTemp);
	}
	
	/**
	 * Method that writes the measurements of a single parameter into the database
	 * @param param
	 */
	public void writeToDatabase(String param){
		Iterator iter = null;
		if(param == "CO") iter = valuesCO.iterator();
	    else if (param == "humidity") iter = valuesHumidity.iterator();
	    else if (param == "NO2") iter = valuesNO2.iterator();
	    else if (param == "temperature") iter = valuesTemp.iterator();
	    else if (param == "O3") iter = valuesO3.iterator();
		
		//iterate over datapoints collection
				while (iter.hasNext()){
					JSONObject datapoint = (JSONObject) iter.next();
					String timestamp = datapoint.get("at").toString();
					double value = Double.valueOf(datapoint.get("value").toString());
					//TODO: add connection to database
					logger.info(timestamp+" "+value);
				}
		
	}
	
	/**
	 * Method that tests concatination of json arrays
	 */
	public void jsontTester(){
		JSONArray a1 = new JSONArray();
		try {
			//JSONArray a1 = (JSONArray) new JSONParser().parse("[{\"at\":\"2013-01-07T01:00:46.176298Z\",\"value\":\"93934.00\"},{\"at\":\"2013-01-07T01:01:21.506129Z\",\"value\":\"94691.00\"}]");
			JSONArray a2 = (JSONArray) new JSONParser().parse("[{\"at\":\"2013-01-07T01:00:46.176298Z\",\"value\":\"93934.00\"},{\"at\":\"2013-01-07T01:01:21.506129Z\",\"value\":\"94691.00\"}]");
			a1.addAll(a2);
			logger.info(a1.toJSONString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	
	

}
