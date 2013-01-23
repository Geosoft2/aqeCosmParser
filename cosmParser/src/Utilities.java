import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Utility class that provides methods that are usefull for the whole program.
 * 
 * @author sven
 * 
 */
public class Utilities {
	
	public static final long HOUR = 3600*1000; // in milli-seconds.

	// empty constructor
	public Utilities() {

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
				//System.out.println(format.format(start));
				list.add(format.format(start));
				//set calendar time to current start value
				cal.setTime(start);
				//add the duration
				cal.add(Calendar.HOUR_OF_DAY, duration);
				//subtract one second to decrement the upper interval border
				cal.add(Calendar.SECOND, -1);
				//System.out.println(format.format(cal.getTime()));
				//TODO: Das geht bestimmt auch eleganter... Hier könnte es noch irgendwann in die Hose gehen
				if(cal.getTime().compareTo(end)<=0)list.add(format.format(cal.getTime()));
				cal.add(Calendar.SECOND, +1);
				start = cal.getTime();
			}
			//System.out.println(format.format(start));
			//list.add(format.format(start));
			//System.out.println(format.format(end));
			list.add(format.format(end));

			
			
			
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
		
		
	}
	
	/**
     * Returns the current time as String like this: 2012-11-23T13:41:15Z
     * 
     * @return Current timestamp.
     */
    @SuppressWarnings("deprecation")
    public String getCurrentTimeOld()
    {
	Date currentDate = new Date(System.currentTimeMillis());

	int year = currentDate.getYear() + 1900;
	int month = currentDate.getMonth() + 1;
	int day = currentDate.getDate();
	int hours = currentDate.getHours();
	int minutes = currentDate.getMinutes();
	int seconds = currentDate.getSeconds();

	int[] values = { month, day, hours, minutes, seconds };

	String[] valuesAsStrings = new String[values.length];

	for (int i = 0; i < values.length; i++)
	{
	    if (values[i] < 10)
	    {
		valuesAsStrings[i] = "0" + values[i];
	    }
	    else
	    {
		valuesAsStrings[i] = "" + values[i];
	    }
	}

	String timestamp = year + "-" + valuesAsStrings[0] + "-"
		+ valuesAsStrings[1] + "T" + valuesAsStrings[2] + ":"
		+ valuesAsStrings[3] + ":" + valuesAsStrings[4] + "Z";

	return timestamp;
    }
    
    /**
     * 
     * @param param
     * @return
     */
    public String getCurrentTime(){
    	//calendar object to read out time from
    	Calendar cal = Calendar.getInstance();
    	//the format that we want
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		//return the calendars current time, formatted as above
		return (format.format(cal.getTime()));
		
    }


    public String getLastUpdateTime(String param){
		//TODO: Implement method (feed ID missing)
		/*
		 * Grab last timestamp of this param
		 */
		return "2013-01-02T01:00:00Z";
	}
    
    
}
