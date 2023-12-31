package com.actelion.research.chem.vs.test;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.actelion.research.chem.descriptor.DescriptorHandlerSkeletonSpheres;

/**
 * 
 * 
 * PerformanceVectorComparison
 * Creates dummy descriptors and calculates all similarities. 
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Jul 18, 2012 MvK: Start implementation
 */
public class PerformanceVectorComparison {
	
	private static final int DESCRIPTOR_SIZE = 1024;
	
	public static void main(String[] args) {
		
		int nDummies = 1000;
		
		DescriptorHandlerSkeletonSpheres dhSkeletonSpheres = DescriptorHandlerSkeletonSpheres.getDefaultInstance();
		
		Random rand = new Random();
		
		byte [] [] arrDescriptor = new byte [nDummies][DESCRIPTOR_SIZE];
		
		for (int i = 0; i < nDummies; i++) {
			
			byte [] descriptor = new byte[DESCRIPTOR_SIZE];	
			
			for (int j = 0; j < descriptor.length; j++) {
				descriptor[j]=(byte)rand.nextInt(Byte.MAX_VALUE);
			}
			
			arrDescriptor[i]=descriptor;
			
		}
		
		double sumSim=0;
		
		long start = new Date().getTime();
		
		AtomicInteger atomicIndexI = new AtomicInteger();
		AtomicInteger atomicIndexJ = new AtomicInteger();
		
		int ccSimCalc=0;
		for (int i = 0; i < nDummies; i++) {
			int indexI = atomicIndexI.getAndIncrement();
			
			atomicIndexJ.set(0);
			for (int j = 0; j < nDummies; j++) {
				double sim = dhSkeletonSpheres.getSimilarity(arrDescriptor[indexI], arrDescriptor[atomicIndexJ.getAndIncrement()]);
				ccSimCalc++;	
				sumSim+=sim;
			}
		}
		
		long end = new Date().getTime();
		
		System.out.println("Sum sim " + sumSim);
		
		
		long delta = end-start;
		
		double msPerSimCalc = delta/(double)ccSimCalc;
		
		System.out.println("Time needed " + delta + "[ms]. Comparisons " + ccSimCalc + ".");
		
		System.out.println("Milli sec per similarity calculation " + msPerSimCalc + "[ms/sim calc].");
		
		
		
	}
}
