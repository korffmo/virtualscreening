package com.actelion.research.chem.vs.util;

import java.io.IOException;
import java.net.URL;



/**
 * 
 * IOClassLoader
 * <p>Copyright: Actelion Ltd., Inc. All Rights Reserved

 * Use is subject to license terms.</p>
 * @author Modest von Korff
 * @version 1.0
 * Sep 16, 2013 MvK Start implementation
 */
public class IOClassLoader {
	
	
	public static final URL getURL(String sURL) throws IOException {
		
		URL url =  com.actelion.research.chem.vs.util.IOClassLoader.class.getResource(sURL);
		
		if(url == null ) {
			String e = "Not able to load " + sURL + ".";
			throw new IOException(e);
		}
		
		// System.out.println(url.toString());
		
		return url;
	}

}
