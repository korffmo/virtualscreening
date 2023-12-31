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
 * Creates dummy descriptors and calculates similarities. 
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Jul 18, 2012 MvK: Start implementation
 */
public class PerformanceVectorComparisonParallelMemory {
	
	private static final int MAX_MULTIPLIER = Byte.MAX_VALUE+1;
	
	private static final int SHIFT = 7;
	
	
	private static final int [] MULT_TABLE = new int [MAX_MULTIPLIER*MAX_MULTIPLIER];

	
	private static final int DESCRIPTOR_SIZE = 1024;
	
	private AtomicInteger atomicIndexDB;
	
	private AtomicInteger ccSimilarityCalculations;
	
	private int nDummies;
	private int nQueries;
	
//	private byte [] [] arrDescriptor;
//	
//	private byte [] [] arrDescriptorQuery;
	
	private int nKernels;
	
	public PerformanceVectorComparisonParallelMemory(int nDummies, int nQueries) {
		
		this.nDummies = nDummies;
		this.nQueries = nQueries;
		
//		arrDescriptor = createDummyData (nDummies);
//		
//		arrDescriptorQuery = new byte [nQueries] []; 
//		
//		for (int i = 0; i < nQueries; i++) {
//			
//			byte [] descriptorQuery = new byte [DESCRIPTOR_SIZE];
//			
//			System.arraycopy(arrDescriptor[i], 0, descriptorQuery, 0, DESCRIPTOR_SIZE);
//			
//			arrDescriptorQuery[i]=descriptorQuery;
//		}
		
		atomicIndexDB = new AtomicInteger();
		
		ccSimilarityCalculations = new AtomicInteger();
		
		
//		int n = MAX_MULTIPLIER * MAX_MULTIPLIER;
//		
//		System.out.println("n " + n);
//		
//		for (int i = 0; i < MAX_MULTIPLIER; i++) {
//			
//			int index1 = i << SHIFT;
//			
//			for (int j = 0; j < MAX_MULTIPLIER; j++) {
//				MULT_TABLE[index1+j]=i*j;
//			}
//		}

		
	}
	
	public static void main(String[] args) {
		
		int nDummies = 1000000;
		int nQueries = 1000;
		

		int cores = Runtime.getRuntime().availableProcessors();
		
		System.out.println("Cores " + cores);
		
		
		PerformanceVectorComparisonParallelMemory performanceVectorComparisonParallel = new PerformanceVectorComparisonParallelMemory(nDummies, nQueries);
		
//		for (int i = 1; i < cores+1; i++) {
//			performanceVectorComparisonParallel.setKernels(i);
//			performanceVectorComparisonParallel.runComparison();	
//		}
		
		performanceVectorComparisonParallel.setKernels(cores);
		performanceVectorComparisonParallel.runComparison();	
		
		
		
		
	}
	
	static byte [] [] createDummyData (int nDummies) {
		
		byte [] [] arrDescriptor = new byte  [nDummies] [];
		
			
		Random rand = new Random();
		
		for (int i = 0; i < nDummies; i++) {
			
			byte [] descriptor = new byte[DESCRIPTOR_SIZE];	
			
			for (int j = 0; j < descriptor.length; j++) {
				descriptor[j]=(byte)rand.nextInt(Byte.MAX_VALUE);
			}
			
			arrDescriptor[i]=descriptor;
			
		}
		
		return arrDescriptor;
		
	}
	
	private void runComparison(){
		
		// int nProcessors = Runtime.getRuntime().availableProcessors() - 1;

		// int nProcessors = 1;
		
		ccSimilarityCalculations.set(0);
		
		atomicIndexDB.set(0);
		
		System.out.println("Threads " + nKernels);
		
		List<Thread> liThread = new ArrayList<Thread>(); 
		List<SimilarityThread> liSimilarityThread = new ArrayList<SimilarityThread>(); 
		
		int nDummiesPerThread = nDummies / nKernels;
		
		for (int i = 0; i < nKernels; i++) {
			
			SimilarityThread similarityThread = new SimilarityThread(nDummiesPerThread, nQueries);
			
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
		
		double msPerSimCalc = delta/(double)ccSimilarityCalculations.get();
		
		System.out.println("Time needed " + delta + "[ms]. Comparisons " + ccSimilarityCalculations.get() + ".");
		
		System.out.println("Milli sec per similarity calculation " + msPerSimCalc + "[ms/sim calc].");
		
		double sumSimilarity=0;
		for(SimilarityThread similarityThread : liSimilarityThread){
			sumSimilarity += similarityThread.getSumSimilarity();
		}
		System.out.println("Sum similarity " + sumSimilarity + ".");
	}
	
	
	
	class SimilarityThread implements Runnable {
		
		private double sumSimilarity;

		private byte [] [] arrDescriptor;
		
		private byte [] [] arrDescriptorQuery;
		

		public SimilarityThread(int dummies, int queries) {
			
			arrDescriptor = createDummyData (dummies);
			
			arrDescriptorQuery = new byte [queries] []; 
			
			for (int i = 0; i < queries; i++) {
				
				byte [] descriptorQuery = new byte [DESCRIPTOR_SIZE];
				
				System.arraycopy(arrDescriptor[i], 0, descriptorQuery, 0, DESCRIPTOR_SIZE);
				
				arrDescriptorQuery[i]=descriptorQuery;
			}

			
		}
		
		public void run() {
			
			sumSimilarity = 0;
		
			
			
			for (int i = 0; i < arrDescriptor.length; i++) {
				
				for (int j = 0; j < arrDescriptorQuery.length; j++) {
					
					// double sim = dhSkeletonSpheres.getSimilarity(arrDescriptor[indexDB], arrDescriptorQuery[i]);
					
					// double sim = i + indexDB;
					
					double sim = getSimilarity(arrDescriptor[i], arrDescriptorQuery[j]);
										
					sumSimilarity += sim;
					
					ccSimilarityCalculations.incrementAndGet();
				}
				
				
				
			}
		}
		
		
		
		public double getSumSimilarity() {
			return sumSimilarity;
		}
		
//		private double getTanimotoDist(byte [] a1, byte [] a2) {
//
//	        double sum = 0;
//	        double dAtB = mult(a1, a2);
//	        double dAtA = mult(a1, a1);
//	        double dBtB = mult(a2, a2);
//	        
//	        sum = dAtB / (dAtA + dBtB - dAtB);
//
//	        return sum;
//	    }

		
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
		
		/**
		 * No increase in performance when using binary operators.
		 * @param a1
		 * @param a2
		 * @return
		 */
		private int multBinary(byte [] a1, byte [] a2){
			int product=0;
			
			for (int i = 0; i < a1.length; i++) {
				
				int shift = Integer.bitCount(a2[i]);
				
				product += (a1[i] << shift) - a1[i];
				
			}
			
			return product;
		}
		
		final public float getSimilarity(final byte [] d1, final byte [] d2) {
	       
	        int total = 0;
	        
	        int matching = 0;
	        
	        for (int i=0; i<d1.length; i++) {
	        	
	            total += Math.max(d1[i], d2[i]);
	            
	            matching += Math.min(d1[i], d2[i]);
	        }
	
	       
	        return (float)matching/(float)total;
	    }

		
		
	}



	public void setKernels(int nKernels) {
		this.nKernels = nKernels;
	}
	
	
}
