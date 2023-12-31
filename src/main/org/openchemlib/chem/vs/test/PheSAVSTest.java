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

package org.openchemlib.chem.vs.test;

import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.conf.ConformerSet;
import com.actelion.research.chem.conf.ConformerSetGenerator;
import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.dwar.DWARFileHandler;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARFileWriterHelper;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.chem.dwar.pipe.DWARInterface2PipelineDWARRecord;
import com.actelion.research.chem.dwar.toolbox.export.ConvertString2PheSA;
import com.actelion.research.chem.dwar.toolbox.export.NativeForSimilarityFromDWARExtractor;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;
import com.actelion.research.util.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PheSAVSTest
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 06.03.20.
 */
public class PheSAVSTest {

    public static void main(String[] args) throws NoSuchFieldException, IOException {
        singleDescriptor();
    }

    public static void single() throws NoSuchFieldException, IOException {

        boolean flexible = false;

        String path = "C:\\Users\\korffmo1\\git\\VirtualScreening\\virtualscreening\\src\\resources\\testdatavs\\phesa\\test01\\";

        String pathLibrary = path + "ACE_100Descriptors.dwar";
        String pathQuery = path + "queryPhESASingleConf.dwar";
//        String pathLibrary = "C:\\Users\\korffmo1\\Projects\\Software\\Development\\VirtualScreening\\VSCmd\\PheSA\\input.dwar";
//        String pathQuery = "C:\\Users\\korffmo1\\Projects\\Software\\Development\\VirtualScreening\\VSCmd\\PheSA\\query_PheSA.dwar";

        File fiDWARLib = new File(pathLibrary);
        File fiDWARQuery = new File(pathQuery);

        int indexLibMol=0;
        int indexQueryMol=0;


        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        dhShape.setFlexible(flexible);

        ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);

        DWARFileHandler fhLib = new DWARFileHandler(fiDWARLib);
        DWARFileHandler fhQuery = new DWARFileHandler(fiDWARQuery);

        System.out.println("Header " + fiDWARLib.getAbsolutePath());
        System.out.println(StringFunctions.toString(fhLib.getHeader()));

        int indexLib=0;
        int indexQuery=0;

        int parsedLib=0;
        int parsedQuery=0;

        IDCodeParser parser = new IDCodeParser();

        while (fhLib.hasMore()){
            DWARRecord recLib = fhLib.next();

            if(indexLib==indexLibMol) {
                System.out.println("Convert lib");
                PheSAMolecule pheSAMoleculeLib = convertString2PheSA.getNative(recLib);

                String idCodeLib = recLib.getIdCode();
                StereoMolecule molLib = parser.getCompactMolecule(idCodeLib);
                molLib.ensureHelperArrays(Molecule.cHelperRings);
                pheSAMoleculeLib = dhShape.createDescriptor(molLib);

                int nPPLib = pheSAMoleculeLib.getVolumes().get(0).getPPGaussians().size();

                while (fhQuery.hasMore()) {
                    DWARRecord recQuery = fhQuery.next();
                    if(indexQuery==indexQueryMol) {
                        System.out.println("Convert query");
                        System.out.println(recQuery.getAsString(DescriptorConstants.DESCRIPTOR_ShapeAlignSingleConf.shortName));

                        PheSAMolecule pheSAMoleculeQuery = convertString2PheSA.getNative(recQuery);
                        int nPPQuery = pheSAMoleculeLib.getVolumes().get(0).getPPGaussians().size();

//                        String idCodeQuery = recQuery.getIdCode();
//                        StereoMolecule molQuery = parser.getCompactMolecule(idCodeQuery);
//                        molQuery.ensureHelperArrays(Molecule.cHelperRings);
//                        pheSAMoleculeQuery = dhShape.createDescriptor(molQuery);


                        try {
                            System.out.println("nPPLib " + nPPLib + ", nPPQuery " + nPPQuery + "\t" + indexLib + "\t" + indexQuery);
                            long n1 = System.nanoTime();
                            System.out.println(recLib.get("index"));
                            System.out.println(recQuery.get("ID"));
                            System.out.println(dhShape.encode(pheSAMoleculeLib));
                            System.out.println(dhShape.encode(pheSAMoleculeQuery));

                            double sim = dhShape.getSimilarity(pheSAMoleculeQuery, pheSAMoleculeLib);
                            long n2 = System.nanoTime();
                            long diffNano = n2 - n1;
                            long diffMS = diffNano / TimeDelta.NANO_MS;
                                System.out.println("Delta t=" + TimeDelta.toString(diffMS) + ".");
                                // System.out.println(ccLib + "\t" + ccQuery + "\t" + Formatter.format3(sim));
                                System.out.println("nPPLib " + nPPLib + ", nPPQuery " + nPPQuery + "\t" + indexLib + "\t" + indexQuery + "\t" + Formatter.format3(sim));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    indexQuery++;

                    parsedQuery++;
                }

                fhQuery.close();
                fhQuery = new DWARFileHandler(fiDWARQuery);
            }
            indexLib++;

            parsedLib++;
        }

        fhLib.close();

    }

    public static void singleDescriptor() throws NoSuchFieldException, IOException {

        boolean flexible = false;

        String path = "C:\\Users\\korffmo1\\git\\VirtualScreening\\virtualscreening\\src\\resources\\testdatavs\\phesa\\test02\\";


        String pathLibrary = path + "actives.dwar";
        String pathQuery = path + "queryPheSA.dwar";

//        String pathLibrary = path + "basePhESADescriptors.dwar";
//        String pathQuery = path + "queryPhESASingleConf.dwar";


//        String pathLibrary = "C:\\Users\\korffmo1\\Projects\\Software\\Development\\VirtualScreening\\VSCmd\\PheSA\\input.dwar";
//        String pathQuery = "C:\\Users\\korffmo1\\Projects\\Software\\Development\\VirtualScreening\\VSCmd\\PheSA\\query_PheSA.dwar";

        File fiDWARLib = new File(pathLibrary);
        File fiDWARQuery = new File(pathQuery);

        int nPPPointsMin=1;

        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        dhShape.setFlexible(flexible);

        ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);

        DWARFileHandler fhLib = new DWARFileHandler(fiDWARLib);
        DWARFileHandler fhQuery = new DWARFileHandler(fiDWARQuery);

        int indexLib=-1;
        int indexQuery=-1;
        int ccCmp=0;
        long sumNano=0;
        while (fhLib.hasMore()){
            DWARRecord recLib = fhLib.next();
            PheSAMolecule pheSAMoleculeLib = convertString2PheSA.getNative(recLib);
            indexLib++;

            int nPPLib = pheSAMoleculeLib.getVolumes().get(0).getPPGaussians().size();

            if(nPPLib<nPPPointsMin)
                continue;


            while (fhQuery.hasMore()) {
                DWARRecord recQuery = fhQuery.next();
                indexQuery++;


                PheSAMolecule pheSAMoleculeQuery = convertString2PheSA.getNative(recQuery);
                int nPPQuery = pheSAMoleculeLib.getVolumes().get(0).getPPGaussians().size();

                if(nPPQuery<nPPPointsMin)
                    continue;

                try {

                    // System.out.println("nPPLib " + nPPLib + ", nPPQuery " + nPPQuery + "\t" + indexLib + "\t" + indexQuery);

                    long n1 = System.nanoTime();
                    double sim = dhShape.getSimilarity(pheSAMoleculeQuery, pheSAMoleculeLib);
                    long n2 = System.nanoTime();
                    long diffNano = n2 - n1;
                    long diffMS = diffNano / TimeDelta.NANO_MS;

                    System.out.println("nPPLib " + nPPLib + ", nPPQuery " + nPPQuery + "\t" + indexLib + "\t" + indexQuery + "\t" + Formatter.format3(sim) );
                    if(diffMS>TimeDelta.MS_SECOND * 10) {
                        System.out.println("Delta t=" + TimeDelta.toString(diffMS) + ".");
                        // System.out.println(ccLib + "\t" + ccQuery + "\t" + Formatter.format3(sim));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ccCmp++;

                if(ccCmp%10==0){
                    System.out.println("Sim calcs " + ccCmp);
                }
            }


            fhQuery.close();
            fhQuery = new DWARFileHandler(fiDWARQuery);

        }

        fhLib.close();

    }


    public static void parallel() throws NoSuchFieldException, IOException {

        File fiDWARQuery = new File("/home/korffmo1/Projects/Software/Development/Descriptors/ShapeAlignment/Errors/20200306_JW/active3.dwar");
        File fiDWARLib = new File("/home/korffmo1/Projects/Software/Development/Descriptors/ShapeAlignment/Errors/20200306_JW/actives_final.dwar");

        List<PheSAMolecule> liQuery = get(fiDWARQuery, 0);

        PheSAMolecule pheSAMoleculeQuery = liQuery.get(0);

        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        dhShape.setFlexible(true);

        DWARInterface2PipelineDWARRecord dwarInterface2PipelineDWARRecord = new DWARInterface2PipelineDWARRecord(fiDWARLib);

        Pipeline<DWARRecord> pipelineDWAR = dwarInterface2PipelineDWARRecord.getPipe();

        int nProcessors = Runtime.getRuntime().availableProcessors();

        if(nProcessors>1){
            nProcessors--;
        }


        ExecutorService executorService = Executors.newFixedThreadPool(nProcessors);

        for (int i = 0; i < nProcessors; i++) {
            executorService.submit(new RunCalcShapeSim(pipelineDWAR, pheSAMoleculeQuery, true));
        }

        executorService.shutdown();

        while (!executorService.isTerminated()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished");
    }

    public static void inOut() throws NoSuchFieldException, IOException {

        String path = "C:\\Users\\korffmo1\\git\\VirtualScreening\\virtualscreening\\src\\resources\\testdatavs\\phesa\\test01\\";
        String pathOut = "C:\\Users\\korffmo1\\tmp\\tmp00";

        String nameFile = "queryPhESASingleConf.dwar";

        File fiQuery = new File(path, nameFile);
        File fiOut = new File(pathOut, nameFile);

        DWARFileWriterHelper.write(fiOut, fiQuery, null);


    }

    //
    // Error was a bug in PheSA single conf
    //
    public static void error20210528() throws NoSuchFieldException, IOException {

        String path = "C:\\Users\\korffmo1\\git\\VirtualScreening\\virtualscreening\\src\\resources\\testdatavs\\phesa\\test01\\";

        String nameFile = "queryPhESASingleConf.dwar";

        String strPheSALib = "49   20  8 0 #13F$3k)Ek+ #33N0JX!E_Kw4iH2Fk1Dl20hCs01#1:V  6 1 #13F$3k)Ek+ #33Pkp9!6w3x6N3QCk0nHLCPDV01#1:V  7 2 #13F$3k)Ek+ #33GyyHk3xmRNAwuNbjzk8VunV8!z!1:F  6 3 #13F$3k)Ek+ #33PnXhF03uI0_aHeqk0umttWQ!1#1:k  6 4 #13F$3k)Ek+ #33Ph3k03Z@UsZMQGnjxS5C1:s!z!1sUk  6 5 #13F$3k)Ek+ #33GSHEk02CHhcSehHkEPhyzo01Twz!1:0  6 6 #13F$3k)Ek+ #33UeheV!jzEM0Dl1k0v7eFEzs01#1OCk  6 7 #13F$3k)Ek+ #33QbgfV04RU0BDMzwk1LU24i@c01#1:V  6 8 #13F$3k)Ek+ #33MakXk3mdqJk4jjdjzkzXjF07zwz!18t0  6 9 #13F$3k)Ek+ #33QdmwV09P0H1g5H@k0CoFjR07rh#1:0  6 10 #13F$3k)Ek+ #33PzAhF!AhCz9YBwk0oW01Azs01$bAk  7 11 #13F$3k)Ek+ #33Qs6aF3S0JBnLOl8jk8gg@o07zx$1A0  6 12 #13F$3k)Ek+ #33TYPuF02xTRmZlqYk0PwtIB07zx$td0  6 13 #13F$3k)Ek+ #33KkxJ!2qxM92rAPk1H_kSfzs01$DLk  6 14 #13F$3k)Ek+ #33NcWEV3ugAYEy_Pujl06Tw7ztSd&k  6 15 #13F$3k)Ek+ #33QJiBk3RThhgapDrjkd3f9J07zx$Wz0  8 16 #13F$3k)Ek+ #33LpqtF3kNXFJWUOojkw0mgR07zx#1Ic0  6 17 #13F$3k)Ek+ #33PlW_F2kq1I035v4jl0yJeOe:x(  6 18 #13F$3k)Ek+ #33NVR0F3Vps76LBCfEl9ruhJ07zx$2G0  6 19 #13F$3k)Ek+ #33SrDPF2rj3tZ53xbElY8zfnCbzx(  5  6 P/AAAAAAAAA= a 0 590088 1 1 2  6 P/AAAAAAAAA= a 0 590088 2 1 2  6 P/AAAAAAAAA= a 16 590088 1 15 11  6 P/AAAAAAAAA= a 16 590088 2 15 11  6 P/AAAAAAAAA= d 11 32 598407  0  !183JMYdk0Ebp7riVkEk2O46v@RHUG09BiktK@Elfma62jOjkoBPHLHN@Gy9biD1Nu847p6hfT_yk3APD3C1!4SANeKRwe_ijk2vEMhBYf5l4Dci5roQPmCFy7b5YX3YnSWktPLyBpEdKLWH9v4BQBO1ad92yqyzdM7LkCOUzslbW97IagqF!Gak   #w3O0Aq!G9Z2j@koOk29WpKzhXHC051IR2Mca0iDFe59@1oCNassmowW2Frmoa9_x2F3Vma_umvq0WCJlVNU6IIvHjh5XTXuuhNTCf2Pp2RfyDJWeejRimK_kjiyx07vmsRG12zgt1Yo1Y_yR7g:vuJ01qtP  #E3KusvF3y7ca0@imMjlKayBH3VJx0k@UZZUzmHB7ivF07Sc03ztAj2WR#9u  #E3H@xT03jC6YHWKmGjzwxHi6BXrwzQCjao:rf@DIGbRU_F01lnewTjZ#AS  $3F$3k):)1z  $3F$3k):)1z  !183Q67uF0I8YbeTwRkk2I_v6eXPyp0SlW@NCMda8eg13WGJmtwseeTJ7LC!BC1eqbo4YR1UqCPtr0e@SHivb73J6PwC6wcuOoF6VqIc_xarRL0nWkLvf7TLfMC9FHVo4q2edmuQ2WkUHPqSi772v0BBnelj78z0oT4NNKZjyALzk5rrO95V!QKo   #w3RoWR!FyMT__g38k2986VQmHPp0G39R1MKa9DMFdp9u9BGRbsryojW@F1Hd6IpzuF3XDcCzJx_0hBhGEcIpyInuoO44SatvhHRt@duUHI3oyHpfojZkqJck@DbxPbxmoZVO2leG07nqY5yDrJjzvyB01pYO  #E3SZUUk3xkDGBIEq6jlH_HNqON_h0gHrbqUznvBnl8k07ZN03zp6b5cV#7q  #E3PD@SV3i0csBhGq8jzrDtjqgf9RzfLldc:rr9mqarnjhs01fUdqXVc#7v  $3F$3k):)1z  $3F$3k):)1z  !183P3hJV0I0T9iBM6Vk2JIOIiV@v10lld9OAEcfcTg6nX9Letcp@eNJMMA!BFWVLpBA3WtWaFZv4019imHAHaj3zPht40eJOEEjUrnM4c6YRJ0k5dysQ3uL7Ls8iHPntCx2TiqO3tjaIFphDa6rPwR2MTGa35z3kScNioejwQFzX9fYNG4F!OKo   #w3H1iB!FKk8k@DIkk27jfy2gXaW0O4oOEKKJKDYBeOOXN2pFe@oPltP_Cteqau8aZk3mBwK_Dld0tjWF@pA5EIdf2Tc_Nwx3BqUYnivGRefNl4dmS@Hr4bPi3jJ0ULcBfuSqcnYwtWn4OExYMUUzvG801r2V  #E3LogfV02WwXf06ABk1GVegaDtnS0Rti!Uz!BUaFS178DVYzqSvJUB#KO  #E3NJZi03d_0iXtxLOjzbxvDo03eszIWkEoBS6INK3UME7CjzxCFRZh@B#T_  $3F$3k):)1z  $3F$3k):)1z  !183R@K8!HE@dqhLQak2IgUTMb2dS0dhcbp_jFVo2jpuIdeR1BhglNL0V5!EpVxqwejQMWqKAek@0WkRFtlG68fFeFuW1i1N6cpOe96KGsEBHTMpOvEVw1XlMTo4vkuT17wnFkz1@0VKy3YF07JPvnDE3i7@lycDQWTK9Fzxn77FlmYPHj#WcZ   #w3FTQHk0FO71vab4mk2837EGxXZp0G3oPJJFJehGBUNr2_2XkePfNkBQGCL@O73NTZV3wkpCc6gD0Wi4jgZj_4YYY56_GLBztALTunD@XwQ3FHQpxpE6wwSthNnv33LV4Y@MDXdVHsJmgNexOrhEzvIk01ukV  #E3MAvhV01yDZ8qCB0k1Dfyy8t9h90ZqZ!Uz!9n_Fw_jcTHrzmQfJ68#KS  #E3Gx_uV3eyr6ImEMPjzd3_nJ03yNzbYRMsDZ6asO4LMOz2Ut5Dgeym1B#Uz  $3F$3k):)1z  $3F$3k):)1z  !183FwZGk0HApR94w0Zk2IiGJA3mW_0xeX_PZEEjmsEROstcAq4g0f@LZYY!FW0mr@HBfN_hCCyeM0bj9kNG0L0f4mAHUMapLPcOR@YG_7N5ZDqEKKWCyu4WAQNIMAeqlfBA8p8A5ycVP2BiFyuQPsovz0y86kycBPdTu9MEwXyzHKnUPwlF!XZZ   #w3KAhUV0LQoD3sIxEk2chmM1bXIS0W8NZXSjLZhLhc722pQrcl9Wku1LBKqhe3Os3Z@YF9mdJ9AvBnS3lQmqLvIU0n23QYisfKfRVBlcqcPgEHVg9Y88!XKi_hqJbqbnVXzq5xTNj9oDNwSD6u:vF!1gec  #E3HSQQk3xxFHcU5wBjlTokIDJc2t0J1R!Ez!4faINOj9gBrzsNlZeB#CT  #E3LfWNV3hGsJIGAlqjzfzuT9htT0zYmRCpUzNCZY@aSeMRUt0gk017JN#Nz  $3F$3k):)1z  $3F$3k):)1z  !183MtZ2!OulW6BTzTk2padTShAnC09qmleSaUafUHAMo@1JSCDxz@C5XzAKCf955oRyA4uTS9KAIE2V5GMZ1M039IaaG1piuibgRM@IIS_cx2RYPKRy33YX53cHh7mO9I9Je99NDsg8!EF1zL_gGYQ37ltVdylqT8TpAyUz5CUr9DfhPjV!T0c   #w3Fm8g!K_VbzAhRsk2a4oA1@XOS0O8ua3SJLXBoCbr6@XQgwjpZouMJRJtiB39rnoUf7hhO@38WAqTRGOIiac3YkldxMZktCJiT3Cl8eO1J8HUF7E1c!Xgi4i94paNIMuSGnCOJjboELeww7Ejzv6Z01j0a  #E3GNsVV3tdxs5jcFsjlK10FNyk350N1X!Ez!1zVk_rUNHvjzx1Yj2$Hm  #E3IWrVk3dAsm03p@HjzhOXoTnGCRz3_e6t97OTXSV07GL7pgZAx3W9B8#Uz  $3F$3k):)1z  $3F$3k):)1z  !183GjnI!O5AObewzGk2nrjTtgvqG0WqJoIQMVpg1GmcA@_BVC@KsuAkU4@SBn8c55ZcekPOp096KEeVPGEouq_A6nST8WqBqncDS69v1C6eC7BoyFl8A1LWD2jHb8Zsxf8ZkOA0Cebl!CZ2DaZgNYZitD5ojye@O2vvg1jx4EbeOUMienk!Vz8   #w3K@dtV0JmhWgiuJuk2Ya1M0zPPt0t8F_mRpLAgZCP79HNvagh_bHu_KLJEi5XsMcoF34ldK5h6nAjUWlLJB6PIRsk_tG_gtSIzSgDX8G9Mg4XNR6UAu!Y0hYDL346GvFeNOZONejroFJnwe77Ezv5J01jL_  #E3LgaGF3sLYcnQj3MjlDXBrkfF9S0c4i!Ez!1MVBYvU6Gxjzlg7vE$Im  #E3LyI5F3dIrypScoJjziOvBX8dMRzQe09VCiPmYF0C3WhwoZ40D3QJD4#Uz  $3F$3k):)1z  $3F$3k):)1z  !183OrByV0NMRH@2F5Nk2lM@mOovtl0OraoPP_VxBUlbsL@Q4X17_p0@fSJ9Ygc8CZqwiPcmNVwf4aDWWElBpRqMI23Ma4jrHoqcxSR9gG@rUxE_1XDtDC0pVO2RmOsfO0f3ZpK9eDwZm!BN2T6YgaQeej@6BoydqKHPAAMzxYtbb1hAlys#WHc   #w3OH7Q!GWMsGs2zuk2SD8zZaWkh04oJQ3IyHPAYfzqDPj3PRN_K4mIFZF7BDV1rOQmmgGVt1z6S68PtEd1w_YnCN01v7OFjyFGP_fiN2bxngtaJRE@B2LPSg8Ezn4Kbv9XBSr9P8dqmoKdRg69Uzue401ScX  #E3Ho1Tk3yewBSRnAMjlO9vpeYN9G0g4c!Ez!57ZCsUz6wE:kmnec8#Gg  #E3FNFo03ci7W35d2cjzWA@TNYWEgzbzy!PtLE3B2pApjxEI0E4BEB3B#U5  $3F$3k):)1z  $3F$3k):)1z  !183Q_37k0JgBco2qLak2dyUZJuIOx0Cc4fNHoS99kl2b2@IR6grtiT!SD4vfW6B4jJb2bDK0r365@fTEEcn8KbHnPym9r1mpk_oLI7_II_G4lBNT4up0usVcDQGzbP6n@EYDocDKVcz5nBc14suvwJF2rxpL_y_BPGtwA9Ux43bilxYgTcV!RNZ   #w3Q6_5!GJMO@jJJgk2RQYE4MloG0sowQuIxHRAEfzqFubIJcNGLsmdGiEkgSV07Gfy2ddSsxI4e5LQhEcXP_QAB81KrlOokgEmQ9BIAs8Hvd9_RSNum2RQMfskHIg_XQ61z_i3MZeSmpJfwYa3EzubZ01UHX  #E3RTwVk3wxPT8ExuujlK21XGus@90R54!Ez!3rX@rMb5Qfnzp7fjK4#Ic  #E3UNixV3csBJJO@lnjzdzg_MP1JFz3ji20AzMFYI!8XLY_zZ8RpCAZ8#Uz  $3F$3k):)1z  $3F$3k):)1z  !183O_7n!JargYatyBk2d_VjJxfS90WcwgqHLSr@9kw7F@8g9JqWfZ!UR4hfG63ZRRLeTTIFnI3q@UTdEd3f_SenXva5z3enwaULr7Bk0KqgooYL3IA2uJV8DiGy7hLx2G3NZbyLLe55mBv1D8pR24LamGugdyXqN0sqADjz3iEex8vhcfF!Sm8   #w3JJNDV0G2IEqvCIuk2Q6jks7ltd0JpyS9J6HX@wAvaM@WYB8N5PunQINELB_V0763ned_O0sI1u5ZSFjdY75Dn9c5_mGQ6mQDtQ7hPAOsoY_1ZoVav23WRafSFoIypTv0lttSjFVfrmtI4wHqS:uco01WkW  #E3T5vIF3ueDdcAfOyjlCzOTC7cAx0o5y!Ez!2EVlpAENu_bztoztz$Jp  #E3FO21k3diV8q1JimjzjaklDQlY0zMho7WECO_4M!4Jx9ZZN0hx7Io8#Uz  $3F$3k):)1z  $3F$3k):)1z  !183S@dCV0JS2VPiLEDk2cLdZW9nYC0KfViEH9TY@tFnrZuwgC4p5am!Nj4AAC5lJ@gGeJqFsj_0r@WUfjdoUKG2i2qT0Q7FlGbPN7acVEaxRxopP1Q4Zu3U5EQlkcKrAPJQascFNofL68CC1f95BFoXXcxxVkyXiJ3MC@tUy3R7c5U7l0lF!Toc   #w3PnpOV0I8KpIr0eVk2JiEd99HhC0pHWCZiiV8keisqQ2NJfG0_GqrsOHKhGbZ3_PmxYCPMS@IH2J6H1lvtorLIscQ166YE17XSSSBSvWYQgDYDs!hgCvUlkWHg647wA@RQ5Ehdn28oSXwy5rOEzvsR01mhK  #E3HFeHF3rkYJWWm6jjlKEqFJGVQK0kCf@yjz_UFYjM!7fV03zxCx@6R#6Y  #E3UJxy03h51QjKtktjzpci3jWuoozqxWQg:xJAEpvLu7s!1Rvh2_Vc#7O  $3F$3k):)1z  $3F$3k):)1z  !183NyLgk0M1pDbrPgEk2YLjrcaIGO05sY8lNkbWgMfwp4@4QQKJTTBI5J@IZAbgyJOhLuoqi1@E8iOtFwHygt8HBFvcpBNIFU@IZYbHzWoba4j9_tvcwrBDOV5zfCn06qv1gHxcJ1qofEwRH4yssQaCIASOhf9z4SUDu5oGUyA3UfWbAUz4#Pko   #w3Kk@kV0LD2OFbwEDk2Pod7@KXzC0OTqf@LFQoDCitcoQZn8_DaToyyVTPQgB64dLwlwKd_aWqeWNRdCneo98WfTkx2Y1hRtCS5RuG@tcvKwnvFR015c15PSmsEK8W7DwFYUuT5h6ocppNXT_bajzvoV01zeW  #E3GS1vk3vcHVYxB60jlTx8tOnFYW0cGk!Ez!3ba88_744HYzowU8oB#7o  #E3TFItV3gI17vHQF4jzrxut0L2A0z@l8XBEz!HrDe8i76zw1_g3JQ4N#Gd  $3F$3k):)1z  $3F$3k):)1z  !183OeZOk0Mj4ITCbAik2XmnQ6hAZS0SyZiSizIom6H5uN@wwH9U9SDKYbVJwHrhOeemJwdW6i_2lkQaf5JI1Kt0Q0w76VOkOMldLN5jSme7GBuCzm@Zn3IcWk!H@vFMGPfeb9c@1z1X0i_NVdcsfrAPQlOtugyukRSIz8ajxYuEzqgnXHpk!UxZ   #w3RHSJV0Kkjk0DcKZk2Obi@y1A490CUHgiKyREhxioclviA9GCHRJzLWyOfAz5qtDZs4Kx_HbHgNMLdDIeopNHYR8vyX5huriRKSxGGtLfZJif5B012B0BOamhiT8F6y4A@zDM1jeo9pqMZTUbajzvhB0234V  #E3TNpHV3sPDd0cKAajlP0OsBqNXK0BG@!Ez!1jWX7eEl3p7zt1G@I$@n  #E3NHZnV3g_hVEbxnFjzoyReN02SwzaykdAEz0elq9OboUvEk__NQ4VRB#Ob  $3F$3k):)1z  $3F$3k):)1z  !183MxU9V0MLFJf_mcuk2YjcpXIfWG09yJfZg_IPlDl@@JXfoJdOlRiJ1_6ImHZgzukuW4mS8D_ynEPBdxIz1g8cmzvsLU9kMK2e2LviZnw7MorhxT5Lo7FbUt!GdQU72PVPRpTX0czs02YY0X8Gfgn4zYL3XhynAPAHjcmzwIJ7d@inToqV!XX4   #w3TxoP!KUlrr_Limk2NPpUCt39K0hVei4LHRfCtip8pIqvAWCmR8:YSOTft5rt@gsBNG_XfDi6MEdfIm_RcCvUsx6WSjPrSQXTYGb99fnwhXxs0pEq!OTmZi8MrqrJ8@jXFjdooJpyLoTLbx:vfk026HU  #E3F7le03tMeYq7GGqjlMt8pwoFnx08Oy4PEz2C1fV07@b03_IzlHCFF$CB  #E3KMX_F3hTz4H@T6zjzl8_2w02oczbC4kbEz20m07QqyE1MglaNsie3B#Tj  $3F$3k):)1z  $3F$3k):)1z  !183R7z3V0MFhQva43ek2YoEAuAYWt0dzZeLfuIQFtkj@NH_oOKNWSTJ3aqIdmaC2@tehZu1A6bVp3ObdsnyXANXHsfn@TvllIgfTLfiA0drfwua063d7AEdTx0PGMvmb2XPXQKOH18z6!XYVb80vk@wQNa9PjykDNQH6cojwY1zRXpETgsk!ZD4   #w3OQY#GKg08GgiRk2R@Qya8dUt08hBL@GtGMex@soiXI@gNF1@slBMUER9V0VrFlk31Wi_LcRP0lKZj0dFpCv7sbqAmLTjXXwLW@Sc6JM3aJfZbKBaPdTAeuAScxaFQAfflu9IFjUmLQURI5KEzuvN01FVn  #E3MT@LV02hyN43w0Kk1Nob7mysE10R7V!Uz!Czn5@5rX_2vzwl7oTZ#86  #E3O7mRF3dGokcg1i4jzYTYDJKkr0zMzy@8lH6_I4O7UC7BxwR!ClMfo#2p  $3F$3k):)1z  $3F$3k):)1z  !183HPAWV0JhYlhAhnLk2c_POdjYL90tgFWMJeIo7xiiIGuqmJcp9ku1nTX!CnWc6NuCuLRw5aHPy6aQoDe985TIMXiLRHM771OELWnN2NolgoHdD2LfFwkYpCLEvYNsiBEIzXd@O2x5H_au8zLDw82eHj_EodysLVROkBkUxrjbnOojl4aF!HTc   #w3OR62k0G9PZR0f_mk2Q7gA6s9hK0BkZLrHNFuAk@YZnex2qNGdBgmMG8Dh9ZVcqpOk2yplC8aUS0GJHj1rnZtQH0ge9JMin6YUNS9q86neQToVFbGBiR7VoeVBo8H6@nCAHD0CHwjKmPRbR3KVUzuoJ01HJp  #E3OElsF020pUMzXC7k1HwokJp0bG0VIc!jz!GfkIfJb9peIzsS7ySV#Bl  #E3Oi3c03g@Ic9X8dZjzkieUdJlawzEtRIClqCno6GK:yIKyN2N01SPc#AU  $3F$3k):)1z  $3F$3k):)1z  !183Hx5Vk0JgSSS7Fkwk2bYasT73Vh0piyYZIWIF8QhY36uRPCRmhnH2BS9!CUWy__mQdwd6hNqRo67PkDpMXK6nfXc6NFQz8NLXNyIzVKoKoyuE_yVn4vFZeBxEYIX9AQzgSmYVNNxOIIbc9g6VgMv8Tb_S0cymiXkPbDRjwrc7m_nUomZF!KO8   #w3R9dFV0FdX1Nu1npk2Oik33KOmS0JlPMbHIFSgC@9opPduu0GCDFmnIED@dTVNqNGV2utkJxZVz01IQisqhJcnKFbi4BMXpEXyOLdWvhYB3NoGVXeuBQ5XJe4hecm61n@HyWzGBHj1mKRkvs5u:uhw01Hsp  #E3T2T$j@QUiyJ6k1Fn7AOikgO0NLB!fG!IRhKQ6rehYQqVIr:N#E@  #E3FZ8XF3gyRRNFxX9jznqDYm59sV:vJOSwDHIKFCLUzxnxpJ!3zWxV#FK  $3F$3k):)1z  $3F$3k):)1z  !183J1U8F0JJ4Ib3JAOk2_pvQmOvaS09lX_5IKHf8tgTY0u2@78l1mX1OTM!CBXGZy@Sle9ChDBSx6eOxik6Norfk@WXHHQ789JnPko3lW4Eh39ySviyPujZbAviyH2OO3jwl2RPNwx3ICbd9eLkRXQT6Yl_@cyioYFugEPEx7TrpdqnrkYk!MG8   #w3Q7h0F0FV3XCq67vk2QDjCCrlc50gk0J@I1F6e7@JoEXqPPBGW5@moH7Ec9BVUasle2q5Z5AeMQ0qIljArj59f5F_i8RNoiZViM1dmftniIaowk!pV6vLkedj4tJ5ynBII9yYHJk3mYS5R45S:ujN01D@q  #E3PJGeF02c24gMQGDk1M4qfv1Fl50cNX!Uz!D3l5fHUXpdEzwcWzfV#AN  #E3FKULV3fhYoY4FCyjzjOVj85tFRz7ooDWkmADIwIDjzyxhi4085ETMc#98  $3F$3k):)1z  $3F$3k):)1z  !183QFbOV0JnClDmxx1k2dHHOhj3Y505mwXaM8IlbuDCXwHcP6ZsxdJ3hUI1VCN2c5vGr@8VsdUNNF8mPsE2N1pZIRHl@PVsS7TMTMCkoYgJXS2uRP3qaLyYZN!Et3cL74senLigBd7q4zcU698owSR92btNXeyo3YJuFiqUw857kx07p0ZV!JOB   #w3K@U2V0FLs0nE1Lqk2PCb2PY1pd0co8KQJ@Ep@r@84MeV2WZJC8coXFOEQdUVZqV1_uktZsy2Oj12IAjE6ZZuIJka266PHlFVfOVdd82YE3WJxB01EV5IO4eIF7cf5u3BuohzGEZkgmcSlQp5f:ucR01GRs  #E3JZOo!1bKKXJWG6k1MCCFB0G2908W4!aT!F3BHPub9CSIgoUp:N#F1  #E3T3akF3huKWNEbdujzq28f_B9nsz3sPKYs8HEpkAxEzxP5_g0BDIaSV#HJ  $3F$3k):)1z  $3F$3k):)1z  !183JFDFF0JZiq4DgeHk2b02ee@nid0hrs_WMNIjcTCCXoPEGzgrxdg3URn1rBtYDp7PDtjp11HeOV8bPLj8Lg5IIn@eiM8u98pKAODl@X5oUpCH9X18kuyL_Z!EkIlqURxfD@mTD74Q3qb6_iOERmJXTY1cyeyhTZNu_FbjxMp7rd7IuPZ#N5B   #w3TE3BV0JBecwS_aNk2NA9jKhAEl0hSSGDVyXlGlkI6xIFwy59lOuuFSaK2Hd_lqMn6AMeIhaUTYKBHrlhuLakg394dGeXTwfSFSCgKsbYfQwIYw!kH7ZUrk0gf6Y7rY8RVxHUaoz9oIWCxuLs:vbs01Z_E  #E3IVnuV3rxj3abEk3jlIy07xYOOp0ggvIyjzdzI7kGF078c03zt3SBNV#4z  #E3KFtHV3dHTJ7MKVTjzh_EarzvQRz7AYMizZuIdTq46XrLc018YRNYkk#2R  $3F$3k):)1z  $3F$3k):)1z  !183OORXF0LsH6PgYvDk2ZIo14u3GO0_xdBXJpc8BfhY5QHifYSCHO@KhKUHnfwAU_g5sAgj19T5NFPcFQmTBX7bwH38tDetgXuIJWhhSVrM9gg9bKjTsd55QF!AN3wrWZ54UWai24o75UI83i55v_IHzNKW94z0pSZLZmCjwPTjh5@7WTuk!L3o   #w3IQBHV0IqYs8J1yfk2LTwShiIRO05WUFKWjXSGokCatfB50WCWMquvS9JlmZ626MQA3NTLSYWToLHGhlf9v6_Qwt5dFTYRxlTPRFf_cUYOApvjs!o@6OVqjUh6ql803@4oGQKWPy2oKX3ScLt:viJ01Z7E  #E3TH5603lgQPR3as8jl8@a25btPS0whEI6jzdYIEmnk07Os03zsPQGfZ$8  #E3MqXr03gihIllAxVjzpCgfjzw2BzMEtMhp_tXeQu@ODEhd@O7HqRn5o%  $3F$3k):)1z  $3F$3k):)1z  !183H5UYV0LLseevpX8k2YPN7VkIDp0SyQAjIDbIBBCT5KPbYUSATOsKwJmIUAlg4pRKlYYDzKNxLUQsDwH8ApbOZ8PxW@ytBWvHKWdCWna6MojGThe8X32APs!@zYrrcgCZZOew3Hos4ZIUYAp3QcALf7lNN:4AUPrulqzwPeMRWsnWkrk!J5F   #w3NrKO!HAps3795@k2UOjZ9CP2K0FuBWj_NKDF6AgLZmCZ1ZSp_6nnOzEyeC1bb6eWQ5Kdhn3Cl5jMeEdtdpL3IcgP9POHdZIYMYf2vV4MA_dYsCmwq!Mjg@8UI6aRIBYmKtUNRi7moOgwgpXUzuuw01DXi  #E3UaIlF3s@KclT_hGjlICwJzlk010s!2cjz1JMYk2s4U1R2EzoHIc7V#43  #E3HtKkF3aeM2dNuPijzW_q_7ztPJz7zDR1qg6SmirVhTUI65R240164o$H  $3F$3k):)1z  $3F$3k):)1z  !183Sjt5!LMazBdUxPk2hUBKU_Qdp01hmc3JAIjAeCe4meuA10vCjy7UR55BiCa@MwRlo@2TxfzJX97SKk6tA5Wfof0iKutA@lOjJmfD35KCBq2Oq3RWJuoYI55ieJcrRWwI9Nk2Mgaw!ExY6osg6GuTx5I@eytHU0OQfaEyrCbqxe7iXak!Hdc   #w3KBYgF0Ly@F_2AePk2d5jJcqPM101CJwzGTTyghDgbHvMAfRrWd4yWV@N9DfYvaywZAt9maPU4yEJR8lTHzqxmuFnSM7a@nzMWMCgsA7ssJMHVg!ZD2fP4hp@R6L6vQS41GxpQDl4oLOaRkaiEzvpJ01qcg  #E3R04G03xsLxscbYkjlJlFqFJV010F!t_EzRmVfkSMmzDftUzwZrs1V#1A  #E3SMrSV3db3fSWPdHjzY_FeCINRNzUblGZCRJIWNUzwAyGTms08O5Qsg#2C  $3F$3k):)1z  $3F$3k):)1z  !183P@ugk0OrZHAyDJRk2p1OYpanwK066Bw1KHVxD@jm9SYygctIGx6DENICDkI9DdhRf_@a0uGO9vIMPfk7oj72HZvHWNMn0agYTO7@xXUKrSDoVyHLYn8sIV!CE9_bUHmI@t2V4NvT1wN62_5cRamB6oCT4gI4qS7GeeNERLejzyYEmeuF!P0c   #w3H@RJ!GBawxmFVBk2F18o9h20x0p8rLhMGYmfpjl7ruV@2dD5zHgBUDHgfF7it0Rseo4za2qHXOO_7EA6dad2i9AeAWK2bULyPCF_NfvWf_Hjo!lT@JKSfxhCbSKXIiOmpqlNZo4mMLFRpb1EzvCV01zJ_  #E3K8@6#RRsYgtJXk1BQIWVQVS_0kDntSUzwicrV081z040:uPnUq$I6  #E3LGn4F3d4wCa@lDqjzm4OLV02f8zGKtXn@orVOBXuIwa0wyoZSd8s4$Uz  $3F$3k):)1z  $3F$3k):)1z  !183I@f4V0IvrdpyZ3jk2SEeV0HPLK0dTwc5j6JoC5FEe7dzJgd@cp6YabwOodxEHazkyR4cqyGgI4M@ZGEQZ9bgPAvDaFtpp7XjREbh33rbz4SKhOW6tO1_M21mgTf06F2D2F9Xc0qpz98Ws3gN2vMv9apT6cpyc3LOTQABEz3RrQCXMYkvk!Zmc   #w3My1G!FTNFK3jIok2CblZpb@7K09A5MRLSYqAijarq@W1yOIGyuf0URGPevcHNuJyHncua3XHgQHa5De7CaBXZOBL8OIob8K9OClUN7AnQKeNR!tP9IKaeZCCLu_OAe9l9c_HBpxm2KNwHaqUzvAk01yJa  #E3NqqPV3z_Qt1h0U8jl7jazRUVjx0kM;Mn:cxV06UM03Efjb6CcY$Ha  #E3JWH303d1j5ZkcXHjzhQLWo03Foz9GWcl2JtKuC4I1IiKw6kHTYv9o$Uz  $3F$3k):)1z  $3F$3k):)1z  !183U6TT!IQWOnEV8ik2Q@d6JAmGC0CUybNg_JXh8F9e2tyoiW7sliatZPRO@4FKM517J5ov2IkIePW_CDzZvrSu1XzyDtpn6NkWEVBPIPcv4axlSOLzcxvLD2mAzf06P1uHHlMX2BnT9KVJYINCQcn9yYm44vycmI_RSA1ExYbMK_vbcj0F!ZP8   #w3JWxr!GDGj5aO7Gk2Ert3aLXDW0dBCR_NPaTBjFcsA2LPAWClzwfFXYJ1B17HOEK9AIl1aFcJsOUZpDwrEqy2h13q9MJgjIMROrFXcIAYfbQ5kQanT!DBfGdq5epMQcGhCkMJLnImEKKRYakEzv2!1s6Z  #E3QzXlV01OxVyLSyFk1GT6itkt1K0RVf:JP:fW!75703XYcuPzU1$HC  #E3Nex!3bxKiFB9D5jzjRNCFLXmZzhXQdi4RpVuc1hmsi04eBJxpcz7$Uz  $3F$3k):)1z  $3F$3k):)1z  !183Gvvfk0JfFvYo5L1k2TTmUROmLd0STFgUXWMBCMkyuY2IRztJBsZXYerOtg@jbry10JOoxTVaMnOPYsjC4t89eEvUHEpjY6akuR7D_IxcDo__jpYatx2xMDCEg3fuYBuklqhwRGwWH3OOdV05Ef_eoaha5BqyasKbRZA9EwnVEO_dMawv#Ykc   #w3HYo#F_@c4qS4Dk2Ce82DQ@Mh0KEOSSMxacgnkTsCPQ@9SIhzVe9YUI1Ah7qO750vM4x@GwKbRx_cDR7uL_XWG4e4bIek6KYNTGNfYAo3N@nVOTbw!DNdp@FJ2KEQY9lKWUG0p0luJNQyLZEzv1J01qH_  #E3Kw83k3zwGTVCYXfjlE1OKheWAO0F_h:Kp:fLV050q02VQAardWy$Eb  #E3KYtJk3boYFkIUUQjz_qF7wWf_4zlZEpsDxtSxN4Sk7Slnto0DuxLJ$Uz  $3F$3k):)1z  $3F$3k):)1z  !183P@heF0JDoCKnI6Tk2R90YwJuHG0KUcgIVaM8CNFx@_uIS6lI0pZ_sfgRaBYFlc8dCBQp2TYqOEQdZfin_qc0u4AIeBhjv4wmXRfD8nBtABk5opRakLzkLPCPfPgWIIHZWt5psI6RY4LMA!5Pvv@y6Ru0VxycyHpud@qEwIjrN92vhHzk!XtB   #w3PAheF0Klck1zOgZk2NFLrMn2b10lQvOOKL_rEiG5OQA7I79MDKXtqW_RQgz7NtapdffpSptLbdRhbGmnbZtDnANzTKxcoz5ROSKkoPRApZsIkN78nJ!OWlWk86W6pR1PkHCWd@o@p@MXTEcCjzvvs02H8X  #E3KNww03xWj_dNoy4jlNg_FS8V010F01TpUzjPeIW9rCjZvarzxCYB@$Bp  #E3H@nIF3_otvmYNUsjzadjV!18oz9n1ZBEzuEP@5zq7j6iux8sOmNF$Rs  $3F$3k):)1z  $3F$3k):)1z  !183Gsaxk0NQPDxgVcik2Z_HFKpIFG0SkHiqY2MyEqlZQbYUhLKglFsUSeSPwh6jzqhX4nTlT@8X_JTRaBHlJddeeaZ_ePEi7BblDXZFDFVbYwnL2uAO8YJtPR5SD7vK_BAQnCXA66VbY2jQ5V07bfewRnT6DRkyq9P5q3@Ljy3_UdD6MUr0V!bGc   #w3PzJlV0JhBEqfc_Bk2J8pF85@Yx05QxPfZjaFF8FzdhAHI6dPmNDuGWVQEgmbptSxr3j9OGyccET9cuI0t1cyQANzPJ5gsyePpSulw@2wRBoQQV6ZtX!NfkkF2_zLdYrXPCsvYHocpSJxxg8A:vsc02KiV  #E3NAWkk!4ce95Lbok1FjQVqn8JK0Z@AgdUzqJhbV05hM02qfzm@pX9$GQ  #E3QrmDF3eSGwPY4Mujzolb51J9p8zOfWZv5lqJu_4e2Ti05OJxCp2pO$Uz  $3F$3k):)1z  $3F$3k):)1z  !183FV2NF0MciTP65N8k2XXEnGjYCt0xmDgujrMkEiFqvlmFxW1alA4XBdyRkhGF_qy@@nZOUyCBamUUanmp5lOP@VRLuN_gl@HnUWoEgWGNbp2T@L6SEzHvO56BhgfBZzn4Y260L6JXd3kNpV07gQz4M6zqJaqylzJcnM9UUw33ED6NfXz6#d28   #w3OjoYF0IvJQ71MZ4k2FOGWgA@dK0CTdRT__alkQl99xALY89TPRiv@VGQOBh8Ad_yDfktNGymcwUWeaIMP094A4W0PKMluzXPWSiHmdygxorIKw5LpP!Ntk6kK_TaeQoHQlijUmohprIBS8c3jzvvV02MRT  #E3TmJEF3ymjJKPkzRjl53lbjgkUd0NEpwdUzyJjQV04lz02N:ms7vI$Ht  #E3JRBuV3dMlfLdrTajzijv8osWxwzhdQcB3_s@vH5GV050ZvwZiQrAf$Uz  $3F$3k):)1z  $3F$3k):)1z  !183Ki4M!LryYzA@J4k2TRvo_zvEK0howgkjfMdk@G6B5XRxeObl@2YveQTDhLGFLyH4QZKXuDLbeWjcPI9ad9_uUJNePEhIAApCXRjfGB9LKNaNy6f8_IuO26CCdgyp1A0Q51yg0XVe4BMy!7qgXZSqduJquyn6G_H68HUzXpU4yTveQ@V!dKc   #w3SRBfF0HKR7wGNRPk2HOauuK2290O@MJDQbXph2jYcImCmNS8y5ckjSlLhfB_VNy52YB12teRS8PrZWFgaE7rPiNNaBQQerOK0P9jUNyAHwEfkFCakP!TdivA6b@q1vn2Ji9W_NkSnTLSSmbNUzvZV027XV  #E3M7GO03zR21wC4ywjlMi9Z8Ek010s01W6UzkY8UXAcVE5oF7zxISCR4#Cc  #E3Me_e03boUrw9Uv8jzf868B01jNzlebXtEzukOq7EM_Eo6r94gO@Uv$RU  $3F$3k):)1z  $3F$3k):)1z  !183P7OIF0Lb_6UcuC4k2WUxEdC@sp0lcNfSf3L9iakyuwPE_0GTRzkSacPPdAxEW6AtwfFGK21RUlSG_6lXIyssuNg4qLUG1A2jpUN@43obBBapsSvyq_D2ODCYh_fQ7yBBwS22DAVxw8hSr!4kvSuwzOX9LjyndQCp6exjzInMjX7QVqxk!a7c   #w3OI4Gk0FxEBFlc0ck2C7lvmKu4W05BjKxP4YFC4jO8WPMXHhI@8skDSDIvemaysnpHvDkrlisTmTEbZFOcAb3ud0ZP9dTVr5FrPGGiNKREnsQHk@avZ!Swgm@V58KjQcdv9cOP4nXnQIMRgrKUzvTo02@@V  #E3KYTI03yiQbXDFDOjl5_1JtWkJl0s@OuIUzx9f3V05vb02xnzv29kJ$I1  #E3TkcG03dPDtBnz26jzjPDIJD@ZwzpJfa4DLsNuH52Vr504UwUiSQ6X$Uz  $3F$3k):)1z  $3F$3k):)1z  !183KkDt!KAmFxE9OLk2SgvRh0HjO0OeicWZEKQDXk@@tHzK6GHwoTZLZLTXfzke6KtzIH9Oe4wXKVsasl45bsFXEYX2IYNb84mKU1bikHO7Ruy3dhb477XMGBTBqgq7fvuRIOlu5PpE8kMk!4_Qx@kHe2CPvyilI3k9dxjyYIrCPjbbl6F!bQc   #w3L2xIk0De2n9QVS7k2HX0jrtk010BQJjxRpPdDiBfcLGwXikUGrmg2T@E0iU3o5AK2AXctLZ4hX94SeiUJQpUupFmotCHkaGCNP4hRtFOXfkkmseec42uDaeRby1upNusOl5KvH6dXluFqRHqY:uvc01mHS  #E3F71uF0De2fehZzZk1fuh9B5c010o019L:ZfNAensCjOw6rzqeCo3J#NX  #E3UEl@k3hrEBz71YTjzth4CwsN!zKGS3IL8BawWZHZ6Tx4ZcUyGSBM4#Uz  $3F$3k):)1z  $3F$3k):)1z  !183K9Jvk0FwqV1_WTZk2RurQpxuft0SSywgGYV8k0B5@h2Wx2wgNegIBI8@qjB7OeCm1Rwd_LYFly6gO7h5pbpaeF@Z4mCRyLMXrFH9bnMLj4UgUa3Tz4w_DzE@AD9rJ_0VlCZZ0Nw711R8RV058vYtS@a5yBgyX_EbtLcvzwHJ7UO7Ydwr#WM8   #w3Moi8k0DtlS_6aUXk2H9pp7dc010VRXm1RcQLiTgwNPO4HesZ9vDgjY0D1iYZf5LK@3Zoraa2gL@kUIDX_2KB@k0yBulJkanC0OXiZtFu03e0oFm6_H6nDXdt85lxKUmuOydBvCLgGm1FdQwaYEzv0401luU  #E3S9Dr!DtlKTobKZk1aTYYFG8010Z01FB:c6NYgg7hML3qf:CSyiN#NU  #E3JQKzk3gcB3yuKoQjzgiMeZIs!zG7v@RJ7CwToaf1cqlw3sDUBOUa8#Uz  $3F$3k):)1z  $3F$3k):)1z  !183Pg19V0F0n22_Limk2OuhwGw2fp0_QTvRSGUiF@B1ujGUp0RXoZHJvF2AZiw7QeCHEowWWeZiiQ5MOlBpq6ZyH9PKFjSPaJ_YJEdd0HM7W4bogpxgoRtTChFG@p@zZF8O12RVwP62N2I6L!4rAolLqN5r0lyYdBJqv86EwHIETSMAjBtk!Ug8   #w3IlSgV0DRzmjzAFBk2FjMn7J8010FTVqvRWRji0COsOGA@W0fSwei3YjBiiM5bKlh9fWwnmbPcaCJVsDPKoocef13kyELFaDBXM6E_t4@VAT8s8oNYF8VBwcqbq1S_cevPY94CAPiLm1F8vP62Ezv6V01gcV  #E3FxZk!DRzfHziuTk1caABbHc010J01JnX0eOqZEjL_ErfHbZESO:R#IE  #E3KDyZ03ivxxs3pD0jzhQdcJ9saszwzlJZORKRUyEvk05Svck3bytqCN#MV  $3F$3k):)1z  $3F$3k):)1z  !183FVOIV0ETW6by16Dk2LX36P4utx0lVyuoQgV8kYhCPzO@h7ZRcVTPJJpEEil8sPKGz_3WSygZeY8OQtBorPZMH3@5ZpKP@L0_cFKd7F0cYJv_7WsNyPsHC7Go@xev3jkSFqw_yNm0G3z5zV04mwSlNyAGaNsygU@7p76nEw2wjfSh7uXyF!Qh8   #w3UU8M!F3@sdCV4Ak2JG_jb6!10NQRnLQjQsC8CtMlGkXFJXGiifBU0CiEJZY6HovAEsiqVFWi9FSZiCoi_9A4szpBtFmWyBrQYhQQP9ef_ubkaibwTfCAeC5oYtKcI6OsdZ1IcgLlfInwHaaUzuu401jBX  #E3QBRzV0F3@oo0H@Dk1i4fEXrF010c01DcjzbJKjfXtZblRmIzqWtmFJ#Oe  #E3Oeoxk3gac3pwgQSjzryZkAoV!z93Rty7uGsSb2Tpo6y4sNOhkS9S4#Uz  $3F$3k):)1z  $3F$3k):)1z  !183I3yKk0B9jHKlIgfk2M4Gy4XHG_0dCqrsLQUvhAh6OgOmRdgKZCFCvIi7Wg9_RtDkuwhS2PPTT61uLyfY5NJIGye0gtl!KdX@A533HlKew9gSOoHpmnmEO8Vg1OK3O@lNctCB40KnGj7y_w2qf@NJXXhq0_yPTF9qU8lzx1vEZ_jvcqgk!TNZ   #w3Ss3Yk0F1GI02vgMk2IgUdejN010kRXoFQKRBgkCxblWq@BBbCm0fcU0BaEI_P6IwnIHcgmWoW4ApU2i@_3Jm31dA5BKGoXZBEQBiZQFu2fRuYRcgbyW2CJdd5rI4Kev69z1MrIBj5lgIMQzLbUzuwR01jFZ  #E3GJ44F0F1GDvy5Gck1cySDijs010w01IKjzdepUgeNkMKBNAzr69uZN#P2  #E3JSWX03f2VY@r10Zjzj@lMTr#z8oBwe3bGeT@YD2mT9nxg7yf3L68#Uz  $3F$3k):)1z  $3F$3k):)1z  !183PZwmV09tuPg2QSZk2J0_4ZS@E50S9gqCFyU@CCgf9_dY4Vc@85JEYFM8AAp_N9AVqwf0yDPJOz15MDA55UnUWrlh4nz!HdWz@8WflvaZRBBbSfstyjQCn79ANdb36macTS4m2PJLGm6T5v2IAJF8@JpjyfyOcAoIKc2EwWjrXO3Ugqjk!S18   #w3Knn_F0EpFN0KCgHk2I@T9TVk010JSFpOQ7RVgVD3bj9rX8Be9n0fxTkAujE5rqOgpYGsfHX6UvB9Uli55DoWn0lEWBvH6XPAzPYE8fAuDvLPX8cVckW_CPdE5h3WpgY5e7pGbCakPleI6Qn6a:uxs01hq_  #E3NadvF0EpFEVUP9Nk1_Us2yf8010Z01MWjzfkpMhuNLrxBAQzrPvzxN#O_  #E3NBOGV3ezGh8FByJjzb8OTqb09FzwjA2m5VJEUw5d1ML8Yfs0ELbYSB#Un  $3F$3k):)1z  $3F$3k):)1z  !183TtQh!8vv47jhzZk2HIMgFQHF_0W96pBTXTshBgktWsaJRc4R1VG@HF9AfZ5dd8cV4eFvHQ@MI2UMe@p5fn2OjdWVnI0DGpXA9dW7l676BFZjOb2wihLC16QfAd9I12YNQK222RJOGl6E5nm@fSk02CddsiyPt9hlfbVEzWoMZ5EQk@lF!QY8   #w3NBX7!MbU@Cexbhk2_kc1l7ldd059atbWNTSFmEpd6QqAFt@H0ZwmUFOjknaXMoKHAnhOa5chcOxWuHJahs0Ae0oCgndmstO_Ubj5@w@MBkmjN7Dv8!MkjUEX_rLqv_mfttUU0laoxLiSHrdUzvSB01wL_  #E3MGEo!@DuZnUOAZk1hmYnemN010B!tpjzRupbbf7xEpYybzplYhuB#KS  #E3H_Qj03i5EilHzkijztEHeKb#zxTDlxQtOrzy3K6qihq70htI14y8#Um  $3F$3k):)1z  $3F$3k):)1z  !183PPUQV0PmmFoTx3kk2nfBfqqQhd0hwx1lKe_MFDGZer3MpWxQKB8TLTcO0EQiJ8J2TfjCWXHsmNPVVPlKMpc7vAns1iE_yiZeAR3EPlk79h9x7mWx4QASQO6JFxPL_ieIHd__X6ZbI3_Pd!8RBJYzXyaBokyiNNTyeAEUwnwbYKObf@tF!YWc   #w3OLw5k0LwY3@zkM6k2YCjy14G_d0_8ot4VwTIFljitEArIFKAL3sx7RlO7klah7hhffotMX6PiCOWY2mQMF7qvfso9eWg6swNfUYEnej@hgiuZN7aq4!Mnj0jc5wqnnV2cOh5Q0lpp7K4wxbVjzvRc01wqZ  #E3OT1JV0@BBrDJ5HVk1aurIvLs010R!x2jzTWLjc2b1M1IVfzqIUuZF#Lv  #E3IE_Yk3hW5tLMgESjzsJE8Fzs!zWK4liTJNcUbXxYn_S5DsRy3SHC8#Uz  $3F$3k):)1z  $3F$3k):)1z  !183U_Cwk0PGt83@ZXwk2lI1tr2Alp01xz29Jg_UknlTQ5nH_aKNt@kUqUEOujbie8PXNIkpYeJ@ncQGWKGUsP7y39ApOgK_ihjfCR4E9lr7uKK5IaWf@QABQ96vkruP_me@XdpVe4J_P44O_!8XBcR1HlXCypyiCKGS5ebUwndzSt_EjRxF!Yv8   #w3TGPeV0KjqLnI0uHk2WYUkZEWLl0h1ikRgGPKjBDXNXeYmqS7GtXsqQUKyEg6fbTS5feC9KPacENzWrGWqc79nY0qhgK_K1jJtU7EHf7uPwNnTsDgoV!TciKiQp5LhIWuYGmGTqm2oKLFx1bXjzvS401yZa  #E3N68KF0@xQ6dDWuBk1ffS3D1N010g!kSjzNDo3ceN1rKB0vzm@flFF#M2  #E3PCB6F3ewQJEBKzajzolLh05s!zK1HUsL1PsTwWmoviUSFBQCa55M4#Uz  $3F$3k):)1z  $3F$3k):)1z  !183OQfOk0ONi3GYsv9k2kFBhXWYcp0CoRx0T6YzjKlIeL@5xJODozDWATNOQikgba2eDXRKQGbFimRkWBG9MtbGA1Id9mAbJiPey_We_lpbYC5_HTOG3f5BRl9TlQugamIrRHlwq9weJ43KQ!6DgGvxesXE2nyiSMSTbf_jyYzMXlbIg8wV!Z_c   #w3JXN2F0@UhpCvy5fk230ZPF8NW10oQcMpXgJIiZALJlGKBUoVO06O8TF8jFT83IitF3lPcmLI7U0G@NfPmRZdR3v1VtTuVg1eHR@cT4WFGmgiYppakv6OIVcUBUvw7Z9vwS4XKKBiSjCMSwhaPUzvGV01JZy  #E3MoIEk06@pkc62s6k1ID_UEQV010k!O4jzBXLrtMGmjfctMzxsbEbk#Ei  #E3N9tmF3gmdGiennUjznxdMUs@qVzpc6aE:!8jK@pT7Skb_6QgVj9k#5U  $3F$3k):)1z  $3F$3k):)1z  !183M3_9V0FgxmiUtWkk2EbUOAqlTK05418@0P_MfwdsHh13e5NhimmbKES77lvV0@Q4nZKiJqTNve5iAyh6qi6T4ke_Jy0Y0QY4vHkHTZj6BfisKdBJx4u3RjRWC9FvLOavOubAwx9MiZnbMJ38PuSXs7@CRBvyyrJWn1iOEysrrsOZITasF!IaF   #w3Np3qk08l@iCDkAGk215KXVwsSd0BOkMEhZIxjrf45CVHgjRbG38JLTO3jFA9gnXGavf6dPOID_288pdYXMn2IonM4mekFccdDOrasbeV01sTEq8ZVzC5GN_ffCevbUGb4jNU_Dw_MhlI2R1KxEzvGN01EFl  #E3LXCxV04znItzRrek19XLRLC!10V!R1:D0wQyHGqj98vMzl_hIaw#Co  #E3Ft3RF3gL6PFlOg3jzouv7jzvXBz1EHb1ss!9dNTa9zVOTsnI8erSo$w  $3F$3k):)1z  $3F$3k):)1z  !183Hj8cF0D7pzOFYi3k2B_JAbzW190gxn6P4CZ9fd9HI8kceEkNDjXhXA49QlF0XeOx1JMyY@T8zx5Y99A45eK3BVWaVmXXmLT3sFHGf6RZxISsBwjyV0fwPqWIAiVOKn6cOWj0E5KDSbfXBJYrd@AePypSAJjz18GBHs@WEybfjkpbvUTmk!EWg   #w3UPlGV0BODU8Ksink2EHnbehk150JSBSKPLGHBF@zrN9udxwepb6evBbADgJ3sL9gsPWJZ0YJRyGEUxDRaFJeXqk7dPhJ6pz@6OjiMtCPCnNlPRDVvJ!ZHdmjg4v5I@tdkdGIDodYlwEIvvqUEzuo801geO  #E3H9QGF0BD7OMSO8uk1XOAPel8010Z!iojzMPKYfDs8UbR4Ezr4UyMJ#OF  #E3LYYHk3cikp_8ENzjzeH@0zGs@wzVZuIXBNUsU82QGLKR4HV0DYhM94#Uz  $3F$3k):)1z  $3F$3k):)1z  !183Uo7X!HC0TM4IAkk2TEyohYn6W09SwkORKUYh@jltQd2ZyJdcW6QhNxKT@jd9n@1bl1OBssXczN7VpDs7q53XPmhldh56aKf4Q9_U3xbnBoSOH0kxmrUPYDDkTdYtJtrp0slk9qnK98M5V07VB2g9iSX1HnyaDEkt08vjzXbrQhNjiwuF!WYc   #w3OaXZ!K4LW@62vvk2TRLS5OtK104z6iFfdNgFriQNs2YQF50_sTr0KHL_j7a4s1SIQ@tIW0BkAKiWLGVawMWfbkstvAXqz0LZTgiAcXu6ZUPL4015JAxVDjAHlJSqrYZXpq3bVwhmoDKxSWba:vU801v2V  #E3Iy97k0@Rl@@huHFk1l2rlfGV010k!dljzJsqYe79Ib3ZdnzxS7X2J#Iv  #E3J3oMF3eyf4HZd_DjzmiUeTYF!zxKNLaMI_6Uz5SszEJr8krJJFmIB#QX  $3F$3k):)1z  $3F$3k):)1z  !183Jm3C!NVq_5DFpsk2gUGeuXvT50KjNs8TSXuiSHLPB@gKXpHS7wSoS1LCBL@f4Hui1mGhhBuoAPAXAlIMy7hI43q24Qd8oYgxVnDP12qM4qx8TT19b5qV22vHgt07AH5oCWRH26SBAaJxZLd0fsCB3AX88eylNPBFreAUw3krdyuUa8q#XUZ   #w3N9Vtk0JQZKzX8T6k2RHXNCCGFS0Vxyg8ekN5FaiDcv@Un@O1luaqtLPK_EB6GbqKUv@dEwzyifKAXLlU7SbGYdssKsjZ4ybKmUSDrNWeSJPeA8012PAwUyidHrJcLjQV2Z9sWTRiKoGJeSDMdjzvPN01wHU  #E3Lf59k0@M70sE4I7k1fJ6nMVs010R!dJjzJeM3d08cUV4JEzlt3f1F#Lp  #E3ULlZk3e0eslm130jzkF7hdgF!zx9NI6RaX_UzWa6BioiJ4ap!xD4#Uj  $3F$3k):)1z  $3F$3k):)1z  !183JG9gk0N3m_rPJATk2fMQkeaQV509iascROY5Dnm1uIuRSY5DC4aUPUXMCgRA1JM@Mtkte5CXnZQGXhlH8NrO37Ajm1IcBnDhAVHi61tM1RxhHiQ7Db4CUw3XHed078duw8tMB3VRHB5JcZHd1B25ATx@D@iyhjMJz6@7zyILMTO6bbysk!Yxc   #w3Rbm9!IsNxyMvPik2PV6q_5GAd08wZf5dyMgkSD5NweQA6x21vmqlLyJpj8aMMk_JQ9OCJyei2LrY2GRbn75Q_crpr9ZyyNK@UTEJNQ@fgLP1w01EBB8Uti@muIvLeQRXSxkbPFiWoHIdwy7f:vN401wDT  #E3R2IxV0@NgCSiNr0k1avjvUo!1$eUjzKErfd2cCrWJ6vzqDhmvF#NC  #E3N_2gF3dZoRgWDDXjzlYNXO6c!z_0VGgGcW2z90dZCTOCcFPCRh504#Uz  $3F$3k):)1z  $3F$3k):)1z  !183UgwgV0MasSf0pMqk2dmd9mdvWt01igsbQEY7iylvuNeLpZ1AG2ZVOU0MkgSf9ZKeS1ilcxC0neQyYGlGNi7BY3YfH03bemWhbVACqH57UK4SPXOd1v3VUq3uHcu078dmZ8KI@0JQFBZJ84KtSBCKBXnPEZkygiKLxudqjxI9UOhDMeZv#ZKc   #w3JZrqF0HpY6k28s4k2MCVU0tG190RtFe0cRM8FDhpsxPEv1t11x@qTM_IYDraPbfKJY518ouVhXLVZ5GKsI6jvWVqppGaVxwIjTkjzNAQ0RFdkN0182BoUkhDmunYaXvL@ROYfJ0iVoHGqRTMhjzvKF01uRR  #E3QPGgk0@ZxGLcDA5k1WZz30Fk010N!hyx2LzNW@BbUj6Ijru6lQ:J#Nx  #E3OyCvF3dFwr3NAoLjzgL3Ikk#zBscHfK4XezwWgGVKl50F5yFbOZ4#Uz  $3F$3k):)1z  $3F$3k):)1z  !183OBA2V0LfHGBvsSFk2a3vHalA_S0xjus3OkY7E8H1eUeLxax6ozXWpTrNOBJA@ZAHHOd5dG@ZoXR1ZVGF9L6uI2nY_zvauluizVKhPGoNEhHpcXN97532Ua4RHe@079WZZ@pAm1BNuC_HrZ_OHBb_FPYiCspyh7HCvzd5zx2vEMWObl4yk!Z08      fbuP`@DZ@GHhhhdmMEYeHugMTuUULuPDdTXp   #qQCne]TgVOZlNzh?lH`Uf]zHmpI]o`wLNfLzEZFPBCXVBzvS|\\ezuLW??Vq^Js@]QFq]bsHNQTFFNKnX@]~_CO_LVAJyJfMtLWlZdiyNl_SnFoUs|uvmM@SrSqHEE\\fp{YT\\~HxAjex_J\\Vz]MX_hhs@j[}E[psQM{HE~QWzLR^zerT|thSsSblNIIRemXqUE~^f|lwtixDq~SgvYOLwjDbGE@Gz]QaPKYE{}oA[rbvFDymDKbnY~MAXFpyDSuP{lWtLQ\\owaJMxpV\\LLIqZ_||hUETi}}eBLNmvB[SfufJVBjbmlMdxyYXBt}SEgFdSKO}OFTgD^Ztgihqs]KGXhi^ptrbhl@_@Al@@";

        File fiQuery = new File(path, nameFile);
        DWARRecord recQuery = DWARFileHandlerHelper.get(fiQuery).get(0);
        String idcoord3D = recQuery.getAsString("idcoordinates3D");
        String conformerset = recQuery.getIdCode() + " " + idcoord3D;
        ConformerSet conformerSet = new ConformerSet(conformerset);


        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        String strPheSASingleConf = recQuery.getAsString(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName);

        PheSAMolecule pheSAMoleculeQueryPheSA = dhShape.decode(strPheSASingleConf);
        PheSAMolecule pheSAMoleculeQueryIDCoord3D = dhShape.createDescriptor(conformerSet);

        PheSAMolecule pheSAMoleculeLib = dhShape.decode(strPheSALib);

        float sim = dhShape.getSimilarity(pheSAMoleculeQueryPheSA, pheSAMoleculeQueryIDCoord3D);

        System.out.println("Similarity " + sim);

    }

    public static void error20210528Working() throws NoSuchFieldException, IOException {

        String path = "C:\\Users\\korffmo1\\git\\VirtualScreening\\virtualscreening\\src\\resources\\testdatavs\\phesa\\test01\\";

        String nameFile = "ACE_Query_CrystalStructureDescriptors.dwar";

        File fiQuery = new File(path, nameFile);
        DWARRecord recQuery = DWARFileHandlerHelper.get(fiQuery).get(0);

        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        String conformerset = recQuery.getAsString(ConstantsDWAR.TAG_CONFORMERSET);
        // String s = recQuery.getIdCode() + " " + sCoord3D;
        ConformerSet conformerSet = new ConformerSet(conformerset);

        PheSAMolecule pheSAMoleculeQuery = dhShape.createDescriptor(conformerSet);


        // PheSAMolecule pheSAMoleculeLib = dhShape.decode(strPheSASingleConf);

        float sim = dhShape.getSimilarity(pheSAMoleculeQuery, pheSAMoleculeQuery);

        System.out.println("Similarity " + sim);

    }

    /**
     * Error was in PheSA code.
     * @throws NoSuchFieldException
     * @throws IOException
     */
    public static void error20200306() throws NoSuchFieldException, IOException {

        int line = 432;

        File fiDWARQuery = new File("/home/korffmo1/Projects/Software/Development/Descriptors/ShapeAlignment/Errors/20200306_JW/active.dwar");
        File fiDWARLib = new File("/home/korffmo1/Projects/Software/Development/Descriptors/ShapeAlignment/Errors/20200306_JW/actives_final.dwar");

        List<PheSAMolecule> liQuery = get(fiDWARQuery, 0);

        List<PheSAMolecule> liLibrary = get(fiDWARLib, 0);

        PheSAMolecule pheSAMoleculeQuery = liQuery.get(0);

        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        DWARFileHandler fhLibrary = new DWARFileHandler(fiDWARLib);
        DWARFileHandler fhQuery = new DWARFileHandler(fiDWARQuery);

        while (fhLibrary.hasMore()){

            DWARRecord recLib = fhLibrary.next();




        }

        for (int i = line; i < liLibrary.size(); i++) {
                PheSAMolecule pheSAMoleculeLib = null;
                try {
                    pheSAMoleculeLib = liLibrary.get(i);

                    double sim = dhShape.getSimilarity(pheSAMoleculeQuery, pheSAMoleculeLib);
                    System.out.println(i +  "\t" + Formatter.format3(sim));
                } catch (Exception e) {
                    double sim = dhShape.getSimilarity(pheSAMoleculeQuery, pheSAMoleculeLib);

                    e.printStackTrace();
                }
        }
    }

    public static void test01(String[] args) {

        List<String> liIdCode = new ArrayList<>();

        liIdCode.add("f`qA`@@HudTRbRVaTIPRfjjifjqaDUH@");
        liIdCode.add("f`qA`@@HudTRbRVaTIPRfjjifjqaDUD@");
        liIdCode.add("en]PN@H@DKHT\\ZT^rJIQIIQQQEPqHjIZSQUPb`QRHhZZ{yjjjijejjjjfijjTpaBhPdTaTFTrNWt@@");
        liIdCode.add("ehVRL@DDEO`PIkeI~gHhhhdhdheEddmCdcBNECLfjeamckfjjjhHHHHJfjBXpQDXgDTQd@@");


        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        ConformerSetGenerator conformerSetGenerator = new ConformerSetGenerator();

        IDCodeParser parser3DCoordinates = new IDCodeParser();

        List<PheSAMolecule> liPheSAMolecule = new ArrayList<>();
        for (String idcode : liIdCode) {

            StereoMolecule mol = parser3DCoordinates.getCompactMolecule(idcode);

            mol.stripSmallFragments();
            mol.ensureHelperArrays(Molecule.cHelperRings);

            System.out.println("Rot bnds: " + mol.getRotatableBondCount());
            // mol.ensureHelperArrays(Molecule.cHelperCIP);

            ConformerSet conformerSet = conformerSetGenerator.generateConformerSet(mol);

            String idcodeConformers = conformerSet.toString();

            int start = idcodeConformers.indexOf(" ") + 1;

            String idcodeNew = idcodeConformers.substring(0, start-1);
            String sCoord3D = idcodeConformers.substring(start);

            if(!idcode.equals(idcodeNew)){
                System.out.println("IdCode differs! " + idcode + "\t" + idcodeNew);
            }

            String s = idcodeNew + " " + sCoord3D;

            ConformerSet conformerSetNew = new ConformerSet(s);

            PheSAMolecule pheSAMolecule = dhShape.createDescriptor(conformerSetNew);
            liPheSAMolecule.add(pheSAMolecule);
        }


        for (int i = 0; i < liPheSAMolecule.size(); i++) {
            for (int j = i+1; j < liPheSAMolecule.size(); j++) {
                double sim = dhShape.getSimilarity(liPheSAMolecule.get(j), liPheSAMolecule.get(i));
                System.out.println(i +  "\t" + Formatter.format3(sim));
            }
        }
    }


    private static List<PheSAMolecule> get(File fiDWAR, int n) throws NoSuchFieldException, IOException {

        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);
        DWARFileHandler fh = new DWARFileHandler(fiDWAR);

        List<PheSAMolecule> liPheSAMolecule = new ArrayList<>((int)fh.size());

        System.out.println("Decode " + fh.size()  + " PheSA descriptors.");

        int cc=0;
        while(fh.hasMore()) {
            DWARRecord record = fh.next();
            String conformerSet = record.getAsString(ConstantsDWAR.TAG_CONFORMERSET);
            System.out.println(cc + "\t" + record.getIdCode() + "\t" + conformerSet);
            PheSAMolecule pheSAMolecule = convertString2PheSA.getNative(record);
            liPheSAMolecule.add(pheSAMolecule);
            cc++;
            if(cc%100==0) {
                System.out.println("Pulled " + cc);
            }

            if(cc==n){
                break;
            }
        }

        fh.close();

        return liPheSAMolecule;
    }

    private static class RunCalcShapeSim implements Runnable {

        Pipeline<DWARRecord> pipelineDWAR;

        PheSAMolecule pheSAMoleculeQuery;

        boolean flexalign;

        public RunCalcShapeSim(Pipeline<DWARRecord> pipelineDWAR, PheSAMolecule pheSAMoleculeQuery, boolean flexalign) {
            this.pipelineDWAR = pipelineDWAR;
            this.pheSAMoleculeQuery = pheSAMoleculeQuery;
            this.flexalign = flexalign;
        }

        @Override
        public void run() {

            DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

            dhShape.setFlexible(flexalign);

            ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);

            while (!pipelineDWAR.wereAllDataFetched()){

                DWARRecord record = pipelineDWAR.pollData();

                if(record==null){
                    try {Thread.sleep(10);} catch (InterruptedException e) {e.printStackTrace();}
                    continue;
                }

                PheSAMolecule pheSAMoleculeLib = convertString2PheSA.getNative(record);

                double sim = dhShape.getSimilarity(pheSAMoleculeQuery, pheSAMoleculeLib);

                System.out.println(record.getID() +  "\t" + Formatter.format3(sim));
            }
        }
    };
}
