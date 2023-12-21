
package com.actelion.research.chem.vs.business.xml;

import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 * DescriptorXML
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Aug 26, 2013 MvK Start implementation
 */
@XmlAccessorType(XmlAccessType.FIELD) 
public class DescriptorXML {

	@XmlAttribute(name="descriptor")
	private String name;
	
	@XmlAttribute(name="parameter")
	private String parameter;
	
	@XmlAttribute(name="minimumsimilarity")
	private double threshold;
	
	@XmlAttribute(name="enabled")
	private boolean enable;
	
	
	
	/**
	 * enable has to be true, otherwise the default will be false. This is misleading if the parameter are read from xml.
	 */
	public DescriptorXML() {
		enable = true;
	}
	
	
	
	/**
	 * @param name
	 * @param parameter
	 * @param threshold
	 * @param enable
	 */
	public DescriptorXML(String name, String parameter, double threshold, boolean enable) {
		this.name = name;
		this.parameter = parameter;
		this.threshold = threshold;
		this.enable = enable;
	}

	/**
	 * 
	 */
	public DescriptorXML(ModelDescriptorVS modelDescriptorVS) {
		
		name = modelDescriptorVS.getSimilarityCalculator().getInfo().shortName;
		
		threshold = modelDescriptorVS.getSimilarityThreshold();
		
		enable = modelDescriptorVS.isEnabled();
		
	}

	public String getName() {
		return name;
	}

	public String getParameter() {
		return parameter;
	}

	public double getThreshold() {
		return threshold;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isEnable() {
		return enable;
	}

	public static List<ModelDescriptorVS> getHandler(List<DescriptorXML> liDescriptorsXML) {

		List<ModelDescriptorVS> liDescriptors = new ArrayList<>();

		for (DescriptorXML descriptorXML : liDescriptorsXML) {

			DescriptorHandler dh = DescriptorHandlerExtendedFactory.getFactory().create(descriptorXML.getName());

			ModelDescriptorVS modelDescriptorVS = new ModelDescriptorVS(dh, descriptorXML.getThreshold(), descriptorXML.getParameter(), descriptorXML.isEnable());

			liDescriptors.add(modelDescriptorVS);
		}

		return liDescriptors;
	}


	public static List<DescriptorXML> convert(List<ModelDescriptorVS> li) {
		List<DescriptorXML> liXML = new ArrayList<>();

		for (ModelDescriptorVS modelDescriptorVS : li) {
			liXML.add(new DescriptorXML(modelDescriptorVS));
		}

		return liXML;
	}

}
