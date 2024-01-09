package org.openchemlib.chem.vs.business;

import com.actelion.research.chem.descriptor.DescriptorHandler;
import org.openchemlib.chem.descriptor.GenDescriptorMulticore;
import org.openchemlib.chem.descriptor.flexophore.hyperspace.search.ProgressListenerVS;
import org.openchemlib.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;
import com.actelion.research.util.datamodel.StringDouble;

import java.io.File;
import java.util.List;

public class VSOnTheFly {

    private List<DescriptorHandler> liDescriptorHandler;
    private List<StringDouble> liDescriptorNameThresh;
    private File workdir;

    public VSOnTheFly(List<StringDouble> liDescriptorNameThresh, File workdir) {
        this.liDescriptorNameThresh = liDescriptorNameThresh;
        this.workdir = workdir;



    }

    public File vs(File fiDWARDescriptors, File fiDWARQuery, ProgressListenerVS progressListenerVS) throws Exception {

        ModelVSXML modelXML = ModelVSXMLHelper.createParameterVS(fiDWARDescriptors, fiDWARQuery, liDescriptorNameThresh, workdir);

        modelXML.setQueryIdentifier(null);

        // System.out.println(modelXML.toStringXML());

        String nameVSResultElusive = IO.getBaseName(fiDWARQuery) + "Elusive" + ConstantsDWAR.DWAR_EXTENSION;
        String nameVSResultSummary = IO.getBaseName(fiDWARQuery) + "Summary" + ConstantsDWAR.DWAR_EXTENSION;

        modelXML.setNameDWARResultElusive(nameVSResultElusive);

        modelXML.setNameDWARResultSummary(nameVSResultSummary);


        VSParallel vsParallel = new VSParallel(modelXML, progressListenerVS);

        vsParallel.run();

        return vsParallel.getFiDWARResultElusive();
    }

    public File genDescriptors(File fiDWAR, String tagIdCode, List<DescriptorHandler> liDescriptorHandler) throws Exception {
        GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);
        String name = IO.getBaseName(fiDWAR) + "Descriptors" + ConstantsDWAR.DWAR_EXTENSION;
        File fiDWARDescriptors = new File(workdir, name);
        genDescriptorMulticore.generate(fiDWAR, tagIdCode, fiDWARDescriptors, liDescriptorHandler);
        return fiDWARDescriptors;
    }

}
