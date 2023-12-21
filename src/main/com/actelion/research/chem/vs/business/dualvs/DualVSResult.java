package com.actelion.research.chem.vs.business.dualvs;

/**
 * DualVSResult
 * <p>Copyright: Actelion Pharmaceuticals Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * Created by korffmo1 on 04.04.16.
 */
public class DualVSResult {

    private long substanceId;


    private float [][] arrSimilaritySampleInclude;

    private int [] arrIdSampleInclude;

    private float [][] arrSimilaritySampleExclude;

    private int [] arrIdSampleExclude;

    public DualVSResult(long substanceId, float [][] arrSimilaritySampleInclude, int [] arrIdSampleInclude, float [][] arrSimilaritySampleExclude, int [] arrIdSampleExclude) {

        this.substanceId = substanceId;

        this.arrSimilaritySampleInclude = arrSimilaritySampleInclude;

        this.arrIdSampleInclude = arrIdSampleInclude;

        this.arrSimilaritySampleExclude = arrSimilaritySampleExclude;

        this.arrIdSampleExclude = arrIdSampleExclude;

    }

    public long getSubstanceId() {
        return substanceId;
    }

    public float[][] getArrSimilaritySampleInclude() {
        return arrSimilaritySampleInclude;
    }

    public int[] getArrIdSampleInclude() {
        return arrIdSampleInclude;
    }

    public float[][] getArrSimilaritySampleExclude() {
        return arrSimilaritySampleExclude;
    }

    public int[] getArrIdSampleExclude() {
        return arrIdSampleExclude;
    }
}
