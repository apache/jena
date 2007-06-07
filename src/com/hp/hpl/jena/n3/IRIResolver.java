/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.n3.RelURI;
import com.hp.hpl.jena.util.FileUtils;

/** A simple class to access IRI resolution */ 

public class IRIResolver
{
    private String baseStr = null ;

    public IRIResolver()
    { this(null) ; }
    
    public IRIResolver(String base)
    {
        if ( base == null )
            base = chooseBaseURI() ;
        baseStr = base ;
//        if ( base == null )
//            throw new JenaURIException("Null base IRI") ;
    }

    public String resolve(String relURI)
    {
        return RelURI.resolve(relURI, baseStr) ;
    }

    public static String resolve(String base, String relative)
    {
        //Resolver resolver = new Resolver(base) ;
        //return resolver.resolve(relative) ;
        return RelURI.resolve(relative, base) ;
    }


    public static String resolveGlobal(String str)
    {
        return RelURI.resolve(str) ;
    }

    /**
     * Turn a filename into a well-formed file: URL relative to the working directory.
     * @param filename
     * @return String The filename as an absolute URL
     */

    static public String resolveFileURL(String filename)
    {
        return RelURI.resolveFileURL(filename) ;
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
//             if ( baseURI.startsWith("/") )
//                 return "file://"+baseURI ;
             
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

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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