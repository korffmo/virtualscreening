package org.openchemlib.chem.vs;

import static org.junit.runner.JUnitCore.runClasses;

/**
 * TestSuiteVS
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
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
