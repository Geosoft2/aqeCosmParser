import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Class that handles data base connection stuff and provides methods for
 * filling database with air quality egg data
 * 
 * @author sven
 * 
 */
public class databaseCon {

	private Properties properties = new Properties();
	private Logger logger;

	Connection con;
	private String db_username;
	private String db_password;
	private String db_url;

	private Utilities utils = new Utilities();

	/**
	 * Constructor for connection object that establishes connection to local
	 * psql databse using user, pw and url from config.properties.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public databaseCon() throws FileNotFoundException, IOException {
		// load data from properties file into attributes
		properties.load(new FileInputStream("config.properties"));
		this.db_username = (String) properties.getProperty("db_username");
		this.db_password = (String) properties.getProperty("db_password");
		this.db_url = (String) properties.getProperty("db_url");

		// logging stuff
		logger = Logger.getLogger(this.getClass());
		PropertyConfigurator.configure("log4j.properties");

		// Get the psql driver
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException cnfe) {
			logger.warn("Driver not found!");
			cnfe.printStackTrace();
			System.exit(1);
		}

		// establish connection
		try {
			con = DriverManager.getConnection("jdbc:postgresql:" + this.db_url,
					this.db_username, this.db_password);
		} catch (SQLException e) {
			logger.warn("Connection failed!");
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Method that closes the database connection
	 */
	public void disconnect() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
				con = null;
			}
		} catch (SQLException e) {
			logger.warn("Cannot disconnect!");
		}
	}
	

	/**
	 * Test method for adding a single measurement: feedID, timestamps, value, outlier
	 * (not used in main program)
	 */
	public void addTestMeasurement(String feedID, String timestamp, double value) {
		
		try {
			// build prepared statement
			String INSERT_RECORD = "INSERT INTO observations(feedid, date, value) VALUES (?,?,?)";
			PreparedStatement pstmt = con.prepareStatement(INSERT_RECORD);
			pstmt.setString(1, feedID);
			
			Date date = utils.toDate(timestamp);

			java.sql.Timestamp sqlDate = new java.sql.Timestamp(date.getTime());
			pstmt.setTimestamp(2, sqlDate);
			
			pstmt.setDouble(3, value);
			
			pstmt.executeUpdate();
			

		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		} catch (ParseException e) {
			logger.warn("Problems while parsing timestamp");
			e.printStackTrace();
		}
	}
	

	
	/**
	 * This testing methods grabs the timestamp of the last measurement from the database.
	 *(not used in main program)
	 * @param feedID of the aqe
	 * @param param requested parameter (O2, C0, temp etc.)
	 * @return
	 */
	public Date getTestLatestUpdate(String feedID, String param){
		String query = "SELECT max(date) AS date FROM observations WHERE feedid='"+feedID+"'";
		Date result = utils.getCurrentTimeAsDate();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) result = rs.getTimestamp("date");
		} catch (SQLException e) {
			logger.info("Invalid query");
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * Function checks if an egg is already in the database and does nothing if it is or the query fails
	 * @param feedID
	 * @return true if the egg is in the database
	 */
	//TODO Makes it sense to assume that egg is in db?
	public boolean isEggInDatabase(String feedID){
		boolean eggInDatabase = true;
		String query = "SELECT COUNT(feature_of_interest_id) FROM feature_of_interest WHERE feature_of_interest_id='"+feedID+"'";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			//move iterator one step
			rs.next();
			if (rs.getInt("count")!=0) {
				eggInDatabase = true;
			} else {
				eggInDatabase = false;
			}
		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		}
		return eggInDatabase;
	}
	
	
	/**
	 * Function adds a new feature (egg) to the database
	 * @param infoList: parameters of feature: id, name, description, lat, lon
	 */
	public void addNewFeature(ArrayList<String> infoList){
		String query = "INSERT INTO feature_of_interest (feature_of_interest_id, feature_of_interest_name, feature_of_interest_description, geom, feature_type, schema_link) VALUES ('"+infoList.get(0)+"', '"+infoList.get(1)+"', '"+infoList.get(2)+"', ST_GeomFromText('POINT("+infoList.get(4)+" "+infoList.get(3)+")', 4326),'sa:SamplingPoint', 'http://xyz.org/reference-url2.html');";
		try {
			Statement stmt = con.createStatement();
			stmt.execute(query);
		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		}
	}
	
	/**
	 * Function for adding a observed phenomenon to the database
	 * 
	 * @param phenomenon_id
	 *            is primary key, urn of phenomenon specified by ogc
	 * @param phenomenon_description
	 *            describes the phenomenon
	 * @param unit
	 *            of the phenomenon
	 * @param valuetype
	 *            use 'numericType'
	 */
	public void addPhenomenon(String phenomenon_id,
			String phenomenon_description, String unit, String valuetype) {
		// only add phenomenon to database if not yet added
		if (!this.isPhenomenonInDatabase(phenomenon_id)) {

			String query = "INSERT INTO phenomenon VALUES ('" + phenomenon_id
					+ "', '" + phenomenon_description + "', '" + unit + "','"
					+ valuetype + "');";
			try {
				Statement stmt = con.createStatement();
				stmt.execute(query);
			} catch (SQLException e) {
				logger.warn("Invalid query");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if the phenomenon is already in database
	 * @param phenomenon_id
	 * @return boolean (true if phenomenon is in database)
	 */
	// TODO Makes it sense to assume that phenomenon is in db?
	public boolean isPhenomenonInDatabase(String phenomenon_id) {
		boolean phenomenonInDatabase = true;
		String query = "SELECT COUNT(phenomenon_id) FROM phenomenon WHERE phenomenon_id='"+phenomenon_id+"';";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			if (rs.getInt("count") != 0) {
				phenomenonInDatabase = true;
			} else {
				phenomenonInDatabase = false;
			}
		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		}
		return phenomenonInDatabase;
	}
	
	/**
	 * Function to add a procedure
	 * @param procedure_id to be added to database
	 */
	public void addProcedure(String procedure_id){
		// only add phenomenon to database if not yet added
				if (!this.isProcedureInDatabase(procedure_id)) {
					String query = "INSERT INTO procedure(procedure_id) VALUES ('" + procedure_id+"');";
					try {
						Statement stmt = con.createStatement();
						stmt.execute(query);
					} catch (SQLException e) {
						logger.warn("Invalid query");
						e.printStackTrace();
					}
				}
	}
	
	/**
	 * Checks if the phenomenon is already in database
	 * @param phenomenon_id
	 * @return boolean (true if phenomenon is in database)
	 */
	// TODO Makes it sense to assume that phenomenon is in db?
	public boolean isProcedureInDatabase(String procedure_id) {
		boolean procedureInDatabase = true;
		String query = "SELECT COUNT(procedure_id) FROM procedure WHERE procedure_id='"+procedure_id+"';";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			if (rs.getInt("count") != 0) {
				procedureInDatabase = true;
			} else {
				procedureInDatabase = false;
			}
		} catch (SQLException e) {
			logger.info("Invalid query");
			e.printStackTrace();
		}
		return procedureInDatabase;
	}
	
	
	/**
	 * Function to add a offering.
	 * @param offering_id is primary key
	 * @param offering_name is name of offering
	 */
	public void addOffering(String offering_id, String offering_name){
		// only add phenomenon to database if not yet added
				if (!this.isOfferingInDatabase(offering_id)) {
					String query = "INSERT INTO offering VALUES ('" + offering_id+"','"+offering_name+"');";
					try {
						Statement stmt = con.createStatement();
						stmt.execute(query);
					} catch (SQLException e) {
						logger.warn("Invalid query");
						e.printStackTrace();
					}
				}
	}
	
	/**
	 * Checks if the offering is already in database
	 * @param offering_id
	 * @return boolean (true if phenomenon is in database)
	 */
	// TODO Makes it sense to assume that phenomenon is in db?
	public boolean isOfferingInDatabase(String offering_id) {
		boolean offeringInDatabase = true;
		String query = "SELECT COUNT(offering_id) FROM offering WHERE offering_id='"+offering_id+"';";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			if (rs.getInt("count") != 0) {
				offeringInDatabase = true;
			} else {
				offeringInDatabase = false;
			}
		} catch (SQLException e) {
			logger.info("Invalid query");
			e.printStackTrace();
		}
		return offeringInDatabase;
	}
	
	/**
	 * Method that establishes the n to m relations of the database by filling the proper tables with foreign keys
	 * @param tableName name of the table that establishes the relation
	 * @param firstKey first foreign key
	 * @param secondKey second foreign key
	 */
	public void linkTwoTables(String tableName, String firstCol, String secondCol, String firstKey, String secondKey){
		// if link is not yet established
		if (!isLinkEstablished(tableName, firstCol, secondCol, firstKey, secondKey)){
			String query = "INSERT INTO "+tableName+" VALUES ('"+firstKey+"' , '"+secondKey+"');";
			try {
				Statement stmt = con.createStatement();
				stmt.execute(query);
			} catch (SQLException e) {
				logger.warn("Invalid query");
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Methods that checks if the link is already established
	 * @param tableName is name of the table
	 * @param firstCol is name of the first column
	 * @param secondCol is name of the second column
	 * @param firstKey is the key to be entered into the table
	 * @param secondKey is the key to be entered into the table
	 * @return true if the link between the two keys is already established
	 */
	// TODO Makes it sense to assume that link is established?
	public boolean isLinkEstablished(String tableName, String firstCol, String secondCol, String firstKey, String secondKey){
		boolean linkEstablished = true;
		String query = "SELECT COUNT("+firstCol+") FROM "+tableName+" WHERE "+firstCol+"= '"+firstKey+"' AND "+secondCol+"='"+secondKey+"';";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			if (rs.getInt("count") != 0) {
				linkEstablished = true;
			} else {
				linkEstablished = false;
			}
		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		}
		return linkEstablished;
		
	}
	
	
/**
 * Method that saves a single measurement to database
 * @param date as a Date object
 * @param procedure_id of the corresponding procedure
 * @param feature_of_interest_id is id fo the corresponding feature
 * @param phenomenon_id of the corresponding phenomenon
 * @param offering_id of the corresponding offering
 * @param value of the measurement
 */
	public void insertMeasurement(Date date, String procedure_id, String feature_of_interest_id, String phenomenon_id, String offering_id, double value ){
		String query = "INSERT INTO observation (time_stamp, procedure_id, feature_of_interest_id,phenomenon_id,offering_id,numeric_value) values ('"+date+"', '"+procedure_id+"', '"+feature_of_interest_id+"','"+phenomenon_id+"','"+offering_id+"','"+value+"');"; 
		//logger.info(query);
		try {
			Statement stmt = con.createStatement();
			stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			//save the keys for linking to quality table
			ResultSet keyset = stmt.getGeneratedKeys();
			keyset.next();
			//save the observation id
			int observation_id = keyset.getInt("observation_id");
			//create new row in the quality table for the observation
			this.writeQuality(observation_id);
		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method that creates a new entry in the quality table for marking outliers.
	 * Standard value for 'outliers' is 'not_tested'.
	 * @param observation_id of corresponding observation
	 * @throws SQLException if query fails
	 */
	public void writeQuality(int observation_id) throws SQLException{
		String query = "INSERT INTO quality (quality_name, quality_unit, quality_value, quality_type, observation_id) VALUES ('outlier','not_tested/yes/no','not_tested','category','"+observation_id+"');";
		Statement stmt = con.createStatement();
		stmt.execute(query);
	}
	
	/**
	 * Method queries the last timestamp for a given phenomenon and feature from the observation table
	 * @param feature_of_interest_id is the id of the feature (AQE feed id)
	 * @param phenomenon_id is the id of the measured phenomenon
	 * @return date of the last update or standard date if no entry available
	 */
	public Date getLatestUpdate(String feature_of_interest_id, String phenomenon_id){
		String query = "SELECT max(time_stamp) AS date FROM observation WHERE feature_of_interest_id='"+feature_of_interest_id+"' AND phenomenon_id='"+phenomenon_id+"'";
		//if there's not yet an entry take the current time and substract 72 hours
		Date result = utils.substractHours(utils.getCurrentTimeAsDate(), 72);
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()  && rs.getObject("date")!=null){
				result = rs.getTimestamp("date");
				//logger.info(result.toString());
			}
		} catch (SQLException e) {
			logger.warn("Invalid query");
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Function returns all features of interest ids
	 * @return ArrayList of Strings
	 */
	public ArrayList<String> getAllFeatures(){
		String query = "SELECT feature_of_interest_id FROM feature_of_interest";
		ArrayList<String> list = new ArrayList<String>();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()){
				list.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	

}
