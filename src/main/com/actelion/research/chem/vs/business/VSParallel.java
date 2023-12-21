package com.actelion.research.chem.vs.business;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.ISimilarityCalculator;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.dwar.*;
import com.actelion.research.chem.dwar.toolbox.export.ConvertString2PheSA;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.vs.CheckHeaderForNeededColumns;
import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.*;
import com.actelion.research.util.Formatter;
import com.actelion.research.util.log.ILog;
import com.actelion.research.util.log.RunnableLog;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Title: VSParallel </p>
 * <p>Description: </p>
 * @author Modest von Korff
 * 25.03.2011 MvK: Start implementation
 * 07.12.2012 MvK: Updates
 * 15.05.2013 MvK: Updates logging
 * 16.05.2013 MvK: Intermediates result output
 * 07.10.2015 MvK: Extensive checks after bug report from Julien. Multi thread changed to Executor.
 * 10.01.2019 MvK: Added extra dwar file output for molecules aligned by ROCS shape descriptor.
 * 25.02.2020 MvK: Rewritten. Small memory footprint now.
 */
public class VSParallel {

	private static final boolean DEBUG = false;

	public static final double THRESH_RATIO_CALCULATIONS_SUCCEEDED_ERROR = 0.95;

	// public static final int SIZE_BATCH_BASE = 100;
	public static final int SIZE_BATCH_BASE = 25000;

	public static final int SIZE_BATCH_QUERY = 10000;

	public static final int SIZE_BATCH_BASE_DEBUG = 1000;
	public static final int SIZE_BATCH_QUERY_DEBUG = 2;

	public static final int SIZE_BATCH_QUERY_FLEXOPHORE = 100;


	private static final long MS_TOO_LONG_FOR_SIMILARITY = TimeDelta.MS_MINUTE * 1;


	public static final String PREFIX_SHAPE_ALIGNED_MOLS = "ShapeAlignedMolecules";

	public static final String TAG_QUERY_STRUCTURE = "Query";

	public static final String TAG_DIFFERENCE_FLEXOPHORE_SKELSPHERES = "DiffFlexophoreSkelSpheres";

	private static final DecimalFormat FORMAT_COUNTER = new DecimalFormat("#,##,###");

	private static final String NAME_SUPPLIER = "SupplierInfo";

	private static final String PREFIX_RESULT_VS = "VS_";

	private static final String PREFIX_RESULT_LIBRARY_COMPARISON = "LibComp_";

	public static final String FILE_NAME_PROPERTIES = "properties.prop";

	public static final String FILE_NAME_ROW_LOGGING = "rowLoggingVS.txt";

	public static final String PROPERTY_FILE_RESULT = "FileResult";

	private static final String PROPERTY_FILE_RESULT_SHAPE_ALIGN = "FileResultShapeAlign";

	private static final String PROPERTY_COMMENT = "VSCmd runtime properties.";


	private ModelVSXML model;

	private boolean enabledPheSADescriptor;

	private File fiDWARDB;

	private File fiDWARQuery;

	private List<ModelDescriptorVS> liEnabledModelDescriptorVS;

	private String strFileOutDWAR;

	private long numFulfillThresh;

	boolean writeResultTable2Log;

	private ConcurrentLinkedQueue<ModelVSRecord> queueModelVSRecordBase;

	private AtomicLong ccSimilarityCalculationsLog;

	private AtomicLong ccSimilarityCalculationsTotal;

	private AtomicLong ccHits;

	private TimeEstimator timeEstimator;

	private long startLog;

	private long numScores2Calc;

	private InfoVS infoVS;

	private int processorsForVS;

	public VSParallel(ModelVSXML model) throws Exception {

		this.model = model;

		processorsForVS = model.getNumProcessors();

		this.liEnabledModelDescriptorVS = DescriptorVSHelper.getEnabled(model.getLiDescriptors());

		enabledPheSADescriptor = false;
		for (ModelDescriptorVS modelDescriptorVS : liEnabledModelDescriptorVS) {
			if(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName.equals(modelDescriptorVS.getShortName())){
				enabledPheSADescriptor = true;
				break;
			}
		}

		String sName = (new File(model.getLibrary())).getName();
		int iStartExtension = sName.lastIndexOf(".");

		String sFileInBaseName = sName;
		if(iStartExtension > 0)
			sFileInBaseName = sName.substring(0, iStartExtension);

		sFileInBaseName = sFileInBaseName.replace(' ', '_');

		if(!model.getWorkDir().isDirectory()){
			throw new IOException("Not a directory '" + model.getWorkDir().getAbsolutePath() + "'.");
		}

		if(ConstantsVS.COMPARISON_MODE_VS.equals(model.getComparisonMode())) {
			strFileOutDWAR = model.getWorkDir().getAbsolutePath() + File.separator + PREFIX_RESULT_VS + sFileInBaseName + ConstantsDWAR.DWAR_EXTENSION;
		} else {
			strFileOutDWAR = model.getWorkDir().getAbsolutePath() + File.separator + PREFIX_RESULT_LIBRARY_COMPARISON + sFileInBaseName + ConstantsDWAR.DWAR_EXTENSION;
		}

		writeResultTable2Log = true;

		queueModelVSRecordBase = new ConcurrentLinkedQueue<>();

		ccSimilarityCalculationsLog = new AtomicLong();

		ccSimilarityCalculationsTotal = new AtomicLong();

		ccHits = new AtomicLong();

		infoVS = new InfoVS();

		if(model.getNameDWARResultElusive()==null || model.getNameDWARResultElusive().length()==0){
			model.setNameDWARResultElusive("vsResultElusive" + ConstantsDWAR.DWAR_EXTENSION);
		}

	}

	public String getFileOutDWAR() {
		return strFileOutDWAR;
	}

	/**
	 * @return the infoVS
	 */
	public InfoVS getInfoVS() {
		return infoVS;
	}

	/**
	 * @param infoVS the infoVS to set
	 */
	public void setInfoVS(InfoVS infoVS) {
		this.infoVS = infoVS;
	}

	public long getNumMoleculesFulfillThreshold() {
		return numFulfillThresh;
	}

	public void run() {

		System.out.println("Start Virtual Screening");

		if(DEBUG){
			System.out.println("!!!!!!!!!!!!!!!!");
			System.out.println("!!!Debug mode!!!");
			System.out.println("!!!!!!!!!!!!!!!!");
		}

		String sLog = "";
		try {
			sLog = "Query " + model.getQuery() + "\n";
			sLog += "Base " + model.getLibrary() + "\n";
			System.out.println(sLog);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			return;
		}

		try {
			numFulfillThresh = processVS();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	private long processVS() throws Exception {


		File fiProperties = new File(model.getWorkDir(), FILE_NAME_PROPERTIES);

		Properties properties = new Properties();
		if(model.isReuptake()){
			if(!fiProperties.isFile()){
				throw new FileNotFoundException("Property file " + fiProperties.getAbsolutePath() + " not found!");
			}

			properties.load(new FileReader(fiProperties));
		}

		int nProcessors = -1;
		if(processorsForVS>0){
			nProcessors = processorsForVS;
			System.out.println("Processors were given as parameter.");
		} else {
			nProcessors = RuntimeHelper.getNumProcessors(Integer.MAX_VALUE, DEBUG);
		}

		System.out.println("VSParallel num processors nProcessors " + nProcessors + ".");

		ccSimilarityCalculationsTotal.set(0);

		fiDWARQuery = new File(model.getQuery());
		fiDWARDB = new File(model.getLibrary());


		if(!fiDWARQuery.isFile()){
			throw new IOException("Unknown file name for query " + model.getQuery() + ".");
		} else if(!fiDWARDB.isFile()){
			throw new IOException("Unknown file name for library " + model.getLibrary() + ".");
		}

		long nRecordsQuery = DWARFileHandlerHelper.getSize(fiDWARQuery);
		long nRecordsDB = DWARFileHandlerHelper.getSize(fiDWARDB);


		DWARHeader headerQuery = DWARFileHandlerHelper.getDWARHeader(fiDWARQuery);
		DWARHeader headerDB = DWARFileHandlerHelper.getDWARHeader(fiDWARDB);

		if(model.getQueryIdentifier() != null && model.getQueryIdentifier().length()>0){
			if(!headerQuery.contains(model.getQueryIdentifier())){
				throw new RuntimeException("Missing query identifier '" + model.getQueryIdentifier() + "' in " + model.getQuery() + ".");
			}
		}

		String [] arrDescriptorShortNames = new String [liEnabledModelDescriptorVS.size()];
		for (int i = 0; i < arrDescriptorShortNames.length; i++) {
			arrDescriptorShortNames[i] = liEnabledModelDescriptorVS.get(i).getShortName();
		}

		CheckHeaderForNeededColumns checkHeaderForNeededColumns = new CheckHeaderForNeededColumns();

		checkHeaderForNeededColumns.setHeader(headerQuery);

		boolean headerCheckFailed=false;
		for (ModelDescriptorVS modelDescriptorVS : liEnabledModelDescriptorVS) {
			if(!checkHeaderForNeededColumns.check(modelDescriptorVS.getSimilarityCalculator())){
				headerCheckFailed=true;
				System.out.println("Header check for " + modelDescriptorVS.getSimilarityCalculator().getInfo().shortName + " failed in file " + model.getQuery() + ".");
				System.out.println(checkHeaderForNeededColumns.getMessage());
			}
		}

		checkHeaderForNeededColumns.setHeader(headerDB);
		for (ModelDescriptorVS modelDescriptorVS : liEnabledModelDescriptorVS) {
			if(!checkHeaderForNeededColumns.check(modelDescriptorVS.getSimilarityCalculator())){
				headerCheckFailed=true;
				System.out.println("Header check for " + modelDescriptorVS.getSimilarityCalculator().getInfo().shortName + " failed in file " + model.getLibrary() + ".");
				System.out.println(checkHeaderForNeededColumns.getMessage());
			}
		}

		if(headerCheckFailed){
			throw new RuntimeException("Error in dwar input file. Header check failed!");
		}

		if(!model.isReuptake() && model.isCheckForMissingDescriptors()) {

			System.out.println("Check for missing descriptors");

			int[] arrMissingDescriptorsQuery = CheckForMissingDescriptors.check(fiDWARQuery, arrDescriptorShortNames);
			int[] arrMissingDescriptorsDB = CheckForMissingDescriptors.check(fiDWARDB, arrDescriptorShortNames);

			boolean toManyMissingDescriptorsQuery = false;

			System.out.println("Missing descriptors query");
			for (int i = 0; i < arrMissingDescriptorsQuery.length; i++) {
				double ratio = 1.0 - arrMissingDescriptorsQuery[i] / (double) nRecordsQuery;
				if (ratio < THRESH_RATIO_CALCULATIONS_SUCCEEDED_ERROR) {
					toManyMissingDescriptorsQuery = true;
				}
				System.out.println(arrDescriptorShortNames[i] + ": " + arrMissingDescriptorsQuery[i]);
			}

			boolean toManyMissingDescriptorsDB = false;

			System.out.println("Missing descriptors db");
			for (int i = 0; i < arrMissingDescriptorsDB.length; i++) {
				double ratio = 1.0 - arrMissingDescriptorsDB[i] / (double) nRecordsDB;
				if (ratio < THRESH_RATIO_CALCULATIONS_SUCCEEDED_ERROR) {
					toManyMissingDescriptorsDB = true;
				}
				System.out.println(arrDescriptorShortNames[i] + ": " + arrMissingDescriptorsDB[i]);
			}

			if (toManyMissingDescriptorsQuery) {
				throw new RuntimeException("To many missing descriptors in query.");
			}
			if (toManyMissingDescriptorsDB) {
				throw new RuntimeException("To many missing descriptors in db.");
			}

		}

		//
		// Just for development testing
		//

//		for (ModelDescriptorVS model : liEnabledModelDescriptorVS) {
//			if(model.getShortName().equals(DescriptorConstants.DESCRIPTOR_Flexophore.shortName)){
//				System.out.println("Parameter for " + DescriptorConstants.DESCRIPTOR_Flexophore.shortName);
//				ModelDescriptorVS modelCpy = model.getThreadSafeCopy();
//				DescriptorHandlerFlexophore dhFlexV5 = (DescriptorHandlerFlexophore)modelCpy.getSimilarityCalculator();
//				System.out.println(dhFlexV5.toStringParameter());
//			}
//		}


		System.out.println("Parallel virtual screening read descriptors on the fly.");

		numScores2Calc = nRecordsQuery * nRecordsDB * (long) liEnabledModelDescriptorVS.size();

		System.out.println("Number of molecules in query " + nRecordsQuery + ".");
		System.out.println("Number of molecules in base " + nRecordsDB + ".");
		System.out.println("Number of scores to calculate: " + FORMAT_COUNTER.format(numScores2Calc) + ".");

		infoVS.setMolsBase(nRecordsDB);
		infoVS.setMolsQuery(nRecordsQuery);
		infoVS.setScores2Calculate(numScores2Calc);

		timeEstimator = new TimeEstimator();

		timeEstimator.setPrecision(TimeDelta.PRECISION_SECONDS);

		int sizeBatchBase = SIZE_BATCH_BASE;

		int sizeBatchQuery = SIZE_BATCH_QUERY;
		for (ModelDescriptorVS model : liEnabledModelDescriptorVS) {
			if(model.getShortName().equals(DescriptorConstants.DESCRIPTOR_Flexophore.shortName)){
				sizeBatchQuery = SIZE_BATCH_QUERY_FLEXOPHORE;
			}
		}

		if(DEBUG){
			sizeBatchQuery = SIZE_BATCH_QUERY_DEBUG;
			sizeBatchBase = SIZE_BATCH_BASE_DEBUG;
		}

		System.out.println("VSParallel  Size batch query " + sizeBatchQuery + ".");

		VSResultHandler vsResultHandler = null;
		long rowQuery = 0;
		long rowBase = 0;
		boolean allResultsWritten = false;
		File fiDWARResult = null;
		if(model.isReuptake()){

			fiDWARResult = new File(properties.getProperty(PROPERTY_FILE_RESULT));
			if(!fiDWARResult.isFile()){
				throw new IOException("Missing file " + fiDWARResult + " for re-uptake of VS!");
			}

			File fiRowLog = new File(model.getWorkDir(), FILE_NAME_ROW_LOGGING);
			if(!fiRowLog.isFile()){
				throw new IOException("Missing file " + fiRowLog + " for re-uptake of VS!");
			}

			FileUtils.deleteAllAfterLastLineFeed(fiRowLog);

			if(fiRowLog.length()==0){
				throw new RuntimeException("Recent VS did not really start. Re-uptake impossible. Please run VS from scatch!");
			}

			String lineRowLog = FileUtils.tail(fiRowLog).trim();

			String [] arrRowLog = lineRowLog.split("\t");

			if(arrRowLog.length!=2){
				throw new RuntimeException("Wrong number of fields in last line ' " + lineRowLog + " ' of row log file " + fiRowLog.getAbsolutePath() + "! Re-uptale impossible. Please run VS from scatch!");
			}

			rowQuery = Long.parseLong(arrRowLog[0]);

			rowBase = Long.parseLong(arrRowLog[1]);

			if(rowQuery==0 && rowBase==0){
				throw new RuntimeException("Recent VS did not really start. Re-uptake impossible. Please run VS from scatch!");
			}

			System.out.println("Re-uptake query id " + rowQuery);
			System.out.println("Re-uptake base id " + rowBase);

			long calculationsAlreadyDone = ((rowQuery)*(nRecordsDB) + rowBase) * liEnabledModelDescriptorVS.size();

			System.out.println("calculationsAlreadyDone " + calculationsAlreadyDone);

			ccSimilarityCalculationsTotal.set(calculationsAlreadyDone);

			String line = FileUtils.tail(fiDWARResult).trim();

			// File not complete?
			if(!DWARFileHandler.TAG_DW_PROPERIES_END.equals(line)) {

				FileUtils.deleteAllAfterLastLineFeed(fiDWARResult);

				if(!DWARFileHandlerHelper.containsTableHeaderLine(fiDWARResult)) {
					throw new RuntimeException("Recent VS did not really start. Re-uptake impossible. Please run VS from scatch!");
				}

				// Delete all above the last rows
				DWARFileHandlerHelper.deleteFooter(fiDWARResult);
				VSResultHandler.deleteAllAbove(fiDWARResult, rowQuery, rowBase);

				vsResultHandler = new VSResultHandler(liEnabledModelDescriptorVS, model.getWorkDir(), model.isHitOr(), fiDWARResult);
			} else {
				allResultsWritten = true;
			}

		} else { // VS from scratch
			File fiRowLog = new File(model.getWorkDir(), FILE_NAME_ROW_LOGGING);

			fiRowLog.delete();

			vsResultHandler = new VSResultHandler(liEnabledModelDescriptorVS, model.getWorkDir(), model.isHitOr(), null);

			fiDWARResult = vsResultHandler.getFiDWARVSResultOut();

			properties.setProperty(PROPERTY_FILE_RESULT, fiDWARResult.getAbsolutePath());

			FileWriter fwProp = new FileWriter(fiProperties);

			properties.store(fwProp, PROPERTY_COMMENT);
		}

		boolean shapeAlignDescriptorHandler = false;

		ISimilarityCalculator[] arrSimilarityCalculator = new ISimilarityCalculator[liEnabledModelDescriptorVS.size()];
		for (int i = 0; i < liEnabledModelDescriptorVS.size(); i++) {
			ISimilarityCalculator dh = liEnabledModelDescriptorVS.get(i).getSimilarityCalculator();

			arrSimilarityCalculator[i] = dh;

			if (dh instanceof DescriptorHandlerShape) {
				shapeAlignDescriptorHandler = true;
			}
		}

		if(!allResultsWritten) {

			System.out.println("VSParallel Start screening.");

			AtomicLong idMinQuery = new AtomicLong();
			AtomicLong idMinBase = new AtomicLong();

			DWARInterface2ModelVSRecordCompiler dwarInterface2ModelVSRecordCompilerQuery =
					new DWARInterface2ModelVSRecordCompiler(
							fiDWARQuery, sizeBatchQuery, arrSimilarityCalculator, model.isStereoDepletion(), true, rowQuery);

			List<ModelVSRecord> liModelVSRecordQuery = dwarInterface2ModelVSRecordCompilerQuery.getNextBatch();

			idMinQuery.set(dwarInterface2ModelVSRecordCompilerQuery.getIdMinRecord());

			System.out.println("Fetched first batch query with " + liModelVSRecordQuery.size() + " records. First id " + dwarInterface2ModelVSRecordCompilerQuery.getIdMinRecord() + ", last id " + dwarInterface2ModelVSRecordCompilerQuery.getIdMaxRecord() + ".");

			if (enabledPheSADescriptor) {
				String modePhESAQuery = ConvertString2PheSA.getPhESAMode(headerQuery);
				System.out.println("Query mode PheSA descriptor " + modePhESAQuery + ".");

				String modePhESABase = ConvertString2PheSA.getPhESAMode(headerDB);
				System.out.println("Library mode PheSA descriptor " + modePhESABase + ".");

				for (ModelDescriptorVS modelDescriptorVS : liEnabledModelDescriptorVS) {
					if (DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName.equals(modelDescriptorVS.getShortName())) {
						if (((DescriptorHandlerShape) modelDescriptorVS.getSimilarityCalculator()).isFlexible()) {
							System.out.println("Comparison mode PheSA descriptor flexible alignment.");
						} else {
							System.out.println("Comparison mode PheSA descriptor rigid alignment.");
						}
						break;
					}
				}
			}

			if (liModelVSRecordQuery.size() == 0) {
				System.out.println("Missing query descriptors!");
				throw new RuntimeException("Missing query descriptors!");
			}

			//
			// Create the descriptor handlers for the similarity calculations as deep copies.
			//
			List<ModelDescriptorVS[]> liArrModelDescriptorVSThread = new ArrayList<>();

			for (int i = 0; i < nProcessors; i++) {
				ModelDescriptorVS[] arrModelDescriptorVSThread = new ModelDescriptorVS[liEnabledModelDescriptorVS.size()];
				for (int j = 0; j < liEnabledModelDescriptorVS.size(); j++) {
					arrModelDescriptorVSThread[j] = liEnabledModelDescriptorVS.get(j).getThreadSafeCopy();
				}
				liArrModelDescriptorVSThread.add(arrModelDescriptorVSThread);
			}

			ConcurrentLinkedQueue<VSThread> liVSThread = new ConcurrentLinkedQueue<>();


			//
			// Logging
			//
			ILog logFct = new ILog() {
				@Override
				public void log() {
					try {
						VSParallel.this.log(arrSimilarityCalculator, liVSThread);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			RunnableLog runnableLog = new RunnableLog(logFct);
			ExecutorService executorServiceLog = Executors.newSingleThreadExecutor();
			executorServiceLog.submit(runnableLog);
			executorServiceLog.shutdown();
			System.out.println("Logging thread started.");
			startLog = new Date().getTime();

			//
			// Special write for the ROCS shape descriptor
			//
			Pipeline<VSParallel.ModelTwoStereoMolecules> pipeShape2Mol = new Pipeline<>();

			DWARWriterTwoMoleculesAligned dwarWriterTwoMoleculesAligned = null;

			if (shapeAlignDescriptorHandler) {
				String name = PREFIX_SHAPE_ALIGNED_MOLS + ConstantsDWAR.DWAR_EXTENSION;
				File fiDWAROutShapeAlignedMolecules = new File(model.getWorkDir(), name);
				properties.setProperty(PROPERTY_FILE_RESULT_SHAPE_ALIGN, fiDWAROutShapeAlignedMolecules.getAbsolutePath());
				dwarWriterTwoMoleculesAligned = new DWARWriterTwoMoleculesAligned(pipeShape2Mol, fiDWAROutShapeAlignedMolecules);
				dwarWriterTwoMoleculesAligned.startWriteThread();
			}

			System.out.println("Start outer loop");

			while (liModelVSRecordQuery.size() > 0) { // Outer loop

				DWARInterface2ModelVSRecordCompiler dwarInterface2ModelVSRecordCompilerBase =
						new DWARInterface2ModelVSRecordCompiler(fiDWARDB, sizeBatchBase, arrSimilarityCalculator, model.isStereoDepletion(), false, rowBase);

				List<ModelVSRecord> liModelVSRecordBase = dwarInterface2ModelVSRecordCompilerBase.getNextBatch();

				idMinBase.set(dwarInterface2ModelVSRecordCompilerBase.getIdMinRecord());

				System.out.println("Fetched first batch base with " + liModelVSRecordBase.size() + " records. First id " + dwarInterface2ModelVSRecordCompilerBase.getIdMinRecord() + ", last id " + dwarInterface2ModelVSRecordCompilerBase.getIdMaxRecord() + ".");

				queueModelVSRecordBase.addAll(liModelVSRecordBase);

				System.out.println("VSParallel queueModelVSRecordBase " + queueModelVSRecordBase.size());

				while (queueModelVSRecordBase.size() > 0) {

					ExecutorService pool = Executors.newFixedThreadPool(nProcessors);
					liVSThread.clear();
					for (int i = 0; i < nProcessors; i++) {

						VSThread vsThread = new VSThread(i,
								queueModelVSRecordBase,
								ccSimilarityCalculationsTotal, ccSimilarityCalculationsLog, ccHits,
								liArrModelDescriptorVSThread.get(i), liModelVSRecordQuery, model.isStereoDepletion(),
								pipeShape2Mol, vsResultHandler);

						liVSThread.add(vsThread);

						pool.submit(vsThread);

					}

					pool.shutdown();

					while (!pool.isTerminated()) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					liModelVSRecordBase = dwarInterface2ModelVSRecordCompilerBase.getNextBatch();

					if (liModelVSRecordBase.size() > 0) {
						idMinBase.set(dwarInterface2ModelVSRecordCompilerBase.getIdMinRecord());
						writeBatchProgress(model.getWorkDir(), idMinQuery, idMinBase);
						System.out.println("VSParallel: Fetched poll batch base " + dwarInterface2ModelVSRecordCompilerBase.getBatchNumber() + " with size " + liModelVSRecordBase.size() + " records. First id " + dwarInterface2ModelVSRecordCompilerBase.getIdMinRecord() + ", last id " + dwarInterface2ModelVSRecordCompilerBase.getIdMaxRecord() + ".");
					} else {
						idMinBase.set(0);
						System.out.println("VSParallel: Fetched poll batch base " + dwarInterface2ModelVSRecordCompilerBase.getBatchNumber() + " with size 0.");
					}

					queueModelVSRecordBase.addAll(liModelVSRecordBase);


				} // End inner while

				liModelVSRecordQuery = dwarInterface2ModelVSRecordCompilerQuery.getNextBatch();

				if (liModelVSRecordQuery.size() > 0) {
					idMinQuery.set(dwarInterface2ModelVSRecordCompilerQuery.getIdMinRecord());
					writeBatchProgress(model.getWorkDir(), idMinQuery, idMinBase);
					System.out.println("VSParallel: Fetched poll batch query " + dwarInterface2ModelVSRecordCompilerQuery.getBatchNumber() + " with size " + liModelVSRecordQuery.size() + " records. First id " + dwarInterface2ModelVSRecordCompilerQuery.getIdMinRecord() + ", last id " + dwarInterface2ModelVSRecordCompilerQuery.getIdMaxRecord() + ".");
				} else {
					System.out.println("VSParallel: Fetched poll batch query " + dwarInterface2ModelVSRecordCompilerQuery.getBatchNumber() + " with size 0.");
				}


			} // End outer loop. For query.

			vsResultHandler.setAllDataIn();

			if (shapeAlignDescriptorHandler) {
				pipeShape2Mol.setAllDataIn();
			}

			double fractionDone = ((double) ccSimilarityCalculationsTotal.get() / (double) numScores2Calc);

			double percentDone = fractionDone * 100.0;

			String s = "Finally calculated " + FORMAT_COUNTER.format(ccSimilarityCalculationsTotal.get()) + " scores (" + Formatter.format0(percentDone) + " %).";
			String s2 = "Hits " + FORMAT_COUNTER.format(ccHits) + ".";
			System.out.println(s);
			System.out.println(s2);

			infoVS.setScoresCalculated(ccSimilarityCalculationsTotal.get());
			infoVS.setHits(ccHits.get());

			runnableLog.stop();

			while (!executorServiceLog.isTerminated()) {
				Thread.sleep(100);
			}

			//
			// Error evaluation
			//
			int[] arrFailedSimilarityCalculations = new int[arrSimilarityCalculator.length];

			System.out.println("Errors in VSParallel:");

			boolean errors = false;
			for (VSThread vsThread : liVSThread) {

				if (vsThread.getEhm().hasErrors()) {
					System.out.println("Errors in thread");
					System.out.println(vsThread.getEhm().toString());
					errors = true;
				}

				for (int i = 0; i < arrSimilarityCalculator.length; i++) {
					arrFailedSimilarityCalculations[i] += vsThread.getArrFailedSimilarityCalculations()[i];
				}
			}

			for (int i = 0; i < arrSimilarityCalculator.length; i++) {
				if (arrFailedSimilarityCalculations[i] > 0) {
					String m = "Similarity calculations failed " + arrFailedSimilarityCalculations[i] + " times for descriptor " + arrSimilarityCalculator[i].getInfo().shortName + ".";
					System.out.println(m);
					errors = true;
				}
			}

			if (!errors) {
				System.out.println("No errors.");
			}

			//
			// Start merging results from result handler file.
			//
			while (!vsResultHandler.allResultsWritten()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			System.out.println("VSParallel vsResultHandler write for file " + vsResultHandler.getFiDWARVSResultOut().getAbsolutePath() + " finished.");


			System.out.println("Start writing results virtual screening on " + fiDWARDB.getAbsolutePath() + ".");
			System.out.println("ccSimilarityCalculationsTotal " + ccSimilarityCalculationsTotal + ".");

			long diff = numScores2Calc - ccSimilarityCalculationsTotal.get();

			if (diff > 0) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("Not all scores were calculated, missing " + diff + " scores!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				errors = true;
			}

			double ratioCalcSucceeded = ccSimilarityCalculationsTotal.get() / (double)numScores2Calc;

			if (ratioCalcSucceeded < THRESH_RATIO_CALCULATIONS_SUCCEEDED_ERROR) {
				throw new RuntimeException("Fraction of succeeded calculations (" + Formatter.format4(ratioCalcSucceeded) + ") fell below threshold " + THRESH_RATIO_CALCULATIONS_SUCCEEDED_ERROR + ".");
			}
		}

		File fiDWARQuery = new File(model.getQuery());
		File fiDWARLibrary = new File(model.getLibrary());

		long recordsMatchedSimilarityQuery = DWARFileHandlerHelper.getSize(fiDWARResult);

		VSResultSummarizer vsResultSummarizer = new VSResultSummarizer(
				model.getWorkDir(), liEnabledModelDescriptorVS, fiDWARResult, model.isSkipDescriptorsInOutput());
		if(ConstantsVS.COMPARISON_MODE_VS.equals(model.getComparisonMode())) {
			vsResultSummarizer.writeResultsVS(fiDWARLibrary, fiDWARQuery, model.getQueryIdentifier(), infoVS, model.getNameDWARResultElusive(), model.getNameDWARResultSummary(), model.isSkipDescriptorsInOutput());
			System.out.println("Result elusive: " + vsResultSummarizer.getFiDWARVSResultBaseAndQueryStructures());
			System.out.println("Result short: " + vsResultSummarizer.getFiDWARSummary());
		} else {
			vsResultSummarizer.writeResultsLibraryComparison(fiDWARLibrary, infoVS, model.getNameDWARResultElusive());
			System.out.println("Result library comparison. File with library molecules not similar to query.");
			System.out.println(vsResultSummarizer.getFiDWARVSResultNotSimilarBaseMolecules().getAbsolutePath());
		}

		if(shapeAlignDescriptorHandler) {
			System.out.println("Shape aligned molecule pairs written to " + properties.getProperty(PROPERTY_FILE_RESULT_SHAPE_ALIGN) + ".");
		}

		return recordsMatchedSimilarityQuery;
	}

	public void setProcessorsForVS(int processorsForVS) {
		this.processorsForVS = processorsForVS;
	}

	private static void writeBatchProgress(File workdir, AtomicLong idMinQuery, AtomicLong idMinBase) throws IOException {
		//
		// Track the processed rows from the input files.
		//
		// We just select randomly one thread.
		StringBuilder sb = new StringBuilder();
		sb.append(idMinQuery.get());
		sb.append("\t");
		sb.append(idMinBase.get());
		sb.append("\n"); // We need the line feed for the re-uptake

		File fiRowLog = new File(workdir, FILE_NAME_ROW_LOGGING);

		BufferedWriter bwRowLog = new BufferedWriter(new FileWriter(fiRowLog, true));
		bwRowLog.append(sb.toString());
		bwRowLog.close();


	}

	private void log(ISimilarityCalculator [] arrModelDescriptorVS, ConcurrentLinkedQueue<VSThread> liVSThread) throws IOException {

		long end = new Date().getTime();

		long delta = end - startLog;

		// The sum of all nanoseconds from all processes used for similarity calculation.
		long [] arrDescriptorNano = new long[arrModelDescriptorVS.length];
		long [] arrSimilarityCalculations = new long[arrModelDescriptorVS.length];
		int [] arrFailedSimilarityCalculations = new int[arrModelDescriptorVS.length];

		for (VSThread vsThread : liVSThread) {
			for (int i = 0; i < arrModelDescriptorVS.length; i++) {
				arrDescriptorNano[i] += vsThread.getArrDescriptorNano()[i];
				arrSimilarityCalculations[i] += vsThread.getArrSimilarityCalculations()[i];
				arrFailedSimilarityCalculations[i] += vsThread.getArrFailedSimilarityCalculations()[i];
			}
		}

		double [] arrDescriptorCalculationsPerSecondPerCore = new double[arrModelDescriptorVS.length];

		for (int i = 0; i < arrModelDescriptorVS.length; i++) {
			double secs = arrDescriptorNano[i] / (double)TimeDelta.NANO_SECOND;
			arrDescriptorCalculationsPerSecondPerCore[i] = (double)arrSimilarityCalculations[i] / secs;
		}

		for (int i = 0; i < arrModelDescriptorVS.length; i++) {

			long millisecSum = arrDescriptorNano[i] / TimeDelta.NANO_MS;

			if(liVSThread.size()>0) {
				long millisecPerCore = millisecSum / liVSThread.size();

				String shortName = arrModelDescriptorVS[i].getInfo().shortName;

				System.out.println(
						shortName + " sim calc " + arrSimilarityCalculations[i] +
								", failed sim calc " + arrFailedSimilarityCalculations[i] +
								", time spend " + TimeDelta.toString(millisecPerCore) +
								", sum time spend on all " + liVSThread.size() + " cores " + TimeDelta.toString(millisecSum) +
								", sim calc per sec per core " + Formatter.format4(arrDescriptorCalculationsPerSecondPerCore[i]) + ".");
			}

		}

		double fractionDone =  ((double)ccSimilarityCalculationsTotal.get() / (double)numScores2Calc);

		double percentDone =  fractionDone * 100.0;

		String s = "Calculated " + FORMAT_COUNTER.format(ccSimilarityCalculationsTotal.get()) + " scores (" + Formatter.format0(percentDone) + " %).";

		System.out.println(s);

		timeEstimator.setFractionDone(fractionDone);

		System.out.println("Estimated calculation time left " + timeEstimator.toString() + ".");

		double deltaSec = delta / (double)1000;

		double calcPerSec = (ccSimilarityCalculationsLog.get()/deltaSec);

		ccSimilarityCalculationsLog.set(0);

		System.out.println("Performance " + Formatter.format3(calcPerSec) + " similarity calculations per second [calc/sec].");
		System.out.println("Similarity > threshold for " + ccHits.get() + " comparisons.");

		startLog = new Date().getTime();

	}

	private static class VSThread implements Runnable {

		private int idThread;

		private List<ModelVSRecord> liModelVSRecordQuery;

		private ConcurrentLinkedQueue<ModelVSRecord> queueModelVSRecordBase;
		private AtomicLong ccSimilarityCalculationsTotal;
		private AtomicLong ccSimilarityCalculationsLog;
		private AtomicLong ccHits;

		@SuppressWarnings("rawtypes")
		private ModelDescriptorVS [] arrDHVS;


		private ErrorHashMap ehm;

		private long [] arrDescriptorNano;
		private int [] arrSimilarityCalculations;
		private int [] arrFailedSimilarityCalculations;

		private boolean stereoDepletion;

		private Pipeline<VSParallel.ModelTwoStereoMolecules> pipeOutShape2Mol;

		private VSResultHandler vsResultHandler;

		private AtomicLong idBase;

		/**
		 *
		 * @param queueModelVSRecordBase
		 * @param ccSimilarityCalculationsTotal
		 * @param ccSimilarityCalculationsLog
		 * @param ccHits
		 * @param arrDHVSIn
		 * @param liModelVSRecordQuery
		 * @param stereoDepletion if true and the hash codes of two molecules are identical the similarity is set to 1.
		 * @param pipeOutShape2Mol
		 */
		public VSThread(int idThread,
						ConcurrentLinkedQueue<ModelVSRecord> queueModelVSRecordBase,
						AtomicLong ccSimilarityCalculationsTotal,
						AtomicLong ccSimilarityCalculationsLog,
						AtomicLong ccHits,
						ModelDescriptorVS [] arrDHVSIn,
						List<ModelVSRecord> liModelVSRecordQuery,
						boolean stereoDepletion,
						Pipeline<VSParallel.ModelTwoStereoMolecules> pipeOutShape2Mol,
						VSResultHandler vsResultHandler) {

			this.idThread = idThread;
			this.queueModelVSRecordBase = queueModelVSRecordBase;
			this.ccSimilarityCalculationsTotal = ccSimilarityCalculationsTotal;
			this.ccSimilarityCalculationsLog = ccSimilarityCalculationsLog;
			this.ccHits = ccHits;

			this.arrDHVS = arrDHVSIn;

			this.liModelVSRecordQuery = liModelVSRecordQuery;

			List<ModelDescriptorVS> liModelDescriptorHandlerVS = new ArrayList<>();


			for (int i = 0; i < arrDHVS.length; i++) {

				liModelDescriptorHandlerVS.add(arrDHVS[i]);

				if(DEBUG) {
					if (arrDHVS[i].getSimilarityCalculator() instanceof DescriptorHandlerShape) {
						DescriptorHandlerShape dhShape = (DescriptorHandlerShape) arrDHVS[i].getSimilarityCalculator();
						if (dhShape.isFlexible()) {
							System.out.println(" VSThread DescriptorHandlerShape flexible align.");
						}
						System.out.println(" VSThread DescriptorHandlerShape ppWeight = " + Formatter.format3(dhShape.getPpWeight()) + ".");
					}
				}
			}

			arrFailedSimilarityCalculations = new int [arrDHVS.length];
			arrSimilarityCalculations = new int [arrDHVS.length];

			arrDescriptorNano = new long [arrDHVS.length];

			// vsResultContainerThread = new VSResultContainer(liModelDescriptorHandlerVS, null);

			this.stereoDepletion = stereoDepletion;

			this.pipeOutShape2Mol = pipeOutShape2Mol;

			this.vsResultHandler = vsResultHandler;

			ehm = new ErrorHashMap();

			idBase = new AtomicLong();


		}

		@SuppressWarnings("unchecked")
		public void run() {

			// System.out.println("Start VSThread");

			ExecutorService executorServiceRunSimilarity = Executors.newSingleThreadExecutor();

			float [] arrSimilarity = new float[arrDHVS.length];

			while(!queueModelVSRecordBase.isEmpty()) {

				// System.out.println("VSThread queueModelVSRecordBase " + queueModelVSRecordBase.size());

				ModelVSRecord modelVSRecordBase = queueModelVSRecordBase.poll();

				if(modelVSRecordBase!=null) {

					for (int i = 0; i < liModelVSRecordQuery.size(); i++) {

						ModelVSRecord modelVSRecordQuery = liModelVSRecordQuery.get(i);

						double [] arrPheSASimilarity = null;

						for (int j = 0; j < arrDHVS.length; j++) {

							ModelDescriptorVS modelDescriptorVS = arrDHVS[j];

							Object objBase = modelVSRecordBase.getArrDescriptors()[j];

							Object objQuery = modelVSRecordQuery.getArrDescriptors()[j];

							if((objQuery==null) || (objBase==null)) {

								arrFailedSimilarityCalculations[j]++;

								continue;
							}

							double sim=0;
							try {

								RunGetSimilarity runGetSimilarity = new RunGetSimilarity(modelDescriptorVS, objQuery, objBase);

								long n1 = System.nanoTime();

								executorServiceRunSimilarity.submit(runGetSimilarity);

								int ccWhile=0;
								long t0Sim = System.currentTimeMillis();
								while (runGetSimilarity.isRunning()){
									ccWhile++;
									if(ccWhile>1000) {
										Thread.sleep(1);
									}
									if(ccWhile>10000) {
										Thread.sleep(100);
									}
									long deltaSim = System.currentTimeMillis()-t0Sim;

									if (deltaSim>(TimeDelta.MS_MINUTE*15)){
										System.err.println("Similarity calculation for descriptor\t" + modelDescriptorVS.getShortName() + " exceeds " + TimeDelta.toString(deltaSim));
										System.err.println("Id base\t" + modelVSRecordBase.getId() + "\t" +  modelVSRecordBase.getIDCode());
										System.err.println("Id query\t" + modelVSRecordQuery.getId() + "\t" +  modelVSRecordQuery.getIDCode());
										System.err.println("Break!!!");
										executorServiceRunSimilarity.shutdownNow();
										executorServiceRunSimilarity = Executors.newSingleThreadExecutor();
										break;
									}
								}
								sim = runGetSimilarity.getSim();

								long n2 = System.nanoTime();

								long diffNano = n2-n1;

								arrDescriptorNano[j] += diffNano;

								long diffMS = diffNano / TimeDelta.NANO_MS;

								if(diffMS > MS_TOO_LONG_FOR_SIMILARITY) {
									System.out.println("VSThread needed " + TimeDelta.toString(diffMS) + " for " + modelDescriptorVS.getShortName()  + " descriptor similarity with base " + modelVSRecordBase.getIDCode());
								}

								arrSimilarityCalculations[j]++;

								if(stereoDepletion){
									if(modelVSRecordBase.getStereoDepletedHash()==modelVSRecordQuery.getStereoDepletedHash()){
										sim = 1.0;
									}
								}

								arrSimilarity[j]=(float)sim;

								if(modelDescriptorVS.getSimilarityCalculator() instanceof DescriptorHandlerShape){

									if((sim > 0) && (sim >= modelDescriptorVS.getSimilarityThreshold())){

										DescriptorHandlerShape dhShape = (DescriptorHandlerShape)modelDescriptorVS.getSimilarityCalculator();

										arrPheSASimilarity = dhShape.getPreviousPheSAResult();

										ModelTwoStereoMolecules mtsm = new ModelTwoStereoMolecules(dhShape.getPreviousAlignment()[0], dhShape.getPreviousAlignment()[1]);

										pipeOutShape2Mol.addData(mtsm);
									}
								}

							} catch (Exception e) {
								System.err.println("Id base\t" + modelVSRecordBase.getId() + "\t" +  modelVSRecordBase.getIDCode());
								System.err.println("Id query\t" + modelVSRecordQuery.getId() + "\t" +  modelVSRecordQuery.getIDCode());
								e.printStackTrace();
							}

							ccSimilarityCalculationsTotal.incrementAndGet();

							ccSimilarityCalculationsLog.incrementAndGet();

						}

						if(vsResultHandler.conditionalAdd(modelVSRecordBase.getId(), arrSimilarity, arrPheSASimilarity, modelVSRecordQuery.getId())){
							ccHits.incrementAndGet();
						}

						Arrays.fill(arrSimilarity, 0.0f);
					}
					idBase.set(modelVSRecordBase.getId());
				}
			}

			executorServiceRunSimilarity.shutdown();
			System.out.println("VSThread " + idThread + " finished.");
		}

		public ErrorHashMap getEhm() {
			return ehm;
		}

		public int[] getArrFailedSimilarityCalculations() {
			return arrFailedSimilarityCalculations;
		}

		public long[] getArrDescriptorNano() {
			return arrDescriptorNano;
		}

		public int[] getArrSimilarityCalculations() {
			return arrSimilarityCalculations;
		}

		public long getIdBase() {
			return idBase.get();
		}
	}

	static class ModelTwoStereoMolecules {
    	StereoMolecule mol1;
    	StereoMolecule mol2;

		public ModelTwoStereoMolecules(StereoMolecule mol1, StereoMolecule mol2) {
			this.mol1 = mol1;
			this.mol2 = mol2;
		}
	}


	private static class RunGetSimilarity implements Runnable {

		boolean running;

		ModelDescriptorVS modelDescriptorVS;

		Object objQuery;
		Object objBase;

		double sim;


		public RunGetSimilarity(ModelDescriptorVS modelDescriptorVS, Object objQuery, Object objBase) {
			this.modelDescriptorVS = modelDescriptorVS;
			this.objQuery = objQuery;
			this.objBase = objBase;
			running = true;
		}

		@Override
		public void run() {
			sim = 0;
			try {
				sim = modelDescriptorVS.getSimilarity(objQuery, objBase);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				running = false;
			}
		}

		boolean isRunning() {
			return running;
		}

		public double getSim() {
			return sim;
		}
	};

}



