/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils; 

import org.apache.commons.logging.LogFactory;

// Apache URI code
//import com.hp.hpl.jena.rdf.arp.URI ;
//import com.hp.hpl.jena.rdf.arp.MalformedURIException; 
//import com.hp.hpl.jena.rdf.arp.RelativeURIException;

//java.net.URI code
import java.net.URI ;

/** com.hp.hpl.jena.query.util.RelURI
 * 
 * @author Andy Seaborne
 * @version $Id: RelURI.java,v 1.3 2005-10-05 21:12:13 andy_seaborne Exp $
 */

public class RelURI
{
    public static class JenaURIException extends JenaException
    {
        public JenaURIException(String msg) { super(msg) ; }
    }

    public static class RelativeURIException extends JenaException
    {
        public RelativeURIException(String msg) { super(msg) ; }
    }

    
    static private String globalBase = null ;
    /** Create resolve a URI agaisnt a base.
     *  Returns null if the result is not absolute. 
     * @param relStr
     * @param baseStr
     * @return String An absolute URI
     * @throws JenaURIException       Unacceptable base URI string
     * @throws RelativeURIException   Base is relative or opaque
     */
    
    static public String resolve(String relStr, String baseStr)
    {
        // Special case is for non-path URIs: (file: and ftp: and http:)
        
        if ( baseStr == null )
        {
            //return relStr ;
            // Check absolute?
            try {
                URI u = new URI(relStr) ;
                if ( ! u.isAbsolute() )
                    throw new JenaURIException("Null base for relative URI resolution") ;
                return relStr ;
            } catch (java.net.URISyntaxException ex)
            { throw new JenaURIException("Illegal URI (base was null and URI is not absolute) "+relStr) ; }
        }
        
        if ( baseStr.length() == 0 )
            throw new JenaURIException("Empty base for relative URI resolution") ;
        
//      if ( baseStr.endsWith("#") )
//      throw new JenaURIException("Base URI ends in #") ;
        
        // RFC 3986 says any URI to be used as a base URI is stripped of its fragment.
        
        if ( baseStr.indexOf('#') >= 0 )
        {
            int i = baseStr.indexOf('#') ;
            baseStr = baseStr.substring(0,i) ;
        }
        
        // Check for just "scheme:"
        if ( baseStr.endsWith(":") )
        {
            // Slightly confusingly, this leads to a slightly different exception
            // for ("base:" "#") and ("base:x" "#") 
            return resolve(baseStr+relStr) ;
        }
        
        if ( relStr.equals("") )
            return baseStr ;
        
        // Cache this?  One slot cache.
        URI base = null ;
        try {
            base = new URI(baseStr) ;
        } catch (java.net.URISyntaxException ex)
        { throw new JenaURIException("Illegal URI (base): "+baseStr) ; }

        if ( ! base.isAbsolute() )
            throw new RelativeURIException("Relative URI for base: "+baseStr) ; 
        
        if ( base.isOpaque() )
            // tag: and urn: but also anOther:...
            //return baseStr+relURI ;
            throw new RelativeURIException("Can't resolve a relative URI against an opaque URI: rel="+relStr+" : base="+baseStr) ;
        
        if ( base.getPath().length() == 0 && !relStr.startsWith("/") )
        {
            // No path in base. Unrooted relative string.
            // Fudge - make base have a p
            try {
                base = new URI(baseStr+"/") ;
            } catch (java.net.URISyntaxException ex)
            { 
                LogFactory.getLog(RelURI.class).fatal("Base now illegal fixing up path-less base URI ("+baseStr+")") ;
                throw new JenaURIException("Illegal URI (base) ptII: "+baseStr) ;
            }
        }
            
        String baseScheme = base.getScheme() ;
        
        String scheme = FileUtils.getScheme(relStr) ;
        if ( scheme != null )
        {
            // Is relStr "scheme:"?
            if ( relStr.length() == scheme.length()+1 )
                // Empty hierarchical part.  Best we can do.
                return relStr ;
            // Case of "base:#"
            
        }
        
        URI rel = null ;
        try {
            rel = new URI(relStr) ;
        } catch (java.net.URISyntaxException ex)
        {
            throw new JenaURIException("Illegal URI: "+relStr) ;
        }
            
        URI abs = resolve(rel, base) ;
        if ( abs == null )
            return null ;

        // Finally, sort out file URLs
        // 1 - file:filename => file:///dir/filename
        // 2 - file:/ => file:///
        

        // Fix for file names
        String s = abs.toString() ;

        if ( s.startsWith("file:") )
            s = resolveFileURL(s) ;
        return s ;
        
//        if ( s.startsWith("file:/") && ! s.startsWith("file:///") )
//        {
//            s = s.substring("file:".length()) ;
//            if ( s.startsWith("//") )
//                s = "file:/"+s ;
//            else
//                s = "file://"+s ;
//        }
//        
//        return s ;
    }

    private static URI resolve(URI rel, URI base)
    {
        URI abs = base.resolve(rel) ;
        if ( ! abs.isAbsolute() )
            return null ;
        return abs ;
    }
    
    /** Create resolve a URI against the global base.
     *  Returns null if the result is not absolute. 
     *  @param relURI
     */
    
    static public String resolve(String relURI)
    {
        if ( globalBase == null )
            globalBase = chooseBaseURI() ;
        return resolve(relURI, globalBase) ;  
    }

    static public void setBaseURI(String uriBase)
    {
        if ( uriBase != null && uriBase.startsWith("file:/") && ! uriBase.startsWith("file:///") )
            LogFactory.getLog(RelURI.class).warn("setBaseURI: File URIs should look like 'file:///path' (or at least file://host/path)") ;
        
        globalBase = uriBase ;
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
    
    /** Choose a baseURI based on a suggestion
     * 
    * @return String      Absolute URI
     */ 
    
    static public String chooseBaseURI(String baseURI)
    {
        if ( baseURI == null )
            baseURI = "file:." ;
        String scheme = FileUtils.getScheme(baseURI) ;
        if ( scheme == null )
        {
            scheme = "file" ;
            baseURI = "file:"+baseURI ;
        }
        
        // Not quite resolveFileURL (e.g. directory canonicalization).
        if ( scheme.equals("file") )
        {
//            if ( baseURI.startsWith("/") )
//                return "file://"+baseURI ;
            
            if ( ! baseURI.startsWith("file:///") )
            {
                try {
                    String tmp = baseURI.substring("file:".length()) ;
                    File f = new File(tmp) ;
                    String s = f.getCanonicalPath() ;
                    s = s.replace('\\', '/') ;
                    if ( s.indexOf(' ') >= 0 )
                        s = s.replaceAll(" ", "%20") ;
                    
                    if ( s.startsWith("/"))
                        // Already got one / - UNIX-like
                        baseURI = "file://"+s ;
                    else
                        // Absolute name does not start with / - Windows like
                        baseURI = "file:///"+s ;
                    
                    if ( f.isDirectory() && ! baseURI.endsWith("/") )
                        baseURI = baseURI+"/" ;

                } catch (IOException ex)
                {
                    LogFactory.getLog(RelURI.class).warn("IOException in chooseBase - ignored") ;
                    return null ;
                }
            }
        }
        return baseURI ;
    }
    
    
    /**
     * Turn a filename into a well-formed file: URL relative to the working directory.
     * @param filename
     * @return String The filename as an absolute URL
     */
    
    static public String resolveFileURL(String filename)
    {
        String s = filename ;
        try {
            // Pragmatic windows hack.
            if ( s.indexOf('\\') > -1 )
                s = s.replace('\\', '/') ;
            
            // Absolute path names
            if ( s.startsWith("file:///"))
                return s ;
            
            if ( s.startsWith("file://"))
                // Strictly legal but the next thing is the host
                // file://C:/ means host C, default port!
                return s ;
            
            if ( s.startsWith("file:/") )
            {
                // This converts Java's idea of file: URL
                // to one with ///
                s = filename.substring("file:/".length()) ;
                return "file:///"+s ;
            }

            // Relative path name.
            if ( s.startsWith("file:") )
                s = filename.substring("file:".length()) ;
            
            File f = new File(s) ;
            // If it ends in "/" keep it
            // (don't test for a directory - may not exist, and it costs more)
            if ( s.endsWith("/") )
                s = f.getAbsolutePath()+"/" ;
            else
                s = f.getCanonicalPath() ;
            // Windows file name to URI hierarchical paths
            s = s.replace('\\', '/') ;

            // java.net.URI messes up file:/// 
            if ( s.startsWith("/"))
                // Already got one / - UNIX-like
                s = "file://"+s ;
            else
                // Absolute name does not start with / - Windows like
                s = "file:///"+s ;
            return s ;
        } catch (IOException ex)
        {
            return null ;
        }

    }
}

/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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