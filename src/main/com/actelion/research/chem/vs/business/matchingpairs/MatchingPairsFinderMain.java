package com.actelion.research.chem.vs.business.matchingpairs;

import com.actelion.research.chem.descriptor.DescriptorConstants;
import com.actelion.research.chem.descriptor.DescriptorHandlerFlexophore;
import com.actelion.research.chem.descriptor.flexophore.MolDistHist;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.chem.descriptor.vs.ConstantsVSEvaluation;
import com.actelion.research.chem.dwar.ModelVSRecord;
import com.actelion.research.util.Formatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MatchingPairsFinderMain


 * Use is subject to license terms.</p>
 * Created by korffmo1 on 31.03.16.
 */
public class MatchingPairsFinderMain {





    public static void main(String[] args) throws NoSuchFieldException, IOException {


        File fiDWARTestsSeedsDecoys = new File(args[0]);

        File fiDWARInactive = new File(args[1]);

        String tagFlexophoreV4 = DescriptorConstants.DESCRIPTOR_Flexophore.shortName;

        DescriptorHandlerFlexophore dhFlexophore = new DescriptorHandlerFlexophore();

        List<DWARRecord> liTestsSeedsDecoys = DWARFileHandlerHelper.get(fiDWARTestsSeedsDecoys);

        List<DWARRecord> liInactive = DWARFileHandlerHelper.get(fiDWARInactive);



        List<ModelVSRecord> liDescriptorsTest = new ArrayList<>();
        List<ModelVSRecord> liDescriptorsSeed = new ArrayList<>();
        List<ModelVSRecord> liDescriptorsDecoy = new ArrayList<>();

        List<ModelVSRecord> liDescriptorInactive = new ArrayList<>();

        int id = 0;
        for (DWARRecord rec : liTestsSeedsDecoys) {

            ModelVSRecord model = getModelVSRecord(rec, tagFlexophoreV4, dhFlexophore);

            model.setId(id++);

            String attr = rec.getAsString(ConstantsVSEvaluation.TAG_CLASS);

            if(attr.equalsIgnoreCase(ConstantsVSEvaluation.ATTR_TEST)) {

                liDescriptorsTest.add(model);

            } else if(attr.equalsIgnoreCase(ConstantsVSEvaluation.ATTR_SEED)) {

                liDescriptorsSeed.add(model);

            } else {

                liDescriptorsDecoy.add(model);

            }
        }



        id = 0;

        for (DWARRecord rec : liInactive) {

            ModelVSRecord model = getModelVSRecord(rec, tagFlexophoreV4, dhFlexophore);

            model.setId(id++);

            liDescriptorInactive.add(model);

        }
        List<ResultVS> liResultTest_Inactive = new ArrayList<>();

        for (ModelVSRecord modelTest : liDescriptorsTest) {

            MolDistHist mdhTest = (MolDistHist)modelTest.getArrDescriptors()[0];

            for (ModelVSRecord model : liDescriptorInactive) {

                MolDistHist mdhSeed = (MolDistHist)model.getArrDescriptors()[0];

                double sim = dhFlexophore.getSimilarity(mdhTest, mdhSeed);

                liResultTest_Inactive.add(new ResultVS(modelTest, model, sim));

            }
        }

        Collections.sort(liResultTest_Inactive);

        ResultVS resultVSMatchingPair = liResultTest_Inactive.get(liResultTest_Inactive.size()-1);

        System.out.println("Matching pair test:inactive ");

        System.out.println(resultVSMatchingPair.toString());


        MolDistHist mdhTest = (MolDistHist)resultVSMatchingPair.pair.model1.getArrDescriptors()[0];

        MolDistHist mdhMatchingInactive = (MolDistHist)resultVSMatchingPair.pair.model2.getArrDescriptors()[0];

        System.out.println("Matching pair test:seed ");

        for (ModelVSRecord model : liDescriptorsSeed) {

            MolDistHist mdhSeed = (MolDistHist)model.getArrDescriptors()[0];

            double simTestSeed = dhFlexophore.getSimilarity(mdhTest, mdhSeed);

            double simTestMatchingInactive_Seed = dhFlexophore.getSimilarity(mdhMatchingInactive, mdhSeed);


            System.out.println(Formatter.format2(simTestSeed) + "\t" + Formatter.format2(simTestMatchingInactive_Seed));
        }

        System.out.println("----------------------------------------------------------------------------");

        List<ResultVS> liResultTest_Decoy = new ArrayList<>();

        System.out.println("Matching pair test:descoy ");

        for (ModelVSRecord model : liDescriptorsDecoy) {

            MolDistHist mdh = (MolDistHist)model.getArrDescriptors()[0];

            double simTestDecoy = dhFlexophore.getSimilarity(mdhTest, mdh);

            double simTestMatchingInactive_Decoy = dhFlexophore.getSimilarity(mdhMatchingInactive, mdh);

            System.out.println(Formatter.format2(simTestDecoy) + "\t" + Formatter.format2(simTestMatchingInactive_Decoy));
        }





    }

    private static ModelVSRecord getModelVSRecord(DWARRecord rec, String tagFlexophoreV4, DescriptorHandlerFlexophore dhFlexophore) {

        String descr = rec.getAsString(tagFlexophoreV4);

        MolDistHist mdh = dhFlexophore.decode(descr);

        ModelVSRecord model = new ModelVSRecord();

        Object [] arrDescriptor = new Object[1];

        arrDescriptor[0] = mdh;

        model.setArrDescriptors(arrDescriptor);


        return model;

    }

    private static class ResultVS implements Comparable<ResultVS> {

        Pair pair;

        double score;

        public ResultVS(ModelVSRecord model1, ModelVSRecord model2, double score) {

            pair = new Pair(model1, model2);

            this.score = score;
        }

        @Override
        public int compareTo(ResultVS o) {

            int cmp = 0;

            if(score > o.score){

                cmp = 1;

            } else if(score < o.score){

                cmp = -1;

            }

            return cmp;
        }

        @Override
        public String toString() {

            final StringBuilder sb = new StringBuilder();

            sb.append(pair.model2.getId());

            sb.append("\t");

            sb.append(Formatter.format2(score));

            return sb.toString();
        }
    }

    private static class Pair {

        ModelVSRecord model1;

        ModelVSRecord model2;

        public Pair(ModelVSRecord model1, ModelVSRecord model2) {

            this.model1 = model1;

            this.model2 = model2;

        }
    }


}
