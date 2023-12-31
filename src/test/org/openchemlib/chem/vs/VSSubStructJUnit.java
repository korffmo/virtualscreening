/*
 * Copyright (c) 2019.

 *
 *  This file is part of DataWarrior.
 *
 *  DataWarrior is free software: you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  DataWarrior is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with DataWarrior.
 *  If not, see http://www.gnu.org/licenses/.
 *
 *  @author Modest v. Korff
 *
 */

package org.openchemlib.chem.vs;

import com.actelion.research.util.UserDirsDefault;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openchemlib.chem.vs.business.InfoVS;
import org.openchemlib.chem.vs.business.VSParallel;
import org.openchemlib.chem.vs.business.xml.ModelVSXML;

import java.io.File;
import java.net.URL;

@RunWith(JUnit4.class)
public class VSSubStructJUnit {

	private static final int EXPECTED_LIBRARIES_ON_SHARE = 4;
	
	private static final int EXPECTED_HITS = 1;
	private static final int EXPECTED_MOLECULES_FOUND = 1;
	private static final int EXPECTED_MOLS_BASE = 100;
	private static final int EXPECTED_MOLS_QUERY = 1;
	private static final int EXPECTED_SCORES2CALCULATE = 100;
	private static final int EXPECTED_SCORES_CALCULATED = 100;


	public static final String PATH_LIBRAY = "resources/testdatavs/ACE_100.dwar";

	public static final String PATH_QUERY = "resources/testdatavs/ACE_Query_SubStruct.dwar";

	public static final String PATH_PARAMETER_VS = "resources/testdatavs/parameterVS_ACE_SubStruct.xml";

	public static final String PARAMETER_VS = "parameterVS.xml";
	
	@Test
	public void dummy() throws Exception {
		org.junit.Assert.assertFalse("Value screening finished.", false);
	}

	@Test
	public void vs() throws Exception {

		File dir = new File(UserDirsDefault.getTmp(0));

		URL urlPathLib = this.getClass().getClassLoader().getResource(PATH_LIBRAY);
		URL urlPathQuery = this.getClass().getClassLoader().getResource(PATH_QUERY);
		URL urlParameter = this.getClass().getClassLoader().getResource(PATH_PARAMETER_VS);

		File fiDWARLibrary = new File(urlPathLib.getFile());

		File fiDWARQuery = new File(urlPathQuery.getFile());

        File fiXMLVS = new File(urlParameter.getFile());
		
		ModelVSXML modelXML = ModelVSXML.get(fiXMLVS.toURI().toURL());
				
		modelXML.setLibrary(fiDWARLibrary.getAbsolutePath());

		modelXML.setQuery(fiDWARQuery.getAbsolutePath());
				
		System.out.println(modelXML.toString());

		modelXML.setWorkDir(dir);

		File fiXML = new File(dir, PARAMETER_VS);
		
		modelXML.write(fiXML);
        
		VSParallel vs = new VSParallel(modelXML);
		
		vs.run();
		
		InfoVS infoVS = vs.getInfoVS();
		
		boolean differ = false;
		if(infoVS.isDifference(getExpectedValues())){
			differ = true;
		}
		
		System.out.println(infoVS.toString());

		org.junit.Assert.assertFalse("Value screening finished.", differ);
		
	}
	
	
	
	private static InfoVS getExpectedValues(){
		InfoVS infoVS = new InfoVS();
		
		infoVS.setHits(EXPECTED_HITS);
		infoVS.setMoleculesFound(EXPECTED_MOLECULES_FOUND);
		infoVS.setMolsBase(EXPECTED_MOLS_BASE);
		infoVS.setMolsQuery(EXPECTED_MOLS_QUERY);
		infoVS.setScores2Calculate(EXPECTED_SCORES2CALCULATE);
		infoVS.setScoresCalculated(EXPECTED_SCORES_CALCULATED);
		
		return infoVS;
	}
	
	

}
