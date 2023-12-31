package com.actelion.research.chem.vs.business.batch;

import com.actelion.research.chem.vs.business.xml.ModelVSXML;
import com.actelion.research.util.FileUtils;
import com.actelion.research.util.IO;
import com.actelion.research.util.UserDirsDefault;
import com.actelion.research.util.UserDirsDefault;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * XMLParameterFileCreator
*

 * Use is subject to license terms.</p>
 * @author Modest von Korff
  *
 * Mar 5, 2014 MvK Start implementation
 */
public class XMLParameterFileCreatorMain {

	private static final String PATH = UserDirsDefault.getTmp(0);
	
	private static final String FILE_BATCH = "BatchVSCmd";
	
	public static void main(String[] args) throws JAXBException, IOException, SAXException, ParserConfigurationException, ClassNotFoundException {
		
		
		File dirQueryFiles = new File(args[0]);
				
		File fiXMLParameterFileTemplate = new File(args[1]);
		
		// ./VSCmd /home/korffmo1/Projects/LibrarySynthesis/WuXi/Wuxi_final_selection/VS/vsWX100660_11770_LL_FuzzyScore.xml
		// /home/korffmo1/Projects/LibrarySynthesis/WuXi/Wuxi_final_selection/VS
		String folderParameterFiles = args[2];
		
		
		
		URL url = fiXMLParameterFileTemplate.toURI().toURL();
		
		ModelVSXML modelVSXMLTemplate = ModelVSXML.get(url);
		
		
		List<File> liQueryFile = FileUtils.getDWARFiles(dirQueryFiles);
		
		File dirOut = new File(PATH);
		
		Collections.sort(liQueryFile);
		
		int tmp = 1;
		
		StringBuilder sbBatchFile = new StringBuilder();
		
		for (File fiQuery : liQueryFile) {
			
			ModelVSXML modelVSXML = new ModelVSXML(modelVSXMLTemplate);
			
			modelVSXML.setQuery(fiQuery.getAbsolutePath());
						
			File dirTmp = new File(UserDirsDefault.getTmp(tmp));
			
			modelVSXML.setWorkDir(dirTmp);
						
			String name = "parameterVS_" + IO.getBaseName(fiQuery.getName()) + ".xml";
			
			File fiXML = new File(dirOut, name);
			
			modelVSXML.write(fiXML);
			
			tmp++;
			
			
			
			sbBatchFile.append("./VSCmd ");
			
			File fiFinal = new File(folderParameterFiles, fiXML.getName());
			
			sbBatchFile.append(fiFinal.getAbsolutePath());
			
			sbBatchFile.append("\n");
		}
		
		File fiBatch = new File(dirOut, FILE_BATCH);
		
		FileWriter fw = new FileWriter(fiBatch);
		
		fw.append(sbBatchFile.toString());
		
		fw.close();
				
		System.out.println("Finished");
		
		
	}

}
