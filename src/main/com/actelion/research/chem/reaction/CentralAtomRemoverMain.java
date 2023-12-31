package com.actelion.research.chem.reaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import com.actelion.research.chem.Canonizer;
import com.actelion.research.chem.PeriodicTable;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.dwar.DWARFileHandler;
import com.actelion.research.chem.dwar.DWARFileWriter;
import com.actelion.research.chem.dwar.DWARHeader;
import com.actelion.research.chem.dwar.DWARRecord;
import com.actelion.research.util.ConstantsDWAR;
import com.actelion.research.util.UserDirsDefault;
import com.actelion.research.util.UserDirsDefault;

/**
 * CentralAtomRemoverMain
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Apr 16, 2014 MvK Start implementation
 */
public class CentralAtomRemoverMain {

	
	private static final String DIR = UserDirsDefault.getTmp(0);
	
	private static final int MIN_NUM_COORDINATION_POINTS = 2;
	
	
	private static final String USAGE = "CentralAtomRemoverMain.\n" +
    	    
	    "Modest von Korff\n" +

	    "2014\n\n" +
	    
 	"Removes all atoms for the given atomic number. The ligand molecules are set to fragments. " +
 	"For coordinating atoms the flag i set 'no further substitution allowed'.\n" +
 	"Ligands with less than " + MIN_NUM_COORDINATION_POINTS + " coordination points are removed.\n" +
  	"Generates:\n" +
	    "A dwar output file\n" +
    "\n";

	
	public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
		
		
		if(args.length == 0 || args[0].equals("-h")){
	        System.out.println(USAGE);
	        System.exit(0);
		}
		
		File fiDWAR = new File(args[0]);
		
		if(!fiDWAR.isFile()){
			throw new FileNotFoundException("File '" + fiDWAR.getAbsolutePath() + "' not found.");
		}
		
		DWARFileHandler fh = new DWARFileHandler(fiDWAR);
		
		int atomicNo = PeriodicTable.number("Zn");
		
		if(args.length == 2){
			atomicNo = Integer.parseInt(args[1]);
		}
		
		System.out.println("All atoms with atomic no " + atomicNo + " (" + PeriodicTable.symbol(atomicNo) +") will be removed.");
		
		CentralAtomRemover centralAtomRemover = new CentralAtomRemover();
		
		HashSet<String> hsIdCodeLigands = new HashSet<String>();
		
		
		int ccParsed=0;
		int ccNoMol=0;
		int ccLigands=0;
		
		while(fh.hasMore()){
			
			DWARRecord rec = fh.next();
			
			ccParsed++;
			
			StereoMolecule mol = rec.getMolecule();
			
			if(mol==null){
				ccNoMol++;
				continue;
			}
			
			StereoMolecule [] arrLigands = centralAtomRemover.remove(mol, atomicNo);
			
			ccLigands += arrLigands.length;
			
			for (int i = 0; i < arrLigands.length; i++) {
				
				StereoMolecule frag = arrLigands[i];
				
				if(frag.getAtoms()>1){
					
					int nMarked=0;
					for (int j = 0; j < frag.getAtoms(); j++) {
						
						if(frag.isMarkedAtom(j)){
							nMarked++;
						}
						
					}
					
					if(nMarked >= MIN_NUM_COORDINATION_POINTS){
						Canonizer can = new Canonizer(arrLigands[i]);
						
						String idcode = can.getIDCode();
						
						hsIdCodeLigands.add(idcode);
					}
				}
			}
		}
		
		fh.close();
		
		String nameOut = "ligands" + ConstantsDWAR.DWAR_EXTENSION;
		
		File fiDWAROut = new File(DIR, nameOut);
		
		System.out.println("Parsed " + ccParsed + " records.");
		System.out.println("Records without molecule " + ccNoMol + ".");
		System.out.println("Extracted " + ccLigands + " ligands.");
		System.out.println("Unique ligands fulfilling criteria " + hsIdCodeLigands.size() + ".");
		
		System.out.println("The output is written to '" + fiDWAROut.getAbsolutePath() + "'.");
		
		DWARHeader header = DWARHeader.getStructureDWARHeader();
		
		
		DWARFileWriter fw = new DWARFileWriter(fiDWAROut, header);
		
		for(String idcode : hsIdCodeLigands){
			
			DWARRecord rec = new DWARRecord(header);
			
			rec.setIdCode(idcode);
		
			fw.write(rec);
			
		}
		
		
		fw.close();
		
		
		System.out.println("Finished");
	} 
	

}
