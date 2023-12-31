package com.actelion.research.chem.vs.business.dualvs;

/**
 * InfoDualVS
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0

 * Apr 01. 2016  MvK Start implementation
 */
public class InfoDualVS {


	private long nMolsdataBase;

	private long nMolsSamplesInclude;

	private long nMolsSamplesExclude;

	private long nScores2Calculate;

	private long nScoresCalculated;

	private long nHits;

	private long nMoleculesFound;


	/**
	 *
	 */
	public InfoDualVS() {
		// TODO Auto-generated constructor stub
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


	public long getMolsDataBase() {
		return nMolsdataBase;
	}

	public void setMolsDataBase(long nMolsdataBase) {
		this.nMolsdataBase = nMolsdataBase;
	}

	public long getMolsSamplesInclude() {
		return nMolsSamplesInclude;
	}

	public void setMolsSamplesInclude(long nMolsSamplesInclude) {
		this.nMolsSamplesInclude = nMolsSamplesInclude;
	}

	public long getMolsSamplesExclude() {
		return nMolsSamplesExclude;
	}

	public void setMolsSamplesExclude(long nMolsSamplesExclude) {
		this.nMolsSamplesExclude = nMolsSamplesExclude;
	}

	public boolean isDifference(InfoDualVS infoVS){

		boolean differ = false;
		
		if(nScoresCalculated != infoVS.nScoresCalculated) {
			differ=true;
		} else if(nHits != infoVS.nHits) {
			differ=true;
		} else if(nMoleculesFound != infoVS.nMoleculesFound) {
			differ=true;
		} 
		
		return differ;
	}

	
}
