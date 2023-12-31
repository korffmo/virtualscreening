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

/**
 * PhESAParameter
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 04.03.20.
 */
public class PhESAParameter {

    private boolean flexibleAlign;
    private double pharmacophoreWeight;

    public PhESAParameter(String parameter) {

        pharmacophoreWeight = Double.NaN;

        String [] arr = parameter.split(ConstantsPhESAParameter.SEP);

        for (String s : arr) {
            if(ConstantsPhESAParameter.ATTR_FLEXIBLE_ALIGN.equals(s)){
                flexibleAlign = true;
            } else if(s.startsWith(ConstantsPhESAParameter.TAG_PHARMACOPHORE_WEIGHT)){
                pharmacophoreWeight = Double.parseDouble(s.split("=")[1].trim());

                if(pharmacophoreWeight <0 || pharmacophoreWeight > 1){
                    throw new RuntimeException("PhESA pharmacophore weight of limits " + pharmacophoreWeight + "!");
                }

            } else {
                throw new RuntimeException("Unknown parameter string for PhESA '" + s + "'.");
            }
        }
    }

    public boolean isFlexibleAlign() {
        return flexibleAlign;
    }

    public double getPharmacophoreWeight() {
        return pharmacophoreWeight;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhESAParameter{");
        sb.append("flexibleAlign=").append(flexibleAlign);
        sb.append(", pharmacophoreWeight=").append(pharmacophoreWeight);
        sb.append('}');
        return sb.toString();
    }


    public static void main(String[] args) {

        String parameterPhESA = ConstantsPhESAParameter.ATTR_FLEXIBLE_ALIGN + ConstantsPhESAParameter.SEP + ConstantsPhESAParameter.TAG_PHARMACOPHORE_WEIGHT+"=0.6";

        PhESAParameter phESAParameter = new PhESAParameter(parameterPhESA);

        System.out.println(phESAParameter.toString());

    }
}
