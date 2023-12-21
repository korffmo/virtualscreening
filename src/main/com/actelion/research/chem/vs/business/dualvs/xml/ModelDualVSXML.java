package com.actelion.research.chem.vs.business.dualvs.xml;

import com.actelion.research.chem.descriptor.ConstantsDescriptorLists;
import com.actelion.research.chem.vs.business.xml.DescriptorXML;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ModelDualVSXML
 * <p>Copyright: Actelion Pharmaceuticals Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * Created by korffmo1 on 04.04.16.
 */
@XmlRootElement(name="ParameterDualVS")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModelDualVSXML {

    public static final String EXTENSION = ".xml";

    public static final String BASE_NAME = "parameterDualVirtualScreening";

    protected static final String PACKAGE = "com.actelion.research.chem.vs.simple.dualvs.xml";

    @XmlElement(name="Library")
    private String library;

    @XmlElement(name="Samples2Include")
    private String samples2Include;

    @XmlElement(name="Samples2Exclude")
    private String samples2Exclude;

    @XmlElement(name="PathWorkDir")
    private File workDir;

    //
    // For performance testing
    //
    @XmlElement(name="SimpleVSMode")
    private boolean simpleVSMode;

    @XmlElementWrapper(name="DescriptorsForSamples2Include")
    @XmlElement(name="Descriptor")
    private List<DescriptorXML> liDescriptorsXMLInclude;

    @XmlElementWrapper(name="DescriptorsForSamples2Exclude")
    @XmlElement(name="Descriptor")
    private List<DescriptorXML> liDescriptorsXMLExclude;


    /**
     *
     */
    public ModelDualVSXML() {
        init();
    }

    private void init(){
        liDescriptorsXMLInclude = new ArrayList<DescriptorXML>();
        liDescriptorsXMLExclude = new ArrayList<DescriptorXML>();

        simpleVSMode = false;


    }

    public void addDescriptorForSamples2Include(DescriptorXML d){
        liDescriptorsXMLInclude.add(d);
    }
    public void addDescriptorForSamples2Exclude(DescriptorXML d){
        liDescriptorsXMLExclude.add(d);
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


    public String getSamples2Include() {
        return samples2Include;
    }

    public void setSamples2Include(String samples2Include) {
        this.samples2Include = samples2Include;
    }

    public String getSamples2Exclude() {
        return samples2Exclude;
    }

    public void setSamples2Exclude(String samples2Exclude) {
        this.samples2Exclude = samples2Exclude;
    }

    public boolean isSimpleVSMode() {
        return simpleVSMode;
    }

    public void setSimpleVSMode(boolean simpleVSMode) {
        this.simpleVSMode = simpleVSMode;
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
    /**
     * @return the liDescriptors
     */
    public List<DescriptorXML> getDescriptorForSamples2Include() {
        return liDescriptorsXMLInclude;
    }
    public List<DescriptorXML> getDescriptorForSamples2Exclude() {
        return liDescriptorsXMLExclude;
    }

    /**
     * @param liDescriptors
     *            the liDescriptors to set
     */
    public void setDescriptorForSamples2Include(List<DescriptorXML> liDescriptors) {
        this.liDescriptorsXMLInclude = liDescriptors;
    }

    public void setDescriptorForSamples2Exclude(List<DescriptorXML> liDescriptors) {
        this.liDescriptorsXMLExclude = liDescriptors;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ModelVSXML [library=");
        sb.append(library);
        sb.append(", samples2Include=");
        sb.append(samples2Include);
        sb.append(", samples2Exclude=");
        sb.append(samples2Exclude);
        sb.append(", workDir=");
        sb.append(workDir.getAbsolutePath());
        sb.append(", liDescriptorsXMLInclude=");
        sb.append(liDescriptorsXMLInclude);
        sb.append(", liDescriptorsXMLExclude=");
        sb.append(liDescriptorsXMLExclude);
        sb.append("]");
        return sb.toString();
    }


    public String toStringXML() throws JAXBException, IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JAXBContext jc = JAXBContext.newInstance(PACKAGE);

        Marshaller m = jc.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        m.marshal(this, baos);

        baos.close();

        return baos.toString();
    }

    public void write(File fi) throws JAXBException, IOException {
        JAXBContext jc = JAXBContext.newInstance(PACKAGE);

        Marshaller m = jc.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        OutputStream os = new FileOutputStream(fi);

        m.marshal(this, os);

        os.close();

    }

    public static ModelDualVSXML get(URL url) throws JAXBException, IOException {

        JAXBContext jaxbContext = JAXBContext.newInstance(PACKAGE);

        Unmarshaller m = jaxbContext.createUnmarshaller();

        return (ModelDualVSXML) m.unmarshal(url.openStream());
    }

    public static ModelDualVSXML getExample() throws JAXBException, IOException {

        ModelDualVSXML m = new ModelDualVSXML();

        m.setLibrary("/home/username/library.dwar");

        m.setSamples2Include("/home/username/activesTargetXYZ.dwar");

        m.setSamples2Exclude("/home/username/inactivesTargetXYZ.dwar");

        m.setWorkDir(new File("/home/username/tmp"));

        m.setSimpleVSMode(false);

        for (int i = 0; i < ConstantsDescriptorLists.ARR.length; i++) {

            DescriptorXML d = new DescriptorXML(ConstantsDescriptorLists.ARR[i], "", 0.85, true);
            m.addDescriptorForSamples2Include(d);
        }

        for (int i = 0; i < ConstantsDescriptorLists.ARR_FP.length; i++) {

            DescriptorXML d = new DescriptorXML(ConstantsDescriptorLists.ARR_FP[i], "", 0.85, true);
            m.addDescriptorForSamples2Exclude(d);
        }

        return m;
    }


}
