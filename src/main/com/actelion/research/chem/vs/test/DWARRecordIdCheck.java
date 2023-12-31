package com.actelion.research.chem.vs.test;

import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHandlerFlexophore;
import com.actelion.research.chem.dwar.*;

import java.io.File;
import java.util.List;

/**
 * DWARRecordIdCheck
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * May 14, 2013 MvK Start implementation
 */
public class DWARRecordIdCheck {

	
	public static void main(String[] args) throws Exception {
		
		File fiDWAR = new File(args[0]);


		DescriptorHandler dhFlexophore = DescriptorHandlerFlexophore.getDefaultInstance();

		DescriptorHandler [] arrDHVS = new DescriptorHandler [1];
		
		arrDHVS[0] = dhFlexophore;
		
        DWARInterface2ModelVSRecordCompiler dwarInterface2ModelVSRecordCompilerQuery =
				new DWARInterface2ModelVSRecordCompiler(fiDWAR, 1000, arrDHVS, false, false, 0);
        
        List<ModelVSRecord> liModelVSRecordQuery = dwarInterface2ModelVSRecordCompilerQuery.getNextBatch();
		
		
        for (int i = 0; i < liModelVSRecordQuery.size(); i++) {
        	ModelVSRecord modelVSRecord = liModelVSRecordQuery.get(i);
        	
        	long id = modelVSRecord.getId();
			
			int recordNo = i;
			
			if(id!=recordNo){
				System.err.println("Error in row " + i + " id " + id + " recordNo " + recordNo);
			}
		}
		
		System.out.println("Finished");

	}

	
	
	public static void testODEFileHandler(String pathDWAR) throws Exception {
	
	
		
		File fiIn = new File(pathDWAR);
		
		if(!fiIn.isFile()){
			System.err.println("Not a file "+ fiIn.getAbsolutePath() + ".");
		}
		
		
		
		DWARFileHandler fh = new DWARFileHandler(fiIn);
		
		
		int ccRecord=0;
		while(fh.hasMore()){
			DWARRecord rec = fh.next();
			
			int id = (int)rec.getID();
			
			int recordNo = Integer.parseInt(rec.getAsString(AddRecordNumber.TAG_RECORD_NO));
			
			if(id!=recordNo){
				System.err.println("Error in row " + ccRecord + " id " + id + " recordNo " + recordNo);
			}
			
			ccRecord++;
		}
			
		fh.close();
		
		System.out.println("Finished");
		
	}


}
