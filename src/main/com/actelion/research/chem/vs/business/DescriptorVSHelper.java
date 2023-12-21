/*
 * Copyright (c) 2020.
 * Idorsia Pharmaceuticals Ltd., Hegenheimermattweg 91, CH-4123 Allschwil, Switzerland
 *
 *  This file is part of DataWarrior.
 *
 *  DataWarrior is free software: you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  DataWarrior is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with DataWarrior.
 *  If not, see http://www.gnu.org/licenses/.
 *
 *  @author Modest v. Korff
 *
 */

package com.actelion.research.chem.vs.business;

import com.actelion.research.chem.descriptor.*;
import com.actelion.research.chem.descriptor.scaffold.DescriptorHandlerSimpleSubStructures;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.vs.business.xml.DescriptorXML;

import java.util.ArrayList;
import java.util.List;

/**
 * DescriptorVSHelper
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 24.09.20.
 */
public class DescriptorVSHelper {

    public static List<ModelDescriptorVS> getEnabled(List<DescriptorXML> liDescriptorsXML){

        List<ModelDescriptorVS> liDescriptors = new ArrayList<>();

        for (DescriptorXML descriptorXML : liDescriptorsXML) {

            if(!descriptorXML.isEnable()){
                continue;
            }

            ISimilarityCalculator similarityCalculator = null;

            if(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName.equals(descriptorXML.getName())) {

                String parameter = descriptorXML.getParameter();

                if(parameter != null && parameter.length()>0){
                    PhESAParameter phESAParameter = new PhESAParameter(parameter);

                    if(!Double.isNaN(phESAParameter.getPharmacophoreWeight())){
                        similarityCalculator = new DescriptorHandlerShape(phESAParameter.getPharmacophoreWeight());
                    } else {
                        similarityCalculator = new DescriptorHandlerShape();
                    }

                    if(phESAParameter.isFlexibleAlign()){
                        ((DescriptorHandlerShape)similarityCalculator).setFlexible(true);
                    }


                } else {
                    similarityCalculator = new DescriptorHandlerShape();
                }

            } else {
                similarityCalculator = DescriptorHandlerExtendedFactory.getFactory().create(descriptorXML.getName());
            }

            if(similarityCalculator==null){
                similarityCalculator = SimilarityCalculatorStandardFactory.getInstance().create(descriptorXML.getName());
            }

            if(similarityCalculator==null) {
                throw new RuntimeException("Unknown descriptor tag '" + descriptorXML.getName() + "'.");
            }


            ModelDescriptorVS modelSimCalc = new ModelDescriptorVS(similarityCalculator, descriptorXML.getThreshold(), descriptorXML.getParameter(), descriptorXML.isEnable());

            liDescriptors.add(modelSimCalc);
        }

        return liDescriptors;
    }
}
