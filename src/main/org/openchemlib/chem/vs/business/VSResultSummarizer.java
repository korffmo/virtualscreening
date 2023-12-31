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

import com.actelion.research.chem.descriptor.*;
import chem.descriptor.flexophore.hardppp.DescriptorHandlerFlexophoreHardPPP;
import chem.descriptor.flexophore.highres.DescriptorHandlerFlexophoreHighRes;
import com.actelion.research.chem.descriptor.pharmacophoretree.DescriptorHandlerPTree;
import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import chem.descriptor.vs.VSResultArray;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARFileWriterHelper;
import com.actelion.research.chem.dwar.DWARHeader;
import com.actelion.research.chem.dwar.disk.sort.DiskSortDWAR;
import com.actelion.research.chem.dwar.disk.compare.DiskCompareSortedSetsDWAR;
import com.actelion.research.chem.dwar.disk.compare.MergeSortedSetsDWAR;
import com.actelion.research.chem.dwar.disk.sort.DiskSetCreatorDWAR;
import com.actelion.research.chem.dwar.toolbox.merge.RowMerger;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.IO;
import com.actelion.research.util.datamodel.StrStr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * VSResultSummarizer
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 29.09.20.
 */
public class VSResultSummarizer {

    private static final String TAG_QUERY_STRUCTURE = "QueryStructure";

    private static final String PREFIX_QUERY = "Query";

    private File workdir;

    private List<ModelDescriptorVS> liModelDescriptorVS;

    //
    // Result files
    //
    private boolean skipDescriptorsInOutput;

    private File fiDWARVSResultOut;

    private File fiDWARVSResultBaseAndQueryStructures;

    private File fiDWARSummary;

    private File fiDWARVSResultNotSimilarBaseMolecules;


    public VSResultSummarizer(File workdir, List<ModelDescriptorVS> liModelDescriptorVS, File fiDWARVSResultOut, boolean skipDescriptorsInOutput) {
        this.workdir = workdir;
        this.liModelDescriptorVS = liModelDescriptorVS;
        this.fiDWARVSResultOut = fiDWARVSResultOut;
        this.skipDescriptorsInOutput = skipDescriptorsInOutput;

    }

    /**
     *
     * @param fiDWARBase
     * @param fiDWARQuery
     * @param tagQueryIdentifier
     * @param infoVS
     * @return
     * @throws IOException
     * @throws NoSuchFieldException
     */
    public File writeResultsVS(
            File fiDWARBase,
            File fiDWARQuery,
            String tagQueryIdentifier,
            InfoVS infoVS,
            String nameDWARResultElusive,
            String nameDWARResultSummary, boolean skipDescriptorsInOutput) throws IOException, NoSuchFieldException {

        System.out.println("VSResultHandler writeResultsVS fhDWARBase " + fiDWARBase.getAbsolutePath());
        System.out.println("VSResultHandler writeResultsVS  fhDWARQuery " + fiDWARQuery.getAbsolutePath());


        DiskSortDWAR externalSortDWAR = new DiskSortDWAR(workdir);

        // Sort vs results by base id (row number).
        File fiDWARVSResultOutSorted = new File(workdir, IO.getBaseName(fiDWARVSResultOut) + "Sorted" + ConstantsDWAR.DWAR_EXTENSION);

        externalSortDWAR.sort(fiDWARVSResultOut, VSResultArray.TAG_BASE_ID, fiDWARVSResultOutSorted);


        //
        // Merge the structures (idcode) to the vs results.
        // The PheSA descriptor is not copied to the output file.
        //
        DWARHeader headerBase = DWARFileHandlerHelper.getDWARHeader(fiDWARBase);

        String tagPheSA = DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName;
        if(headerBase.contains(tagPheSA)) {
            headerBase.remove(tagPheSA);
            System.out.println("Removed " + tagPheSA + " descriptor from VS output.");
        }

        if(skipDescriptorsInOutput){

            List<String> liDescriptor2Remove = new ArrayList<>();

            for (DescriptorInfo di : DescriptorConstants.DESCRIPTOR_EXTENDED_LIST) {
                String tag = di.shortName;
                liDescriptor2Remove.add(tag);
            }

            liDescriptor2Remove.add(DescriptorHandlerPTree.getDefaultInstance().getInfo().shortName);
            liDescriptor2Remove.add(DescriptorHandlerFlexophoreHighRes.getDefaultInstance().getInfo().shortName);
            liDescriptor2Remove.add(DescriptorHandlerFlexophoreHardPPP.getDefaultInstance().getInfo().shortName);

            System.out.println("Skip descriptors in output:");

            if(headerBase.contains(ConstantsDWAR.TAG_CONFORMERSET)) {
                headerBase.remove(ConstantsDWAR.TAG_CONFORMERSET);
                System.out.println("Removed " + ConstantsDWAR.TAG_CONFORMERSET + " from VS output.");
            }

            for (String tag : liDescriptor2Remove) {

                if(ConstantsDWAR.TAG_IDCODE2.equals(tag)){
                    continue;
                }

                if(headerBase.contains(tag)) {
                    headerBase.remove(tag);
                    System.out.println("Removed " + tag + " descriptor from VS output.");
                }
            }
        }

        MergeSortedSetsDWAR mergeSortedSetsDWAR = new MergeSortedSetsDWAR(workdir);

        mergeSortedSetsDWAR.mergeWithRowNumber(fiDWARVSResultOutSorted, VSResultArray.TAG_BASE_ID, fiDWARBase, headerBase);

        // File with vs results and the structures of the base molecules.
        File fiDWARMerged = mergeSortedSetsDWAR.getFiOutCommonElements();

        File fiDWARVSResultOutSortedForQueryRowNumber = new File(workdir, IO.getBaseName(fiDWARMerged) + "SortedByQueryRowNum" + ConstantsDWAR.DWAR_EXTENSION);

        // Sort by query row number.
        externalSortDWAR.sort(fiDWARMerged, VSResultArray.TAG_QUERY_ID, fiDWARVSResultOutSortedForQueryRowNumber);

        mergeSortedSetsDWAR.getFiOutCommonElements().delete();

        List<StrStr> liTagFile2_TagFileMerge = new ArrayList<>();

        liTagFile2_TagFileMerge.add(new StrStr(ConstantsDWAR.TAG_IDCODE2, TAG_QUERY_STRUCTURE));


        if((tagQueryIdentifier!=null) && (tagQueryIdentifier.length() > 0)){
            String tagIdentifierMergeFile = PREFIX_QUERY + tagQueryIdentifier;

            liTagFile2_TagFileMerge.add(new StrStr(tagQueryIdentifier, tagIdentifierMergeFile));
        }
        // Merge the query structures to the results. And, if available, the query molecule identifier.
        mergeSortedSetsDWAR.mergeWithRowNumber(fiDWARVSResultOutSortedForQueryRowNumber, VSResultArray.TAG_QUERY_ID, fiDWARQuery, liTagFile2_TagFileMerge);

        if(nameDWARResultElusive == null){
            String nameResult = IO.getBaseName(fiDWARVSResultOut.getName()) + "Elusive" + ConstantsDWAR.DWAR_EXTENSION;
            this.fiDWARVSResultBaseAndQueryStructures = new File(workdir, nameResult);
        } else {
            this.fiDWARVSResultBaseAndQueryStructures = new File(workdir, nameDWARResultElusive);
        }
        mergeSortedSetsDWAR.getFiOutCommonElements().renameTo(fiDWARVSResultBaseAndQueryStructures);


        //
        // Create summary file
        //

        if(nameDWARResultSummary == null){
            this.fiDWARSummary = new File(workdir, IO.getBaseName(fiDWARVSResultOut.getName()) + "Summary" + ConstantsDWAR.DWAR_EXTENSION);
        } else {
            this.fiDWARSummary = new File(workdir, nameDWARResultSummary);
        }

        File fiDWARVSResultOutSortedResultFile = new File(workdir, IO.getBaseName(fiDWARVSResultOut) + "SortedElusiveResultFile" + ConstantsDWAR.DWAR_EXTENSION);

        externalSortDWAR.sort(fiDWARVSResultOutSortedForQueryRowNumber, VSResultArray.TAG_BASE_ID, fiDWARVSResultOutSortedResultFile);

        List<String> liTag2Merge = getHeaderTags2Merge(liModelDescriptorVS);

        long nBaseHits = RowMerger.merge(fiDWARVSResultOutSortedResultFile, VSResultArray.TAG_BASE_ID, liTag2Merge, fiDWARSummary);

        infoVS.setMoleculesFound(nBaseHits);


        fiDWARVSResultOutSorted.delete();
        fiDWARVSResultOutSortedForQueryRowNumber.delete();
        fiDWARVSResultOutSortedResultFile.delete();

        return fiDWARVSResultBaseAndQueryStructures;
    }

    /**
     * Writes every record from <code>fhDWARBase</code> into the dwar result file what was not found in the virtual screening.
     * Used for library comparison.
     *
     * @param fiDWARBase
     * @param infoVS
     * @return
     * @throws IOException
     * @throws NoSuchFieldException
     */
    public File writeResultsLibraryComparison(File fiDWARBase, InfoVS infoVS, String nameDWARResultElusive) throws IOException, NoSuchFieldException {

        DiskSetCreatorDWAR externalSetCreatorDWAR = new DiskSetCreatorDWAR(workdir);

        File fiDWARVSResultSortedSet = externalSetCreatorDWAR.createSortedSet(fiDWARVSResultOut, VSResultArray.TAG_BASE_ID);

        DiskCompareSortedSetsDWAR externalCompareSortedSetsDWAR = new DiskCompareSortedSetsDWAR(workdir);

        externalCompareSortedSetsDWAR.compareForRowNumber(fiDWARBase, fiDWARVSResultSortedSet, VSResultArray.TAG_BASE_ID);

        infoVS.setMoleculesFound(externalCompareSortedSetsDWAR.getOnlyInSet1());

        String nameResult = IO.getBaseName(nameDWARResultElusive) + "NotSimilar2Query" + ConstantsDWAR.DWAR_EXTENSION;

        fiDWARVSResultNotSimilarBaseMolecules = new File(workdir, nameResult);

        DWARHeader headerBase = DWARFileHandlerHelper.getDWARHeader(fiDWARBase);

        String tagPheSA = DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName;

        if(headerBase.contains(tagPheSA)) {
            headerBase.remove(tagPheSA);
            System.out.println("!!! Removed " + tagPheSA + " descriptor from library comparison output!!!");
            DWARFileWriterHelper.write(fiDWARVSResultNotSimilarBaseMolecules, externalCompareSortedSetsDWAR.getFiOutOnlyInSet1(), headerBase);
        } else {
            externalCompareSortedSetsDWAR.getFiOutOnlyInSet1().renameTo(fiDWARVSResultNotSimilarBaseMolecules);
        }

        return fiDWARVSResultNotSimilarBaseMolecules;
    }

    public File getFiDWARVSResultBaseAndQueryStructures() {
        return fiDWARVSResultBaseAndQueryStructures;
    }

    public File getFiDWARSummary() {
        return fiDWARSummary;
    }

    public File getFiDWARVSResultNotSimilarBaseMolecules() {
        return fiDWARVSResultNotSimilarBaseMolecules;
    }

    private static List<String> getHeaderTags2Merge(List<ModelDescriptorVS> liModelDescriptorVS){

        List<String> li = new ArrayList<>();

        li.add(VSResultArray.TAG_QUERY_ID);

        for (ModelDescriptorVS mvs : liModelDescriptorVS) {
            String tagSimilarity = DescriptorHelper.getTagDescriptorSimilarity(mvs.getSimilarityCalculator());
            li.add(tagSimilarity);

            String shortName = mvs.getShortName();

            if(DescriptorConstants.DESCRIPTOR_ShapeAlign.shortName.equals(shortName)) {
                li.add(ConstantsPhESAParameter.TAG_PP_SIMILARITY);
                li.add(ConstantsPhESAParameter.TAG_SHAPE_SIMILARITY);
                li.add(ConstantsPhESAParameter.TAG_VOLUME_CONTRIBUTION);
            }
        }

        return li;
    }



}
