package org.openchemlib.chem.vs.test;

import java.util.HashSet;
import java.util.Random;

public class PerformanceHashSet {
	
	public static void main(String[] args) {
		int n = 1000000;
		
		HashSet<Integer> hs = new HashSet<Integer>();
		
		
		Random rand = new Random();
		for (int i = 0; i < n; i++) {
			int rnd = rand.nextInt(n);
			hs.add(rnd);
		}
		
		
		System.out.println("Added " + hs.size());
		
		int ccRem = 0;
		for (int i = 0; i < n; i++) {
			int rnd = rand.nextInt(n);
			if(hs.contains(rnd)){
				hs.remove(n);
				ccRem++;
			}
			
		}
		
		System.out.println("Removed " + ccRem);
		
		
		
	}
}
