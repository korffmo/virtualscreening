package com.actelion.research.chem.vs;

import com.actelion.research.chem.descriptor.*;
import com.actelion.research.chem.vs.business.ConstantsVS;
import com.actelion.research.chem.vs.business.InfoVS;
import com.actelion.research.chem.vs.business.VSParallel;
import com.actelion.research.chem.vs.business.xml.DescriptorXML;
import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.chem.vs.test.ConstantsVSTest;
import com.actelion.research.util.*;
import com.actelion.research.util.datamodel.StringDouble;
import org.junit.Test;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VSPTreeJUnit
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * Dec 13, 2021 MvK Start implementation
 * JUnit test for PTree
 */

public class VSPTreeJUnit {

	private static final double THRESHOLD_PTREE = 0.8;


	private static final int EXPECTED_HITS = 10;
	private static final int EXPECTED_MOLECULES_FOUND = 9;
	private static final int EXPECTED_MOLS_BASE = 100;
	private static final int EXPECTED_MOLS_QUERY = 3;
	private static final int EXPECTED_SCORES2CALCULATE = 300;
	private static final int EXPECTED_SCORES_CALCULATED = 300;


	private static final int EXPECTED_HITS_DELTA = 2;
	private static final int EXPECTED_MOLECULES_FOUND_DELTA = 1;

	private static final String PATH_LIBRAY = ConstantsVSTest.PATH_LIBRAY;

	private static final String PATH_QUERY = ConstantsVSTest.PATH_QUERY;

	private static final String PATH_PARAMETER_VS = ConstantsVSTest.PATH_PREFIX + "parameterVS_ACE.xml";

	private static final String PARAMETER_VS = "parameterVS.xml";

	private File fiDWARLibrary;
	private File fiDWARQuery;

	private File fiDWARLibraryDescriptors;
	private File fiDWARQueryDescriptors;

	private File workdir;

	private List<String> liDescriptorNames;

	public VSPTreeJUnit() throws Exception {

		init();

		conditionalCreateDescriptors();
	}

	private void init(){
		workdir = new File(UserDirsDefault.getTmp(0));

		URL urlPathLib = this.getClass().getClassLoader().getResource(PATH_LIBRAY);

		URL urlPathQuery = this.getClass().getClassLoader().getResource(PATH_QUERY);

		fiDWARLibrary = new File(urlPathLib.getFile());

		fiDWARQuery = new File(urlPathQuery.getFile());

		System.out.println("fiDWARLibrary " + fiDWARLibrary.getAbsolutePath());
		System.out.println("fiDWARQuery " + fiDWARQuery.getAbsolutePath());

		String nameOutLibrary = IO.getBaseName(fiDWARLibrary) + "LibraryDescriptors" + ConstantsDWAR.DWAR_EXTENSION;
		String nameOutQuery = IO.getBaseName(fiDWARQuery) + "Descriptors" + ConstantsDWAR.DWAR_EXTENSION;

		fiDWARLibraryDescriptors = new File(workdir, nameOutLibrary);

		fiDWARQueryDescriptors = new File(workdir, nameOutQuery);

		liDescriptorNames = new ArrayList<>();
		liDescriptorNames.add(DescriptorConstants.DESCRIPTOR_PTREE.shortName);
	}

	private void createDescriptors() throws Exception {

		GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);
		List<DescriptorHandler> liDescriptorHandler = DescriptorHandlerExtendedFactory.getFromNames(liDescriptorNames);

		String tagIdCode = ConstantsDWAR.TAG_IDCODE2;

		genDescriptorMulticore.generate(fiDWARLibrary, tagIdCode, fiDWARLibraryDescriptors, liDescriptorHandler);
		genDescriptorMulticore.generate(fiDWARQuery, tagIdCode, fiDWARQueryDescriptors, liDescriptorHandler);
	}

	private void conditionalCreateDescriptors() throws Exception {

		int maxHours = 1;

		boolean create = false;

		if((fiDWARLibraryDescriptors==null) || (fiDWARQueryDescriptors == null)){
			create=true;
		} else if(!fiDWARLibraryDescriptors.isFile() || !fiDWARQueryDescriptors.isFile()) {
			create=true;
		} else if(TimeDelta.isOlderThanHours(fiDWARLibraryDescriptors.lastModified(), maxHours)) {
			create=true;
		} else if(TimeDelta.isOlderThanHours(fiDWARQueryDescriptors.lastModified(), maxHours)) {
			create=true;
		}

		if(create){
			createDescriptors();
		}
	}

	@Test
	public void vsPTree() throws Exception {

		StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_PTREE.shortName, THRESHOLD_PTREE);

		InfoVS infoVS = vs(fiDWARLibraryDescriptors, fiDWARQueryDescriptors, Collections.singletonList(sd), workdir);

		System.out.println(infoVS.toString());

		assertResults(getExpectedValuesVSPTree(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);

	}

	public static File createParameterFile(File fiLibrary, File fiQuery, List<StringDouble> liDescriptorName, File workdir) throws JAXBException, IOException, ClassNotFoundException, ParserConfigurationException, SAXException {
		List<DescriptorXML> liDescriptors = new ArrayList<>();

		for (StringDouble sdShortNameThresh : liDescriptorName) {
			DescriptorXML descriptorXML = new DescriptorXML(sdShortNameThresh.getStr(), null, sdShortNameThresh.getVal(), true);
			liDescriptors.add(descriptorXML);
		}

		URL urlParameterXML =  new InfoVS().getClass().getClassLoader().getResource(PATH_PARAMETER_VS);

		ModelVSXML modelXML = ModelVSXML.get(urlParameterXML);

		modelXML.setHitOr(true);

		modelXML.setComparisonMode(ConstantsVS.COMPARISON_MODE_VS);

		modelXML.setWorkDir(workdir);

		modelXML.setLibrary(fiLibrary.getAbsolutePath());

		modelXML.setQuery(fiQuery.getAbsolutePath());

		modelXML.setLiDescriptors(liDescriptors);

		System.out.println(modelXML.toString());

		File fiXML = new File(workdir, PARAMETER_VS);

		modelXML.write(fiXML);

		return fiXML;
	}


	public static InfoVS vs(

		File fiLibrary, File fiQuery, List<StringDouble> liDescriptorName, File workdir) throws Exception {

		File fiParameterVSXML = createParameterFile(fiLibrary, fiQuery, liDescriptorName, workdir);

		ModelVSXML modelXML = ModelVSXML.get(fiParameterVSXML.toURI().toURL());

		VSParallel vs = new VSParallel(modelXML);

		vs.run();

		InfoVS infoVS = vs.getInfoVS();

		return infoVS;

	}


	public static void assertResults(InfoVS infoVSExpected, InfoVS infoVS, int maxDeltaHits, int maxDeltaMolFound){

		org.junit.Assert.assertEquals(infoVSExpected.getMolsBase(), infoVS.getMolsBase());

		org.junit.Assert.assertEquals(infoVSExpected.getMolsQuery(), infoVS.getMolsQuery());

		org.junit.Assert.assertEquals(infoVSExpected.getScores2Calculate(), infoVS.getScores2Calculate());

		org.junit.Assert.assertEquals(infoVSExpected.getScoresCalculated(), infoVS.getScoresCalculated());

		long deltaHits = Math.abs(infoVSExpected.getHits()-infoVS.getHits());
		boolean numHitsInRange = true;
		if(deltaHits>maxDeltaHits){
			numHitsInRange = false;
		}

		org.junit.Assert.assertTrue("Number of hits differ to much!", numHitsInRange);

		long deltaMolecules = Math.abs(infoVSExpected.getMoleculesFound()-infoVS.getMoleculesFound());
		boolean numMoleculesInRange = true;
		if(deltaMolecules>maxDeltaMolFound){
			numMoleculesInRange = false;
		}

		org.junit.Assert.assertTrue("Number of molecules found differ to much!", numMoleculesInRange);

	}


	private static InfoVS getExpectedValuesVSPTree(){
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
