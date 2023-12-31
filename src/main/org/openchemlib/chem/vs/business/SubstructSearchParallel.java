package org.openchemlib.chem.vs.business;


/**
 * 
 * SubstructSearchParallel
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * 10 Nov 2008 MvK: Start implementation
 *  5 May 2011 MvK: SubstructSearch-->SubstructSearchParallel
 */

import com.actelion.research.calc.ThreadTask;
import com.actelion.research.chem.IDCodeParser;
import com.actelion.research.chem.SSSearcherWithIndex;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.descriptor.DescriptorHandlerFFP512;
import com.actelion.research.chem.descriptor.MolFragFp;
import com.actelion.research.chem.descriptor.TwoMolFragFp;
import com.actelion.research.chem.dwar.*;
import com.actelion.research.chem.filter.TwoMolecules;
import com.actelion.research.util.ErrorHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubstructSearchParallel extends ThreadTask {

	private static final long SLEEP = 1000;
	
	private ModelSubStructSearch model;
	
	private DescriptorHandlerFFP512 dh512;
	
	public SubstructSearchParallel(ModelSubStructSearch model) {
		super(model.getLog());
		
		this.model = model;
		
		dh512 = DescriptorHandlerFFP512.getDefaultInstance();
		
	}
	

    public void run() {

        try {
        	
        	model.setRunning(true);
        	
            log.append("Start substructure search for source " + model.getSource() + ".");
            
            search();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            log.append(ex.getMessage());
        	model.setRunning(false);
            return;
        }
    	model.setRunning(false);
    }
    
	public void search() throws IOException  {
		
		ErrorHashMap ehm = new ErrorHashMap();
		
		
		try {
			DWARInterface dwarInterface = new DWARFileHandler(new File(model.getSource()));
			
			dwarInterface.reset();
			
			long numMols = dwarInterface.size();
			
			log.append("Number of molecules to search " + numMols + ".");
			
	
			
			DWARHeader header = DWARHeader.getStructureDWARHeader();
			
			header.add(dwarInterface.getHeader());
			
			DWARFileWriter fw = new DWARFileWriter(model.getFileResultSubstructureSearch(), header);
			
			
	        List<SubStructSearchThread> liSubStructSearchThread = new ArrayList<SubStructSearchThread>();
	        
	        int nProcessors = Runtime.getRuntime().availableProcessors();
	        // int nProcessors = 1;
	        
	        for (int i = 0; i < nProcessors; i++) {
	        	
	    		List<TwoMolFragFp> liFpIncludeExclude = createFragFp(model.getListIncludeExclude());
	    		
	        	SubStructSearchThread subStructSearchThread = new SubStructSearchThread(dwarInterface, liFpIncludeExclude, fw);
	        	
	        	new Thread(subStructSearchThread).start();
	        	
	        	liSubStructSearchThread.add(subStructSearchThread);
			}
			
	        int nProcessedLast = 0;
			while(!areThreadsFinished(liSubStructSearchThread)){
				
				try {
					
					processInfo(model, liSubStructSearchThread);
					
					if((model.getProcessed()-nProcessedLast) > 10000){
						String sMessage = "Accepted "  + model.getAccepted() + ", substructure not found "  + model.getNotFound()+ ", excluded "  + model.getExcluded() + " and processed "  + model.getProcessed() + " molecules.";
						log.append(sMessage);
						
						nProcessedLast = model.getProcessed();
						
//						for (int i = 0; i < liSubStructSearchThread.size(); i++) {
//							SubStructSearchThread subStructSearchThread = liSubStructSearchThread.get(i);
//							
//							System.out.println("Thread " + i + " processed " + subStructSearchThread.getProcessed());
//							
//						}
						
					}
					
					if(model.isStop()) {
						
						for(SubStructSearchThread subStructSearchThread : liSubStructSearchThread) {
							subStructSearchThread.setStop(true);
						}
					}
					
					Thread.sleep(SLEEP);
					
//					for (int i = 0; i < liSubStructSearchThread.size(); i++) {
//						SubStructSearchThread subStructSearchThread = liSubStructSearchThread.get(i);
//						if(subStructSearchThread.isFinished()){
//							System.out.println("Thread " + i + " finished.");
//						}
//					}
					
					
				} catch (Exception ex) {
					log.append(ex);
					ehm.add(ex);
				}
			}

			synchronized(dwarInterface){
				dwarInterface.close();	
			}
			
			synchronized(fw){
				fw.close();
			}
			
			processInfo(model, liSubStructSearchThread);
			
			String sMessage = "Finally accepted "  + model.getAccepted() + ", substructure not found "  + model.getNotFound()+ ", excluded "  + model.getExcluded() + " and processed "  + model.getProcessed() + " molecules.";
			log.append(sMessage);
			
			log.updateProgress(0);
			
			for(SubStructSearchThread subStructSearchThread : liSubStructSearchThread) {
				if(subStructSearchThread.getEhm().hasErrors()){
					log.append("Errors is substructure search thread");
					log.append(subStructSearchThread.getEhm().toString());
				}
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(ehm.hasErrors())
			log.append(ehm.toString());

	}
	
	private List<TwoMolFragFp> createFragFp(List<TwoMolecules> liTwoMolecules){
		
		List<TwoMolFragFp> li = new ArrayList<TwoMolFragFp>();
		
		for (TwoMolecules tm : liTwoMolecules) {
			
			MolFragFp mff1 = createMolFragFp(tm.molInclude);
			
			MolFragFp mff2 = null;
			
			if(tm.molExclude != null){
				mff2 = createMolFragFp(tm.molExclude);
			}
			
			li.add(new TwoMolFragFp(mff1, mff2));
			
		}
		
		return li;
	}
	
	private MolFragFp createMolFragFp(StereoMolecule mol){
		
		MolFragFp mff = new MolFragFp();
		
		mff.mol = mol;
		
		mff.arr = (int [])dh512.createDescriptor(mff.mol);
		
		
		return mff;
	}
	
	
	private static void processInfo(ModelSubStructSearch model, List<SubStructSearchThread> liSubStructSearchThread) {
		
		int nProcessed = 0;
		int nAccepted = 0;
		int nNotFound = 0;
		int nExcluded = 0;

		for(SubStructSearchThread subStructSearchThread : liSubStructSearchThread) {
			nProcessed += subStructSearchThread.getProcessed();
			nAccepted += subStructSearchThread.getAccepted();
			nNotFound += subStructSearchThread.getNotFound();
			nExcluded += subStructSearchThread.getExcluded();
		}
		
		model.getLog().updateProgress(nProcessed);
		
		model.setProcessed(nProcessed);
		model.setAccepted(nAccepted);
		model.setNotFound(nNotFound);
		model.setExcluded(nExcluded);
	}
	
	class SubStructSearchThread implements Runnable {

		private SSSearcherWithIndex sss;
		
		private DescriptorHandlerFFP512 dh512;

		
		private DWARInterface dwarInterface;
		
		private IDCodeParser idCodeParser;
		
		private DWARFileWriter fw;
		
		private List<TwoMolFragFp> liFpIncludeExclude;
		
		private boolean stop;
		
		private boolean finished;
		
		private long ccProcessed;
		
		private long ccAccepted;
		
		private long ccNotFound;
		
		private long ccExcluded;
		
		private ErrorHashMap ehm;
		
		public SubStructSearchThread(DWARInterface odeInterface, List<TwoMolFragFp> liFpIncludeExclude, DWARFileWriter fw) {
			
			this.dwarInterface = odeInterface;
			
			this.fw = fw;
			
			this.liFpIncludeExclude = liFpIncludeExclude;
			
			this.sss = new SSSearcherWithIndex();
			
			this.dh512 = new DescriptorHandlerFFP512();
			
			ehm = new ErrorHashMap();
			
			idCodeParser = new IDCodeParser();
			
			finished = false;
			
			stop = false;
		}
		
		public void run() {
			
			try {
				finished = false;
				
				DWARRecord rec = null;
				
				synchronized(dwarInterface) {
					if(dwarInterface.hasMore()) {
						rec = dwarInterface.next();
					}
				}
				
				while(rec!=null) {
					
					if(stop){
						break;
					}
					
					ccProcessed++;
					
					try {
						MolFragFp molFragFp = new MolFragFp();
						
						String sFragFp = rec.getAsString(dh512.getInfo().shortName);
						
						molFragFp.arr = (int[])dh512.decode(sFragFp);
						
						molFragFp.mol = idCodeParser.getCompactMolecule(rec.getIdCode());
						
						boolean bAllFound= isMatch(molFragFp);
						
						if(bAllFound) {
							synchronized(fw) {
								fw.write(rec);
							}
							ccAccepted++;
						} 
						
						if(model.isStop()) {
							break;
						}

					} catch (Exception ex) {
						ex.printStackTrace();
						ehm.add(ex);
					}
					
					rec=null;
					
					synchronized(dwarInterface) {
						if(dwarInterface.hasMore()) {
							rec = dwarInterface.next();
						}
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				finished = true;
			}
		}

		public boolean isFinished() {
			return finished;
		}

		public ErrorHashMap getEhm() {
			return ehm;
		}

		private boolean isMatch(MolFragFp molFragFp){
			
			boolean isMatch=true;
			
			sss.setMolecule(molFragFp.mol, molFragFp.arr);
			
			boolean bFoundExclude=false;
			
			boolean bFound = false;
			
			for(TwoMolFragFp twoMFF : liFpIncludeExclude) {
				
				sss.setFragment(twoMFF.molFragFp1.mol, twoMFF.molFragFp1.arr);
				
				if(sss.isFragmentInMolecule()) {
					
					bFound = true;
					
					if(twoMFF.molFragFp2 != null && twoMFF.molFragFp2.mol!=null) {
					
						sss.setFragment(twoMFF.molFragFp2.mol, twoMFF.molFragFp2.arr);
						
						if(sss.isFragmentInMolecule()) {
							bFoundExclude=true;
							ccExcluded++;
							break;
						}
					}
				}
			}
			
			if(bFound==true)
				isMatch=true;
			else
				isMatch=false;
			
			if(bFoundExclude){
				isMatch=false;
			}
			
			return isMatch;
		}

		public long getProcessed() {
			return ccProcessed;
		}

		public long getAccepted() {
			return ccAccepted;
		}

		public long getNotFound() {
			return ccNotFound;
		}

		public long getExcluded() {
			return ccExcluded;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		
	}

	private static boolean areThreadsFinished(List<SubStructSearchThread> liSubStructSearchThread){
		boolean fin=true;
		
		for (SubStructSearchThread subStructSearchThread : liSubStructSearchThread) {
			if(!subStructSearchThread.isFinished()){
				fin=false;
				break;
			}
		}
		
		
		return fin;
	}

}
