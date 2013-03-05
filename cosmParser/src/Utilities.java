import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/*Notes on dates:
 * cosm: 14:00:00Z (Z is offset of +1 hour)
 * our time: 15:00:00
 * sql time: 14:00:00+01
 * 
 */

/**
 * Utility class that provides methods that are useful for the whole program.
 * 
 * @author sven
 * 
 */
public class Utilities {
		
	private Logger logger;

	//constructor
	public Utilities() {
		// logging stuff
		logger = Logger.getLogger(this.getClass());
		PropertyConfigurator.configure("log4j.properties");
	}

	/**
	 * This method splits a long time interval given as strings (start and end value) that represent a timestamp encoded in ISO 8601
	 * into smaller ones with a defined length.
	 * 
	 * @param timestampStart is the beginning of the period
	 * @param timestampEnd is the end of the period
	 * @param duration defines the length of the splitting interval in hours
	 * @return List of Strings / timestamps containing lower and upper borders of intervals -> always even number of entries
	 */
	public ArrayList<String> splitInterval(String timestampStart, String timestampEnd,
			int duration) {
		
		//create calendar object and simple date format corresponding to cosm request
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		ArrayList<String> list = new ArrayList<String>();

		try {
			//parse given interval borders (strings) to Date objects
			//TODO: Rethink: Increment lower border of first interval with one second
			Date start = format.parse(timestampStart);
			cal.setTime(start);
			cal.add(Calendar.SECOND, +1);
			start = cal.getTime();
			
			Date end = format.parse(timestampEnd);
			
			
			//TODO: rethink border issues
			//while start date is smaller than end date add duration to start date			
			while (start.compareTo(end)<=0){
				list.add(format.format(start));
				//set calendar time to current start value
				cal.setTime(start);
				//add the duration
				cal.add(Calendar.HOUR_OF_DAY, duration);
				//subtract one second to decrement the upper interval border
				cal.add(Calendar.SECOND, -1);
				//TODO: works, but looks weird...
				// if current date is still before the end date -> add to list
				if(cal.getTime().compareTo(end)<=0)list.add(format.format(cal.getTime()));
				// add one second
				cal.add(Calendar.SECOND, +1);
				start = cal.getTime();
			}
			
			// if list contains an odd number of elements, attach the end timestamp, seems to work
			if(list.size()%2!=0) list.add(format.format(end));
			
			//logger.info(list.size());
			//logger.info(list);
			
		} catch (ParseException e) {
			logger.warn("Problems splitting time interval: "+list);
			e.printStackTrace();
		}
		
		return list;
		
	}
    
    /**
     * Get the current time as a String timestamp
     * @param param
     * @return current timestamp as string
     */
    public String getCurrentTimeAsString(){
    	//calendar object to read out time from
    	Calendar cal = Calendar.getInstance();
    	//the format that we want
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		//return the calendars current time, formatted as above
		return (format.format(cal.getTime()));		
    }
    
    /**
     * Get the current time as a Date object
     * @param param
     * @return current time as Date
     */
    public Date getCurrentTimeAsDate(){
    	//calendar object to read out time from
    	Calendar cal = Calendar.getInstance();
		//return the calendars current time, formatted as above
		return cal.getTime();
    }
    
    /**
     * Method for substracting hours from date
     * @param date to be modified
     * @param hours to be substracted
     * @return modified date
     */
    public Date substractHours(Date date, int hours){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR, -hours);
		return cal.getTime();
    }
    
    
    /**
     * Convert a string timestamp to a date object
     * @param timestamp
     * @return Date representation of timestamp parameter
     * @throws ParseException
     */
    public Date toDate(String timestamp) throws ParseException{
    	//remove last three digits
    	/*
    	 * http://stackoverflow.com/questions/5636491/date-object-simpledateformat-not-parsing-timestamp-string-correctly-in-java-and
    	 * fractional seconds = 0.271816 seconds
		 * What DateFormat sees is 271816 / 1000 of a second
		 * 271816 / 1000 == 271 seconds
		 * 271 / 60 = 4 minutes
		 * 271 % 60 = 31 seconds
		 *17:11:15 to 17:15:46 is exactly 4 minutes, 31 seconds off
    	 */
    	timestamp = timestamp.substring(0, timestamp.length()-4)+"Z";
		//format of timestamp string
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	return (format.parse(timestamp));
    }
    
    /**
     * Method that converts a timestamp with time zone read out of a psql database to a formatted string
     * @param date
     * @return
     */
    public String sqlDateToCosmString(Date date){
    	Calendar cal = Calendar.getInstance();
    	//the format we want
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	//set date of calendar to parameter date
    	cal.setTime(date);
    	// return formatted date
    	return format.format(cal.getTime());
    	
    }
    
    public Date TestToDate(String timestamp) throws ParseException{
		//format of timestamp string
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	return (format.parse(timestamp));
    }
    

    
}
