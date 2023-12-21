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

import com.actelion.research.chem.descriptor.DescriptorConstants;

/**
 * ConstantsPhESAParameter
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 04.03.20.
 */
public class ConstantsPhESAParameter {

    // result[0]: PheSASimilarity
    // r[1]: PPSimilarity
    // r[2]: ShapeSimilarity
    // r[3]: AdditionalVolumeContribution
    // wäre perfekt, wenn die im DWAR Output so heissen würden!


    // public static final String TAG_PHARMACOPHORE_SIMILARITY = DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName + "Similarity";
    public static final String TAG_PP_SIMILARITY = DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName + "PPSimilarity";
    public static final String TAG_SHAPE_SIMILARITY = DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName + "ShapeSimilarity";
    public static final String TAG_VOLUME_CONTRIBUTION = DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName + "AdditionalVolumeContribution";

    public static final String SEP = ";";

    public static final String ATTR_FLEXIBLE_ALIGN = "flexalign";
    public static final String TAG_PHARMACOPHORE_WEIGHT = "pharmweigh";

}
