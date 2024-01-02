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

package org.openchemlib.chem.vs.business;

import com.actelion.research.chem.descriptor.DescriptorHandler;
import org.openchemlib.chem.descriptor.DescriptorHandlerExtendedFactory;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * ConstantsVS
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 24.09.20.
 */
public class ConstantsVS {

    public static final String COMPARISON_MODE_VS = "VS";
    public static final String COMPARISON_MODE_LibComp = "LibComp";
    public static final SimpleDateFormat DATEFORMAT_YMD = new SimpleDateFormat("yyyyMMdd");
    public static final String PREFIX_QUERY = "Query";
    public static final String NAME_VSRESULT = "VSResult";
    public static final String PREFIX_EVALUATED = "Evaluated";
    public static final String FILE_PARAMETER_VS = "parameterVS.xml";
    public static final String FILE_PARAMETER_LIBCOMP = "parameterLibComp.xml";
    public static final String FILE_PARAMETER_LIBCOMP_OPTISIM = "parameterLibCompOptiSim.xml";
    public static final String FILE_PARAMETER_SUBSTRUCTURE_SEARCH = "parameterSubstructureSearch.xml";
    public static final String SUBFOLDER_IDORSIA = ".idorsia";
    public static final String SUBFOLDER_VS = "VirtualScreening";
    public static final String RESOURCE_DESCRIPTORS_LIBCOMP = "/resources/parametersDescriptorsLibComp.xml";
    public static final String RESOURCE_DESCRIPTORS_VS = "/resources/parametersDescriptorsVS.xml";



    public static File getPath(String filename){
        String sHomeParameter = System.getProperty("user.home") + "/" + SUBFOLDER_IDORSIA + "/" + SUBFOLDER_VS;
        File dirParameter = new File(sHomeParameter);
        dirParameter.mkdirs();
    	File fiXML = new File(dirParameter, filename);
    	return fiXML;

    }

    public static List<ModelDescriptorVS> getDescriptor(List<String> liDescriptorShortName, double thresh){
        List<ModelDescriptorVS> li = new ArrayList<>();
        for (int i = 0; i < liDescriptorShortName.size(); i++) {
            DescriptorHandler dh = DescriptorHandlerExtendedFactory.getFactory().create(liDescriptorShortName.get(i));
            ModelDescriptorVS md = new ModelDescriptorVS(dh,thresh);
            li.add(md);
        }
        return li;
    }
}
