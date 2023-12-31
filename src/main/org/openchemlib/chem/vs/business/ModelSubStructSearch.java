package org.openchemlib.chem.vs.business;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.filter.TwoMolecules;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.LogHandler;
import com.actelion.research.util.UserDirsDefault;
import com.actelion.research.util.UserDirsDefault;


/**
 * 
 * ModelSubStructSearch
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * 10 Nov 2008 MvK: Start implementation
 * 4 May 2011 MvK: Changes
 */
public class ModelSubStructSearch {
	
	public static final String NAME_VSRESULT = "SubStructSearchResult";
	
	public static final String PATH_FILE_GPCR = "D:\\KorffModest\\data\\Collections\\InHouse\\GPCR_Ligand\\090107\\GPCRLigand_Descriptors.dwar";
	
	public static final String DEFAULT_FILE_OUT_DIR = UserDirsDefault.getTmp(0);
	
	public static final String DEFAULT_FILE_IN = "D:\\KorffModest\\data\\Supplier\\ChemNavigator\\Sourceable\\Structure\\Sourceable_01.dwar";
	
	public static final String DEFAULT_FILE_OUT_NAME = "subStructSearch";
	
	public static final double DEFAULT_MAX_MW = 350;
	
	public static final int PROPERTY_MIN_MW = 0;
	
	public static final int PROPERTY_MAX_MW = 1;

	private static final boolean ADD_SUPPLIER = false; 
	
	private StereoMolecule sterMolQuery;
	
	private List<TwoMolecules> liMolIncludeExclude;
	
	private String source;
	
	private File workDir;
	
	private File fiResultSubstructureSearch;
	
	private File fiIncludeExcludeDWAR;
	
	private LogHandler log;
	
	private boolean running;
	
	private boolean addSupplierInfo;
	
	private boolean debug;
	
	private boolean stop;
	
	private int ccAccepted;
	
	private int ccNotFound;
	
	private int ccExcluded;
	
	private int ccProcessed;

	
	public ModelSubStructSearch() throws IOException {
		
		liMolIncludeExclude = new ArrayList<TwoMolecules>();
		
		sterMolQuery = new StereoMolecule();
		sterMolQuery.setFragment(true);
		
		addSupplierInfo = ADD_SUPPLIER;
		
		ccAccepted=0;
		
		ccNotFound=0;
		
		ccExcluded=0;
		
		ccProcessed=0;
		
		workDir = new File(System.getProperty("user.home"));
		
		initOutFile();
		
	}
	
	public void setAccepted(int n){
		ccAccepted=n;
	}
	public void setNotFound(int n){
		ccNotFound=n;
	}
	public void setExcluded(int n){
		ccExcluded=n;
	}
	public void setProcessed(int n){
		ccProcessed=n;
	}
	
	public void initOutFile() throws IOException {
		File dir = new File(DEFAULT_FILE_OUT_DIR);
		if(dir.isDirectory())
			fiResultSubstructureSearch = File.createTempFile(DEFAULT_FILE_OUT_NAME, ConstantsDWAR.DWAR_EXTENSION, dir);
		else
			fiResultSubstructureSearch = File.createTempFile(DEFAULT_FILE_OUT_NAME, ConstantsDWAR.DWAR_EXTENSION);
		
		fiResultSubstructureSearch.delete();
		
	}
	
	public void clearLists(){
		liMolIncludeExclude.clear();
	}
	
	public void addInclude(StereoMolecule molInclude, StereoMolecule molExclude){
		
		if(molInclude==null){
			throw new RuntimeException("No include molecule given.");
		}
		
		liMolIncludeExclude.add(new TwoMolecules(molInclude, molExclude));
		
	}
	
	public StereoMolecule getSterMolQuery() {
		return sterMolQuery;
	}
	
	public void setSterMolQuery(StereoMolecule sterMolQuery) {
		this.sterMolQuery = sterMolQuery;
	}

	public void setLog(LogHandler log) {
		this.log = log;
	}
	
	public LogHandler getLog() {
		return log;
	}
	
	public boolean isFragment() {
		return sterMolQuery.isFragment();
	}
	
	public void setFragment(boolean structureIsFragment) {
		sterMolQuery.setFragment(structureIsFragment);
	}
	
	public List<TwoMolecules> getListIncludeExclude() {
		return liMolIncludeExclude;
	}
	
	public File getFileResultSubstructureSearch() {
		return fiResultSubstructureSearch;
	}


	public void setFileResultSubstructureSearch(File fiOut) {
		this.fiResultSubstructureSearch = fiOut;
	}

	public boolean isRunning() {
		return running;
	}


	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isAddSupplierInfo() {
		return addSupplierInfo;
	}

	public void setAddSupplierInfo(boolean addSupplierInfo) {
		this.addSupplierInfo = addSupplierInfo;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String sourceTxt) {
		this.source = sourceTxt;
	}

	public int getAccepted() {
		return ccAccepted;
	}

	public int getNotFound() {
		return ccNotFound;
	}

	public int getExcluded() {
		return ccExcluded;
	}

	public int getProcessed() {
		return ccProcessed;
	}
	
	public File getWorkDir() {
		return workDir;
	}

	public void setWorkDir(File workDir) {
		this.workDir = workDir;
	}

	public File getFileIncludeExcludeDWAR() {
		return fiIncludeExcludeDWAR;
	}

	public void setFileIncludeExcludeDWAR(File fiIncludeExcludeDWAR) {
		this.fiIncludeExcludeDWAR = fiIncludeExcludeDWAR;
	}

	
}
