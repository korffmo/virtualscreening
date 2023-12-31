package org.openchemlib.chem.vs.test;

import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.chem.dwar.toolbox.export.ConvertString2PheSA;
import com.actelion.research.chem.phesa.DescriptorHandlerShape;
import com.actelion.research.chem.phesa.PheSAMolecule;

import java.io.File;
import java.io.IOException;

/**
 * Modest v. Korff

 * 28.05.2021 Start implementation
 **/
public class PheSACreateMain {

    public static void main(String[] args) throws NoSuchFieldException, IOException {


        String path = "C:\\Users\\korffmo1\\git\\VirtualScreening\\virtualscreening\\src\\resources\\testdatavs\\phesa\\test02\\";


        String pathQuery = path + "queryPheSA.dwar";

        File fiDWARQuery = new File(pathQuery);
        DescriptorHandlerShape dhShape = new DescriptorHandlerShape();

        ConvertString2PheSA convertString2PheSA = new ConvertString2PheSA(dhShape);


        DWARRecord record = DWARFileHandlerHelper.get(fiDWARQuery).get(0);
        PheSAMolecule pheSAMoleculeQuery = convertString2PheSA.getNative(record);
        System.out.println(dhShape.encode(pheSAMoleculeQuery));
    }
}
