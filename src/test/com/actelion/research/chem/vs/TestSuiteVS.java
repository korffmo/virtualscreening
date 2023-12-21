package com.actelion.research.chem.vs;

import static org.junit.runner.JUnitCore.runClasses;

/**
 * TestSuiteVS
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Sep 17, 2013 MvK Start implementation
 */
public class TestSuiteVS {

	public static void main(String[] args) {
		runClasses(VSJUnit.class);
		runClasses(GenDescriptorMulticorePhESATest.class);
		runClasses(VSPhESAJUnit.class);
		runClasses(VSPhESATest02JUnit.class);
		runClasses(VSSubStructJUnit.class);
		runClasses(VSMaxCommSubStructJUnit.class);
		System.out.println("Finished");
	}

}
