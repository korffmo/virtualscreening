package com.actelion.research.chem.reaction;

import java.util.ArrayList;
import java.util.List;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.util.ArrayUtils;

/**
 * CentralAtomRemover
 * Written to remove a central atom (Zn) in a complex.
 * The coordinating atoms are set to fragments.
 * The resulting atoms can be used for a substructure based virtual screening.
 * Developed for anti-infectives project
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Apr 16, 2014 MvK Start implementation
 */
public class CentralAtomRemover {

	/**
	 * 
	 */
	public CentralAtomRemover() {
		// TODO Auto-generated constructor stub
	}
	
	
	public StereoMolecule [] remove(StereoMolecule mol, int atomicNo){
				
		mol.setFragment(true);
				
		List<Integer> liIndexAtom2Remove = new ArrayList<Integer>();
		
		for (int indexAt = 0; indexAt < mol.getAtoms(); indexAt++) {
			
			if(mol.getAtomicNo(indexAt) == atomicNo){
				
				liIndexAtom2Remove.add(indexAt);
				
				int nConnected = mol.getConnAtoms(indexAt);
				
				for (int j = 0; j < nConnected; j++) {
					
					int indexConnected = mol.getConnAtom(indexAt, j);
								
					mol.setAtomMarker(indexConnected, true);
					
				}
			}
			
		}
				
		int [] atomList = ArrayUtils.toIntArray(liIndexAtom2Remove);
				
		mol.deleteAtoms(atomList);
		
		mol.ensureHelperArrays(Molecule.cHelperRings);
		
		StereoMolecule [] arrFrags = mol.getFragments();
		
		for (int i = 0; i < arrFrags.length; i++) {
			
			StereoMolecule frag = arrFrags[i];
			
			frag.ensureHelperArrays(Molecule.cHelperRings);
			
			for (int indexAt = 0; indexAt < frag.getAtoms(); indexAt++) {
				
				if(frag.isMarkedAtom(indexAt)){
					
					frag.setAtomQueryFeature(indexAt, Molecule.cAtomQFNoMoreNeighbours, true);
					
					if(frag.isRingAtom(indexAt)){
						
						frag.setAtomQueryFeature(indexAt, Molecule.cAtomQFNotChain, true);
						
					} else {
						
						frag.setAtomQueryFeature(indexAt, Molecule.cAtomQFNotChain, false);
		    			
						frag.setAtomQueryFeature(indexAt, Molecule.cAtomQFNot2RingBonds, true);
						frag.setAtomQueryFeature(indexAt, Molecule.cAtomQFNot3RingBonds, true);
						frag.setAtomQueryFeature(indexAt, Molecule.cAtomQFNot4RingBonds, true);
					}
				}
			}
		}
		
		return arrFrags;
	}
	

}
