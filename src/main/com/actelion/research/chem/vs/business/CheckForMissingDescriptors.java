package com.actelion.research.chem.vs.business;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.dwar.DWARFileHandler;
import com.actelion.research.chem.dwar.DWARInterface;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.chem.dwar.toolbox.export.ConvertString2PheSA;
import com.actelion.research.chem.dwar.toolbox.export.NativeForSimilarityFromDWARExtractor;
import com.actelion.research.util.ConstantsDWAR;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * CheckForMissingDescriptors
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Sep 18, 2013 MvK Start implementation
 */
public class CheckForMissingDescriptors {

	public static int [] check(File fiDWAR, String [] arrDescriptorShortNames) throws NoSuchFieldException, IOException {

		DWARFileHandler fh = new DWARFileHandler(fiDWAR);
		
		int [] arrMissingDescriptors = new int [arrDescriptorShortNames.length];

		while(fh.hasMore()){
			
			DWARRecord  record = fh.next();
			
			for (int i = 0; i < arrDescriptorShortNames.length; i++) {

				String shortName = arrDescriptorShortNames[i];

				if(!containsDescriptor(record, shortName)){
					arrMissingDescriptors[i]++;
				}
			}
		}
		
		return arrMissingDescriptors;
	}

	public static int check(DWARInterface idwar, DescriptorHandler dh) {

		idwar.reset();

		String shortName = dh.getInfo().shortName;

		int intMissingDescriptors = 0;

		while(idwar.hasMore()){

			DWARRecord  record = idwar.next();

			if(!containsDescriptor(record, shortName)){
				intMissingDescriptors++;
			}
		}

		return intMissingDescriptors;
	}


	public static int [] check(File fiDWAR, List<ModelDescriptorVS> li) throws NoSuchFieldException, IOException {

		DWARFileHandler fh = new DWARFileHandler(fiDWAR);

		int [] arrMissingDescriptors = new int [li.size()];

		while(fh.hasMore()){

			DWARRecord  record = fh.next();

			for (int i = 0; i < li.size(); i++) {

				String shortName = li.get(i).getShortName();

				if(!containsDescriptor(record, shortName)){
					arrMissingDescriptors[i]++;
				}
			}

		}

		return arrMissingDescriptors;
	}



	private static boolean containsDescriptor(DWARRecord record, String shortName) {

		boolean contains = false;

		if(shortName.equals(DescriptorConstants.DESCRIPTOR_MAX_COMMON_SUBSTRUCT.shortName)){
			String sDescriptor = record.getIdCode();

			if(sDescriptor!=null && sDescriptor.length()>0){
				contains = true;
			}

		} else if(shortName.equals(DescriptorConstants.DESCRIPTOR_SUBSTRUCT_QUERY_IN_BASE.shortName)){
			String sDescriptor = record.getIdCode();

			if(sDescriptor!=null && sDescriptor.length()>0){
				contains = true;
			}

		} else if(shortName.equals(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName)){
			contains = ConvertString2PheSA.containsPheSA(record);
		} else {
			String sDescriptor = record.getAsString(shortName);
			if(sDescriptor!=null && sDescriptor.length()>0){
				contains = true;
			}
		}

		return contains;
	}

}
