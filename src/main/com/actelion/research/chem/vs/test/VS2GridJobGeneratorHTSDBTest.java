package com.actelion.research.chem.vs.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * VS2GridJobGeneratorHTSDBTest
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * 17 Mar 2010 MvK: Start implementation
 */
public class VS2GridJobGeneratorHTSDBTest {
	public static void main(String[] args) {
		
		int n = 1001;
		int molsPerJob = 200;
		
		List<Integer> liSubstanceId = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++) {
			liSubstanceId.add(i+1);
		}
		
		
		for (int startIndex = 0; startIndex < liSubstanceId.size(); startIndex += molsPerJob) {
			
			int endIndex= startIndex + molsPerJob - 1;
			if(endIndex >= liSubstanceId.size()) {
				endIndex = liSubstanceId.size()-1;
			}
			
			int startSubstanceId = liSubstanceId.get(startIndex);
			
			int endSubstanceId = liSubstanceId.get(endIndex);
			
			System.out.println(startSubstanceId + "\t" + endSubstanceId);
			
		}

	}
}
