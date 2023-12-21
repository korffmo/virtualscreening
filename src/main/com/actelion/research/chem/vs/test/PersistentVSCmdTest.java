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

package com.actelion.research.chem.vs.test;

import com.actelion.research.chem.descriptor.vs.VSResultArray;
import com.actelion.research.chem.dwar.DWARFileHandler;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.disk.sort.DiskSortDWAR;
import com.actelion.research.chem.vs.business.VSParallel;
import com.actelion.research.chem.vs.business.VSResultHandler;
import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * PersistentVSCmdTest
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 28.09.20.
 */
public class PersistentVSCmdTest {

    public static void main(String[] args) throws Exception {
        sortSpeed();
    }



    public static void reUptake() throws Exception {

        int nCompareFirstResults = 100;

        File dirData = new File("/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/dataResilientVSCmd/small");
        File workdir = new File(UserDirsDefault.getTmp(1));
        FileUtils.deleteDirContent(workdir);

        ModelVSTest modelVSTest = new ModelVSTest(dirData, workdir);

        DWARFileHandlerHelper.deleteFooter(modelVSTest.fiResult);

        damageRowLoggingFile(modelVSTest.fiRowLog);

        File fiDWARResultElusiveTmp = new File(workdir, modelVSTest.nameResultElusive);

        ModelVSXML modelVSXML = ModelVSXML.get(modelVSTest.fiParameterXML.toURI().toURL());

        modelVSXML.setReuptake(true);

        VSParallel vs = new VSParallel(modelVSXML);

        vs.run();

        if(DWARFileHandlerHelper.equal(modelVSTest.fiDWARResultElusiveOrig, fiDWARResultElusiveTmp, nCompareFirstResults)){
            System.out.println("Results are equal.");
        }else{
            System.err.println("Results are not equal!");
        }
    }
    public static void deleteAllAbove() throws IOException {

        long idQuery=4;
        long idBase=0;

        File dirData = new File("/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/dataResilientVSCmd/small");
        File dirResult = new File(dirData, "results");

        File workdir = new File(UserDirsDefault.getTmp(1));
        FileUtils.deleteDirContent(workdir);

        Properties properties = new Properties();
        File fiPropertiesOrig = new File(dirResult, VSParallel.FILE_NAME_PROPERTIES);
        File fiProperties = new File(workdir, fiPropertiesOrig.getName());
        FileUtils.copy(fiPropertiesOrig, fiProperties);

        properties.load(new FileReader(fiPropertiesOrig));

        File fiResultOrig = new File(dirResult, new File(properties.getProperty(VSParallel.PROPERTY_FILE_RESULT)).getName());
        File fiResult = new File(workdir, fiResultOrig.getName());
        FileUtils.copy(fiResultOrig, fiResult);


        DWARFileHandlerHelper.deleteFooter(fiResult);

        VSResultHandler.deleteAllAbove(fiResult, idQuery, idBase);

    }

    private static void damageResultDWARFile(File fiResult) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(fiResult, "rw");

        String line = null;
        while ((line=raf.readLine())!=null){
            if(DWARFileHandler.TAG_DW_FILEINFO_END.equals(line)){
                break;
            }
        }

        // Header line
        raf.readLine();

        long pos = raf.getFilePointer();

        Random random = new Random();

        long len = raf.length();

        long lenNew = (long)((len - pos) * (random.nextDouble()) + pos);

        raf.setLength(lenNew);

        raf.close();

        System.out.println("Result file length " + len);
        System.out.println("Result file new length " + lenNew);

    }

    private static void damageRowLoggingFile(File fiRowLog) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(fiRowLog, "rw");

        int offset=2;

        String line = null;
        while ((line=raf.readLine())!=null){
            if(DWARFileHandler.TAG_DW_FILEINFO_END.equals(line)){
                break;
            }
        }

        for (int i = 0; i < offset; i++) {
            raf.readLine();
        }

        long pos = raf.getFilePointer();

        Random random = new Random();

        long len = raf.length();

        long lenNew = (long)((len - pos) * (random.nextDouble()) + pos);

        raf.setLength(lenNew);

        raf.close();

        System.out.println("Row log file length " + len);
        System.out.println("Row log file new length " + lenNew);

    }



    public static void compareDWARFiles() throws Exception {

        File dirData = new File("/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/dataResilientVSCmd");

        int nCompareFirstResults = 100;

        File workdir = new File(UserDirsDefault.getTmp(1));
        String nameResultElusive = "vsResultElusive.dwar";

        File fiDWARResultElusiveOrig = new File(dirData, nameResultElusive);
        File fiDWARResultElusiveTmp = new File(workdir, nameResultElusive);

        if(DWARFileHandlerHelper.equal(fiDWARResultElusiveOrig, fiDWARResultElusiveTmp, nCompareFirstResults)){
            System.out.println("Results are equal.");
        }else{
            System.err.println("Results are not equal!");
        }
    }

    public static void deleteFooter() throws IOException, NoSuchFieldException {

        // String f = "/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/vsResultNoFooterDamaged.dwar";
        String f = "/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/vsResultFooterDamaged.dwar";

        File fiDWAROrig = new File(f);

        File dir = new File(UserDirsDefault.getTmp(3));

        File fiDWAR = new File(dir, fiDWAROrig.getName());
        fiDWAR.delete();

        FileUtils.copy(fiDWAROrig, fiDWAR);

        DWARFileHandlerHelper.deleteFooter(fiDWAR);

        long rowsOrig = DWARFileHandlerHelper.countRows(fiDWAROrig);
        long rows = DWARFileHandlerHelper.countRows(fiDWAR);

        System.out.println("Rows " + rows + " rowsOrig " + rowsOrig);

    }

//    public static void main(String[] args) throws IOException, NoSuchFieldException {
//
//        String f = "/home/korffmo1/tmp/tmp03/vsResultOriginal.dwar";
//
//        File fiDWAROrig = new File(f);
//
//        File fiDWAR = new File(fiDWAROrig.getParent(), IO.getBaseName(fiDWAROrig.getName()) + "Cpy" + ConstantsDWAR.DWAR_EXTENSION);
//
//        fiDWAR.delete();
//        FileUtils.copy(fiDWAROrig, fiDWAR);
//
//        FileUtils.deleteAllAfterLastLineFeed(fiDWAR);
//
//        DWARHeader header = DWARFileHandlerHelper.getDWARHeader(fiDWAR);
//
//        List<String> liTag = header.get();
//
//        String line = FileUtils.tail(fiDWAR);
//
//        String [] arrLine = line.split("\t");
//
//        DWARRecord record = new DWARRecord(header);
//        for (int i = 0; i < liTag.size(); i++) {
//            record.addOrReplaceField(liTag.get(i), arrLine[i]);
//        }
//
//        System.out.println(DWARFunctions.toString(record));
//
//
//
//    }

    public static void deleteAfterLastLineFeed() throws IOException, NoSuchFieldException {

        String f = "/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/dataResilientVSCmd/small/rowLoggingVS.txt";

        File fiOrig = new File(f);

        File fi = new File(UserDirsDefault.getTmp(3), fiOrig.getName());

        fi.delete();
        FileUtils.copy(fiOrig, fi);

        FileUtils.deleteAllAfterLastLineFeed(fi);

        List<String> li = FileUtils.readIntoList(fi);

        for (String s : li) {
            System.out.println(s);
        }


    }

    private static class ModelVSTest {

        File dirData;
        File dirResult;
        File fiParameterXML;
        File workdir;
        Properties properties;
        File fiProperties;
        File fiResult;
        File fiRowLog;

        String nameResultElusive;

        File fiDWARResultElusiveOrig;

        public ModelVSTest(File dirData, File workdir) throws IOException {
            this.dirData = dirData;
            this.workdir = workdir;

            dirResult = new File(dirData, "results");

            fiParameterXML = new File(dirData, "paramsVSTestResilient.xml");

            properties = new Properties();
            File fiPropertiesOrig = new File(dirResult, VSParallel.FILE_NAME_PROPERTIES);

            fiProperties = new File(workdir, fiPropertiesOrig.getName());
            FileUtils.copy(fiPropertiesOrig, fiProperties);

            properties.load(new FileReader(fiPropertiesOrig));

            File fiResultOrig = new File(dirResult, new File(properties.getProperty(VSParallel.PROPERTY_FILE_RESULT)).getName());
            fiResult = new File(workdir, fiResultOrig.getName());
            FileUtils.copy(fiResultOrig, fiResult);

            File fiRowLogOrig = new File(dirResult, VSParallel.FILE_NAME_ROW_LOGGING);
            fiRowLog = new File(workdir, fiRowLogOrig.getName());
            FileUtils.copy(fiRowLogOrig, fiRowLog);

            nameResultElusive = "vsResultElusive.dwar";

            fiDWARResultElusiveOrig = new File(dirResult, nameResultElusive);
        }
    }


    public static void sortSpeed() throws IOException, NoSuchFieldException {
        File fiDWARVSResultOut = new File("/home/korffmo1/Projects/Software/Development/VirtualScreening/VirtualScreening/VSCmd/dataResilientVSCmd/library1600000/results/vsResult2831299468748511999.dwar");

        File workdir = new File(UserDirsDefault.getTmp(3));
        DiskSortDWAR externalSortDWAR = new DiskSortDWAR(workdir);

        // Sort vs results by base id (row number).
        File fiDWARVSResultOutSorted = new File(workdir, IO.getBaseName(fiDWARVSResultOut) + "Sorted" + ConstantsDWAR.DWAR_EXTENSION);

        long t0 = new Date().getTime();
        externalSortDWAR.sort(fiDWARVSResultOut, VSResultArray.TAG_BASE_ID, fiDWARVSResultOutSorted);
        long d = new Date().getTime() - t0;

        System.out.println("Sort duration " + TimeDelta.toString(d));


    }
}
