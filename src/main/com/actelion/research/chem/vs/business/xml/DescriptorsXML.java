package com.actelion.research.chem.vs.business.xml;

import com.actelion.research.chem.descriptor.vs.DescriptorVS;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * DescriptorsXML
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Aug 27, 2013 MvK Start implementation
 */
@XmlRootElement(name="Descriptors")
@XmlAccessorType(XmlAccessType.FIELD) 
public class DescriptorsXML {
	
	
	@XmlElement(name="Descriptor")
    private List<DescriptorXML> liDescriptorsXML;

	/**
	 * 
	 */
	public DescriptorsXML() {
		liDescriptorsXML = new ArrayList<DescriptorXML>();
	}
	
	public void add(DescriptorXML d){
		liDescriptorsXML.add(d);
	}
	
	
	public List<ModelDescriptorVS> getHandler() {

        return DescriptorXML.getHandler(liDescriptorsXML);
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

	public void write(File fi) throws JAXBException, IOException {
		JAXBContext jc = JAXBContext.newInstance(ConstantsVSXML.PACKAGE);
		
		Marshaller m = jc.createMarshaller();
		
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		OutputStream os = new FileOutputStream(fi);
		
	    m.marshal(this, os);
	    
		os.close();
		
	}


	public static DescriptorsXML get(URL url) throws JAXBException, IOException {
		
		JAXBContext jaxbContext = JAXBContext.newInstance(ConstantsVSXML.PACKAGE);
		
		Unmarshaller m = jaxbContext.createUnmarshaller();
		
		return (DescriptorsXML) m.unmarshal(url.openStream());
	}




}
