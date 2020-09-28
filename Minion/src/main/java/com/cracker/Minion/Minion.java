package com.cracker.Minion;

public class Minion {
	private String 	rangeEnd;
	private String 	rangeStart;
	private String 	hashToCrack;
	private boolean stopSolve;

	public Minion(String rangeStart, String rangeEnd, String hashToCrack) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.hashToCrack = hashToCrack;
		this.stopSolve = false;
	}
	
	
	public void stopSolve()
	{
		this.stopSolve = true;
	}

	public String solve() 
	{
		this.stopSolve = false;
		int start = Integer.parseInt(rangeStart.substring(2));
		int end = Integer.parseInt(rangeEnd.substring(2));
		String tmpPass;
		for(int i = start; i <= end; i++)
		{
			if(stopSolve) return "hash not in range:" + rangeStart.charAt(2);
			tmpPass = intToPass(i);
			if(MD5.getMd5(tmpPass).equals(this.hashToCrack)) return hashToCrack + "===" + tmpPass;
		}
		return "hash not in range:" + rangeStart.charAt(2);
	}

	private String intToPass(int i) {
		String pass = "" + i;
		while(pass.length() < 8)
		{
			pass = "0" + pass;
		}
		return "05" + pass;
	}
}
