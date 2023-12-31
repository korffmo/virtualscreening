package org.openchemlib.chem.vs;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorConstantsExtended;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;
import com.actelion.research.util.TimeDelta;
import com.actelion.research.util.UserDirsDefault;
import com.actelion.research.util.datamodel.StringDouble;
import jakarta.xml.bind.JAXBException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openchemlib.chem.descriptor.DescriptorHandlerExtendedFactory;
import org.openchemlib.chem.descriptor.GenDescriptorMulticore;
import org.openchemlib.chem.descriptor.create.DescriptorSelectionHelper;
import org.openchemlib.chem.vs.business.ConstantsVS;
import org.openchemlib.chem.vs.business.InfoVS;
import org.openchemlib.chem.vs.business.VSParallel;
import org.openchemlib.chem.vs.business.xml.DescriptorXML;
import org.openchemlib.chem.vs.business.xml.ModelVSXML;
import org.openchemlib.chem.vs.test.ConstantsVSTest;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * VSJUnit
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * Sep 16, 2013 MvK Start implementation
 * 27.01.2017 re-implementation
 * August 2020 added tests for crystal structures.
 */
@RunWith(JUnit4.class)
public class VSJUnit {

	private static final double THRESHOLD = 0.8;
	private static final double THRESHOLD_PHESA = 0.65;
	private static final double THRESHOLD_PHESA_CRYSTAL = 0.5;
	private static final double THRESHOLD_FLEXOPHORE = 0.7;
	private static final double THRESHOLD_FLEXOPHORE_CRYSTAL = 0.4;


	private static final int EXPECTED_HITS = 14;
	private static final int EXPECTED_MOLECULES_FOUND = 10;
	private static final int EXPECTED_MOLS_BASE = 100;
	private static final int EXPECTED_MOLS_QUERY = 3;
	private static final int EXPECTED_SCORES2CALCULATE = 1500;
	private static final int EXPECTED_SCORES_CALCULATED = 1500;


	private static final int EXPECTED_HITS_DELTA = 2;
	private static final int EXPECTED_MOLECULES_FOUND_DELTA = 1;


	private File fiDWARLibrary;
	private File fiDWARLibraryShort;
	private File fiDWARQuery;
	private File fiDWARQueryCrystal;

	private File fiDWARLibraryDescriptors;
	private File fiDWARLibraryShortDescriptors;


	private File fiDWARQueryDescriptors;
	private File fiDWARQueryCrystalDescriptors;
	private File workdir;

	private List<String> liDescriptorNames2D;
	private List<String> liDescriptorNames3D;


	public VSJUnit() throws Exception {

		init();

		conditionalCreateDescriptors();
	}

	private void init(){
		workdir = new File(UserDirsDefault.getTmp(0));

		URL urlPathLib = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_LIBRAY);
		URL urlPathLibShort = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_LIBRAY_SHORT);

		URL urlPathQuery = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_QUERY);
		URL urlPathQueryCrystal = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_QUERY_CRYSTAL_STRUCTURE);

		fiDWARLibrary = new File(urlPathLib.getFile());
		fiDWARLibraryShort = new File(urlPathLibShort.getFile());

		fiDWARQuery = new File(urlPathQuery.getFile());
		fiDWARQueryCrystal = new File(urlPathQueryCrystal.getFile());

		System.out.println("fiDWARLibrary " + fiDWARLibrary.getAbsolutePath());
		System.out.println("fiDWARQuery " + fiDWARQuery.getAbsolutePath());
		System.out.println("fiDWARLibraryShort " + fiDWARLibraryShort.getAbsolutePath());
		System.out.println("fiDWARQueryCrystal " + fiDWARQueryCrystal.getAbsolutePath());

		String nameOutLibrary = IO.getBaseName(fiDWARLibrary) + "LibraryDescriptors" + ConstantsDWAR.DWAR_EXTENSION;
		String nameOutLibraryShort = IO.getBaseName(fiDWARLibraryShort) + "LibraryDescriptors" + ConstantsDWAR.DWAR_EXTENSION;
		String nameOutQuery = IO.getBaseName(fiDWARQuery) + "Descriptors" + ConstantsDWAR.DWAR_EXTENSION;
		String nameOutQueryCrystal = IO.getBaseName(fiDWARQueryCrystal) + "Descriptors" + ConstantsDWAR.DWAR_EXTENSION;

		fiDWARLibraryDescriptors = new File(workdir, nameOutLibrary);
		fiDWARLibraryShortDescriptors = new File(workdir, nameOutLibraryShort);

		fiDWARQueryDescriptors = new File(workdir, nameOutQuery);
		fiDWARQueryCrystalDescriptors = new File(workdir, nameOutQueryCrystal);

		liDescriptorNames2D = new ArrayList<>();

		liDescriptorNames2D.addAll(DescriptorSelectionHelper.select(DescriptorSelectionHelper.NAME_SMALL_SET));
		liDescriptorNames2D.add(DescriptorConstants.DESCRIPTOR_PTREE.shortName);
		for (int i = liDescriptorNames2D.size() - 1; i >= 0; i--) {
			if(DescriptorConstants.DESCRIPTOR_Flexophore.shortName.equals(liDescriptorNames2D.get(i))){
				liDescriptorNames2D.remove(i);
				break;
			}
		}

		liDescriptorNames3D = new ArrayList<>();
		liDescriptorNames3D.add(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);
		liDescriptorNames3D.add(DescriptorConstants.DESCRIPTOR_Flexophore.shortName);
		liDescriptorNames3D.add(DescriptorConstantsExtended.DESCRIPTOR_Flexophore_HighRes.shortName);
		liDescriptorNames3D.add(DescriptorConstantsExtended.DESCRIPTOR_FlexophoreHardPPP.shortName);
	}


	@Test
	public void dummy() throws Exception {
		org.junit.Assert.assertFalse("Value screening finished.", false);
	}

	private void createDescriptors() throws Exception {

		//
		// Generate descriptors
		//
		GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);

		List<DescriptorHandler> liDescriptorHandler2D = DescriptorHandlerExtendedFactory.getFromNames(liDescriptorNames2D);

		List<DescriptorHandler> liDescriptorHandler3D = DescriptorHandlerExtendedFactory.getFromNames(liDescriptorNames3D);

		List<DescriptorHandler> liDescriptorHandler = new ArrayList<>(liDescriptorHandler2D);
		liDescriptorHandler.addAll(liDescriptorHandler3D);

		String tagIdCode = ConstantsDWAR.TAG_IDCODE2;

		genDescriptorMulticore.generate(fiDWARLibrary, tagIdCode, fiDWARLibraryDescriptors, liDescriptorHandler2D);
		genDescriptorMulticore.generate(fiDWARQuery, tagIdCode, fiDWARQueryDescriptors, liDescriptorHandler);

		genDescriptorMulticore.generate(fiDWARLibraryShort, tagIdCode, fiDWARLibraryShortDescriptors, liDescriptorHandler3D);
		genDescriptorMulticore.generate(fiDWARQueryCrystal, tagIdCode, fiDWARQueryCrystalDescriptors, liDescriptorHandler3D);

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

		// create = true;
		if(create){
			createDescriptors();
		}
	}

	@Test
	public void vsPheSA() throws Exception {

		StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName, THRESHOLD_PHESA);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSPheSA(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsPheSACrystal() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName, THRESHOLD_PHESA_CRYSTAL);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryCrystalDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSPheSACrystal(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsFlexophore() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_Flexophore.shortName, THRESHOLD_FLEXOPHORE);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSFlexophore(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsFlexophoreCrystal() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_Flexophore.shortName, THRESHOLD_FLEXOPHORE_CRYSTAL);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryCrystalDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSFlexophoreCrystal(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsFlexophoreHard() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstantsExtended.DESCRIPTOR_FlexophoreHardPPP.shortName, THRESHOLD_FLEXOPHORE);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSFlexophoreHard(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsFlexophoreHardCrystal() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstantsExtended.DESCRIPTOR_FlexophoreHardPPP.shortName, 0.5);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryCrystalDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSFlexophoreHardCrystal(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsFlexophoreHighRes() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstantsExtended.DESCRIPTOR_Flexophore_HighRes.shortName, THRESHOLD_FLEXOPHORE);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSFlexophoreHighRes(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsFlexophoreHighResCrystal() throws Exception {
		StringDouble sd = new StringDouble(DescriptorConstantsExtended.DESCRIPTOR_Flexophore_HighRes.shortName, 0.75);
		InfoVS infoVS = vs(fiDWARLibraryShortDescriptors, fiDWARQueryCrystalDescriptors, Collections.singletonList(sd), workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSFlexophoreHighResCrystal(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vs() throws Exception {
		double threshVS = THRESHOLD;
		List<StringDouble> liDescriptor = new ArrayList<>();
		for (String shortName : liDescriptorNames2D) {
			StringDouble sd = new StringDouble(shortName, threshVS);
			liDescriptor.add(sd);
		}
		InfoVS infoVS = vs(fiDWARLibraryDescriptors, fiDWARQueryDescriptors, liDescriptor, workdir);
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVS(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsHitAND() throws Exception {

		// double threshVS = THRESHOLD;
		double threshVS = 0.5;

		List<DescriptorXML> liDescriptorXML = new ArrayList<>();
		for (String shortName : liDescriptorNames2D) {
			DescriptorXML descriptorXML = new DescriptorXML(shortName, null, threshVS, true);
			liDescriptorXML.add(descriptorXML);
		}

		URL urlParameterXML = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_PARAMETER_VS);
		ModelVSXML modelXML = ModelVSXML.get(urlParameterXML);
		modelXML.setHitOr(false);
		modelXML.setComparisonMode(ConstantsVS.COMPARISON_MODE_VS);
		modelXML.setWorkDir(workdir);
		modelXML.setLibrary(fiDWARLibraryDescriptors.getAbsolutePath());
		modelXML.setQuery(fiDWARQueryDescriptors.getAbsolutePath());
		modelXML.setLiDescriptors(liDescriptorXML);
		File fiXML = new File(workdir, ConstantsVSTest.PARAMETER_VS);
		modelXML.write(fiXML);
		VSParallel vs = new VSParallel(modelXML);
		vs.run();
		InfoVS infoVS = vs.getInfoVS();
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSHitAND(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsLibComp() throws Exception {
		double threshVS = 0.75;

		List<DescriptorXML> liDescriptorXML = new ArrayList<>();
		for (String shortName : liDescriptorNames2D) {
			DescriptorXML descriptorXML = new DescriptorXML(shortName, null, threshVS, true);
			liDescriptorXML.add(descriptorXML);
		}

		URL urlParameterXML = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_PARAMETER_VS);
		ModelVSXML modelXML = ModelVSXML.get(urlParameterXML);
		modelXML.setHitOr(true);
		modelXML.setComparisonMode(ConstantsVS.COMPARISON_MODE_LibComp);
		modelXML.setWorkDir(workdir);
		modelXML.setLibrary(fiDWARLibraryDescriptors.getAbsolutePath());
		modelXML.setQuery(fiDWARQueryDescriptors.getAbsolutePath());
		modelXML.setLiDescriptors(liDescriptorXML);
		System.out.println(modelXML.toString());

		File fiXML = new File(workdir, ConstantsVSTest.PARAMETER_VS);
		modelXML.write(fiXML);
		VSParallel vs = new VSParallel(modelXML);
		vs.run();
		InfoVS infoVS = vs.getInfoVS();
		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSLibComp(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	@Test
	public void vsLibCompHitAND() throws Exception {
		double threshVS = 0.5;

		List<DescriptorXML> liDescriptorXML = new ArrayList<>();
		for (String shortName : liDescriptorNames2D) {
			DescriptorXML descriptorXML = new DescriptorXML(shortName, null, threshVS, true);
			liDescriptorXML.add(descriptorXML);
		}

		URL urlParameterXML = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_PARAMETER_VS);
		ModelVSXML modelXML = ModelVSXML.get(urlParameterXML);
		modelXML.setHitOr(false);
		modelXML.setComparisonMode(ConstantsVS.COMPARISON_MODE_LibComp);
		modelXML.setWorkDir(workdir);
		modelXML.setLibrary(fiDWARLibraryDescriptors.getAbsolutePath());
		modelXML.setQuery(fiDWARQueryDescriptors.getAbsolutePath());
		modelXML.setLiDescriptors(liDescriptorXML);
		System.out.println(modelXML.toString());

		File fiXML = new File(workdir, ConstantsVSTest.PARAMETER_VS);
		modelXML.write(fiXML);
		VSParallel vs = new VSParallel(modelXML);
		vs.run();
		InfoVS infoVS = vs.getInfoVS();

		System.out.println(infoVS.toString());
		assertResults(getExpectedValuesVSLibCompAND(), infoVS, EXPECTED_HITS_DELTA, EXPECTED_MOLECULES_FOUND_DELTA);
	}

	public static File createParameterFile(File fiLibrary, File fiQuery, List<StringDouble> liDescriptorName, File workdir) throws JAXBException, IOException, ClassNotFoundException, ParserConfigurationException, SAXException {
		List<DescriptorXML> liDescriptors = new ArrayList<>();

		for (StringDouble sdShortNameThresh : liDescriptorName) {
			DescriptorXML descriptorXML = new DescriptorXML(sdShortNameThresh.getStr(), null, sdShortNameThresh.getVal(), true);
			liDescriptors.add(descriptorXML);
		}

		URL urlParameterXML =  new InfoVS().getClass().getClassLoader().getResource(ConstantsVSTest.PATH_PARAMETER_VS);
		ModelVSXML modelXML = ModelVSXML.get(urlParameterXML);
		modelXML.setHitOr(true);
		modelXML.setComparisonMode(ConstantsVS.COMPARISON_MODE_VS);
		modelXML.setWorkDir(workdir);
		modelXML.setLibrary(fiLibrary.getAbsolutePath());
		modelXML.setQuery(fiQuery.getAbsolutePath());
		modelXML.setLiDescriptors(liDescriptors);
		System.out.println(modelXML.toString());
		File fiXML = new File(workdir, ConstantsVSTest.PARAMETER_VS);
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


	private static InfoVS getExpectedValuesVSPheSA(){
		InfoVS infoVS = new InfoVS();
		
		infoVS.setHits(15);
		infoVS.setMoleculesFound(7);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(3);
		infoVS.setScores2Calculate(30);
		infoVS.setScoresCalculated(30);
		
		return infoVS;
	}
	private static InfoVS getExpectedValuesVSPheSACrystal(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(4);
		infoVS.setMoleculesFound(4);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(1);
		infoVS.setScores2Calculate(10);
		infoVS.setScoresCalculated(10);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSFlexophore(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(5);
		infoVS.setMoleculesFound(5);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(3);
		infoVS.setScores2Calculate(30);
		infoVS.setScoresCalculated(30);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSFlexophoreCrystal(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(3);
		infoVS.setMoleculesFound(3);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(1);
		infoVS.setScores2Calculate(10);

		infoVS.setScoresCalculated(10);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSFlexophoreHard(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(9);
		infoVS.setMoleculesFound(5);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(3);
		infoVS.setScores2Calculate(30);
		infoVS.setScoresCalculated(30);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSFlexophoreHighResCrystal(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(2);
		infoVS.setMoleculesFound(2);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(1);
		infoVS.setScores2Calculate(10);
		infoVS.setScoresCalculated(10);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSFlexophoreHardCrystal(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(2);
		infoVS.setMoleculesFound(2);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(1);
		infoVS.setScores2Calculate(10);
		infoVS.setScoresCalculated(10);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSFlexophoreHighRes(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(10);
		infoVS.setMoleculesFound(5);
		infoVS.setMolsBase(10);
		infoVS.setMolsQuery(3);
		infoVS.setScores2Calculate(30);
		infoVS.setScoresCalculated(30);

		return infoVS;
	}


	private static InfoVS getExpectedValuesVS(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(EXPECTED_HITS);
		infoVS.setMoleculesFound(EXPECTED_MOLECULES_FOUND);
		infoVS.setMolsBase(EXPECTED_MOLS_BASE);
		infoVS.setMolsQuery(EXPECTED_MOLS_QUERY);
		infoVS.setScores2Calculate(EXPECTED_SCORES2CALCULATE);
		infoVS.setScoresCalculated(EXPECTED_SCORES_CALCULATED);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSHitAND(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(21);
		infoVS.setMoleculesFound(10);
		infoVS.setMolsBase(EXPECTED_MOLS_BASE);
		infoVS.setMolsQuery(EXPECTED_MOLS_QUERY);
		infoVS.setScores2Calculate(EXPECTED_SCORES2CALCULATE);
		infoVS.setScoresCalculated(EXPECTED_SCORES_CALCULATED);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSLibComp(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(20);
		infoVS.setMoleculesFound(87);
		infoVS.setMolsBase(EXPECTED_MOLS_BASE);
		infoVS.setMolsQuery(EXPECTED_MOLS_QUERY);
		infoVS.setScores2Calculate(EXPECTED_SCORES2CALCULATE);
		infoVS.setScoresCalculated(EXPECTED_SCORES_CALCULATED);

		return infoVS;
	}

	private static InfoVS getExpectedValuesVSLibCompAND(){
		InfoVS infoVS = new InfoVS();

		infoVS.setHits(21);
		infoVS.setMoleculesFound(90);
		infoVS.setMolsBase(EXPECTED_MOLS_BASE);
		infoVS.setMolsQuery(EXPECTED_MOLS_QUERY);
		infoVS.setScores2Calculate(EXPECTED_SCORES2CALCULATE);
		infoVS.setScoresCalculated(EXPECTED_SCORES_CALCULATED);

		return infoVS;
	}
}
