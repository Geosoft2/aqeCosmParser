import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

/**
 * This class provides the ability to search the cosm platform for Eggs located
 * in Münster / tagged with "munster egg".
 * 
 * Furthermore its capable of
 * - filling the SOS database with necessary AQE / initial informations
 * - creating new Air Quality Eggs
 * 
 * @author sven
 * 
 */
public class eggSeeker {

	private String API_KEY;
	private HttpClient httpClient;
	private HttpGet httpGet;
	private HttpResponse httpResponse;
	private Properties properties = new Properties();
	//aqe sensor set with fixed metadata conserning the aqe sensors
	private sensorSet aqeSensors = sensorSet.getInstance();
	
	private Logger logger;

	/**
	 * Constructor sets up basic parameters / tools
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public eggSeeker() throws FileNotFoundException, IOException {
		// load data from properties file into attributes
		properties.load(new FileInputStream("config.properties"));
		// get the cosm api key
		this.API_KEY = (String) properties.getProperty("apikey");
		// create http client
		this.httpClient = new DefaultHttpClient();

		// logging stuff
		logger = Logger.getLogger(this.getClass());
		PropertyConfigurator.configure("log4j.properties");
	}

	/**
	 * Method gets the Eggs feedIDs that match a certain tag via cosm api request
	 * https://cosm.com/docs/v2/feed/list.html
	 * 
	 * @param tag to search for
	 */
	public ArrayList<String> getEggsByTag(String tag) {
		
		ArrayList<String> feedList = new ArrayList<String>();

		String result = "";
		// Create http GET request with parameter, format, start- and end time
		String getString = "http://api.cosm.com/v2/feeds/?content=summary&tag=" + tag;
		//logger.info(getString);
		httpGet = new HttpGet(getString);
		httpGet.addHeader("X-ApiKey", API_KEY);

		// Response object, used later
		HttpResponse httpResponse;

		try {
			// Execute request
			httpResponse = httpClient.execute(httpGet);
			/* Checking response */
			if (httpResponse != null) {
				// In case of success the status code is 2xx
				//logger.info("http status code: "httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() / 100 == 2) {
					// Get the data in the entity
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(httpResponse.getEntity()
									.getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						//logger.info(line);
						result += line;
					}
					// work with json objects / arrays to get the feed ids
					JSONObject obj = (JSONObject) new JSONParser()
							.parse(result);
					JSONArray resultObj = (JSONArray) obj.get("results");
					Iterator iter = resultObj.iterator();
					while (iter.hasNext()){
						JSONObject temp_obj = (JSONObject) iter.next();
						String tempFeedID = temp_obj.get("id").toString();
						feedList.add(tempFeedID);
					}
					// clear the result string!
					result = "";
				} else {
					// TODO: Do something that makes sense: Just missing values
					logger.warn("Bad response: "+httpResponse.getStatusLine().getStatusCode());
				}
			}
		} catch (Exception e) {
			logger.warn("Something went wrong");
			e.printStackTrace();
		}
		return feedList;
	}
	
	/**
	 * This method adds a new Air Quality Egg to the database and checks whether it is already stored or not
	 * 1. Is the egg already in the database? Yes: Do nothing. No: See step 2.
	 * 2. Retrieve Parameters of AQE from cosm (Tags, position,...?)
	 * 3. Save them to database
	 * @param feedID
	 */
	//TODO: Eggs without location?
	public void addNewEgg(String feedID){
		try {
			databaseCon dbCon = new databaseCon();
			// Step 1. Check if the egg is NOT in the database
			if (!dbCon.isEggInDatabase(feedID)) {
				// Step 2. see new method: getEggMetadata
				//logger.info(this.getEggMetadata(feedID).toString());
				dbCon.addNewFeature(this.getEggMetadata(feedID));
				//establish link between feature_of_interest table and offering table
				this.linkFeatureOfInterestToOfferings(feedID);
				//establish link between procedure and feature_of_interest table
				this.linkProceduresToFeatureOfInterest(feedID);
			}
			//close database connection
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method that gets the metadata of a AQE via cosm (id, title, description, location(lat, lon))
	 * @param feedID
	 * @return an array list containing the feedID, title, description, lat, lon
	 */
	public ArrayList<String> getEggMetadata(String feedID){
		
		ArrayList<String> dataList = new ArrayList<String>();

		String result = "";
		// Create http GET request with parameter, format, start- and end time
		String getString = "http://api.cosm.com/v2/feeds/"+feedID+".json";
		//logger.info(getString);
		httpGet = new HttpGet(getString);
		httpGet.addHeader("X-ApiKey", API_KEY);

		// Response object, used later
		HttpResponse httpResponse;
		
		try {
			// Execute request
			httpResponse = httpClient.execute(httpGet);
			/* Checking response */
			if (httpResponse != null) {
				// In case of success the status code is 2xx
				//logger.info("http status code: " + httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() / 100 == 2) {
					// Get the corresponding data of the aqe entity
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(httpResponse.getEntity()
									.getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						//logger.info("getEggMetadata");
						//logger.info(line);
						result += line;
					}
					// work with json objects / arrays to get the eggs metadata
					JSONObject obj = (JSONObject) new JSONParser()
							.parse(result);
					//feed id
					dataList.add(obj.get("id").toString());
					//title
					if (obj.get("title")==null) dataList.add("No title aviable");
					dataList.add(obj.get("title").toString());
					//description
					if (obj.get("description")==null) dataList.add("No description available");
					else dataList.add(obj.get("description").toString());
					//location: if location is null -> set lat and lon to 0.0
					if (obj.get("location")==null) {
						dataList.add("0.0");
						dataList.add("0.0");
					} else {
						JSONObject locObj = (JSONObject) obj.get("location");
						//lat
						dataList.add(locObj.get("lat").toString());
						//lon
						dataList.add(locObj.get("lon").toString());
					}

					// clear the result string!
					result = "";
				} else {
					logger.warn("Bad response: "+httpResponse.getStatusLine().getStatusCode());
				}
			}
		} catch (Exception e) {
			logger.warn("Something went wrong");
			e.printStackTrace();
		}
		
		return dataList;
		
	}
	
	/**
	 * Method adds the phenomena of the air quality eggs to database
	 */
	public void addEggPhenomena(){
		try {
			databaseCon dbCon = new databaseCon();
			//co
			dbCon.addPhenomenon(aqeSensors.getCoPhenomenon(), "carbon monoxide", "ppm", "numericType");
			//humidity
			dbCon.addPhenomenon(aqeSensors.getHumidityPhenomenon(), "relative air humidity", "%", "numericType");
			//NO2
			dbCon.addPhenomenon(aqeSensors.getNo2Phenomenon(), "nitrogen dioxide", "ppm", "numericType");
			//temperature
			dbCon.addPhenomenon(aqeSensors.getTemperaturePhenomenon(), "air temperature", "°C", "numericType");
			//O3
			dbCon.addPhenomenon(aqeSensors.getO3Phenomenon(), "ozone", "ppm", "numericType");
			
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Methods that adds the measuring procedures of the air quality eggs
	 */
	public void addEggProcedures(){
		try {
			databaseCon dbCon = new databaseCon();
			//co procedure
			dbCon.addProcedure(aqeSensors.getCoProcedure());
			//humidity procedure
			dbCon.addProcedure(aqeSensors.getHumidityProcedure());
			//NO2 procedure
			dbCon.addProcedure(aqeSensors.getNo2Procedure());
			//temperature procedure
			dbCon.addProcedure(aqeSensors.getTemperatureProcedure());
			//O3 procedure
			dbCon.addProcedure(aqeSensors.getO3Procedure());
			
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that adds the offerings of the AQE to the database
	 */
	public void addEggOfferings(){
		try {
			databaseCon dbCon = new databaseCon();
			//co offering
			dbCon.addOffering(aqeSensors.getCoOffering(),"Concentration of carbon monoxide in the air");
			//humidity offering
			dbCon.addOffering(aqeSensors.getHumidityOffering(), "Relative air humidity");
			//NO2 offering
			dbCon.addOffering(aqeSensors.getNo2Offering(), "Concentration of nitrogen dioxide in the air");
			//temperature offering
			dbCon.addOffering(aqeSensors.getTemperatureOffering(), "Air temperature in °C");
			//O3 offering
			dbCon.addOffering(aqeSensors.getO3Offering(), "Concentration of ozone dioxide in the air");
			
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Methods that establishes the relation between the phenomenon and offering for each pair
	 */
	public void linkPhenomenonToOffering(){
		try {
			String tableName = "phen_off";
			String firstCol = "phenomenon_id";
			String secondCol = "offering_id";
			databaseCon dbCon = new databaseCon();
			//co 
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getCoPhenomenon(), aqeSensors.getCoOffering());
			//humidity 
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getHumidityPhenomenon(), aqeSensors.getHumidityOffering());
			//NO2 
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getNo2Phenomenon(), aqeSensors.getNo2Offering());
			//temperature
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getTemperaturePhenomenon(), aqeSensors.getTemperatureOffering());
			//O3
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getO3Phenomenon(), aqeSensors.getO3Offering());
		
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Methods that establishes the relation between the procedure and offering for each pair
	 */
	public void linkProcedureToOffering(){
		try {
			String tableName = "proc_off";
			String firstCol = "procedure_id";
			String secondCol = "offering_id";
			databaseCon dbCon = new databaseCon();
			//co 
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getCoProcedure(), aqeSensors.getCoOffering());
			//humidity
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getHumidityProcedure(), aqeSensors.getHumidityOffering());
			//NO2 
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getNo2Procedure(), aqeSensors.getNo2Offering());
			//temperature 
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getTemperatureProcedure(), aqeSensors.getTemperatureOffering());
			//O3
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getO3Procedure(), aqeSensors.getO3Offering());
		
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Methods that establishes the relation between the procedure and phenomenon for each pair
	 */
	public void linkProcedureToPhenomenon(){
		try {
			String tableName = "proc_phen";
			String firstCol = "procedure_id";
			String secondCol = "phenomenon_id";
			databaseCon dbCon = new databaseCon();
			//co procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getCoProcedure(), aqeSensors.getCoPhenomenon());
			//humidity procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getHumidityProcedure(), aqeSensors.getHumidityPhenomenon());
			//NO2 procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getNo2Procedure(), aqeSensors.getNo2Phenomenon());
			//temperature procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getTemperatureProcedure(), aqeSensors.getTemperaturePhenomenon());
			//O3 procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getO3Procedure(), aqeSensors.getO3Phenomenon());

			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method establishes link between the feature of interest and its offerings
	 * To be used in the addNewEgg() method
	 * @param feedID id of the new added AQE
	 */
	public void linkFeatureOfInterestToOfferings(String feedID){
		try {
			String tableName = "foi_off";
			String firstCol = "feature_of_interest_id";
			String secondCol = "offering_id";
			databaseCon dbCon = new databaseCon();
			//co offering
			dbCon.linkTwoTables(tableName, firstCol, secondCol, feedID, aqeSensors.getCoOffering());
			//humidity offering
			dbCon.linkTwoTables(tableName, firstCol, secondCol, feedID, aqeSensors.getHumidityOffering());
			//NO2 offering
			dbCon.linkTwoTables(tableName, firstCol, secondCol, feedID, aqeSensors.getNo2Offering());
			//temperature offering
			dbCon.linkTwoTables(tableName, firstCol, secondCol, feedID, aqeSensors.getTemperatureOffering());
			//O3 offering
			dbCon.linkTwoTables(tableName, firstCol, secondCol, feedID, aqeSensors.getO3Offering());
			
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that links the procedures of the air quality egg to a feature (AQE)
	 * @param feedID of the newly generated egg
	 */
	public void linkProceduresToFeatureOfInterest(String feedID){
		try {
			String tableName = "proc_foi";
			String firstCol = "procedure_id";
			String secondCol = "feature_of_interest_id";
			databaseCon dbCon = new databaseCon();
			//co procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getCoProcedure(), feedID);
			//humidity procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getHumidityProcedure(), feedID);
			//NO2 procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getNo2Procedure(), feedID);
			//temperature procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getTemperatureProcedure(), feedID);
			//O3 procedure
			dbCon.linkTwoTables(tableName, firstCol, secondCol, aqeSensors.getO3Procedure(), feedID);
			
			dbCon.disconnect();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
