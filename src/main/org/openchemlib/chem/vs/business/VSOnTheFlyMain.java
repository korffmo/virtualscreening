package org.openchemlib.chem.vs.business;

import com.actelion.research.chem.descriptor.DescriptorHandler;
import chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.DescriptorHandlerSkeletonSpheres;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.util.CommandLineParser;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.datamodel.StringDouble;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VSOnTheFlyMain {


    public static final String USAGE =
        "VSOnTheFlyMain" + "\n" +
            "Modest v. Korff" + "\n" +
            "Alipheron AG" + "\n" +
            "2023" + "\n" +
            "Makes a PheSA VS on the fly." + "\n" +
            "input: " + "\n" +
            "-i dwar with library molecule. " + "\n" +
            "-q dwar with query molecule. " + "\n" +
            "-w workdir. " + "\n" +
            "-p Threshold PheSA. " + "\n" +
            "-s Threshold SkeletonSpheres. " + "\n" +
            "";

    public static void main(String[] args) throws Exception {

        if (args.length == 0 || "-h".equals(args[0])) {
            System.out.println(USAGE);
            System.out.println();
            System.exit(0);
        }

        CommandLineParser cmd = new CommandLineParser(args);

        File fiDWARLibrary = cmd.getAsFile("-i");
        File fiDWARQuery = cmd.getAsFile("-q");
        File workdir = cmd.getAsDir("-w");

        double threshPheSA = cmd.getAsDouble("-p");
        double threshSkelSpheres = cmd.getAsDouble("-s");

        String tagIdcode = ConstantsDWAR.TAG_IDCODE2;

        System.out.println("threshPheSA " + threshPheSA);
        System.out.println("threshSkelSpheres " + threshSkelSpheres);

        List<StringDouble> liDescriptorNameThresh = new ArrayList<>();

        liDescriptorNameThresh.add(new StringDouble(DescriptorHandlerShape.getDefaultInstance().getInfo().shortName, threshPheSA));
        liDescriptorNameThresh.add(new StringDouble(DescriptorHandlerSkeletonSpheres.getDefaultInstance().getInfo().shortName, threshSkelSpheres));

        List<DescriptorHandler> liDescriptorHandlerLibrary = new ArrayList<>();

        for (StringDouble descriptorNameThresh : liDescriptorNameThresh) {
            DescriptorHandler dh = DescriptorHandlerExtendedFactory.getFactory().getDefaultDescriptorHandler(descriptorNameThresh.getStr());
            liDescriptorHandlerLibrary.add(dh);
        }

        List<DescriptorHandler> liDescriptorHandlerQuery = new ArrayList<>();
        liDescriptorHandlerQuery.add(DescriptorHandlerSkeletonSpheres.getDefaultInstance());


        VSOnTheFly vsOnTheFly = new VSOnTheFly(liDescriptorNameThresh, workdir);

        File fiDWARLibraryDescriptors = vsOnTheFly.genDescriptors(fiDWARLibrary, tagIdcode, liDescriptorHandlerLibrary);
        File fiDWARQueryDescriptors = vsOnTheFly.genDescriptors(fiDWARQuery, tagIdcode, liDescriptorHandlerQuery);

        File fiDWARVSResult = vsOnTheFly.vs(fiDWARLibraryDescriptors, fiDWARQueryDescriptors);

        System.out.println(fiDWARVSResult.getAbsolutePath());

    }
}
