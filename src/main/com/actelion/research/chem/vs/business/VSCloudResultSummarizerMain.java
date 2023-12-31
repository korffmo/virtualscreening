package com.actelion.research.chem.vs.business;

import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.chem.dwar.*;
import com.actelion.research.util.CommandLineParser;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Modest v. Korff

 * 22/09/2022 Start implementation
 **/
public class VSCloudResultSummarizerMain {

    public static final String TAG_STRUCTURE_QUERY = ConstantsVSTagsDWAR.TAG_IDCODE_QUERY;

    public static final String USAGE =
        "VSCloudResultSummarizerMain" + "\n" +
            "Modest v. Korff" + "\n" +

            "2022" + "\n" +
            "Merges result files from VSCloud." + "\n" +
            "input: " + "\n" +
            "-i path result file from VSCloud. Must contain idcodes from library molecule and query molecule. " +
                "Similarity values from the descriptors. Writes a file with the unified library molecules and one file " +
                "with all library molecules that were found by more than one query molecule." + "\n" +
            "-w working directory" + "\n" +
            "";

    public static void main(String[] args) throws Exception {

        if (args.length == 0 || "-h".equals(args[0])) {
            System.out.println(USAGE);
            System.out.println();
            System.exit(0);
        }

        CommandLineParser cmd = new CommandLineParser(args);

        File fiDWARResult = cmd.getAsFile("-i");
        File workdir = cmd.getAsFile("-w");

        // List<String> liTags2Merge = cmd.getAsList("-t");

        // List<DWARRecord> liDWAR = DWARFileHandlerHelper.get(fiDWARResult);

        DWARHeader header = DWARFileHandlerHelper.getDWARHeader(fiDWARResult);

        List<String> liHeader = header.get();

        System.out.println("Similarity tags to merge");
        List<String> liTags2Merge = new ArrayList<>();
        for (String tag : liHeader) {
            if(tag.startsWith(DescriptorHelper.TAG_SIMILARITY)){
                liTags2Merge.add(tag);
                System.out.println(tag);
            }
        }


        long rowsIn = DWARFileHandlerHelper.getSize(fiDWARResult);

        System.out.println("rows " + rowsIn);

        HashMap<String, List<DWARRecord>> hmIdCode_DWAR = DWARFileHandlerHelper.getHashMap(fiDWARResult, ConstantsDWAR.TAG_IDCODE2);

        System.out.println("rows merged " + hmIdCode_DWAR.size());

        List<List<DWARRecord>> liliDWAR = new ArrayList<>(hmIdCode_DWAR.values());

        int maxLengthListQueryStructure = 0;
        for (List<DWARRecord> liDWARRecords : liliDWAR) {
            if(liDWARRecords.size()>maxLengthListQueryStructure){
                maxLengthListQueryStructure=liDWARRecords.size();
            }
        }

        System.out.println("maxLengthListQueryStructure " + maxLengthListQueryStructure);

        DWARHeader headerMultHits = new DWARHeader(header);

        for (int i = 1; i < maxLengthListQueryStructure; i++) {
                String tag = TAG_STRUCTURE_QUERY + "_" + i;
                headerMultHits.addStructureHeader(tag);
        }

        File fiDWARResultSummary = new File(workdir, IO.getBaseName(fiDWARResult.getName()) + "Summary" + ConstantsDWAR.DWAR_EXTENSION);
        File fiDWARResultMultipleHits = new File(workdir, IO.getBaseName(fiDWARResult.getName()) + "MultipleHits" + ConstantsDWAR.DWAR_EXTENSION);

        DWARFileWriter fw = new DWARFileWriter(fiDWARResultSummary, header);
        DWARFileWriter fwMultHits = new DWARFileWriter(fiDWARResultMultipleHits, headerMultHits);

        for (List<DWARRecord> liDWARRecords : liliDWAR) {

            DWARRecord recordReceiver = new DWARRecord(header);

            recordReceiver.add2Record(liDWARRecords.get(0));

//            if(liDWARRecords.size()>2){
//                System.out.println("Test!!!");
//            }

            for (int i = 1; i < liDWARRecords.size(); i++) {
                DWARRecord recordSender = liDWARRecords.get(i);
                DWARFunctions.mergeRecords(recordReceiver, recordSender, liTags2Merge);
            }

            if(liDWARRecords.size()>1){

                DWARRecord recordReceiverMultHits = new DWARRecord(headerMultHits);
                recordReceiverMultHits.add2Record(recordReceiver);

                for (int i = 1; i < liDWARRecords.size(); i++) {
                    try {
                        String tagStructureQueryReceiver = TAG_STRUCTURE_QUERY + "_" + i;
                        DWARRecord recSend = liDWARRecords.get(i);
                        String idcodeQuery = recSend.getAsString(TAG_STRUCTURE_QUERY);
                        recordReceiverMultHits.addOrReplaceField(tagStructureQueryReceiver, idcodeQuery);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                fwMultHits.write(recordReceiverMultHits);
            }

            fw.write(recordReceiver);

            if(fw.getAdded()%1000000==0){
                System.out.println("Processed " + fw.getAdded() + " records.");
            }
        }
        fw.close();
        fwMultHits.close();
        System.out.println("Finally processed " + fw.getAdded() + " records.");
        System.out.println("Structures with multiple hits " + fwMultHits.getAdded() + " records.");
        System.out.println("Finished");
    }
}
