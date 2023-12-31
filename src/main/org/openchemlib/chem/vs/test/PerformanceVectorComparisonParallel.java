package org.openchemlib.chem.vs.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * PerformanceVectorComparisonParallel
 * Just does the prallel calculationl, does not need memory at all. 
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Jul 18, 2012 MvK: Start implementation
 */
public class PerformanceVectorComparisonParallel {
	
	private static final int MAX_MULTIPLIER = Byte.MAX_VALUE+1;
	
	private static final int SHIFT = 7;
	
	
	private static final int [] MULT_TABLE = new int [MAX_MULTIPLIER*MAX_MULTIPLIER];
	
	private static final int DESCRIPTOR_SIZE = 1024;

	private AtomicInteger ccCalculations;
	
	private int nCalculations;
	
	private int nKernels;

	
	public PerformanceVectorComparisonParallel(int nCalculations) {
		
		this.nCalculations = nCalculations;
				
		ccCalculations = new AtomicInteger();
		
		int n = MAX_MULTIPLIER * MAX_MULTIPLIER;
		
		System.out.println("n " + n);
		
		for (int i = 0; i < MAX_MULTIPLIER; i++) {
			
			int index1 = i << SHIFT;
			
			for (int j = 0; j < MAX_MULTIPLIER; j++) {
				MULT_TABLE[index1+j]=i*j;
			}
		}
		
//		for (int i = 0; i < MAX_MULTIPLIER; i++) {
//			
//			int index1 = MAX_MULTIPLIER*i;
//			
//			for (int j = 0; j < MAX_MULTIPLIER; j++) {
//				
//				
//				System.out.print(MULT_TABLE[index1+j]);
//				System.out.print(" ");
//			}
//			System.out.println("");
//		}
		
		
		
	}
	
	public static void main(String[] args) {
		
		int nCalculations = 1000000;
		
		int cores = Runtime.getRuntime().availableProcessors();
		
		System.out.println("Cores " + cores);
		
		PerformanceVectorComparisonParallel performanceVectorComparisonParallel = new PerformanceVectorComparisonParallel(nCalculations);
		
		for (int i = 1; i < cores+1; i++) {
			performanceVectorComparisonParallel.setKernels(i);
			performanceVectorComparisonParallel.runComparison();	
		}
	}
	
	
	
	private void runComparison(){
		
		ccCalculations.set(0);
		
		System.out.println("Threads " + nKernels);
		
		List<Thread> liThread = new ArrayList<Thread>(); 
		
		List<SimilarityThread> liSimilarityThread = new ArrayList<SimilarityThread>(); 
		
		
		for (int i = 0; i < nKernels; i++) {
			
			SimilarityThread similarityThread = new SimilarityThread();
			
			liSimilarityThread.add(similarityThread);
			
			Thread th = new Thread(similarityThread);
			
			liThread.add(th);
		}
		
		boolean finished = false;
		
		long start = new Date().getTime();
		

		System.out.println("Start threads");
		for(Thread th : liThread){
			th.start();
		}
		System.out.println("Threads started");
		
		while(!finished){
			
			finished = true;
			for(Thread th : liThread){
				if(th.isAlive()){
					finished = false;
					break;
				}
			}
			
			
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		long end = new Date().getTime();
		
		long delta = end-start;
		
		double msPerSimCalc = delta/(double)ccCalculations.get();
		
		System.out.println("Time needed " + delta + "[ms]. Comparisons " + ccCalculations.get() + ".");
		
		System.out.println("Milli sec per similarity calculation " + msPerSimCalc + "[ms/sim calc].");
		
		double sumSimilarity=0;
		for(SimilarityThread similarityThread : liSimilarityThread){
			sumSimilarity += similarityThread.getSumSimilarity();
		}
		System.out.println("Sum similarity " + sumSimilarity + ".");
	}
	
	
	
	class SimilarityThread implements Runnable {
		
		private double sumSimilarity;
		
		byte [] a1;
		byte [] a2;
		
		public SimilarityThread() {
			
			a1 = new byte[DESCRIPTOR_SIZE];
			
			a2 = new byte[DESCRIPTOR_SIZE];
			Random rand = new Random(); 
			for (int i = 0; i < a1.length; i++) {
				a1[i]=(byte) rand.nextInt(Byte.MAX_VALUE);
				a2[i]=(byte) rand.nextInt(Byte.MAX_VALUE);
			}
			
		}
		
		public void run() {
			
			sumSimilarity = 0;
			
			
			int calculations = ccCalculations.getAndIncrement();
			
			while(calculations < nCalculations) {
				
					
				double sim = mult(a1, a2);
						
				sumSimilarity += sim;
					
				calculations = ccCalculations.getAndIncrement();
				
			}
		}
		
		public double getSumSimilarity() {
			return sumSimilarity;
		}
		
		private int mult(byte [] a1, byte [] a2){
			int product=0;
			
			for (int i = 0; i < a1.length; i++) {
				
				product += a1[i]*a2[i];
				
			}
			
			return product;
		}

		private int multLookup(byte [] a1, byte [] a2){
			int product=0;
			
			for (int i = 0; i < a1.length; i++) {
				
				int index = (a1[i] << SHIFT) + a2[i];
				
				product += MULT_TABLE[index];
				
			}
			
			return product;
		}
		
		
	}
	
	public void setKernels(int nKernels) {
		this.nKernels = nKernels;
	}

	
	
}
