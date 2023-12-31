package org.openchemlib.chem.vs.test;

import java.util.Random;

import chem.descriptor.vs.eval.testdata.VSEnrichment;
import com.actelion.research.util.Formatter;

public class VSEnrichmentSimulator {


	private static final int SIZE_DB = 1000;

	private static final double FRACTION_HITS = 0.01;

	public static void main(String[] args) {

		VSEnrichment vsEnrich = createTopResults();

		toString(vsEnrich);


	}


	public static void toString(VSEnrichment vsEnrich) {

		System.out.println(vsEnrich.toString());

		System.out.println(vsEnrich.toStringEnrichment());

		System.out.println("Score " + Formatter.format3(vsEnrich.getScoreRelativeTopRankingDerivation()));
	}

	public static VSEnrichment createRND(){

		String sClassQuery = "Query";
		String sInactive = "Inactive";

		int sizeDB = SIZE_DB;

		double fracLigands = FRACTION_HITS;

		VSEnrichment vsEnrich = new VSEnrichment(sClassQuery, sizeDB);
		vsEnrich.setDescriptor("simulated");

		double threshHit = sizeDB * fracLigands;

		Random rnd = new Random();
		for (int i = 0; i < sizeDB; i++) {

			double score = rnd.nextDouble();

			if(i <= threshHit){
				vsEnrich.addResult(sClassQuery, i, score);
			} else {
				vsEnrich.addResult(sInactive, i, score);
			}
		}

		vsEnrich.evaluate();

		return vsEnrich;
	}

	public static VSEnrichment createTopResults(){

		String sClassQuery = "Query";
		String sInactive = "Inactive";

		int sizeDB = SIZE_DB;

		double fracLigands = FRACTION_HITS;

		VSEnrichment vsEnrich = new VSEnrichment(sClassQuery, sizeDB);

		vsEnrich.setDescriptor("simulated");

		int hits = (int)(sizeDB * fracLigands + 0.5);

		double score = 1.0;

		double increment = score / sizeDB;

		for (int i = 0; i < hits; i++) {
			vsEnrich.addResult(sClassQuery, i, score);

			score -= increment;
		}

		for (int i = hits; i < sizeDB; i++) {

			vsEnrich.addResult(sInactive, i, score);

			score -= increment;
		}

		vsEnrich.evaluate();

		return vsEnrich;
	}

	public static VSEnrichment createGoodResults(){

		// Changing this value will change the enrichment
		double threshScoreHit = 0.99;

		String sClassQuery = "Query";
		String sInactive = "Inactive";

		int sizeDB = SIZE_DB;

		double fracLigands = FRACTION_HITS;

		double factor = 1.0 / (1.0 - threshScoreHit);

		VSEnrichment vsEnrich = new VSEnrichment(sClassQuery, sizeDB);

		vsEnrich.setDescriptor("simulated");

		int hits = (int)(sizeDB * fracLigands + 0.5);

		Random rnd = new Random();

		for (int i = 0; i < hits; i++) {

			double score = threshScoreHit + (rnd.nextDouble() / factor);

			vsEnrich.addResult(sClassQuery, i, score);

		}

		for (int i = hits; i < sizeDB; i++) {

			double score = rnd.nextDouble();

			vsEnrich.addResult(sInactive, i, score);

		}

		vsEnrich.evaluate();

		return vsEnrich;
	}


	public static void partialHits() {


		// int [] arrHitPatternStart = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

		// int [] arrHitPatternStart = {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

		// int [] arrHitPatternStart = {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

		// int [] arrHitPatternStart = {0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1};

//		int [] arrHitPatternStart = {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
//
		int [] arrHitPatternStart = {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};


		String sClassQuery = "Query";

		String sInactive = "Inactive";

		int sizeDB = 100;

		VSEnrichment vsEnrich = new VSEnrichment(sClassQuery, sizeDB);
		vsEnrich.setDescriptor("simulated");

		double factor = 1.0 / sizeDB;

		int id = 0;

		for (int i = 0; i < arrHitPatternStart.length; i++) {


			double similarity = (sizeDB - i) / factor;

			if(arrHitPatternStart[i]==1){
				vsEnrich.addResult(sClassQuery, i, similarity);
			} else {
				vsEnrich.addResult(sInactive, i, similarity);
			}
		}

		for (int i = arrHitPatternStart.length; i < sizeDB; i++) {

			double similarity = (sizeDB - i) / factor;

			vsEnrich.addResult(sInactive, i, similarity);
		}



		vsEnrich.evaluate();

		System.out.println(vsEnrich.toString());
		System.out.println("Score " + Formatter.format3(vsEnrich.getScoreRelativeTopRankingDerivation()));

//		double frac = 0.01;
//		System.out.println("Fraction " + frac + " enrichment " + vsEnrich.getEnrichmentFactor(frac));
//		frac = 0.2;
//		System.out.println("Fraction " + frac + " enrichment " + vsEnrich.getEnrichmentFactor(frac));
	}

	
	public static void fixTest() {
		
		String sClassQuery = "Query";
		String sInactive = "Inactive";
		
		int sizeDB = 100;
		
		VSEnrichment vsEnrich = new VSEnrichment(sClassQuery, sizeDB);
		vsEnrich.setDescriptor("simulated");
		
		
		
		double scHit = 0.1;
		double scNoHit = 0.01;
		
		for (int i = 0; i < sizeDB; i++) {
			if(i<10){
				vsEnrich.addResult(sClassQuery, i, scHit);
			} else {
				vsEnrich.addResult(sInactive, i, scNoHit);
			}
		}
		
		vsEnrich.evaluate();
		
		System.out.println(vsEnrich.toString());
		System.out.println("Score " + Formatter.format3(vsEnrich.getScoreRelativeTopRankingDerivation()));
		
//		double frac = 0.01;
//		System.out.println("Fraction " + frac + " enrichment " + vsEnrich.getEnrichmentFactor(frac));
//		frac = 0.2;
//		System.out.println("Fraction " + frac + " enrichment " + vsEnrich.getEnrichmentFactor(frac));
	}
	
	
}
