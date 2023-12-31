/*
 * Copyright (c) 2020.

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

package com.actelion.research.chem.vs;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.descriptor.ISimilarityCalculator;
import com.actelion.research.chem.descriptor.SubstructureQueryInBase;
import com.actelion.research.chem.dwar.DWARHeader;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.util.ConstantsDWAR;

/**
 * CheckHeaderForNeededColumns
 * Checks the needs for the descriptors and substructure search.
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 30.01.20.
 */
public class CheckHeaderForNeededColumns {

    private DWARHeader header;

    private StringBuilder sbMessage;

    public CheckHeaderForNeededColumns() {
        sbMessage = new StringBuilder();
    }

    public void setHeader(DWARHeader header) {
        this.header = header;
    }

    public boolean check(ISimilarityCalculator similarityCalculator){

        boolean ok = true;

        sbMessage = new StringBuilder();

        if(similarityCalculator instanceof SubstructureQueryInBase) {

            if(!header.contains(ConstantsDWAR.TAG_IDCODE2)){
                ok = false;
                sbMessage.append("Missing header tag '" + ConstantsDWAR.TAG_IDCODE2 + "' in dwar file.");
            }

            if(!header.contains(DescriptorConstants.DESCRIPTOR_PFP512.shortName)){
                ok = false;
                if(sbMessage.length()>0){
                    sbMessage.append("\n");
                }

                sbMessage.append("Missing header tag '" + DescriptorConstants.DESCRIPTOR_PFP512.shortName + "' in dwar file.");
                sbMessage.append(" Descriptor is needed for substructure search.");
            }


        } else if(similarityCalculator instanceof DescriptorHandlerShape){

            DescriptorHandlerShape descriptorHandlerShape = (DescriptorHandlerShape)similarityCalculator;

            String tagPheSA = descriptorHandlerShape.getInfo().shortName;

            String tagCoord3D = header.getAttributeFromProperty(DWARHeader.COLPROP_SPTYPE, ConstantsDWAR.TAG_COOR3D);

            if(!header.contains(tagPheSA) && !header.contains(ConstantsDWAR.TAG_CONFORMERSET) && (tagCoord3D==null)) {
                ok = false;

                if(sbMessage.length()>0){
                    sbMessage.append("\n");
                }

                sbMessage.append("At least one of the three header tags " + tagPheSA + ", " + ConstantsDWAR.TAG_CONFORMERSET + ", or " + ConstantsDWAR.TAG_COOR3D + " is needed for the PheSA descriptor.");
            }


        } else if(similarityCalculator instanceof DescriptorHandler){

            @SuppressWarnings("rawtypes")
            DescriptorHandler dh = (DescriptorHandler)similarityCalculator;

            String tag = dh.getInfo().shortName;

            if(!header.contains(tag)){
                ok = false;

                if(sbMessage.length()>0){
                    sbMessage.append("\n");
                }

                sbMessage.append("Missing descriptor header tag '" +tag + "' in dwar file.");

            }

        }

        return ok;

    }


    public String getMessage() {
        return sbMessage.toString();
    }
}
