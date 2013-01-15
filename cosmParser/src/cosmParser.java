import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;




/**
 * This class represents a parser object to grab data from cosm
 * @author sven h.
 * 
 * Ein Parser-Objekt für jeden Feed?
 * Was brauche ich beim Erstellen eines neuen Parser-Objekts?
 *
 */
public class cosmParser {
	
	private Properties properties = new Properties();
	
	//properties.load(new FileInputStream("config.properties"));
	
	//Standard API_KEY
	private String API_KEY = "-rlATaswLFIN9Ov6_ylUp-7Yo0-SAKxUYlRqT0E4TldQbz0g";
	
	private String feedID;
	private HttpClient httpClient;
	private HttpGet httpGet;
	private HttpResponse httpResponse;
	//standard values for 
	private int interval = 60; //one datapoint every 60 seconds
	private int limit = 1000; //max. 100 results per request, up to 1000 possible -> poor performance 
	private int splitIntervalDuration = 12; //hours
	private Utilities utils = new Utilities();


	
	public cosmParser(String feedID){
		
		try {
			//read values for parametrisation from properties file
			properties.load(new FileInputStream("config.properties"));
			this.interval = Integer.valueOf((String)properties.get("interval"));
			this.limit = Integer.valueOf((String) properties.get("limit"));
			this.splitIntervalDuration = Integer.valueOf((String) properties.get("splitIntervalDuration"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		this.feedID = feedID;
		this.httpClient = new DefaultHttpClient();
	}
	
	/**
	 * 
	 * @param param AQE parameter, one of: "CO", "humidity", "NO2", "O3", "temperature"
	 * @param format to be returned. can be json, xml or csv
	 * @return
	 */
	//TODO: Create Format Exception class
	public ArrayList<String> parseToStringCollection(String param, String format) throws Exception{
		
		ArrayList<String> list = new ArrayList<String>();
		String result = "";
		
		//filter wrong input
		if ((format != "json") && (format != "xml") && (format != "csv")) throw new Exception("Unsupported format");
		if ((param != "CO") && (param != "humidity") && (param != "NO2") && (param != "temperature") && (param != "O3")) throw new Exception("Unsupported parameter");
		else{
		
		//get the time interval
		String end = utils.getCurrentTime();
		String start = utils.getLastUpdateTime(param);
		
		//get the collection of intervals like: 2012-12-02T11:00:01Z, 2012-12-02T23:00:00Z, 2012-12-02T23:00:01Z, 2012-12-03T11:00:00Z
		ArrayList<String> intervalCollection = utils.splitInterval(start, end, splitIntervalDuration);
		Iterator icIterator = intervalCollection.listIterator();
		
		//iterate over intervals and make cosm api requests
		while (icIterator.hasNext()){
			
			//TODO: make limit adjustment possible
			//Create http GET request with parameter, format, start- and end time
			String getString ="http://api.cosm.com/v2/feeds/"+feedID+"/datastreams/"+param+"."+format+"?start="+icIterator.next()+"&end="+icIterator.next()+"&interval="+interval+"&limit="+limit;
			System.out.println(getString);
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
					    	//System.out.println(line);
					        result += line;
					        //System.out.println(line);
					      }
					    list.add(result);
					    
					    //TODO: Testing, remove
					    JSONObject obj = (JSONObject) new JSONParser().parse(result);
					    System.out.println(obj.toJSONString());
					    //clear the result string!
					    result = "";
					}
					else {
						//TODO: Do something that makes sense: Just missing values
						System.out.println("Ooops");
					}
					
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		}
		
		return list;
	}
	
	public void parseAllParams(String format) throws Exception{
		//filter wrong input
			if ((format != "json") && (format != "xml") && (format != "csv")) throw new Exception("Unsupported format");
			else {
				
			}
		
	}
	
	
	public void jsonToDatabase(JSONObject obj){
		//System.out.println("jsonToDatabase");
		String id = obj.get("id").toString();
		JSONArray datapoints = (JSONArray) obj.get("datapoints");
		if(datapoints != null){
		Iterator iter = datapoints.iterator();
		
		//iterate over datapoints collection
		while (iter.hasNext()){
			JSONObject datapoint = (JSONObject) iter.next();
			String timestamp = datapoint.get("at").toString();
			double value = Double.valueOf(datapoint.get("value").toString());
			//TODO: add connection to database
			//System.out.println(timestamp+" "+value);
		}
		//System.out.println("---------------------------------------");
		
		}
		
	}
	
	
	
	
	
	
	
}
