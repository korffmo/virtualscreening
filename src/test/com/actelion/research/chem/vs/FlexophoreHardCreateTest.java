package com.actelion.research.chem.vs;

import com.actelion.research.chem.descriptor.DescriptorConstantsExtended;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.GenDescriptorMulticore;
import org.openchemlib.chem.vs.test.ConstantsVSTest;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;
import com.actelion.research.util.UserDirsDefault;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlexophoreHardCreateTest {


    public static void main(String[] args) throws Exception {
        File workdir = new File(UserDirsDefault.getTmp(0));

        URL urlPathLib = FlexophoreHardCreateTest.class.getClassLoader().getResource(ConstantsVSTest.PATH_QUERY);
        // URL urlPathLibShort = FlexophoreHardCreateTest.class.getClassLoader().getResource(ConstantsVSTest.PATH_LIBRAY_SHORT);

        File fiDWARLibrary  = new File(urlPathLib.getFile());

        String nameOutLibraryShort = IO.getBaseName(fiDWARLibrary) + "LibraryDescriptors" + ConstantsDWAR.DWAR_EXTENSION;
        File fiDWARLibraryDescriptors = new File(workdir, nameOutLibraryShort);

        GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);


        List<String> liDescriptorNames3D = new ArrayList<>();
//        liDescriptorNames3D.add(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);
//        liDescriptorNames3D.add(DescriptorConstants.DESCRIPTOR_Flexophore.shortName);
//        liDescriptorNames3D.add(DescriptorConstantsExtended.DESCRIPTOR_Flexophore_HighRes.shortName);
        liDescriptorNames3D.add(DescriptorConstantsExtended.DESCRIPTOR_PTREE.shortName);
        liDescriptorNames3D.add(DescriptorConstantsExtended.DESCRIPTOR_FlexophoreHardPPP.shortName);

        List<DescriptorHandler> liDescriptorHandler3D = DescriptorHandlerExtendedFactory.getFromNames(liDescriptorNames3D);

        String tagIdCode = ConstantsDWAR.TAG_IDCODE2;

        genDescriptorMulticore.generate(fiDWARLibrary, tagIdCode, fiDWARLibraryDescriptors, liDescriptorHandler3D);

        // FFViewer.viewMolecule(new FFMolecule(mol));
    }

}
