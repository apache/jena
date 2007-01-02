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
import java.util.regex.Pattern;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.cache.Cache;

/** RelURI copied to Jena
 * 
 * @author Andy Seaborne
 * @version $Id: RelURI.java,v 1.10 2007-01-02 11:48:32 andy_seaborne Exp $
 */

public class RelURI
{
    public static class JenaURIException extends JenaException
    {
        public JenaURIException(String msg) { super(msg) ; }
    }

    public static class RelativeURIException extends JenaURIException
    {
        public RelativeURIException(String msg) { super(msg) ; }
    }
    
    static private String globalBase = null ;
    
    static Cache baseCache = new Cache1() ;
    static Pattern patternHttp = Pattern.compile("^http://[^/]*/[^/]+") ; 
    static Pattern patternFile = Pattern.compile("^file:/*[^/]+/") ; 
    
    
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
        // Special case (GNUClassPath workaround)
        if ( relStr.equals(".") )
        {
            if ( baseStr.startsWith("http://") || baseStr.startsWith("file:/") )
            {
                if ( baseStr.endsWith("/") )
                    return baseStr ;

                if ( patternHttp.matcher(baseStr).find() )
                {
                    int j = baseStr.lastIndexOf("/") ;
                    return baseStr.substring(0, j+1) ; 
                }

                if ( patternFile.matcher(baseStr).find())
                {
                    int j = baseStr.lastIndexOf("/") ;
                    return baseStr.substring(0, j+1) ; 
                }
            }
            // Can't shortcut - drop through anyway.
        }
        
        // Encode spaces (for filenames)
        baseStr = encode(baseStr) ;
        relStr = encode(relStr) ;
        // "Adapt" URIs with spaces
        String s = _resolve(relStr, baseStr) ;
        s = decode(s) ;
        return s ;
        
    }
    
    static private String encode(String s)
    {
        if ( s == null ) return s ;
        //s = s.replace("_", "__") ;
        //s = s.replace(" ", "_20") ;
        return s ;
    }
    
    static private String decode(String s)
    {
        //s = s.replace("_20", " ") ;
        //s = s.replace("__", "_") ;
        return s ;
        
    }
    
    static private String _resolve(String relStr, String baseStr)
    {
        URI rel = null ;
        try { rel = new URI(relStr) ; }
        catch (java.net.URISyntaxException ex)
        { throw new JenaURIException("Illegal URI: "+relStr) ; }
        
        if ( rel.isAbsolute() )
        {
            String s = rel.getScheme() ;
            // Corner case : relStr is the strictly absolute URI with an incomplete 
            // scheme specific part -- example: "file:x"
            if ( rel.getScheme().equals("file") )
                return resolveFileURL(relStr) ;
            return relStr ;
        }
        
        if ( baseStr == null )
        {
            // Null base - relStr not absolute
            //return relStr ;
            if ( rel.isAbsolute() )
                return relStr ;
            throw new JenaURIException("Null base for relative URI resolution: "+relStr) ;
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
        URI base = (URI)baseCache.get(baseStr) ;
        
        if ( base == null )
        {
            try {
                base = new URI(baseStr) ;
            } catch (java.net.URISyntaxException ex)
            { throw new JenaURIException("Illegal URI (base): "+baseStr) ; }

            if ( ! base.isAbsolute() )
                throw new RelativeURIException("Relative URI for base: "+baseStr) ; 
            
            if ( base.isOpaque() )
            {
                // The case of file:A and #x
                if ( base.getScheme().equals("file") && relStr.startsWith("#") )
                    return baseStr+relStr ;
                
                // tag: and urn: but also anOther:...
                //return baseStr+relURI ;
                throw new RelativeURIException("Can't resolve a relative URI against an opaque URI: rel="+relStr+" : base="+baseStr) ;
            }
            
            baseCache.put(baseStr, base) ;
        }
        
        if ( base.getPath().length() == 0 && !relStr.startsWith("/") )
        {
            // No path in base. Unrooted relative string.
            // Fudge - make base have a slash
            try {
                base = new URI(baseStr+"/") ;
            } catch (java.net.URISyntaxException ex)
            { 
                LogFactory.getLog(RelURI.class).fatal("Base now illegal fixing up path-less base URI ("+baseStr+")") ;
                throw new JenaURIException("Illegal URI (base) ptII: "+baseStr) ;
            }
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
    }

    private static URI resolve(URI rel, URI base)
    {
        try {
            URI abs = base.resolve(rel) ;
            if ( ! abs.isAbsolute() )
                return null ;
            return abs ;
        } catch (RuntimeException ex)
        {
            LogFactory.getLog(RelURI.class).warn("\nException in Java library: "+ex.getMessage()+"\nresolve("+rel.toString()+", "+base.toString()+")") ;
            throw ex ;
        }
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

class Cache1 implements Cache
{
    boolean isEnabled = true ;
    Object cacheKey = null ;
    Object cacheValue = null ;
    
    int numGet = 0 ;
    int numPut = 0 ;
    int numHits = 0 ;
    
    public Object get(Object key)
    {
        if ( ! isEnabled )
            return null ;
        
        numGet ++ ;
        
        if ( cacheKey == null )
            return null ;
        
        if ( cacheKey.equals(key) )
        {
            numHits ++ ;
            return cacheValue ;
        }
        
        return null ;
    }

    public void put(Object key, Object value)
    {
        if ( ! isEnabled )
            return ;
        numPut ++ ;
        cacheKey = key ; 
        cacheValue = value ;
    }

    public boolean getEnabled()
    {
        return isEnabled ;
    }

    public boolean setEnabled(boolean enabled)
    {
        boolean b = isEnabled ;
        isEnabled = enabled ;
        return b ;
    }

    public void clear()
    {
        cacheKey = null ;
        cacheValue = null ;
    }

    public long getGets() { return numGet ; }

    public long getPuts() { return numPut ; }

    public long getHits() { return numHits ; }
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