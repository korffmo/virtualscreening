package com.actelion.research.chem.vs.test;

/**
 * Modest v. Korff
 * Idorsia Pharmaceuticals Ltd.
 * 18/07/2023 Start implementation
 **/
public class ConstantsVSTestPheSA {
    public static final String PATH_PHESA = ConstantsVSTest.PATH_PREFIX + "phesa/test01/";
    public static final String PATH_LIBRAY_ACE100_DESCRIPTORS = PATH_PHESA + "ACE_100Descriptors.dwar";

    public static final String PATH_LIBRAY_DESCRIPTORS = PATH_PHESA + "basePhESADescriptors.dwar";
    /**
     * Created library file with three molecules. Molecules derived from query molecule.
     */
    public static final String PATH_LIBRAY = PATH_PHESA + "basePhESA.dwar";
    /**
     * Test query from Joel Wahl
     */
    public static final String PATH_QUERY = PATH_PHESA + "queryPhESASingleConf.dwar";
    public static final String PATH_ACE010Descriptors = PATH_PHESA + "ACE_010Descriptors.dwar";
    public static final String NAME_QUERY_ACE3DCrystal = "ACE_Query_CrystalStructure.dwar";
    public static final String PATH_QUERY_ACE3DCrystal = ConstantsVSTest.PATH_PREFIX + NAME_QUERY_ACE3DCrystal;
    public static final String NAME_QUERY_ACE3DCrystalDescriptors = "ACE_Query_CrystalStructureDescriptors.dwar";

    public static final String PATH_QUERY_ACE3DCrystalDescriptors = PATH_PHESA + NAME_QUERY_ACE3DCrystalDescriptors;
}
