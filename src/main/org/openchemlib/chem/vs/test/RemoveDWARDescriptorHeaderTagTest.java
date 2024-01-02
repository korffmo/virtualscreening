package org.openchemlib.chem.vs.test;

import org.openchemlib.chem.descriptor.flexophore.highres.DescriptorHandlerFlexophoreHighRes;
import com.actelion.research.chem.dwar.DWARFileHandlerHelper;
import com.actelion.research.chem.dwar.DWARHeader;
import com.actelion.research.util.ConstantsDWAR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Modest v. Korff

 * 31/10/2023 Start implementation
 **/
public class RemoveDWARDescriptorHeaderTagTest {

    public static void main(String[] args) throws IOException, NoSuchFieldException {

        File fiDWARBase = new File("C:\\Users\\korffmo1\\tmp\\data\\DWAR\\library100.dwar");

        DWARHeader headerBase = DWARFileHandlerHelper.getDWARHeader(fiDWARBase);

        for (String tag : headerBase.get()) {
            System.out.println(tag);
        }


        List<String> liDescriptor2Remove = new ArrayList<>();

//        for (DescriptorInfo di : DescriptorConstants.DESCRIPTOR_EXTENDED_LIST) {
//            String tag = di.shortName;
//            liDescriptor2Remove.add(tag);
//        }

        // liDescriptor2Remove.add(DescriptorHandlerPTree.getDefaultInstance().getInfo().shortName);
        liDescriptor2Remove.add(DescriptorHandlerFlexophoreHighRes.getDefaultInstance().getInfo().shortName);
        // liDescriptor2Remove.add(DescriptorHandlerFlexophoreHardPPP.getDefaultInstance().getInfo().shortName);

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

}
