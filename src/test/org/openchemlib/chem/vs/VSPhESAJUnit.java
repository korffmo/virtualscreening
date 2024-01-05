package org.openchemlib.chem.vs;

import org.openchemlib.chem.descriptor.DescriptorHandlerExtendedFactory;
import org.openchemlib.chem.descriptor.GenDescriptorMulticore;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.chem.dwar.comparator.DWARNumberComparator;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;
import com.actelion.research.util.UserDirsDefault;
import com.actelion.research.util.datamodel.StringDouble;
import org.junit.Test;
import org.openchemlib.chem.vs.business.InfoVS;
import org.openchemlib.chem.vs.business.VSParallel;
import org.openchemlib.chem.vs.business.xml.ModelVSXML;
import org.openchemlib.chem.vs.test.ConstantsVSTestPheSA;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * VSPhESAJUnit
 * @author Modest von Korff
 * Nov 12, 2020 MvK Start implementation
 */
public class VSPhESAJUnit {

    public static final double THRESHOLD_PHESA = 0.6;
    public static final double THRESHOLD_PHESA_SINGLE_CONF = 0.2;

    public static final double MARGIN_SIM = 0.1;

    public static final String TAG_SimilarityPheSA = "SimilarityPheSA";
    public static final String TAG_PheSAPPSimilarity = "PheSAPPSimilarity";
    public static final String TAG_PheSAShapeSimilarity = "PheSAShapeSimilarity";


    File fiDWARQueryACE3DCrystalDescriptor;

    File fiDWARQuery;
    File fiDWARLibrary;
    File fiDWARLibraryDescriptors;
    File workdir;

    public VSPhESAJUnit() throws Exception {

        URL urlPathQueryACE3DCrystal = this.getClass().getClassLoader().getResource(ConstantsVSTestPheSA.PATH_QUERY_ACE3DCrystal);

        URL urlPathQuery = this.getClass().getClassLoader().getResource(ConstantsVSTestPheSA.PATH_QUERY);

        URL urlPathLib = this.getClass().getClassLoader().getResource(ConstantsVSTestPheSA.PATH_LIBRAY);

        File fiDWARQueryACE3DCrystal = new File(urlPathQueryACE3DCrystal.getFile());

        fiDWARQuery = new File(urlPathQuery.getFile());

        fiDWARLibrary = new File(urlPathLib.getFile());

        workdir = new File(UserDirsDefault.getTmp(0));

        fiDWARQueryACE3DCrystalDescriptor = new File(workdir, IO.getBaseName(fiDWARQueryACE3DCrystal.getName())+"Descriptors"+ConstantsDWAR.DWAR_EXTENSION);

        fiDWARLibraryDescriptors = new File(workdir, IO.getBaseName(fiDWARLibrary.getName())+"Descriptors"+ConstantsDWAR.DWAR_EXTENSION);

        createDescriptors(fiDWARQueryACE3DCrystal, fiDWARQueryACE3DCrystalDescriptor);

        createDescriptors(fiDWARLibrary, fiDWARLibraryDescriptors);
    }

    @Test
    public void vsPheSASingleConfQuery() throws Exception {

        String nameVSResult = "vsResultPheSASingleConfQuery" + ConstantsDWAR.DWAR_EXTENSION;

        StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_ShapeAlignSingleConf.shortName, THRESHOLD_PHESA_SINGLE_CONF);

        File fiParameterVSXML = VSJUnit.createParameterFile(fiDWARLibraryDescriptors, fiDWARQueryACE3DCrystalDescriptor, Collections.singletonList(sd), workdir);

        ModelVSXML modelXML = ModelVSXML.get(fiParameterVSXML.toURI().toURL());

        modelXML.setQueryIdentifier(null);

        modelXML.setNameDWARResultElusive(nameVSResult);

        VSParallel vs = new VSParallel(modelXML, null);

        vs.run();

        InfoVS infoVS = vs.getInfoVS();

        System.out.println(infoVS.toString());

        VSJUnit.assertResults(getExpectedValuesVSPheSASingleConfQueryACE(), infoVS, 0, 0);

        File fiDWARResultVS = new File(workdir, nameVSResult);
        URL urlPathLib = this.getClass().getClassLoader().getResource(ConstantsVSTestPheSA.PATH_PHESA + nameVSResult);
        File fiVSResultValidation = new File(urlPathLib.getFile());
        assertSimilarity(fiDWARResultVS, fiVSResultValidation);

    }

    @Test
    public void vsPheSACoordinates3DQuery() throws Exception {

        String nameVSResult = "vsResultPheSACoordinates3DQuery" + ConstantsDWAR.DWAR_EXTENSION;

        StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName, THRESHOLD_PHESA);

        File fiParameterVSXML = VSJUnit.createParameterFile(fiDWARLibraryDescriptors, fiDWARQuery, Collections.singletonList(sd), workdir);

        ModelVSXML modelXML = ModelVSXML.get(fiParameterVSXML.toURI().toURL());

        modelXML.setNameDWARResultElusive(nameVSResult);

        modelXML.setQueryIdentifier(null);

        VSParallel vs = new VSParallel(modelXML, null);

        vs.run();

        InfoVS infoVS = vs.getInfoVS();

        System.out.println(infoVS.toString());

        VSJUnit.assertResults(getExpectedValuesVSPheSASingleConfQuery(), infoVS, 0, 0);

        File fiDWARResultVS = new File(workdir, nameVSResult);
        URL urlPathLib = this.getClass().getClassLoader().getResource(ConstantsVSTestPheSA.PATH_PHESA + nameVSResult);
        File fiVSResultValidation = new File(urlPathLib.getFile());
        assertSimilarity(fiDWARResultVS, fiVSResultValidation);

    }

    public static void createDescriptors(File fiDWARLibrary, File fiDWARLibraryDescriptors) throws Exception {

        //
        // Generate descriptors
        //
        GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);
        List<String> liDescriptorNames3D = new ArrayList<>();
        liDescriptorNames3D.add(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);
        List<DescriptorHandler> liDescriptorHandler3D = DescriptorHandlerExtendedFactory.getFromNames(liDescriptorNames3D);
        String tagIdCode = ConstantsDWAR.TAG_IDCODE2;
        genDescriptorMulticore.generate(fiDWARLibrary, tagIdCode, fiDWARLibraryDescriptors, liDescriptorHandler3D);
    }


    public static void assertSimilarity(File fiVSResult, File fiVSResultValidation) throws NoSuchFieldException, IOException {
        List<DWARRecord> liDWARResultVS = DWARFileHandlerHelper.get(fiVSResult);
        List<DWARRecord> liDWARValidation = DWARFileHandlerHelper.get(fiVSResultValidation);
        DWARNumberComparator dwarNumberComparator = new DWARNumberComparator(TAG_SimilarityPheSA);
        Collections.sort(liDWARResultVS, dwarNumberComparator);
        Collections.sort(liDWARValidation, dwarNumberComparator);
        boolean similarityValid = equalSimilarity(liDWARResultVS, liDWARValidation);
        org.junit.Assert.assertTrue("Similarity equals", similarityValid);
    }


    public static boolean equalSimilarity(List<DWARRecord> liDWARResultVS, List<DWARRecord> liDWARValidation){
        boolean equal = true;
        if(liDWARResultVS.size() != liDWARValidation.size()){
            return false;
        }

        for (int i = 0; i < liDWARResultVS.size(); i++) {

            DWARRecord recResult = liDWARResultVS.get(i);
            DWARRecord recValidation = liDWARResultVS.get(i);

            if(!equalSimilarity(recResult, recValidation, TAG_SimilarityPheSA, MARGIN_SIM) ||
                    !equalSimilarity(recResult, recValidation, TAG_PheSAPPSimilarity, MARGIN_SIM) ||
                    !equalSimilarity(recResult, recValidation, TAG_PheSAShapeSimilarity, MARGIN_SIM)){
                equal=false;
                break;
            }
        }
        return equal;
    }

    public static boolean equalSimilarity(DWARRecord recResult, DWARRecord recValidation, String tag, double delta){

        boolean equal = true;

        String strValResult = recResult.getAsString(tag);
        String strValValidation = recValidation.getAsString(tag);

        if(strValResult==null && strValValidation==null){
            return true;
        }else if(strValResult==null || strValValidation==null){
            return false;
        }

        double simResult = Double.parseDouble(strValResult);
        double simValidation = Double.parseDouble(strValValidation);

        if(Math.abs(simResult-simValidation)>delta){
            equal=false;
        }

        return equal;
    }

    private static InfoVS getExpectedValuesVSPheSASingleConfQuery(){
        InfoVS infoVS = new InfoVS();

        infoVS.setHits(3);
        infoVS.setMoleculesFound(3);
        infoVS.setMolsBase(3);
        infoVS.setMolsQuery(1);
        infoVS.setScores2Calculate(3);
        infoVS.setScoresCalculated(3);

        return infoVS;
    }

    private static InfoVS getExpectedValuesVSPheSASingleConfQueryACE(){
        InfoVS infoVS = new InfoVS();

        infoVS.setHits(3);
        infoVS.setMoleculesFound(3);
        infoVS.setMolsBase(3);
        infoVS.setMolsQuery(1);
        infoVS.setScores2Calculate(3);
        infoVS.setScoresCalculated(3);

        return infoVS;
    }


}
