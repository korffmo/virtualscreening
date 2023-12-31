package com.actelion.research.chem.vs.business;

import com.actelion.research.chem.descriptor.MissingDescriptorGenerator;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.dwar.DWARFileHandler;
import com.actelion.research.chem.dwar.DWARInterface;
import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.ConstantsDWAR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * VSInThread
 * Adds missing descriptors for dwar file input.
 * Performs the virtual screening.
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * 2007 MvK: Start implementation
 * 07.12.2012 MvK: Major updates.
 * 31.01.2014 MvK: IOError thrown if no dwar interface
 */
public class VSInThread implements Runnable {
	
   private ModelVSXML modelVS;

    private String tagIdCode;

    public VSInThread(){
    	tagIdCode = ConstantsDWAR.TAG_IDCODE2;
    }

    public void run() {

        File dir = modelVS.getWorkDir();

        try {
        	List<ModelDescriptorVS> liEnabDescr = DescriptorVSHelper.getEnabled(modelVS.getLiDescriptors());
        	
        	List<String> liEnabDescrNames = new ArrayList<String>();
        	for (ModelDescriptorVS par : liEnabDescr) {
        		liEnabDescrNames.add(par.getShortName());
			}
        	
        	System.out.println("Get interface for library " + modelVS.getLibrary() + ".");
        	
			DWARInterface interfaceDB = new DWARFileHandler(new File(modelVS.getLibrary()));

			if(interfaceDB==null){
        		throw new IOException("Not a valid interface to a dwar source '" + modelVS.getLibrary() + "'.");
        	}
        	
        	System.out.println("Successfully instanciation of base interface " + interfaceDB.getSource() + ".");
        	
        	DWARInterface interfaceQuery = new DWARFileHandler(new File(modelVS.getQuery()));
        	
        	if(interfaceQuery==null){
        		throw new IOException("Not a valid interface to a dwar source '" + modelVS.getQuery() + "'.");
        	}
        	
        	System.out.println("Successfully instanciation of query interface " + interfaceQuery.getSource() + ".");
        	
        	File fiDBDescriptors = MissingDescriptorGenerator.checkAndAddDescriptors(modelVS.getLibrary(), tagIdCode, liEnabDescrNames, dir);
        	if(fiDBDescriptors != null){
        		 modelVS.setLibrary(fiDBDescriptors.getAbsolutePath());
        	}
        	
        	File fiQueryDescriptors = MissingDescriptorGenerator.checkAndAddDescriptors(modelVS.getQuery(), tagIdCode, liEnabDescrNames, dir);
        	if(fiQueryDescriptors != null){
        		modelVS.setQuery(fiQueryDescriptors.getAbsolutePath());
        	}
        	
        	VSParallel vs = new VSParallel(modelVS);
        	
        	vs.run();
        
    		System.out.println("Wrote parameter file.\n");
            try {
            	
            	if(ConstantsVS.COMPARISON_MODE_VS.equals(modelVS.getComparisonMode())) {
            		modelVS.write(new File(modelVS.getWorkDir(), ConstantsVS.FILE_PARAMETER_VS));
            	}else if(ConstantsVS.COMPARISON_MODE_LibComp.equals(modelVS.getComparisonMode())) {
            		modelVS.write(new File(modelVS.getWorkDir(), ConstantsVS.FILE_PARAMETER_LIBCOMP));
            	}
            	
            } catch (Exception ex) { System.out.println(ex.getMessage()); }

            System.out.println("Finished virtual screening.\n");

            
        } catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage() + "\n");
		}
        
    }

    
    
    public void setModel(ModelVSXML m) {
        modelVS = m;
    }
}
