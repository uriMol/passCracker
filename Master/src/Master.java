import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Master {
	
	public static final String	OUT_FILE = "Cracked passwords.txt";
	
	private ArrayList<String> 		hashList;
	private ArrayList<MinionHolder> minions;
	private ArrayList<String> 		notFound;
	private HashMap<String, String> cracked;
	private String 					minionsAddress;
	private int 					numOfMinions;
	
	public Master()
	{
		System.out.println("Master is up");
		this.hashList = new ArrayList<String>();
		this.minions = new ArrayList<MinionHolder>();
		this.notFound =  new ArrayList<String>();
		this.cracked = new HashMap<String, String>();
	}

	/*The idea of initializng the master outside of 
	it's constructor is that the same master 
	can be initialized again with different addresses and hashes
	*/
	private void initialize(String[] args) {
		this.minionsAddress = args[0] + ":";
		this.numOfMinions = Integer.parseInt(args[1]);
		this.initMinions(args[2]);
		try {
			readHashes(args[3]);
		} catch (IOException e) {
			System.out.println("In: initializeMaster : Error reading file");
			e.printStackTrace();
		}
		
	}
	
	
	/*
	 * called by initMaster() and initializes the 
	 * Minions using their constructor
	 */
	private void initMinions(String strPort) {
		int port = Integer.parseInt(strPort);
		System.out.print("Initializing " + this.numOfMinions + " minionHolders in ports: ");
		for(int i = 0; i < this.numOfMinions; i++)
		{
			this.minions.add(new MinionHolder(this.minionsAddress, "" + (port + i), i));
			System.out.print((port + i) + ", ");
		}
		System.out.println();
	}
	
	/*
	 * reads the hashes from a file, line by line, into a list
	 * everytime a hash is being solved, he will be moved
	 * either to 'this.cracked' or to 'this.notFound'
	 */
	private void readHashes(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String hash = reader.readLine();
		while(hash != null)
		{
			this.hashList.add(hash);
			hash = reader.readLine();
		}
		reader.close();
		System.out.println("hashes read, there are " + this.hashList.size() + " hashes");
	}
	
	
	/*
	 * handles each hash one by one, sends it to 'crack()'
	 * if password is found we move it from 'hashList' to 'cracked'
	 * otherwise, we move it to 'notFound'
	 */
	private void crackPasswords() throws InterruptedException, ExecutionException {
		String password;
		int i = 0;
		while(this.hashList.size() != 0)
		{
			System.out.println("Cracking hash no. " + i);
			password = crack(hashList.get(0));
			if(password.equals("hash not found"))
			{
				System.out.println("Crack no. " + i + " was not found");
				this.notFound.add(hashList.get(0));
				this.hashList.remove(0);
			} else if(password.split("===").length == 2)
			{
				System.out.println("Hash no. " + i + " was succesfully cracked");
				String[] hashAndPass = password.split("===");
				this.cracked.put(hashAndPass[0], hashAndPass[1]);
				this.hashList.remove(0);
			}
			i++;
		}
	}
	
	/*
	 * This is master's 'core' function, crack divides the
	 * password's possible range to 10 and sends each part
	 * of the range to a free Minion. 
	 * if a minions falls \ disconnects in the middle of the
	 * run, a different minion will cover for him 
	 */
	private String crack(String hash) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(this.numOfMinions);
        List<Integer> rangesNotDone = initRanges();
        List<Future<String>> futList = activateFirstCalcs(hash, rangesNotDone, executor);
		String tmpFutStr = "";
		int notDone = 10;
		Future<String> fut;
		while(notDone > 0)
		{
			for(int i = 0; i < futList.size(); i++)
			{
				fut = futList.get(i);
				if(fut.isDone())
				{
					tmpFutStr = fut.get();
					if(tmpFutStr.substring(0, 5).equals("error"))
					{
						rangesNotDone.add(Integer.parseInt(tmpFutStr.substring(6)));
						futList.remove(i--);
					} else if (tmpFutStr.substring(2,19).equals("hash not in range"))
					{
						notDone--;
						futList.remove(i--);
						if(rangesNotDone.size() != 0)
						{
							Callable<String> callable = HandleRemaining(tmpFutStr, rangesNotDone, hash);
							futList.add(executor.submit(callable));
						}
					} else
					{
						executor.shutdownNow();
						terminateMinions();
						return tmpFutStr.substring(2);
					}
				}
			}
		}
		executor.shutdownNow();
		terminateMinions();
		return "hash not found";	
	}
	
	
	/*
	 * called by crack, activates the first calculations
	 * and returns a list of these calculations future response
	 */
	private List<Future<String>> activateFirstCalcs(String hash, List<Integer> rangesNotDone,
			ExecutorService executor) 
	{
		List<Future<String>> futList = new ArrayList<Future<String>>();
		for(int range = 0; range < numOfMinions; range++)
		{
			Callable<String> callable = minions.get(range).calculate(hash, range);
			rangesNotDone.remove(Integer.valueOf(range));
            futList.add(executor.submit(callable));
		}		
		return futList;
	}
	
	/*
	 * called by crack() when a Minion finished calculating
	 * his range and sends him for another calculation on
	 * a different range
	 */
	private MyCallable HandleRemaining(String tmpFutStr, List<Integer> rangesNotDone, String hash) {
		int finishedMinionID = Integer.parseInt(tmpFutStr.substring(0,1));
		MinionHolder minion = minions.get(finishedMinionID);
		int range = rangesNotDone.get(0);
		rangesNotDone.remove(0);
		return minion.calculate(hash, range);
	}

	
	
	ArrayList<Integer> initRanges()
	{
		ArrayList<Integer> ranges = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++) {
			ranges.add(i);
		}
		return ranges;
	}
	
	
	/*
	 * called by crack() when finished, stops the minions work  
	 */
	private void terminateMinions() {
		for(MinionHolder minion:minions)
		{
			minion.terminateCalc();
		}
	}
	
	
	
	private void writeResults() {
		try {
		      BufferedWriter output = new BufferedWriter(new FileWriter(OUT_FILE)); 
		      output.write(this.cracked.size() + " hashes were cracked. Cracked hashes:\n");
		      String line;
		      for(Entry<String, String> entry:this.cracked.entrySet())
		      {
		    	  line = "hash: ";
		    	  line += entry.getKey();
		    	  line += "   was cracked as ";
		    	  line += entry.getValue();
		    	  output.write(line + "\n");
		      }
		      if(this.notFound.size() != 0)
		      {
		    	  output.write("\n" + this.notFound.size() + " hashes were not cracked:\n");
		    	  for(String hash:this.notFound)
		    	  {
		    		  output.write(hash + "\n");
		    	  }
		      }
		      output.close();
		    }
		    catch (Exception e) {
		      e.getStackTrace();
		    }
	}

	/*
	 * called if main's arguments are not right
	 */
	public static void mainError()
	{
		System.out.println("Master should recieve 4 arguments:\n"
			+ "-Minions address\n"
			+ "-Number of Minions (1 - 10)\n"
			+ "-Minions first port number (ports are expected to have following port numbers\n"
			+ "-hashes file name");
		System.exit(0);
	}


	private static void checkArgs(String[] args) {
		if(args.length != 4)  mainError();
		int minNum = Integer.parseInt(args[1]);
		if(minNum < 1 || minNum > 10 )  mainError();
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		checkArgs(args);

		Master master = new Master();
				
		master.initialize(args); 
		
		master.crackPasswords();
		
		master.writeResults();
		
		System.out.println("Program is done, " + master.cracked.size() + " hashes were cracked, "
				+ master.notFound.size() + " hashes were not found."
						+ "\nResults are in '" + OUT_FILE + "'");
	}
}

