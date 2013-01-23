import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * in Münster / tagged with munster egg / a certain combination of datastreams.
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
	 * @param tag
	 *            to search for
	 */
	public ArrayList<String> getEggsByTag(String tag) {
		
		ArrayList<String> feedList = new ArrayList<String>();

		String result = "";
		// Create http GET request with parameter, format, start- and end time
		String getString = "http://api.cosm.com/v2/feeds/?content=summary&tag=" + tag;
		logger.info(getString);
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
				logger.info("http status code: "
						+ httpResponse.getStatusLine().getStatusCode());
				if (httpResponse.getStatusLine().getStatusCode() / 100 == 2) {
					// Get the data in the entity
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(httpResponse.getEntity()
									.getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
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
					System.out.println("Ooops");
				}
			}
		} catch (Exception e) {
			logger.warn("Something went wrong");
			e.printStackTrace();
		}
		
		return feedList;
	}

}
