import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MinionHolder {
	
	private String 	address;
	private String 	port;
	private int 	minionID;
	
	
	public MinionHolder(String address, String port, int minionID) {
		this.address = address;
		this.port = port;
		this.minionID = minionID;
	}
	
	
	
	public MyCallable calculate(String hash, int range)
	{
        return new MyCallable(this.address, this.port, hash, range, this.minionID);
	}

	
	
	public void terminateCalc() {
		executePost(this.address + this.port + "/terminateCalc", "");
	}
	
	public static String executePost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;
		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); 
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    //e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}
	
}
	
	
	
	
	

