package com.actelion.research.chem.vs.business.dualvs;

import com.actelion.research.chem.descriptor.vs.ModelDescriptorVS;
import com.actelion.research.chem.dwar.DWARInterface;
import com.actelion.research.util.LogHandler;

import java.io.File;
import java.util.List;

/**
 * ModelDualVS
 * <p>Copyright: Actelion Pharmaceuticals Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * Created by korffmo1 on 01.04.16.
 */
public class ModelDualVS {

    private File fiDWAROut;

    private List<ModelDescriptorVS> liDescriptorsForSamples2Include;

    private List<ModelDescriptorVS> liDescriptorsForSamples2Exclude;

    private LogHandler log;

    private File fiDWARDB;

    private File fiDWARSamplesInclude;

    private File fiDWARSamplesExclude;

    private boolean simpleVSMode;

    private boolean stereoDepletion;


    public boolean isStereoDepletion() {
        return stereoDepletion;
    }

    public void setStereoDepletion(boolean stereoDepletion) {
        this.stereoDepletion = stereoDepletion;
    }

    public File getFiDWAROut() {
        return fiDWAROut;
    }

    public void setFiDWAROut(File fiDWAROut) {
        this.fiDWAROut = fiDWAROut;
    }

    public List<ModelDescriptorVS> getLiDescriptorsForSamples2Include() {
        return liDescriptorsForSamples2Include;
    }

    public void setLiDescriptorsForSamples2Include(List<ModelDescriptorVS> liDescriptors) {
        this.liDescriptorsForSamples2Include = liDescriptors;
    }

    public List<ModelDescriptorVS> getLiDescriptorsForSamples2Exclude() {
        return liDescriptorsForSamples2Exclude;
    }

    public void setLiDescriptorsForSamples2Exclude(List<ModelDescriptorVS> liDescriptors) {
        this.liDescriptorsForSamples2Exclude = liDescriptors;
    }

    public LogHandler getLog() {
        return log;
    }

    public void setLog(LogHandler log) {
        this.log = log;
    }

    public File getFiDWARDB() {
        return fiDWARDB;
    }

    public void setFiDWARDB(File fiDWARDB) {
        this.fiDWARDB = fiDWARDB;
    }

    public File getFiDWARSamplesInclude() {
        return fiDWARSamplesInclude;
    }

    public void setFiDWARSamplesInclude(File fiDWARSamplesInclude) {
        this.fiDWARSamplesInclude = fiDWARSamplesInclude;
    }

    public File getFiDWARSamplesExclude() {
        return fiDWARSamplesExclude;
    }

    public void setFiDWARSamplesExclude(File fiDWARSamplesExclude) {
        this.fiDWARSamplesExclude = fiDWARSamplesExclude;
    }

    public boolean isSimpleVSMode() {
        return simpleVSMode;
    }

    public void setSimpleVSMode(boolean simpleVSMode) {
        this.simpleVSMode = simpleVSMode;
    }
}
