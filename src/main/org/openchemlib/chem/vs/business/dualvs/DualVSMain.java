package org.openchemlib.chem.vs.business.dualvs;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import org.openchemlib.chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import org.openchemlib.chem.vs.business.CheckForMissingDescriptors;
import org.openchemlib.chem.vs.business.VSParallel;
import org.openchemlib.chem.vs.business.dualvs.xml.ModelDualVSXML;
import org.openchemlib.chem.vs.business.xml.DescriptorXML;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;
import com.actelion.research.util.LogHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DualVSMain


 * Use is subject to license terms.</p>
 * Created by korffmo1 on 04.04.16.
 */
public class DualVSMain {

    static final String USAGE =
        "Parallel virtual screening under consideration of inactive samples\n" +
        "Modest von Korff\n" +

        "Gewerbestrasse 16\n" +
        "CH-4123 Allschwil\n" +
        "2016\n" +
        "If the substructure search " + DescriptorConstants.DESCRIPTOR_SUBSTRUCT_QUERY_IN_BASE.shortName + " is used the " +
        "structures will be taken from the column 'Structure'. Please assure that the descriptor " +
        DescriptorConstants.DESCRIPTOR_PFP512.shortName + " is available in the library dwar file as " +
        "well as in the query dwar file. This will speed up the search by more than one order of magnitude.\n" +
        "-h --help         This output.\n" +
        "FILE The only argument is the path for the xml parameter file.\n" +
        ".\n";


    public static void main(String[] args) throws Exception {

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY+1);

        if ((args.length==0) || (args[0].equalsIgnoreCase("-h"))) {
            System.out.println(USAGE);

            ModelDualVSXML modelVSXML = ModelDualVSXML.getExample();

            System.out.println("XML example file");

            System.out.println(modelVSXML.toStringXML());

            System.exit(0);
        }

        File fiXML = new File(args[0]);

        ModelDualVSXML modelDualVSXML = ModelDualVSXML.get(fiXML.toURI().toURL());

        String nameLog = "dualVSMain.log";

        File fiWorkDir = modelDualVSXML.getWorkDir();

        LogHandler log = LogHandler.getLog(fiWorkDir, nameLog);

        System.out.println("Start " + new Date().toString());

        String nameOutDWAR = IO.getBaseName(modelDualVSXML.getLibrary()) + "ResultDualVS" + ConstantsDWAR.DWAR_EXTENSION;

        File fiDWAROut = new File(fiWorkDir, nameOutDWAR);

        File fiDWARDB = new File(modelDualVSXML.getLibrary());

        File fiDWARSamplesInclude = new File(modelDualVSXML.getSamples2Include());

        File fiDWARSamplesExclude = new File(modelDualVSXML.getSamples2Exclude());

        if(!fiDWARDB.isFile()){
            throw new IOException("Unknown interface name for database " + modelDualVSXML.getLibrary() + ".");
        } else if(!fiDWARSamplesInclude.isFile()){
            throw new IOException("Unknown interface name for samples to include " + modelDualVSXML.getSamples2Include() + ".");
        } else if(!fiDWARSamplesExclude.isFile()){
            throw new IOException("Unknown interface name for samples to exclude " + modelDualVSXML.getSamples2Exclude() + ".");
        }

        List<ModelDescriptorVS> liEnabledDHVSForSamples2Include = new ArrayList<>();



        for (DescriptorXML descriptorXML : modelDualVSXML.getDescriptorForSamples2Include()) {

            if(descriptorXML.isEnable()) {
                DescriptorHandler dh = DescriptorHandlerExtendedFactory.getFactory().create(descriptorXML.getName());
                liEnabledDHVSForSamples2Include.add(new ModelDescriptorVS(dh, descriptorXML.getThreshold(), descriptorXML.getParameter(), descriptorXML.isEnable()));
            }
        }

        List<ModelDescriptorVS> liEnabledDHVSForSamples2Exclude = new ArrayList<>();

        for (DescriptorXML descriptorXML : modelDualVSXML.getDescriptorForSamples2Exclude()) {

            if(descriptorXML.isEnable()) {
                DescriptorHandler dh = DescriptorHandlerExtendedFactory.getFactory().create(descriptorXML.getName());
                liEnabledDHVSForSamples2Exclude.add(new ModelDescriptorVS(dh, descriptorXML.getThreshold(), descriptorXML.getParameter(), descriptorXML.isEnable()));
            }
        }

        if(areTooManyDescriptorsMissing(fiDWARDB, liEnabledDHVSForSamples2Include)){

            throw new RuntimeException("To many missing descriptors in library.");

        } else if(areTooManyDescriptorsMissing(fiDWARSamplesInclude, liEnabledDHVSForSamples2Include)){

            throw new RuntimeException("To many missing descriptors in samples to include.");

        } else if(areTooManyDescriptorsMissing(fiDWARSamplesExclude, liEnabledDHVSForSamples2Exclude)){

            throw new RuntimeException("To many missing descriptors in samples to exclude.");

        }

        ModelDualVS modelDualVS = new ModelDualVS();

        modelDualVS.setLog(log);

        modelDualVS.setFiDWARDB(fiDWARDB);

        modelDualVS.setFiDWAROut(fiDWAROut);

        modelDualVS.setFiDWARSamplesInclude(fiDWARSamplesInclude);

        modelDualVS.setFiDWARSamplesExclude(fiDWARSamplesExclude);

        modelDualVS.setLiDescriptorsForSamples2Include(liEnabledDHVSForSamples2Include);

        modelDualVS.setLiDescriptorsForSamples2Exclude(liEnabledDHVSForSamples2Exclude);

        modelDualVS.setSimpleVSMode(modelDualVSXML.isSimpleVSMode());



        DualVS dualVS = new DualVS(modelDualVS);

        dualVS.process();

        System.out.println("Finished " + new Date().toString());


    }

    private static boolean areTooManyDescriptorsMissing(File fiDWAR, List<ModelDescriptorVS> liEnabledDHVS) throws NoSuchFieldException, IOException {

        boolean tooManyMissingDescriptorsDB = false;

        int [] arrMissingDescriptorsDB = CheckForMissingDescriptors.check(fiDWAR, liEnabledDHVS);


        long size = DWARFileHandlerHelper.getSize(fiDWAR);

        for (int i = 0; i < arrMissingDescriptorsDB.length; i++) {

            double ratio = 1.0 - arrMissingDescriptorsDB[i] / (double)size;

            if(ratio < VSParallel.THRESH_RATIO_CALCULATIONS_SUCCEEDED_ERROR){

                tooManyMissingDescriptorsDB = true;
            }
        }

        return tooManyMissingDescriptorsDB;
    }


}
