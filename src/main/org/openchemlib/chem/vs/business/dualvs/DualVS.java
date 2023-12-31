package org.openchemlib.chem.vs.business.dualvs;

import com.actelion.research.chem.descriptor.ISimilarityCalculator;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.dwar.DWARInterface2ModelVSRecordCompiler;
import com.actelion.research.chem.dwar.DWARRecord2ModelVSRecord;
import com.actelion.research.util.*;
import com.actelion.research.util.Formatter;
import com.actelion.research.util.log.ILog;
import com.actelion.research.util.log.RunnableLog;
import com.actelion.research.chem.dwar.*;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DualVS
 *
 * Virtual sceening with active and inactive samples.
 * Created by korffmo1 on 01.04.16.
 */
public class DualVS {

    private static final boolean DEBUG = false;

    private static final long INTERVAL_LOG_TIME_SHORT = 60 * 1000;

    private static final DecimalFormat FORMAT_COUNTER = new DecimalFormat("#,##,###");

    public static final double THRESH_RATIO_CALCULATIONS_DONE_ERROR = 0.05;


    public static final String TAG_MOST_SIMILAR_STRUCTURE2INCLUDE = "Most similar include structure";

    public static final String TAG_SAMPLE_INCLUDE_SIMILARITY = "Similarity include structure";

    public static final String TAG_MOST_SIMILAR_STRUCTURE2EXCLUDE = "Most similar exclude structure";

    public static final String TAG_SAMPLE_EXCLUDE_SIMILARITY = "Similarity exclude structure";

    public static final String TAG_DESCRIPTOR_EXCLUDE = "DescriptorExclude";

    public static final String TAG_SIMILARITY_INCLUDE_ABOVE_SIM_EXCLUDE = "Similarity include > exclude";

    private File fiDWARDB;

    private File fiDWARSamplesInclude;

    private File fiDWARSamplesExclude;

    private List<ModelDescriptorVS> liDescriptorsForSamples2Include;
    private List<ModelDescriptorVS> liDescriptorsForSamples2Exclude;

    private AtomicLong ccSimilarityCalculationsLog;

    private AtomicLong ccSimilarityCalculationsInclude;

    private AtomicLong ccSimilarityCalculationsExclude;

    private AtomicLong ccSimilarityCalculationsTotal;

    private AtomicLong ccHitsInclude;


    private TimeEstimator timeEstimator;

    private long startLog;

    private long numScores2Calc;

    private ModelDualVS modelDualVS;

    private LogHandler log;

    private InfoDualVS infoDualVS;

    public DualVS(ModelDualVS modelDualVS) {

        this.modelDualVS = modelDualVS;

        this.infoDualVS = new InfoDualVS();

        this.log = modelDualVS.getLog();

        this.liDescriptorsForSamples2Include = modelDualVS.getLiDescriptorsForSamples2Include();

        this.liDescriptorsForSamples2Exclude = modelDualVS.getLiDescriptorsForSamples2Exclude();

        ccSimilarityCalculationsTotal = new AtomicLong();

        ccSimilarityCalculationsInclude = new AtomicLong();

        ccSimilarityCalculationsExclude = new AtomicLong();

        ccSimilarityCalculationsLog = new AtomicLong();

        ccHitsInclude = new AtomicLong();

    }

    public int process() throws Exception {

        System.out.println("Parallel virtual screening.");

        int nProcessors = Runtime.getRuntime().availableProcessors();

        if(nProcessors > 1){
            nProcessors--;
        }

        // int nProcessors = 1;

        if((Runtime.getRuntime().availableProcessors() > 2) && (nProcessors == 1)){

            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("!!!VSParallel: Only calculating with one processor unit!!!!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        }

        fiDWARDB = modelDualVS.getFiDWARDB();

        fiDWARSamplesInclude = modelDualVS.getFiDWARSamplesInclude();

        fiDWARSamplesExclude = modelDualVS.getFiDWARSamplesExclude();


        log.startProgress("", 0, 100);

        long sizeDB = DWARFileHandlerHelper.getSize(fiDWARDB);
        long sizeInclude = DWARFileHandlerHelper.getSize(fiDWARSamplesInclude);
        long sizeExclude = DWARFileHandlerHelper.getSize(fiDWARDB);

        numScores2Calc = ((sizeDB * sizeInclude)) * liDescriptorsForSamples2Include.size();

        System.out.println("Number of molecules in base " + sizeDB + ".");

        System.out.println("Number of molecules in samples to include " + sizeInclude + ".");

        System.out.println("Number of molecules in samples to exclude " + sizeExclude + ".");

        System.out.println("Number of scores to calculate: " + FORMAT_COUNTER.format(numScores2Calc) + ".");

        infoDualVS.setMolsDataBase(sizeDB);

        infoDualVS.setMolsSamplesInclude(sizeInclude);

        infoDualVS.setMolsSamplesExclude(sizeExclude);

        timeEstimator = new TimeEstimator();

        timeEstimator.setPrecision(TimeDelta.PRECISION_SECONDS);


        System.out.println("Start screening with VS parallel.");

        ModelDescriptorVS [] arrModelDescriptorVS2Include = new ModelDescriptorVS [liDescriptorsForSamples2Include.size()];
        for (int i = 0; i < liDescriptorsForSamples2Include.size(); i++) {
            arrModelDescriptorVS2Include[i] = liDescriptorsForSamples2Include.get(i);
        }

        ModelDescriptorVS [] arrModelDescriptorVS2Exclude = new ModelDescriptorVS [liDescriptorsForSamples2Exclude.size()];
        for (int i = 0; i < liDescriptorsForSamples2Exclude.size(); i++) {
            arrModelDescriptorVS2Exclude[i] = liDescriptorsForSamples2Exclude.get(i);
        }

        ISimilarityCalculator[] arrDHInclude = new ISimilarityCalculator [liDescriptorsForSamples2Include.size()];
        for (int i = 0; i < liDescriptorsForSamples2Include.size(); i++) {
            arrDHInclude[i] = liDescriptorsForSamples2Include.get(i).getSimilarityCalculator();
        }

        ISimilarityCalculator [] arrDHExclude = new ISimilarityCalculator [liDescriptorsForSamples2Exclude.size()];
        for (int i = 0; i < liDescriptorsForSamples2Exclude.size(); i++) {
            arrDHExclude[i] = liDescriptorsForSamples2Exclude.get(i).getSimilarityCalculator();
        }

        DWARInterface2ModelVSRecordCompiler dwarInterface2ModelVSRecordCompilerSamplesInclude = new DWARInterface2ModelVSRecordCompiler(fiDWARSamplesInclude, Integer.MAX_VALUE, arrDHInclude, modelDualVS.isStereoDepletion(), false, 0);

        List<ModelVSRecord> liModelVSRecordSamplesInclude = dwarInterface2ModelVSRecordCompilerSamplesInclude.getNextBatch();

        System.out.println("liModelVSRecordSamplesInclude " + liModelVSRecordSamplesInclude.size());

        DWARInterface2ModelVSRecordCompiler dwarInterface2ModelVSRecordCompilerSamplesExclude = new DWARInterface2ModelVSRecordCompiler(fiDWARSamplesExclude, Integer.MAX_VALUE, arrDHExclude, modelDualVS.isStereoDepletion(), false, 0);

        List<ModelVSRecord> liModelVSRecordSamplesExclude = dwarInterface2ModelVSRecordCompilerSamplesExclude.getNextBatch();

        System.out.println("liModelVSRecordSamplesExclude " + liModelVSRecordSamplesExclude.size());



        ILog logCalculator = new ILog() {

            @Override
            public void log() {

                long end = new Date().getTime();

                long delta = end - startLog;

                if (delta == 0) {
                    return;
                }

                double fractionDone = ((double) ccSimilarityCalculationsInclude.get() / (double) numScores2Calc);

                double percentDone = fractionDone * 100.0;

                log.updateProgress((int) percentDone);

                String s = "Calculated similarities for samples to include " + FORMAT_COUNTER.format(ccSimilarityCalculationsInclude.get()) + " (" + Formatter.format0(percentDone) + " %).";

                System.out.println(s);

                String s2 = "Calculated similarities for samples to exclude " + FORMAT_COUNTER.format(ccSimilarityCalculationsExclude.get()) + ".";

                System.out.println(s2);

                String s3 = "Calculated similarities for include and exclude " + FORMAT_COUNTER.format(ccSimilarityCalculationsTotal.get()) + ".";

                System.out.println(s3);

                timeEstimator.setFractionDone(fractionDone);

                System.out.println("Estimated calculation time left " + timeEstimator.toString() + ".");

                long deltaSec = delta / 1000;

                if(deltaSec == 0){

                    return;
                }

                long calcPerSec = ccSimilarityCalculationsLog.get() / deltaSec;

                ccSimilarityCalculationsLog.set(0);

                System.out.println("Performance " + calcPerSec + " similarity calculations per second [calc/sec].");

                System.out.println("Similarity >= threshold for " + ccHitsInclude.get() + " comparisons.");

                startLog = new Date().getTime();

            }
        };


        RunnableLog runnableLog = new RunnableLog(INTERVAL_LOG_TIME_SHORT, logCalculator);

        new Thread(runnableLog).start();

        startLog = new Date().getTime();



        ConcurrentHashMap<Long, DualVSResult> hmSubstanceIdDB_DualVSResult = new ConcurrentHashMap<>();

        List<VSThread> liVSThread = new ArrayList<VSThread>();

        ExecutorService pool = Executors.newFixedThreadPool(nProcessors);

        DWARFileHandler fhDB = new DWARFileHandler(fiDWARDB);

        for (int i = 0; i < nProcessors; i++) {

            //
            // Create the descriptor handlers for the similarity calculations as deep copies.
            //
            ModelDescriptorVS [] arrDHVSThreadForSamples2IncludeDeepCpy = new ModelDescriptorVS [arrModelDescriptorVS2Include.length];

            for (int j = 0; j < arrModelDescriptorVS2Include.length; j++) {
                arrDHVSThreadForSamples2IncludeDeepCpy[j] = arrModelDescriptorVS2Include[j].getThreadSafeCopy();
            }

            ModelDescriptorVS [] arrDHVSThreadForSamples2ExcludeDeepCpy = new ModelDescriptorVS [arrModelDescriptorVS2Exclude.length];

            for (int j = 0; j < arrModelDescriptorVS2Exclude.length; j++) {
                arrDHVSThreadForSamples2ExcludeDeepCpy[j] = arrModelDescriptorVS2Exclude[j].getThreadSafeCopy();
            }


            VSThread vsThread = new VSThread(arrDHVSThreadForSamples2IncludeDeepCpy,
                    arrDHVSThreadForSamples2ExcludeDeepCpy,
                    fhDB,
                    liModelVSRecordSamplesInclude,
                    liModelVSRecordSamplesExclude,
                    hmSubstanceIdDB_DualVSResult,
                    modelDualVS.isSimpleVSMode());

                    liVSThread.add(vsThread);

                    pool.execute(vsThread);

        }

        pool.shutdown();

        while(!pool.isTerminated()){
            try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
        }

        runnableLog.stop();

        fhDB.close();

        System.out.println("**************************************************************************************");
        System.out.println("Finally");
        logCalculator.log();

        infoDualVS.setScoresCalculated(ccSimilarityCalculationsTotal.get());

        infoDualVS.setHits(ccHitsInclude.get());

        //
        // Error evaluation
        //
        int [] arrFailedSimilarityCalculationsForSamples2Include = new int [arrModelDescriptorVS2Include.length];

        int [] arrFailedSimilarityCalculationsForSamples2Exclude = new int [arrModelDescriptorVS2Exclude.length];


        System.out.println("Errors in VSParallel:");

        boolean errors = false;
        for (VSThread vsThread : liVSThread) {

            if(vsThread.getEhm().hasErrors()){
                System.out.println("Errors in thread");
                System.out.println(vsThread.getEhm().toString());
                errors = true;
            }

            for (int i = 0; i < arrModelDescriptorVS2Include.length; i++) {
                arrFailedSimilarityCalculationsForSamples2Include[i] += vsThread.getArrFailedSimilarityCalculationsSamples2Include()[i];
            }

            for (int i = 0; i < arrModelDescriptorVS2Exclude.length; i++) {
                arrFailedSimilarityCalculationsForSamples2Exclude[i] += vsThread.getArrFailedSimilarityCalculationsSamples2Exclude()[i];
            }
        }


        for (int i = 0; i < arrModelDescriptorVS2Include.length; i++) {
            if(arrFailedSimilarityCalculationsForSamples2Include[i]>0){
                String m = "Include similarity calculations failed " + arrFailedSimilarityCalculationsForSamples2Include[i] + " times for descriptor " + arrModelDescriptorVS2Include[i].getShortName() + ".";
                System.out.println(m);
                errors = true;
            }

        }

        for (int i = 0; i < arrModelDescriptorVS2Exclude.length; i++) {
            if(arrFailedSimilarityCalculationsForSamples2Exclude[i]>0){
                String m = "Exclude similarity calculations failed " + arrFailedSimilarityCalculationsForSamples2Exclude[i] + " times for descriptor " + arrModelDescriptorVS2Exclude[i].getShortName() + ".";
                System.out.println(m);
                errors = true;
            }

        }

        if(!errors){
            System.out.println("No errors.");
        }


        long diff = numScores2Calc - ccSimilarityCalculationsInclude.get();

        if(diff>0){
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("Not all scores were calculated, missing " + diff + " scores!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            errors = true;
        }

        double ratioCalc = (double)ccSimilarityCalculationsInclude.get() / numScores2Calc;

        if(ratioCalc < THRESH_RATIO_CALCULATIONS_DONE_ERROR){
            throw new RuntimeException("Threshold for ratio of failed calculations (" + THRESH_RATIO_CALCULATIONS_DONE_ERROR + ") fell below with " + Formatter.format4(ratioCalc) + ".");
        }

        log.updateProgress(100);

        //
        // Start writing results
        //
        File fiDWAROut = modelDualVS.getFiDWAROut();

        System.out.println("Start writing results virtual screening to " + fiDWAROut.getAbsolutePath() + ".");

        writeResults2DWAR(
                arrModelDescriptorVS2Include,
                arrModelDescriptorVS2Exclude,
                hmSubstanceIdDB_DualVSResult,
                fiDWARDB,
                liModelVSRecordSamplesInclude,
                liModelVSRecordSamplesExclude,
                fiDWAROut,
                modelDualVS.isSimpleVSMode());

        return hmSubstanceIdDB_DualVSResult.size();
    }


    private void writeResults2DWAR(
            ModelDescriptorVS [] arrModelDescriptorVS2Include,
            ModelDescriptorVS [] arrModelDescriptorVS2Exclude,
            ConcurrentHashMap<Long, DualVSResult> hmSubstanceIdDB_DualVSResult,
            File fiDWARDB,
            List<ModelVSRecord> liModelVSRecordSamplesInclude,
            List<ModelVSRecord> liModelVSRecordSamplesExclude,
            File fiDWAROut,
            boolean simpleVSMode) throws IOException, NoSuchFieldException {


        HashMap<Long, ModelVSRecord> hmId_ModelVSRecordSamplesInclude = ModelVSRecord.getHashMap(liModelVSRecordSamplesInclude);

        HashMap<Long, ModelVSRecord> hmId_ModelVSRecordSamplesExclude = ModelVSRecord.getHashMap(liModelVSRecordSamplesExclude);

        DWARFileHandler fh = new DWARFileHandler(fiDWARDB);


        DWARHeader headerDWAROut = new DWARHeader(fh.getDWARHeader());

        addHeaderTagsOut(arrModelDescriptorVS2Include, headerDWAROut);

        DWARFileWriter fw = new DWARFileWriter(fiDWAROut, headerDWAROut);


        HashMap<String, PairSimilarity> hmDescriptor_PairSimilarity = new HashMap<>();

//        HashMap<String, ISimilarityCalculatorVS<?>> hmDescriptor_DescriptorHandler = new HashMap<>();
//
//        for (int i = 0; i < arrDHVSForSamples2Include.length; i++) {
//
//            String descriptor = arrDHVSForSamples2Include[i].getInfo().shortName;
//
//            hmDescriptor_DescriptorHandler.put(descriptor, arrDHVSForSamples2Include[i]);
//        }



        while (fh.hasMore()){

            DWARRecord recSubstanceDB = fh.next();

            long id = recSubstanceDB.getID();

            if(hmSubstanceIdDB_DualVSResult.containsKey(id)){

                DualVSResult dualVSResult = hmSubstanceIdDB_DualVSResult.get(id);

                int [] arrIdSampleInclude = dualVSResult.getArrIdSampleInclude();

                float [][] arrSimilaritySampleInclude = dualVSResult.getArrSimilaritySampleInclude();

                hmDescriptor_PairSimilarity.clear();


                for (int i = 0; i < arrModelDescriptorVS2Include.length; i++) {

                    String descriptor = arrModelDescriptorVS2Include[i].getShortName();

                    float[] arrSimilaritySampleIncludeDescriptor = arrSimilaritySampleInclude[i];

                    float maxSimSampleInclude = 0;

                    long idMaxSimSampleInclude = -1;

                    for (int j = 0; j < arrSimilaritySampleIncludeDescriptor.length; j++) {

                        if (arrSimilaritySampleIncludeDescriptor[j] > maxSimSampleInclude) {

                            maxSimSampleInclude = arrSimilaritySampleIncludeDescriptor[j];

                            idMaxSimSampleInclude = arrIdSampleInclude[j];
                        }
                    }

                    if(!hmDescriptor_PairSimilarity.containsKey(descriptor)) {

                        hmDescriptor_PairSimilarity.put(descriptor, new PairSimilarity(descriptor));

                    }

                    PairSimilarity ps = hmDescriptor_PairSimilarity.get(descriptor);

                    ps.idMoleculeInclude = idMaxSimSampleInclude;

                    ps.simInclude = maxSimSampleInclude;

                }

                //
                // Optimistic approach
                // One descriptor similarity between library and include sample has to be higher than the similarity
                // between library sample and exclude sample.
                //
                List<String> liDescriptor = new ArrayList<>(hmDescriptor_PairSimilarity.keySet());

                Collections.sort(liDescriptor);

                DWARRecord recOut = new DWARRecord(headerDWAROut);

                recOut.add2Record(recSubstanceDB);

                PairSimilarity psMaxInclude = new PairSimilarity();

                for (String descriptor : liDescriptor) {

                    PairSimilarity ps = hmDescriptor_PairSimilarity.get(descriptor);

                    String tagSimilarityDescriptor = getTagSimilarityDescriptor(descriptor);

                    if(ps.simInclude > psMaxInclude.simInclude) {

                        psMaxInclude = ps;

                    }

                    recOut.addOrReplaceField(tagSimilarityDescriptor, Formatter.format3(ps.simInclude));

                }

                ModelVSRecord modelVSRecordInclude = hmId_ModelVSRecordSamplesInclude.get(psMaxInclude.idMoleculeInclude);


                recOut.addOrReplaceField(TAG_MOST_SIMILAR_STRUCTURE2INCLUDE, modelVSRecordInclude.getIDCode());


                if(!simpleVSMode) {

                    float[][] arrSimilaritySampleExclude = dualVSResult.getArrSimilaritySampleExclude();

                    for (int i = 0; i < arrModelDescriptorVS2Exclude.length; i++) {

                        String descriptor = arrModelDescriptorVS2Exclude[i].getShortName();

                        int[] arrIdSampleExclude = dualVSResult.getArrIdSampleExclude();

                        float[] arrSimilaritySampleExcludeDescriptor = arrSimilaritySampleExclude[i];

                        float maxSimSampleExclude = 0;

                        long idMaxSimSampleExclude = -1;

                        for (int j = 0; j < arrSimilaritySampleExcludeDescriptor.length; j++) {

                            if (arrSimilaritySampleExcludeDescriptor[j] > maxSimSampleExclude) {

                                maxSimSampleExclude = arrSimilaritySampleExcludeDescriptor[j];

                                idMaxSimSampleExclude = arrIdSampleExclude[j];
                            }
                        }

                        PairSimilarity ps = hmDescriptor_PairSimilarity.get(descriptor);

                        if (ps == null) {

                            continue;

                        }

                        ps.idMoleculeExclude = idMaxSimSampleExclude;

                        ps.simExclude = maxSimSampleExclude;


                    }

                    PairSimilarity psMaxExclude = new PairSimilarity();

                    boolean similarityIncludeAboveSimilarityExclude = false;

                    for (String descriptor : liDescriptor) {

                        PairSimilarity ps = hmDescriptor_PairSimilarity.get(descriptor);

                        if(ps.idMoleculeExclude > -1) {

                            if(ps.simExclude > psMaxExclude.simExclude) {

                                psMaxExclude = ps;

                            }

                            if(ps.simInclude > ps.simExclude) {

                                similarityIncludeAboveSimilarityExclude = true;

                            }


                        }
                    }

                    if(psMaxExclude.idMoleculeExclude > -1) {

                        recOut.addOrReplaceField(TAG_DESCRIPTOR_EXCLUDE, psMaxExclude.descriptor);

                        recOut.addOrReplaceField(TAG_SAMPLE_EXCLUDE_SIMILARITY, Formatter.format3(psMaxExclude.simInclude));

                        ModelVSRecord modelVSRecordExclude = hmId_ModelVSRecordSamplesExclude.get(psMaxExclude.idMoleculeExclude);

                        recOut.addOrReplaceField(TAG_MOST_SIMILAR_STRUCTURE2EXCLUDE, modelVSRecordExclude.getIDCode());

                    }


                    int simIncludeAboveSimExclude = (similarityIncludeAboveSimilarityExclude) ? 1 : 0;

                    recOut.addOrReplaceField(TAG_SIMILARITY_INCLUDE_ABOVE_SIM_EXCLUDE, Integer.toString(simIncludeAboveSimExclude));


                } else {

                    recOut.addOrReplaceField(TAG_SIMILARITY_INCLUDE_ABOVE_SIM_EXCLUDE, Integer.toString(1));

                }

                fw.write(recOut);
            }
        }


        fw.close();

    }

    private static void addHeaderTagsOut(ModelDescriptorVS [] arrDHVSForSamples2Include, DWARHeader headerDWAROut){

        for (int i = 0; i < arrDHVSForSamples2Include.length; i++) {

            String descriptor = arrDHVSForSamples2Include[i].getShortName();

            String tag = getTagSimilarityDescriptor(descriptor);

            headerDWAROut.add(tag);


        }

        headerDWAROut.addStructureHeader(TAG_MOST_SIMILAR_STRUCTURE2INCLUDE);

        headerDWAROut.add(TAG_SAMPLE_EXCLUDE_SIMILARITY);

        headerDWAROut.add(TAG_DESCRIPTOR_EXCLUDE);

        headerDWAROut.addStructureHeader(TAG_MOST_SIMILAR_STRUCTURE2EXCLUDE);

        headerDWAROut.add(TAG_SIMILARITY_INCLUDE_ABOVE_SIM_EXCLUDE);
    }


    private File createDWAROutFile(String sFileOutDWAR, LogHandler log){

        File fiOutDWAR = new File(sFileOutDWAR);

        if (fiOutDWAR.exists()) {

            if (!fiOutDWAR.delete()) {

                fiOutDWAR = IO.getUniqueFileName(fiOutDWAR);

                String s = "Output file "
                        + sFileOutDWAR
                        + " already exists and could not be deleted.\nData stored in: "
                        + fiOutDWAR.getAbsolutePath() + "\n";

                System.out.println(s);
            }
        }

        return fiOutDWAR;
    }

    public static String getTagSimilarityDescriptor(String descriptor) {

        String tag = "Similarity" + descriptor;

        return tag;
    }

    class VSThread implements Runnable {

        private DWARInterface dwarInterface;

        private List<ModelVSRecord> liModelVSRecordSampleInclude;

        private List<ModelVSRecord> liModelVSRecordSampleExclude;

        private DWARRecord2ModelVSRecord dwarRecord2ModelVSRecord;

        private ModelDescriptorVS [] arrDHVSForSamples2Include;

        private ModelDescriptorVS [] arrDHVSForSamples2Exclude;

        private ErrorHashMap ehm;

        private int [] arrFailedSimilarityCalculationsSamples2Include;
        private int [] arrFailedSimilarityCalculationsSamples2Exclude;

        private ConcurrentHashMap<Long, DualVSResult> hmId_DualVSResult;

        private boolean simpleVSMode;

        public VSThread(
                ModelDescriptorVS [] arrDHVSForSamples2Include,
                ModelDescriptorVS [] arrDHVSForSamples2Exclude,
                DWARInterface dwarInterface,
                List<ModelVSRecord> liModelVSRecordSampleInclude,
                List<ModelVSRecord> liModelVSRecordSampleExclude,
                ConcurrentHashMap<Long, DualVSResult> hmId_DualVSResult,
                boolean simpleVSMode) {

            this.arrDHVSForSamples2Include = arrDHVSForSamples2Include;

            this.arrDHVSForSamples2Exclude = arrDHVSForSamples2Exclude;

            this.dwarInterface = dwarInterface;

            this.liModelVSRecordSampleInclude = liModelVSRecordSampleInclude;

            this.liModelVSRecordSampleExclude = liModelVSRecordSampleExclude;

            this.simpleVSMode = simpleVSMode;



            dwarRecord2ModelVSRecord = new DWARRecord2ModelVSRecord(this.arrDHVSForSamples2Include, modelDualVS.isStereoDepletion(), false);

            arrFailedSimilarityCalculationsSamples2Include = new int [arrDHVSForSamples2Include.length];

            arrFailedSimilarityCalculationsSamples2Exclude = new int [arrDHVSForSamples2Exclude.length];

            this.hmId_DualVSResult = hmId_DualVSResult;

            ehm = new ErrorHashMap();

        }

        @SuppressWarnings("unchecked")
        public void run() {


            while(dwarInterface.hasMore()) {

                DWARRecord rec = dwarInterface.next();

                ModelVSRecord modelVSRecordBase = dwarRecord2ModelVSRecord.extract(rec);

                if(modelVSRecordBase!=null) {

                    float [][] arrSimSampleInclude = new float[arrDHVSForSamples2Include.length][liModelVSRecordSampleInclude.size()];

                    int [] arrIdSampleInclude = new int[liModelVSRecordSampleInclude.size()];

                    boolean hit=false;

                    for (int i = 0; i < liModelVSRecordSampleInclude.size(); i++) {

                        ModelVSRecord modelVSRecordInclude = liModelVSRecordSampleInclude.get(i);

                        arrIdSampleInclude[i] = (int)modelVSRecordInclude.getId();

                        for (int j = 0; j < arrDHVSForSamples2Include.length; j++) {

                            Object objBase = modelVSRecordBase.getArrDescriptors()[j];

                            Object objQuery = modelVSRecordInclude.getArrDescriptors()[j];

                            if((objQuery==null) || (objBase==null)) {

                                arrFailedSimilarityCalculationsSamples2Include[j]++;

                                continue;
                            }

                            float sim=0;
                            try {
                                sim = this.arrDHVSForSamples2Include[j].getSimilarity(objQuery, objBase);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            arrSimSampleInclude[j][i] = sim;

                            if(sim >= arrDHVSForSamples2Include[j].getSimilarityThreshold()) {

                                hit=true;

                            }

                            ccSimilarityCalculationsInclude.incrementAndGet();

                            ccSimilarityCalculationsTotal.incrementAndGet();

                            ccSimilarityCalculationsLog.incrementAndGet();

                        }
                    }


                    if(simpleVSMode){ // Common virtual screening mode.

                        DualVSResult dualVSResult = new DualVSResult(modelVSRecordBase.getId(), arrSimSampleInclude, arrIdSampleInclude, null, null);

                        hmId_DualVSResult.put(modelVSRecordBase.getId(), dualVSResult);

                        continue;

                    }

                    if(hit) { // Here starts the dual part of the virtual screening.

                        ccHitsInclude.incrementAndGet();

                        float[][] arrSimSampleExclude = new float[arrDHVSForSamples2Exclude.length][liModelVSRecordSampleExclude.size()];

                        int[] arrIdSampleExclude = new int[liModelVSRecordSampleExclude.size()];

                        for (int i = 0; i < liModelVSRecordSampleExclude.size(); i++) {

                            ModelVSRecord modelVSRecordExclude = liModelVSRecordSampleExclude.get(i);

                            arrIdSampleExclude[i] = (int) modelVSRecordExclude.getId();

                            for (int j = 0; j < arrDHVSForSamples2Exclude.length; j++) {

                                Object objBase = modelVSRecordBase.getArrDescriptors()[j];

                                Object objQuery = modelVSRecordExclude.getArrDescriptors()[j];

                                if ((objQuery == null) || (objBase == null)) {

                                    arrFailedSimilarityCalculationsSamples2Exclude[j]++;

                                    continue;
                                }

                                float sim = 0;
                                try {
                                    sim = this.arrDHVSForSamples2Exclude[j].getSimilarity(objQuery, objBase);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                arrSimSampleExclude[j][i] = sim;

                                ccSimilarityCalculationsExclude.incrementAndGet();

                                ccSimilarityCalculationsTotal.incrementAndGet();

                                ccSimilarityCalculationsLog.incrementAndGet();

                            }
                        }

                        DualVSResult dualVSResult = new DualVSResult(modelVSRecordBase.getId(), arrSimSampleInclude, arrIdSampleInclude, arrSimSampleExclude, arrIdSampleExclude);

                        hmId_DualVSResult.put(modelVSRecordBase.getId(), dualVSResult);

                    }

                }

            }

        }

        public ErrorHashMap getEhm() {
            return ehm;
        }

        public int[] getArrFailedSimilarityCalculationsSamples2Include() {
            return arrFailedSimilarityCalculationsSamples2Include;
        }

        public int[] getArrFailedSimilarityCalculationsSamples2Exclude() {
            return arrFailedSimilarityCalculationsSamples2Exclude;
        }

    }


    private static class PairSimilarity {

        String descriptor;

        double simInclude;

        long idMoleculeInclude;

        double simExclude;

        long idMoleculeExclude;

        public PairSimilarity() {

        }

        public PairSimilarity(String descriptor) {

            this.descriptor = descriptor;

            idMoleculeInclude = -1;

            idMoleculeExclude = -1;

        }
    }



}
