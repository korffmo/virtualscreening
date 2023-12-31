package com.actelion.research.chem.vs.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.actelion.research.chem.descriptor.DescriptorHandlerSkeletonSpheres;

/**
 * 
 * 
 * PerformanceVectorComparisonParallelMultiMemory
 * Creates dummy descriptors and calculates similarities. 
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Jul 18, 2012 MvK: Start implementation
 */
public class PerformanceVectorComparisonParallelMultiMemory {
	
	private static final int DESCRIPTOR_SIZE = 1024;
	
	private AtomicInteger ccSimilarityCalculations;
	
	private int nDummies;
	
	private int nQueries;
	
	
	public PerformanceVectorComparisonParallelMultiMemory(int nDummies, int nQueries) {
		
		this.nDummies = nDummies;
		
		this.nQueries = nQueries;
				
		ccSimilarityCalculations = new AtomicInteger();
		
	}
	
	public static void main(String[] args) {
		
		int nDummies = 1000;
		int nQueries = 1000;
		
		
		PerformanceVectorComparisonParallelMultiMemory performanceVectorComparisonParallel = new PerformanceVectorComparisonParallelMultiMemory(nDummies, nQueries);
		
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

		int nProcessors = 7;
		
		System.out.println("Create threads " + nProcessors);
		
		List<Thread> liThread = new ArrayList<Thread>();
		
		List<SimilarityThread> liSimilarityThread = new ArrayList<SimilarityThread>(); 
		
		long start = new Date().getTime();
		
		
		int nQueriesThread = nQueries / nProcessors;
		
		for (int i = 0; i < nProcessors; i++) {
			
			SimilarityThread similarityThread = new SimilarityThread(nQueriesThread);
			
			liSimilarityThread.add(similarityThread);
			

			System.out.println("Create thread " + i);
			
			Thread th = new Thread(similarityThread);
			
			liThread.add(th);
			
		}
		
		System.out.println("Start threads");
		for(Thread th : liThread){
			th.start();
		}
		System.out.println("Threads started");
		
		
		boolean finished = false;
		
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
		
		private DescriptorHandlerSkeletonSpheres dhSkeletonSpheres;
		
		private byte [] [] arrDescriptor;
		
		private byte [] [] arrDescriptorQuery;

		
		private double sumSimilarity;
		
		public SimilarityThread(int nQueriesThread) {
			
			dhSkeletonSpheres = new DescriptorHandlerSkeletonSpheres();	
			
			arrDescriptor = createDummyData (nDummies);
			
			arrDescriptorQuery = new byte [nQueriesThread] []; 
			
			for (int i = 0; i < nQueriesThread; i++) {
				
				byte [] descriptorQuery = new byte [DESCRIPTOR_SIZE];
				
				System.arraycopy(arrDescriptor[i], 0, descriptorQuery, 0, DESCRIPTOR_SIZE);
				
				arrDescriptorQuery[i]=descriptorQuery;
			}

			
		}
		
		public void run() {
			
			sumSimilarity = 0;
			
			
				
			for (int i = 0; i < arrDescriptor.length; i++) {
				
				for (int j = 0; j < arrDescriptorQuery.length; j++) {
					
					double sim = dhSkeletonSpheres.getSimilarity(arrDescriptor[i], arrDescriptorQuery[j]);
										
					sumSimilarity += sim;
					
					ccSimilarityCalculations.incrementAndGet();
				}
			}
				
		}
		
		

		public double getSumSimilarity() {
			return sumSimilarity;
		}
		
	}
	
	
}
