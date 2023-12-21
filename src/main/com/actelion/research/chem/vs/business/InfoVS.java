package com.actelion.research.chem.vs.business;

/**
 * InfoVS
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved
 * This software is the proprietary information of Actelion Pharmaceuticals, Ltd.
 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Sep 17, 2013 MvK Start implementation
 * Apr 01. 2016 Renamed LogVS --> InfoVS
 */
public class InfoVS {

	
	private long nMolsQuery;
	
	private long nMolsBase;
	
	private long nScores2Calculate;
	
	private long nScoresCalculated;
	
	private long nHits;
	
	private long nMoleculesFound;
	
	
	/**
	 * 
	 */
	public InfoVS() {

	}

	public InfoVS(long nMolsQuery, long nMolsBase, long nScores2Calculate, long nScoresCalculated, long nHits, long nMoleculesFound) {
		this.nMolsQuery = nMolsQuery;
		this.nMolsBase = nMolsBase;
		this.nScores2Calculate = nScores2Calculate;
		this.nScoresCalculated = nScoresCalculated;
		this.nHits = nHits;
		this.nMoleculesFound = nMoleculesFound;
	}

	/**
	 * @return the nMolsQuery
	 */
	public long getMolsQuery() {
		return nMolsQuery;
	}


	/**
	 * @param nMolsQuery the nMolsQuery to set
	 */
	public void setMolsQuery(long nMolsQuery) {
		this.nMolsQuery = nMolsQuery;
	}


	/**
	 * @return the nMolsBase
	 */
	public long getMolsBase() {
		return nMolsBase;
	}


	/**
	 * @param nMolsBase the nMolsBase to set
	 */
	public void setMolsBase(long nMolsBase) {
		this.nMolsBase = nMolsBase;
	}


	/**
	 * @return the nScores2Calculate
	 */
	public long getScores2Calculate() {
		return nScores2Calculate;
	}


	public void setScoresCalculated(long n) {
		this.nScoresCalculated = n;
	}

	public long getScoresCalculated() {
		return nScoresCalculated;
	}


	/**
	 * @param nScores2Calculate the nScores2Calculate to set
	 */
	public void setScores2Calculate(long nScores2Calculate) {
		this.nScores2Calculate = nScores2Calculate;
	}

	/**
	 * @return the nHits
	 */
	public long getHits() {
		return nHits;
	}


	/**
	 * @param nHits the nHits to set
	 */
	public void setHits(long nHits) {
		this.nHits = nHits;
	}


	/**
	 * @return the nMoleculesFound
	 */
	public long getMoleculesFound() {
		return nMoleculesFound;
	}


	/**
	 * @param nMoleculesFound the nMoleculesFound to set
	 */
	public void setMoleculesFound(long nMoleculesFound) {
		this.nMoleculesFound = nMoleculesFound;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("InfoVS [MolsQuery=");
		sb.append(nMolsQuery);
		sb.append(", MolsBase=");
		sb.append(nMolsBase);
		sb.append(", Scores2Calculate=");
		sb.append(nScores2Calculate);
		sb.append(", ScoresCalculated=");
		sb.append(nScoresCalculated);
		sb.append(", Hits=");
		sb.append(nHits);
		sb.append(", MoleculesFound=");
		sb.append(nMoleculesFound);
		sb.append("]");
		return sb.toString();
	}
	
	
	public boolean isDifference(InfoVS infoVS){
		boolean differ = false;
		
		if(nMolsQuery != infoVS.nMolsQuery) {
			differ=true;
		} else if(nMolsBase != infoVS.nMolsBase) {
			differ=true;
		} else if(nScores2Calculate != infoVS.nScores2Calculate) {
			differ=true;
		} else if(nScoresCalculated != infoVS.nScoresCalculated) {
			differ=true;
		} else if(nHits != infoVS.nHits) {
			differ=true;
		} else if(nMoleculesFound != infoVS.nMoleculesFound) {
			differ=true;
		} 
		
		return differ;
	}

	
}
