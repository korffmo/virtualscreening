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

package org.openchemlib.chem.vs;

import org.openchemlib.chem.descriptor.DescriptorHandlerExtendedFactory;
import org.openchemlib.chem.descriptor.GenDescriptorMulticore;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandler;
import com.actelion.research.chem.dwar.DWARFileHandler;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.chem.dwar.toolbox.export.ConvertString2PheSA;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.Formatter;
import com.actelion.research.util.IO;
import com.actelion.research.util.UserDirsDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openchemlib.chem.vs.test.ConstantsVSTest;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GenDescriptorMulticorePhESATest
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 04.03.20.
 */

public class GenDescriptorMulticorePhESATest {


    private static final double [] ARR_SIM = {0.5, 0.5, 0.5};
    private static final double MARGIN = 0.2;
    private static final int RECORDS = 3;
    private static GenDescriptorMulticorePhESATest genDescriptorMulticorePhESATest;

//    public static void main(String[] args) throws Exception {
//        GenDescriptorMulticorePhESATest genDescriptorMulticorePhESATest = new GenDescriptorMulticorePhESATest();
//
//        genDescriptorMulticorePhESATest.test1();
//    }

    @BeforeAll
    public static void setup() {
        genDescriptorMulticorePhESATest = new GenDescriptorMulticorePhESATest();
    }

    @Test
    public void test1() throws Exception {

        String strWorkdir = UserDirsDefault.getTmp(0);
        File workdir = new File(strWorkdir);

        String nameQuery = IO.getBaseName(new File(ConstantsVSTest.PATH_QUERY));

        URL urlPathQuery = ConstantsVSTest.class.getClassLoader().getResource(ConstantsVSTest.PATH_QUERY);


        // File fiDWARQuery = new File(workdir, nameQuery + ConstantsDWAR.DWAR_EXTENSION);
        File fiDWARQuery = new File(urlPathQuery.toURI());

        File fiDWARQueryDescriptor = new File(workdir, nameQuery + "_PheSA" + ConstantsDWAR.DWAR_EXTENSION);


//        ClassLoader classLoader = getClass().getClassLoader();
//        InputStream is = classLoader.getResourceAsStream(ConstantsVSTest.PATH_QUERY);
//        String strFileQuery = IO.read(is);
//        IO.write(fiDWARQuery, strFileQuery);



        // File fiDWAR = new File(urlPathQuery.getFile());



        List<String> liDescriptor = Collections.singletonList(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);

        GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);

        List<DescriptorHandler> liDescriptorHandler = DescriptorHandlerExtendedFactory.getFromNames(liDescriptor);

        genDescriptorMulticore.generate(fiDWARQuery, ConstantsDWAR.TAG_IDCODE2, fiDWARQueryDescriptor, liDescriptorHandler);


        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        dhShape.setFlexible(true);

        ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);

        DWARFileHandler fh = new DWARFileHandler(fiDWARQueryDescriptor);

        List<PheSAMolecule> liPheSAMolecule = new ArrayList<>();
        while (fh.hasMore()) {

            DWARRecord record = fh.next();

            try {
                PheSAMolecule pheSAMolecule = convertString2PheSA.getNative(record);

                liPheSAMolecule.add(pheSAMolecule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fh.close();

        int index=0;

        org.junit.Assert.assertEquals("Correct record number", RECORDS, liPheSAMolecule.size());

        for (int i = 0; i < liPheSAMolecule.size(); i++) {

            for (int j = i+1; j < liPheSAMolecule.size(); j++) {

                double sim = dhShape.getSimilarity(liPheSAMolecule.get(i), liPheSAMolecule.get(j));

                System.out.println(i + " " + j + " " + Formatter.format2(sim));

                org.junit.Assert.assertEquals("PheSA similarity in range", ARR_SIM[index], sim, MARGIN);

                index++;
            }
        }

        System.out.println("index " + index);

        int n = liPheSAMolecule.size();
        int similarityComparisons = ((n*n)-n)/2;

        org.junit.Assert.assertEquals("Correct record number", similarityComparisons, index);
    }

    @Test
    public void test2() throws Exception {

        URL urlPathQuery = this.getClass().getClassLoader().getResource(ConstantsVSTest.PATH_LIBRAY);

        File fiDWAR = new File(urlPathQuery.getFile());

        long nMolecules = DWARFileHandlerHelper.getSize(fiDWAR);

        System.out.println("GenDescriptorMulticorePhESATest test2() records " + nMolecules);

        if(nMolecules<3){
            throw new RuntimeException("Test set to small!");
        }

        File fiDWAROut = new File(UserDirsDefault.getTmp(0), IO.getBaseName(fiDWAR.getName()) + "_PheSA" + ConstantsDWAR.DWAR_EXTENSION);

        List<String> liDescriptor = Collections.singletonList(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);

        GenDescriptorMulticore genDescriptorMulticore = new GenDescriptorMulticore(false);

        List<DescriptorHandler> liDescriptorHandler = DescriptorHandlerExtendedFactory.getFromNames(liDescriptor);

        genDescriptorMulticore.generate(fiDWAR, ConstantsDWAR.TAG_IDCODE2, fiDWAROut, liDescriptorHandler);


        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        dhShape.setFlexible(true);

        ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);

        DWARFileHandler fh = new DWARFileHandler(fiDWAROut);

        List<PheSAMolecule> liPheSAMolecule = new ArrayList<>();
        while (fh.hasMore()) {

            DWARRecord record = fh.next();

            try {
                PheSAMolecule pheSAMolecule = convertString2PheSA.getNative(record);

                liPheSAMolecule.add(pheSAMolecule);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fh.close();

        org.junit.Assert.assertEquals("Correct record number", nMolecules, liPheSAMolecule.size());

    }
}
