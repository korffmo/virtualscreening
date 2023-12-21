package com.actelion.research.chem.vs.test;

import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.GenDescriptorMulticore;
import com.actelion.research.chem.vs.business.InfoVS;
import com.actelion.research.chem.vs.business.VSParallel;
import com.actelion.research.chem.vs.business.xml.DescriptorXML;
import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



/**
 * GenerateDescriptorsVSTest
 *
 * <p>Copyright: Idorsia Pharmaceuticals Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Idorsia Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * Created by korffmo1 on 21.06.18.
 */
public class GenerateDescriptorsVSTest {

    public static final String PARAMETER_VS = "parameterVS_Shape.xml";

    public static final String PATH_PARAMETER_VS = "/home/korffmo1/git/VirtualScreening/virtualscreening/resources/TestData/parameterVS_5HT1A_ROCS.xml";


    static final String USAGE =
        "GenerateDescriptorsShapeVSTest\n" +
        "Modest von Korff\n" +
        "Idorsia Pharmaceuticals Ltd.\n" +
        "Hegenheimermattweg 91\n" +
        "CH-4123 Allschwil\n" +
        "2018\n" +
        "Calculates the descriptors and performs a virtual screening.\n" +
        "Parameter parameterFileVS.xml \n" +
        ".\n";



    public static void main(String[] args) throws Exception {

        if(args.length==0 || args[0].equals("-h")){
            System.out.println(USAGE);
            System.exit(0);
        }

        File fiXMLParameterVS = new File(args[0]);

        ModelVSXML modelVSXML = ModelVSXML.get(fiXMLParameterVS.toURI().toURL());

        File dir = modelVSXML.getWorkDir();

        List<DescriptorXML> liDescriptorXML = modelVSXML.getLiDescriptors();
        //
        // Generate descriptors
        //
        List<String> liDescriptorNames = new ArrayList<String>();
        for (DescriptorXML descriptorXML : liDescriptorXML) {
            liDescriptorNames.add(descriptorXML.getName());
        }

        GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);

        List<DescriptorHandler> liDescriptorHandler = DescriptorHandlerExtendedFactory.getFromNames(liDescriptorNames);

        File fiDWARLibrary = new File(modelVSXML.getLibrary());

        File fiDWARQuery = new File(modelVSXML.getQuery());

        String nameOutLibrary = IO.getBaseName(fiDWARLibrary) + "Descriptors" + ConstantsDWAR.DWAR_EXTENSION;
        String nameOutQuery = IO.getBaseName(fiDWARQuery) + "Descriptors" + ConstantsDWAR.DWAR_EXTENSION;

        File fiDWARLibraryDescriptors = new File(dir, nameOutLibrary);

        File fiDWARQueryDescriptors = new File(dir, nameOutQuery);


        String tagIdCode = ConstantsDWAR.TAG_IDCODE2;


        genDescriptorMulticore.generate(fiDWARLibrary, tagIdCode, fiDWARLibraryDescriptors, liDescriptorHandler);
        genDescriptorMulticore.generate(fiDWARQuery, tagIdCode, fiDWARQueryDescriptors, liDescriptorHandler);


        modelVSXML.setLibrary(fiDWARLibraryDescriptors.getAbsolutePath());

        modelVSXML.setQuery(fiDWARQueryDescriptors.getAbsolutePath());

        System.out.println(modelVSXML.toString());

        modelVSXML.setWorkDir(dir);


        File fiXML = new File(dir, PARAMETER_VS);

        modelVSXML.write(fiXML);

        VSParallel vs = new VSParallel(modelVSXML);

        vs.run();

        InfoVS infoVS = vs.getInfoVS();

        System.out.println(infoVS.toString());

    }
}
