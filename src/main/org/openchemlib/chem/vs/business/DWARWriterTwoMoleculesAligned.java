package org.openchemlib.chem.vs.business;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.dwar.DWARFileWriter;
import com.actelion.research.chem.dwar.DWARHeader;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.util.Pipeline;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DWARWriterTwoMoleculesAligned
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 09.01.19.
 */
public class DWARWriterTwoMoleculesAligned {


    public static final String TAG_STRUCTURE1 = "Structure1";
    public static final String TAG_STRUCTURE2 = "Structure2";
    public static final String TAG_STRUCTURES_MERGED = "MergedStructures";
    public static final String TAG_COORD1 = "Coordinates1";
    public static final String TAG_COORD2 = "Coordinates2";
    public static final String TAG_COORD_MERGED = "CoordinatesStructMerged";

    private Pipeline<VSParallel.ModelTwoStereoMolecules> pipeShape2Mol;

    private File fiDWAROut;


    public DWARWriterTwoMoleculesAligned(Pipeline<VSParallel.ModelTwoStereoMolecules> pipeShape2Mol, File fiDWAROut) {
        this.pipeShape2Mol = pipeShape2Mol;
        this.fiDWAROut = fiDWAROut;
    }


    public void startWriteThread(){

        RunWrite runWrite = new RunWrite(pipeShape2Mol, fiDWAROut);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(runWrite);

        executorService.shutdown();
    }

    private static class RunWrite implements Runnable {

        private Pipeline<VSParallel.ModelTwoStereoMolecules> pipeShape2Mol;

        private File fiDWAR;

        public RunWrite(Pipeline<VSParallel.ModelTwoStereoMolecules> pipeShape2Mol, File fiDWAR) {

            this.pipeShape2Mol = pipeShape2Mol;

            this.fiDWAR = fiDWAR;
        }

        @Override
        public void run() {

            DWARHeader header = new DWARHeader();

            header.addStructureHeader(TAG_STRUCTURE1);
            header.addCoordinates3DHeader(TAG_STRUCTURE1, TAG_COORD1);

            header.addStructureHeader(TAG_STRUCTURE2);
            header.addCoordinates3DHeader(TAG_STRUCTURE2, TAG_COORD2);

            header.addStructureHeader(TAG_STRUCTURES_MERGED);
            header.addCoordinates3DHeader(TAG_STRUCTURES_MERGED, TAG_COORD_MERGED);


            try {

                DWARFileWriter fw = new DWARFileWriter(fiDWAR, header);

                while (!pipeShape2Mol.wereAllDataFetched()) {

                    VSParallel.ModelTwoStereoMolecules mtsm = pipeShape2Mol.pollData();

                    if(mtsm == null){
                        try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
                        continue;
                    }

                    DWARRecord record = new DWARRecord(header);

                    Canonizer canonizer1 = new Canonizer(mtsm.mol1);
                    record.addOrReplaceField(TAG_STRUCTURE1, canonizer1.getIDCode());
                    record.addOrReplaceField(TAG_COORD1, canonizer1.getEncodedCoordinates());

                    Canonizer canonizer2 = new Canonizer(mtsm.mol2);
                    record.addOrReplaceField(TAG_STRUCTURE2, canonizer2.getIDCode());
                    record.addOrReplaceField(TAG_COORD2, canonizer2.getEncodedCoordinates());


                    StereoMolecule molMerged = new StereoMolecule(mtsm.mol1);

                    molMerged.addMolecule(mtsm.mol2);

                    molMerged.ensureHelperArrays(Molecule.cHelperRings);

                    Canonizer canonizerMerged = new Canonizer(molMerged);
                    record.addOrReplaceField(TAG_STRUCTURES_MERGED, canonizerMerged.getIDCode());
                    record.addOrReplaceField(TAG_COORD_MERGED, canonizerMerged.getEncodedCoordinates());

                    fw.write(record);

                }

                fw.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("DWARWriterTwoMoleculesAligned finished");
        }
    }

}
