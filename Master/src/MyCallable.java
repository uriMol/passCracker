import java.util.concurrent.Callable;

public class MyCallable implements Callable<String>
{
	private String 	hash;
	private String 	address;
	private String 	port;
	private String 	parameters;
	private int 	range;
	private int 	minionID;
	
	public MyCallable(String address, String port, String hash, int range, int minionID) {
		this.address = address;
		this.port = port;
		this.hash = hash;
		this.range = range;
		this.minionID = minionID;
		String rangeStart = (range == 0) ? "0500000000" : "05" + range*10000000;
		String rangeEnd = "05" + (((range+1)*10000000) - 1);
		this.parameters = "/calculate?hashToCrack=" + this.hash + "&rangeStart=" + rangeStart + "&rangeEnd=" + rangeEnd;
	}

	@Override
	public String call() {
		String response = MinionHolder.executePost(this.address + this.port + this.parameters, "");
		if(response == null) {
			System.out.println("error from minion " + this.minionID+ " in calculating range " + this.range 
					+ ", this range will be calculated by a different minion");
			return "error:" + this.range;
		}
		return this.minionID + ":" + response;
	}
}
