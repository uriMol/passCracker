package com.cracker.Minion;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MinionController {
	private static ArrayList<Minion> minions = new ArrayList<Minion>();
	
	@PostMapping("/terminateCalc")
	public String terminateCalc()
	{
		for(Minion minion:minions)
		{
			minion.stopSolve();
		}
		return "minions terminated";
	}
	
	@PostMapping("/calculate")
	public String calculate(@RequestParam(value = "rangeStart", defaultValue = "") String rangeStart, 
						  @RequestParam(value = "rangeEnd", defaultValue = "") String rangeEnd,
						  @RequestParam(value = "hashToCrack", defaultValue = "") String hashToCrack) {
		Minion minion = new Minion(rangeStart,rangeEnd, hashToCrack);
		minions.add(minion);
		

		System.out.println("calculate was called with range " + rangeStart + 
				" to " + rangeEnd + " on hash : " + hashToCrack);

		
		
		String password = minion.solve();
		
		System.out.println(password);
		
		return password;
	}
}
