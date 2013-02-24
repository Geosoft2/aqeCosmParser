/**
 * This class provides metadata regarding the AQE sensors needed for the SOS
 * database using the singleton pattern.
 * 
 * @author sven
 * 
 */
public class sensorSet {
	
	//constructor and singleton stuff
	
	private static sensorSet uniqInstance;

	  private sensorSet() {
	  }

	  public static synchronized sensorSet getInstance() {
	    if (uniqInstance == null) {
	      uniqInstance = new sensorSet();
	    }
	    return uniqInstance;
	  }
	
	//phenomena
	private String coPhenomenon = "dummy:ogc:co";
	private String humidityPhenomenon = "urn:x-ogc:def:phenomenon:OGC:RelativeHumidity";
	private String no2Phenomenon = "dummy:ogc:no2";
	private String temperaturePhenomenon = "urn:x-ogc:def:phenomenon:OGC:AirTemperature";
	private String o3Phenomenon = "dummy:ogc:o3";
	
	//procedures
	private String coProcedure = "urn:ogc:object:feature:Sensor:AQE:co-sensor";
	private String humidityProcedure = "urn:ogc:object:feature:Sensor:AQE:humidity-sensor";
	private String no2Procedure = "urn:ogc:object:feature:Sensor:AQE:no2-sensor";
	private String temperatureProcedure = "urn:ogc:object:feature:Sensor:AQE:temperature-sensor";
	private String o3Procedure = "urn:ogc:object:feature:Sensor:AQE:o3-sensor";
	
	//offerings
	private String coOffering = "CO_CONCENTRATION";
	private String humidityOffering = "AIR_HUMIDITY";
	private String no2Offering = "NO2_CONCENTRATION";
	private String temperatureOffering = "TEMPERATURE";
	private String o3Offering = "O3_CONCENTRATION";
	
	//getters and setters
	
	public String getCoPhenomenon() {
		return coPhenomenon;
	}
	public void setCoPhenomenon(String coPhenomenon) {
		this.coPhenomenon = coPhenomenon;
	}
	public String getHumidityPhenomenon() {
		return humidityPhenomenon;
	}
	public void setHumidityPhenomenon(String humidityPhenomenon) {
		this.humidityPhenomenon = humidityPhenomenon;
	}
	public String getNo2Phenomenon() {
		return no2Phenomenon;
	}
	public void setNo2Phenomenon(String no2Phenomenon) {
		this.no2Phenomenon = no2Phenomenon;
	}
	public String getTemperaturePhenomenon() {
		return temperaturePhenomenon;
	}
	public void setTemperaturePhenomenon(String temperaturePhenomenon) {
		this.temperaturePhenomenon = temperaturePhenomenon;
	}
	public String getO3Phenomenon() {
		return o3Phenomenon;
	}
	public void setO3Phenomenon(String o3Phenomenon) {
		this.o3Phenomenon = o3Phenomenon;
	}
	public String getCoProcedure() {
		return coProcedure;
	}
	public void setCoProcedure(String coProcedure) {
		this.coProcedure = coProcedure;
	}
	public String getHumidityProcedure() {
		return humidityProcedure;
	}
	public void setHumidityProcedure(String humidityProcedure) {
		this.humidityProcedure = humidityProcedure;
	}
	public String getNo2Procedure() {
		return no2Procedure;
	}
	public void setNo2Procedure(String no2Procedure) {
		this.no2Procedure = no2Procedure;
	}
	public String getTemperatureProcedure() {
		return temperatureProcedure;
	}
	public void setTemperatureProcedure(String temperatureProcedure) {
		this.temperatureProcedure = temperatureProcedure;
	}
	public String getO3Procedure() {
		return o3Procedure;
	}
	public void setO3Procedure(String o3Procedure) {
		this.o3Procedure = o3Procedure;
	}
	public String getCoOffering() {
		return coOffering;
	}
	public void setCoOffering(String coOffering) {
		this.coOffering = coOffering;
	}
	public String getHumidityOffering() {
		return humidityOffering;
	}
	public void setHumidityOffering(String humidityOffering) {
		this.humidityOffering = humidityOffering;
	}
	public String getNo2Offering() {
		return no2Offering;
	}
	public void setNo2Offering(String no2Offering) {
		this.no2Offering = no2Offering;
	}
	public String getTemperatureOffering() {
		return temperatureOffering;
	}
	public void setTemperatureOffering(String temperatureOffering) {
		this.temperatureOffering = temperatureOffering;
	}
	public String getO3Offering() {
		return o3Offering;
	}
	public void setO3Offering(String o3Offering) {
		this.o3Offering = o3Offering;
	}




}
