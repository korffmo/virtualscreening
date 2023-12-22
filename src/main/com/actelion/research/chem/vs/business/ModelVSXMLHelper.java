package com.actelion.research.chem.vs.business;

import com.actelion.research.chem.vs.business.xml.DescriptorXML;
import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.chem.vs.test.ConstantsVSTest;
import com.actelion.research.util.datamodel.StringDouble;
import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ModelVSXMLHelper {

    public static ModelVSXML createParameterVS(File fiLibrary, File fiQuery, List<StringDouble> liDescriptorName, File workdir) {
        List<DescriptorXML> liDescriptors = new ArrayList<>();

        for (StringDouble sdShortNameThresh : liDescriptorName) {
            DescriptorXML descriptorXML = new DescriptorXML(sdShortNameThresh.getStr(), null, sdShortNameThresh.getVal(), true);
            liDescriptors.add(descriptorXML);
        }


        ModelVSXML modelXML = new ModelVSXML();
        modelXML.setHitOr(true);
        modelXML.setComparisonMode(ConstantsVS.COMPARISON_MODE_VS);
        modelXML.setWorkDir(workdir);
        modelXML.setLibrary(fiLibrary.getAbsolutePath());
        modelXML.setQuery(fiQuery.getAbsolutePath());
        modelXML.setLiDescriptors(liDescriptors);



        return modelXML;
    }


}
