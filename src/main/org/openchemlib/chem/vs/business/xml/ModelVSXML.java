package org.openchemlib.chem.vs.business.xml;

import com.actelion.research.chem.descriptor.ConstantsDescriptorLists;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import org.openchemlib.chem.vs.business.ConstantsPhESAParameter;
import org.openchemlib.chem.vs.business.ConstantsVS;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.xml.JAXBMarshalServices;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ModelVSXML
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Aug 26, 2013 MvK Start implementation
 */
@XmlRootElement(name="ParameterVS")
@XmlAccessorType(XmlAccessType.FIELD) 
public class ModelVSXML {

	@XmlElement(name="Library")
    private String library;

	@XmlElement(name="Query")
    private String query;
    
	@XmlElement(name="QueryIdentifier")
    private String queryIdentifier;
    
	@XmlElement(name="PathWorkDir")
    private File workDir;

	@XmlElement(name="NameElusiveResultFileDWAR")
	private String nameDWARResultElusive;

	@XmlElement(name="NameSummaryResultFileDWAR")
	private String nameDWARResultSummary;

	@XmlElement(name="CheckForMissingDescriptors")
	private boolean checkForMissingDescriptors;

	@XmlElement(name="AddSupplierInfo")
	private boolean addSupplierInfo;

	@XmlElement(name="HitCombinationWithLogicalOR")
	private boolean hitOr;
	
	@XmlElement(name="StereoDepletion")
	private boolean stereoDepletion;

	@XmlElement(name="ComparisonMode")
	private String comparisonMode;
    
	@XmlElementWrapper(name="Descriptors")
	@XmlElement(name="Descriptor")
    private List<DescriptorXML> liDescriptorsXML;

	@XmlElement(name="NumProcessors")
	private int numProcessors;

	@XmlElement(name="ReUptake")
	private boolean reuptake;

	@XmlElement(name="SkipDescriptorsInOutput")
	private boolean skipDescriptorsInOutput;

	// for debugging purposes on the cloud
	@XmlElement(name="MaxNumberOfJobs")
	private int maxNumBatchJobs;

	// for excluding columns from Cassandra DB output, i.e. PheSA, missing descriptors will be otherwise recalculated
	@XmlElementWrapper(name="PatternExcludeColumnsFromOutputDWAR")
	@XmlElement(name="Pattern")
	private List<String> liPatternExcludeFromOutputDWAR;

	/**
	 * 
	 */
	public ModelVSXML() {
		init();
	}


	private void init(){
		liDescriptorsXML = new ArrayList<>();
		
		comparisonMode = ConstantsVS.COMPARISON_MODE_VS;

		stereoDepletion = false;

		reuptake = false;

		maxNumBatchJobs=0;

		liPatternExcludeFromOutputDWAR = new ArrayList<>();

		skipDescriptorsInOutput = false;
	}
	
	public ModelVSXML(ModelVSXML model) {
		copy(model);
	}

	/**
	 * Copy model into this.
	 * @param model
	 */
	public void copy(ModelVSXML model){
		library = model.getLibrary();

		query = model.getQuery();

		queryIdentifier = model.getQueryIdentifier();

		workDir = model.getWorkDir();

		nameDWARResultElusive = model.getNameDWARResultElusive();

		nameDWARResultSummary = model.getNameDWARResultSummary();

		checkForMissingDescriptors = model.isCheckForMissingDescriptors();

		addSupplierInfo = model.isAddSupplierInfo();

		hitOr = model.isHitOr();

		stereoDepletion = model.isStereoDepletion();

		comparisonMode = model.getComparisonMode();

		liDescriptorsXML = new ArrayList<>(model.getLiDescriptors());

		numProcessors = model.numProcessors;

		reuptake = model.reuptake;

		skipDescriptorsInOutput = model.skipDescriptorsInOutput;

	}

	public String getNameDWARResultElusive() {
		return nameDWARResultElusive;
	}

	public void setNameDWARResultElusive(String nameDWARResultElusive) {
		this.nameDWARResultElusive = nameDWARResultElusive;
	}

	public String getNameDWARResultSummary() {
		return nameDWARResultSummary;
	}

	public void setNameDWARResultSummary(String nameDWARResultSummary) {
		this.nameDWARResultSummary = nameDWARResultSummary;
	}

	public void add(DescriptorXML d){
		liDescriptorsXML.add(d);
	}

	/**
	 * @return the library
	 */
	public String getLibrary() {
		return library;
	}


	/**
	 * @param library
	 *            the library to set
	 */
	public void setLibrary(String library) {
		this.library = library;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @return the queryIdentifier
	 */
	public String getQueryIdentifier() {
		return queryIdentifier;
	}

	/**
	 * @param queryIdentifier the queryIdentifier to set
	 */
	public void setQueryIdentifier(String queryIdentifier) {
		this.queryIdentifier = queryIdentifier;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @return the workDir
	 */
	public File getWorkDir() {
		return workDir;
	}

	/**
	 * @param workDir
	 *            the workDir to set
	 */
	public void setWorkDir(File workDir) {
		this.workDir = workDir;
	}

	/**
	 * @return the addSupplierInfo
	 */
	public boolean isAddSupplierInfo() {
		return addSupplierInfo;
	}

	/**
	 * @param addSupplierInfo
	 *            the addSupplierInfo to set
	 */
	public void setAddSupplierInfo(boolean addSupplierInfo) {
		this.addSupplierInfo = addSupplierInfo;
	}

	/**
	 * @return the hitOr
	 */
	public boolean isHitOr() {
		return hitOr;
	}

	/**
	 * @param hitOr
	 *            the hitOr to set
	 */
	public void setHitOr(boolean hitOr) {
		this.hitOr = hitOr;
	}

	public String getComparisonMode() {
		return comparisonMode;
	}

	public void setComparisonMode(String comparisonMode) {
		this.comparisonMode = comparisonMode;
	}

	/**
	 * @return the liDescriptors
	 */
	public List<DescriptorXML> getLiDescriptors() {
		return liDescriptorsXML;
	}

	/**
	 * @param liDescriptors
	 *            the liDescriptors to set
	 */
	public void setLiDescriptors(List<DescriptorXML> liDescriptors) {
		this.liDescriptorsXML = liDescriptors;
	}

	public boolean isCheckForMissingDescriptors() {
		return checkForMissingDescriptors;
	}

	public void setCheckForMissingDescriptors(boolean checkForMissingDescriptors) {
		this.checkForMissingDescriptors = checkForMissingDescriptors;
	}

	public boolean isStereoDepletion() {
		return stereoDepletion;
	}

	public void setStereoDepletion(boolean stereoDepletion) {
		this.stereoDepletion = stereoDepletion;
	}

	public int getNumProcessors() {
		return numProcessors;
	}

	public void setNumProcessors(int numProcessors) {
		this.numProcessors = numProcessors;
	}

	public boolean isReuptake() {
		return reuptake;
	}

	public void setReuptake(boolean reuptake) {
		this.reuptake = reuptake;
	}

	public boolean isSkipDescriptorsInOutput() {
		return skipDescriptorsInOutput;
	}

	public void setSkipDescriptorsInOutput(boolean skipDescriptorsInOutput) {
		this.skipDescriptorsInOutput = skipDescriptorsInOutput;
	}

	public int getMaxNumBatchJobs() {
		return maxNumBatchJobs;
	}

	public List<String> getLiPatternExcludeFromOutputDWAR() {
		return liPatternExcludeFromOutputDWAR;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ModelVSXML [library=");
		sb.append(library);
		sb.append(", query=");
		sb.append(query);
		sb.append(", queryIdentifier=");
		sb.append(queryIdentifier);
		sb.append(", workDir=");
		sb.append(workDir);
		sb.append(", addSupplierInfo=");
		sb.append(addSupplierInfo);
		sb.append(", hitOr=");
		sb.append(hitOr);
		sb.append(", comparisonMode=");
		sb.append(comparisonMode);
		sb.append(", liDescriptors=");
		sb.append(liDescriptorsXML);
		sb.append(", stereoDepletion=");
		sb.append(stereoDepletion);
		sb.append(", numProcessors=");
		sb.append(numProcessors);
		sb.append(", reuptake=");
		sb.append(reuptake);
		sb.append(", skipDescriptorsInOutput=");
		sb.append(skipDescriptorsInOutput);
		sb.append("]");
		return sb.toString();
	}




	public String toStringXML() throws JAXBException, IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
		JAXBContext jc = JAXBContext.newInstance(ConstantsVSXML.PACKAGE);
		
		Marshaller m = jc.createMarshaller();
		
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
	    m.marshal(this, baos);
	    
	    baos.close();
		
	    return baos.toString();
	}


    public void write(File fi) throws JAXBException, IOException, ClassNotFoundException {

        JAXBMarshalServices<ModelVSXML> jaxbModelGenenames = new JAXBMarshalServices<ModelVSXML>(ModelVSXML.class.getPackage().getName());

        jaxbModelGenenames.write(this, fi);
    }

    public static ModelVSXML get(URL url) throws JAXBException, IOException, SAXException, ParserConfigurationException, ClassNotFoundException {

        JAXBMarshalServices<ModelVSXML> jaxbMarshalServices = new JAXBMarshalServices<ModelVSXML>(ModelVSXML.class.getPackage().getName());

        return jaxbMarshalServices.get(url, true);

    }

    public static ModelVSXML get(String strXML) throws JAXBException, IOException, SAXException, ParserConfigurationException, ClassNotFoundException {

        JAXBMarshalServices<ModelVSXML> jaxbMarshalServices = new JAXBMarshalServices<ModelVSXML>(ModelVSXML.class.getPackage().getName());

        return jaxbMarshalServices.get(strXML);

    }


    public static ModelVSXML getExample() {
		
		ModelVSXML m = new ModelVSXML();
		
		m.setLibrary("/home/username/library.dwar");
		
		m.setQuery("/home/username/query.dwar");
		
		m.setQueryIdentifier("Idorsia No");
		
		m.setWorkDir(new File("/home/username/tmp"));

		m.setNameDWARResultElusive("elusiveResultMyTarget" + ConstantsDWAR.DWAR_EXTENSION);
		m.setNameDWARResultElusive("summaryResultMyTarget" + ConstantsDWAR.DWAR_EXTENSION);

		m.setCheckForMissingDescriptors(true);

		m.setStereoDepletion(false);
		
		for (int i = 0; i < ConstantsDescriptorLists.ARR.length; i++) {
			DescriptorXML d = new DescriptorXML(ConstantsDescriptorLists.ARR[i], "", 0.85, true);
			m.add(d);
		}

		m.numProcessors = 0;


		String parameterPhESA = ConstantsPhESAParameter.ATTR_FLEXIBLE_ALIGN + ConstantsPhESAParameter.SEP + ConstantsPhESAParameter.TAG_PHARMACOPHORE_WEIGHT+"=0.6";
		DescriptorXML d = new DescriptorXML(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName, parameterPhESA, 0.85, true);
		m.add(d);

		
		m.add(new DescriptorXML(DescriptorConstants.DESCRIPTOR_SUBSTRUCT_QUERY_IN_BASE.shortName, "", 0.9, true));
		m.add(new DescriptorXML(DescriptorConstants.DESCRIPTOR_MAX_COMMON_SUBSTRUCT.shortName, "", 0.9, true));

		m.skipDescriptorsInOutput=true;

		m.liPatternExcludeFromOutputDWAR.add(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);
		m.liPatternExcludeFromOutputDWAR.add(DescriptorConstants.DESCRIPTOR_Flexophore.shortName);

		return m;
	}

}
