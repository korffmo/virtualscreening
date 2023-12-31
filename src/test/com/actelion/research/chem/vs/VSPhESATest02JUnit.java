package com.actelion.research.chem.vs;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import org.openchemlib.chem.vs.business.InfoVS;
import org.openchemlib.chem.vs.business.VSParallel;
import org.openchemlib.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.UserDirsDefault;
import com.actelion.research.util.datamodel.StringDouble;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collections;

public class VSPhESATest02JUnit {

    public static final String PATH = "resources/testdatavs/phesa/test02/";

    @Test
    public void vsPheSASingleConfQuery() throws Exception {

        File workdir = new File(UserDirsDefault.getTmp(0));

        URL urlPathQuery = this.getClass().getClassLoader().getResource(PATH + "queryPheSA.dwar");

        URL urlPathLib = this.getClass().getClassLoader().getResource(PATH + "actives.dwar");

        File fiDWARQuery = new File(urlPathQuery.getFile());
        File fiDWARLibraryDescriptors = new File(urlPathLib.getFile());

        String nameVSResult = "vsResultPheSATest02" + ConstantsDWAR.DWAR_EXTENSION;

        StringDouble sd = new StringDouble(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName, 0);

        File fiParameterVSXML = VSJUnit.createParameterFile(fiDWARLibraryDescriptors, fiDWARQuery, Collections.singletonList(sd), workdir);

        ModelVSXML modelXML = ModelVSXML.get(fiParameterVSXML.toURI().toURL());

        modelXML.setQueryIdentifier(null);

        modelXML.setNameDWARResultElusive(nameVSResult);

        VSParallel vs = new VSParallel(modelXML);

        vs.run();

        InfoVS infoVS = vs.getInfoVS();

        System.out.println(infoVS.toString());

        File fiDWARResultVS = new File(workdir, nameVSResult);
        URL urlResultValidation = this.getClass().getClassLoader().getResource(PATH + nameVSResult);
        File fiVSResultValidation = new File(urlResultValidation.getFile());
        VSPhESAJUnit.assertSimilarity(fiDWARResultVS, fiVSResultValidation);
    }

}
