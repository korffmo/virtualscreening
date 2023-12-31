package org.openchemlib.chem.vs.business;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHelper;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.descriptor.vs.VSResultArray;
import com.actelion.research.chem.dwar.DWARFileWriter;
import com.actelion.research.chem.dwar.DWARHeader;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.Pipeline;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VSResultHandler
 *
 * Writes the results from the virtual screening on the fly into a dwar file.
 *
 * @author Modest von Korff
 * 18.02.2020 start implementation, derived from VSResultContainer
 */
public class VSResultHandler {


	private static final DecimalFormat DF_SIM = (DecimalFormat)DecimalFormat.getInstance(Locale.US);
	{
		DF_SIM.applyPattern("0.0000");
	}


	private static final String PREFIX = "vsResult";

    private ModelDescriptorVS [] arrSimilarityCalculatorVS;


	private boolean hitORCondition;

	private File fiDWARVSResultOut;

	private Pipeline<VSResultArray> pipeVSResultOut;

	private ExecutorService executorServiceWriteResults;




	/**
	 *
	 * @param liModelDescriptorVS
	 * @param workdir
	 * @param hitORCondition if set true, one of the similarity values must be below or equal to its given threshold.
	 *                       if set to false, all similarity values must be below their given thresholds.
	 * @throws IOException
	 */
	public VSResultHandler(List<ModelDescriptorVS> liModelDescriptorVS, File workdir, boolean hitORCondition, File fiDWARVSResultOut) throws IOException, NoSuchFieldException {

		boolean append = false;
		if(fiDWARVSResultOut==null){
			this.fiDWARVSResultOut = File.createTempFile(PREFIX, ConstantsDWAR.DWAR_EXTENSION, workdir);
		} else {
			this.fiDWARVSResultOut = fiDWARVSResultOut;
			append = true;
		}

		this.hitORCondition = hitORCondition;

		this.pipeVSResultOut = new Pipeline<> ();

		arrSimilarityCalculatorVS = new ModelDescriptorVS [liModelDescriptorVS.size()];
		for (int i = 0; i < liModelDescriptorVS.size(); i++) {
			arrSimilarityCalculatorVS[i]= liModelDescriptorVS.get(i);
		}

		initializeWrite(append, liModelDescriptorVS);
	}


	private void initializeWrite(boolean append, List<ModelDescriptorVS> liModelDescriptorVS) throws IOException, NoSuchFieldException {

		DWARHeader header = new DWARHeader(getHeaderTags(liModelDescriptorVS));

		DWARFileWriter fw = new DWARFileWriter(fiDWARVSResultOut, header, append);

		Runnable runWrite = new Runnable() {
			@Override
			public void run() {

				try {
					while (!pipeVSResultOut.wereAllDataFetched()) {
						VSResultArray vsResult = pipeVSResultOut.pollData();
						if(vsResult==null){
							try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
							continue;
						}
						DWARRecord record = new DWARRecord(header);
						add2DWAR(vsResult, record);
						fw.write(record);
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					fw.close();
				}
			}
		};

		executorServiceWriteResults = Executors.newSingleThreadExecutor();

		executorServiceWriteResults.submit(runWrite);

		executorServiceWriteResults.shutdown();

	}


	public boolean allResultsWritten(){
		return (executorServiceWriteResults.isTerminated()) ? true : false;
	}


	public File getFiDWARVSResultOut() {
		return fiDWARVSResultOut;
	}


	public boolean conditionalAdd(long molId, float [] arrSimilarity, double [] arrPheSASimilarity, long queryMoleculeId){

		boolean hit = false;

		if(hitORCondition) { // Only one similarity value must be higher than its threshold.

			for (int i = 0; i < arrSimilarity.length; i++) {

				if (Float.isNaN(arrSimilarity[i])) {
					continue;
				}

				double thresh = arrSimilarityCalculatorVS[i].getSimilarityThreshold();

				if (arrSimilarity[i] >= thresh) {
					hit = true;
					break;
				}
			}
		} else { // All similarity values must be higher than their threshold.
			hit = true;

			for (int i = 0; i < arrSimilarity.length; i++) {

				if (Float.isNaN(arrSimilarity[i])) {
					hit = false;
					break;
				}

				double thresh = arrSimilarityCalculatorVS[i].getSimilarityThreshold();

				if (arrSimilarity[i] < thresh) {
					hit = false;
					break;
				}
			}
		}

		if(hit){

			int n = arrSimilarity.length;

			float [] arrSimilarityCpy = new float[n];

			System.arraycopy(arrSimilarity, 0, arrSimilarityCpy, 0, n);

			VSResultArray result = new VSResultArray(molId, arrSimilarityCpy, arrPheSASimilarity, queryMoleculeId);

			pipeVSResultOut.addData(result);

		}

		return hit;
    }

    public long getNumberHits(){
		return pipeVSResultOut.getAdded();
	}

	public void setAllDataIn(){
		pipeVSResultOut.setAllDataIn();
	}


	private void add2DWAR(VSResultArray vsResultArray, DWARRecord record) {
		record.addOrReplaceField(VSResultArray.TAG_TASK_ID, Integer.toString(vsResultArray.getTaskId()));
		record.addOrReplaceField(VSResultArray.TAG_BASE_ID, Long.toString(vsResultArray.getSubstanceId()));
		for (int i = 0; i < arrSimilarityCalculatorVS.length; i++) {
			String tagSimilarity = DescriptorHelper.getTagDescriptorSimilarity(arrSimilarityCalculatorVS[i].getSimilarityCalculator());
			double v = vsResultArray.getArrResult()[i];
			record.addOrReplaceField(tagSimilarity, DF_SIM.format(v));
		}

		double [] arr = vsResultArray.getArrResultPheSA();
		if(arr!=null){
			// result[0]: PheSASimilarity
			// r[1]: PPSimilarity
			// r[2]: ShapeSimilarity
			// r[3]: AdditionalVolumeContribution

			// Already to the output
			// record.addOrReplaceField(ConstantsPhESAParameter.TAG_PHARMACOPHORE_SIMILARITY, Formatter.format4(arr[0]));
			record.addOrReplaceField(ConstantsPhESAParameter.TAG_PP_SIMILARITY, DF_SIM.format(arr[1]));
			record.addOrReplaceField(ConstantsPhESAParameter.TAG_SHAPE_SIMILARITY, DF_SIM.format(arr[2]));
			record.addOrReplaceField(ConstantsPhESAParameter.TAG_VOLUME_CONTRIBUTION, DF_SIM.format(arr[3]));
		}

		record.addOrReplaceField(VSResultArray.TAG_QUERY_ID, Long.toString(vsResultArray.getQueryId()));
	}

	public static List<String> getHeaderTags(List<ModelDescriptorVS> liModelDescriptorVS){

		List<String> li = new ArrayList<>();

		li.add(VSResultArray.TAG_TASK_ID);
		li.add(VSResultArray.TAG_BASE_ID);
		li.add(VSResultArray.TAG_QUERY_ID);

		for (ModelDescriptorVS mvs : liModelDescriptorVS) {

			String tagSimilarity = DescriptorHelper.getTagDescriptorSimilarity(mvs.getSimilarityCalculator());
			li.add(tagSimilarity);

			String shortName = mvs.getShortName();

			if(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName.equals(shortName)) {
				// Already to the output
				// li.add(ConstantsPhESAParameter.TAG_PHARMACOPHORE_SIMILARITY);
				li.add(ConstantsPhESAParameter.TAG_PP_SIMILARITY);
				li.add(ConstantsPhESAParameter.TAG_SHAPE_SIMILARITY);
				li.add(ConstantsPhESAParameter.TAG_VOLUME_CONTRIBUTION);
			}
		}

		return li;
	}


	public static void deleteAllAbove(File fiDWARVSResultOut, long idQueryMax, long idBaseMax) throws IOException {

		ReversedLinesFileReader fr = new ReversedLinesFileReader(fiDWARVSResultOut);
		String line;

		long ccBytes = 0;
		do {
			line = fr.readLine();
			String [] arr = line.split("\t");

			long idBase = Long.parseLong(arr[1]);
			long idQuery = Long.parseLong(arr[2]);

			// System.out.println(line);

			if(idQuery<idQueryMax || idBase<idBaseMax){
				break;
			}

			ccBytes += line.getBytes().length+1;

		} while (line != null);
		fr.close();

		RandomAccessFile f = new RandomAccessFile(fiDWARVSResultOut, "rw");
		long length = f.length() - ccBytes;
		f.setLength(length);
		f.close();


	}

}
