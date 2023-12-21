package com.actelion.research.chem.vs.business;

import java.io.File;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorInfo;
import com.actelion.research.chem.dwar.DWARFileHandler;

/**
 * CheckForMissingDescriptorsMain
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Sep 18, 2013 MvK Start implementation
 */
public class CheckForMissingDescriptorsMain {

	
	
	public static void main(String[] args) throws Exception {
		File fiDWAR = new File(args[0]);

		
		DescriptorInfo[] arrDescriptorInfo =  DescriptorConstants.DESCRIPTOR_LIST;
		
		String [] arrDescriptorShortNames = new String [arrDescriptorInfo.length];
		for (int i = 0; i < arrDescriptorInfo.length; i++) {
			arrDescriptorShortNames[i] = arrDescriptorInfo[i].shortName;
		}
		
		int [] arrMissingDescriptors = CheckForMissingDescriptors.check(fiDWAR, arrDescriptorShortNames);

		System.out.println("Missing descriptors");
		
		for (int i = 0; i < arrMissingDescriptors.length; i++) {
			
			System.out.println(arrDescriptorShortNames[i] + ": " + arrMissingDescriptors[i]);
		}
		
		System.out.println("Finished");
		
	}

}
