/*
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.*;

import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.shared.JenaException;

public class FileUtils {
    
    public static final String langXML  = "RDF/XML" ;
    public static final String langXMLAbbrev = "RDF/XML-ABBREV" ;
    public static final String langNTriple = "N-TRIPLE" ;
    public static final String langN3 = "N3" ;
    
        /**
        Guess the language of the specified file [or URL] by looking at the suffix.
        If it ends in .n3, assume N3; if it ends in .nt, assume N-TRIPLE;
        otherwise assume RDF/XML.
        
        @param name the pathname of the file to guess from
        @return "N3", "N-TRIPLE", or "RDF/XML"
     */
    public static String guessLang( String name )
        { return guessLang( name, langXML ); }
    
    
    public static String guessLang( String name, String otherwise )
        { 
        String suffix = getFilenameExt( name );
        if (suffix.equals( "n3" )) return langN3;
        if (suffix.equals( "nt" )) return langNTriple;
        if (suffix.equals( "rdf" )) return langXML;
        if (suffix.equals( "owl" )) return langXML;
        return otherwise; 
        }    
    
    /**
         Get the suffix part of a file name or a URL in file-like format.
    */
    private static String getFilenameExt( String filename)
        {
        int iSlash = filename.lastIndexOf( '/' );      
        int iBack = filename.lastIndexOf( '\\' );
        int iExt = filename.lastIndexOf( '.' ); 
        if (iBack > iSlash) iSlash = iBack;
        return iExt > iSlash ? filename.substring( iExt+1 ).toLowerCase() : "";
        }
    
	public static String readWholeFileAsUTF8(String filename) throws IOException {
		Reader r = new BufferedReader(asUTF8(new FileInputStream(filename)),1024) ;
		StringWriter sw = new StringWriter(1024);
		char buff[] = new char[1024];
		while (r.ready()) {
			int l = r.read(buff);
			if (l <= 0)
				break;
			sw.write(buff, 0, l);
		}
		r.close();
		sw.close();
		return sw.toString();
	}

	static public Reader asUTF8(InputStream in) {
		try {
			return new InputStreamReader(in, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// Give up and die.
			throw new Error("utf-8 *must* be a supported encoding.");
		}
	}
	

	static public Writer asUTF8(OutputStream out) {
		try {
			return new OutputStreamWriter(out, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// Give up and die.
			throw new Error("utf-8 *must* be a supported encoding.");
		}
	}

	/**
	    create a temporary file that will be deleted on exit, and do something
	    sensible with any IO exceptions - namely, throw them up wrapped in
	    a JenaException.
	
	    @param prefix the prefix for File.createTempFile
	    @param suffix the suffix for File.createTempFile
	    @return the temporary File
	*/
	public static  File tempFileName( String prefix, String suffix )
	    {
	    File result = new File( getTempDirectory(), prefix + randomNumber() + suffix );
	    if (result.exists()) return tempFileName( prefix, suffix );
	    result.deleteOnExit();
	    return result;
	    }  

    private static int counter = 0;

    private static int randomNumber()
        {
        return ++counter;
        }
 
    /**
        Answer a File naming a freshly-created directory in the temporary directory. This
        directory should be deleted on exit.
        TODO handle threading issues, mkdir failure, and better cleanup
        
        @param prefix the prefix for the directory name
        @return a File naming the new directory
     */
    public static File getScratchDirectory( String prefix )
        {
        File result = new File( getTempDirectory(), prefix + randomNumber() );
        if (result.exists()) return getScratchDirectory( prefix );
        if (result.mkdir() == false) throw new JenaException( "mkdir failed on " + result );
        result.deleteOnExit();
        return result;   
        } 
        
    public static String getTempDirectory()
        { return System.getProperty( "java.io.tmpdir" ); }
    
    /**
     * Open an resource file for reading.
     */
    public static BufferedReader openResourceFile(String filename) throws IOException {
       	InputStream is = FileUtils.openResourceFileAsStream(filename);
        return new BufferedReader(new InputStreamReader(is, "UTF-8"));
    }

    /**
     * Open an resource file for reading.
     */
    public static InputStream openResourceFileAsStream(String filename)
    	throws FileNotFoundException {
        InputStream is = ClassLoader.getSystemResourceAsStream(filename);
    	if (is == null) {
    	    // Try local loader with absolute path
            is = Util.class.getResourceAsStream("/" + filename);
            if (is == null) {
                // Can't find it on classpath, so try relative to current directory
    	        is = new FileInputStream(filename);
    	    }
    	}
    	return is;
    }
                 
}

/*
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
