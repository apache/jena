/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.cache.Cache;

/** RelURI.  To be replaced by the IRI library.
 * 
 * @author Andy Seaborne
 * @version $Id: RelURI.java,v 1.16 2007-07-30 13:59:49 jeremy_carroll Exp $
 */

public class RelURI
{
	public static class JenaURIException extends JenaException
	{
	    public JenaURIException(Exception msg) { super(msg) ; }
	}


	static final private String globalBase;
	static final IRIFactory factory = new IRIFactory(IRIFactory.jenaImplementation());
    static {
    	factory.setSameSchemeRelativeReferences("file");
    }
	
	
	static final IRI cwd;
	static {
		String baseURI;
		File f = new File(".") ;
		try {
			 baseURI = fileToAbsoluteURL(f);
		} catch (IOException e) {
			e.printStackTrace();
			baseURI = "http://example.org/";
		}
		globalBase = baseURI;
		IRI cwdx;
		try {
		  cwdx = factory.construct(globalBase);
		} 
		catch (IRIException e) {
			System.err.println("Unexpected IRIException in initializer: "+e.getMessage());
			cwdx = factory.create("file:///");
		}
		cwd = cwdx;
	}
	static String fileToAbsoluteURL(File f) throws IOException {
		String baseURI;
		baseURI = sanitizeFileURL(f.getCanonicalPath());     
		if ( f.isDirectory() && ! baseURI.endsWith("/") )
		    baseURI = baseURI+"/" ;
		return baseURI;
	}
	static String sanitizeFileURL(String s) {
		String baseURI;
		s = s.replaceAll("\\\\", "/") ;
		s = s.replaceAll("%","%25");
		s = s.replaceAll(" ", "%20") ;
		if ( s.startsWith("/"))
		    // Already got one / - UNIX-like
		    baseURI = "file://"+s ;
		else
		    // Absolute name does not start with / - Windows like
		    baseURI = "file:///"+s ;
		return baseURI;
	}
	/**
	 * @param baseURI
	 * @return
	 */
	static public String chooseBaseURI(String baseURI)
    {
		if (baseURI!=null)
			return baseURI;
		
        return globalBase;
    }
    /**
     * Turn a filename into a well-formed file: URL relative to the working directory.
     * @param filename
     * @return String The filename as an absolute URL
     */
	static public String resolveFileURL(String filename) throws IRIException {
		IRI r = cwd.resolve(filename);
		if (!r.getScheme().equalsIgnoreCase("file")) {
			return resolveFileURL("./"+filename);
		}
		return r.toString();
	}
	
    /** Create resolve a URI against a base.
     *  If baseStr is a relative file IRI then it is first resolved
     *  against the current working directory.
     * @param relStr
     * @param baseStr          Can be null if relStr is absolute
     * @return String          An absolute URI
     * @throws IRIException    If result would not be legal, absolute IRI
     */
	static public String resolve(String relStr, String baseStr)  throws IRIException {
	    try {
		IRI i = factory.create(relStr);
	    if (i.isAbsolute())
	    	// removes excess . segments, and throws exceptions:
	    	return cwd.construct(i).toString();
	    
	    IRI base = factory.create(baseStr);
	    
	    if ("file".equalsIgnoreCase(base.getScheme()))
	    	return cwd.create(base).construct(i).toString();
	    
	    return base.construct(i).toString();
	    }
	    catch (IRIException e) {
	    	throw new JenaURIException(e);
	    }
	    
	}
    /** Create resolve a URI against the global base.
     *  Returns null if the result is not absolute. 
     *  @param relURI
     */
    
    static public String resolve(String relURI) throws IRIException
    {
        return resolve(relURI, globalBase) ;  
    }

    static public String getBaseURI()
    {
        return globalBase ;
    }
    
    
    /** Choose a base URI based on the current directory 
    * 
    * @return String      Absolute URI
    */ 
    
    static public String chooseBaseURI() { return chooseBaseURI(null) ; }
    
   
    
 
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
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