/*
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.util.file;

import java.io.*;

import java.nio.charset.Charset ;
import com.hp.hpl.jena.JenaRuntime;

public class FileUtils2
{
    public static final String langXML          = "RDF/XML" ;
    public static final String langXMLAbbrev    = "RDF/XML-ABBREV" ;
    public static final String langNTriple      = "N-TRIPLE" ;
    public static final String langN3           = "N3" ;
    // Non-standard
    public static final String langBDB          = "RDF/BDB" ;
    public static final String langSQL          = "RDF/SQL" ;
    
    static Charset utf8 = null ;
    static {
        try {
            utf8 = Charset.forName("utf-8") ;
        } catch (Throwable ex) {}
    }
    
    
    /** Create a reader that uses UTF-8 encoding */ 
    
	static public Reader asUTF8(InputStream in) {
        if ( JenaRuntime.runUnder(JenaRuntime.featureNoCharset) )
            return new InputStreamReader(in) ;
        return new InputStreamReader(in, utf8);
	}

    /** Create a buffered reader that uses UTF-8 encoding */ 
	
    static public BufferedReader asBufferedUTF8(InputStream in) {
        return new BufferedReader(asUTF8(in)) ;
    }

    /** Create a writer that uses UTF-8 encoding */ 

    static public Writer asUTF8(OutputStream out) {
        if ( JenaRuntime.runUnder(JenaRuntime.featureNoCharset) )
            return new OutputStreamWriter(out) ;
        return new OutputStreamWriter(out, utf8);
    }

    /** Create a print writer that uses UTF-8 encoding */ 

    static public PrintWriter asPrintWriterUTF8(OutputStream out) {
        return new PrintWriter(asUTF8(out)); 
    }
    
    /** Guess the language/type of model data. Updated by Chris, hived off the
     * model-suffix part to FileUtils as part of unifying it with similar code in FileGraph.
     * 
     * <ul>
     * <li> If the URI of the model starts jdbc: it is assumed to be an RDB model</li>
     * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
     * <li> If the URI end .nt, it is assumed to be N-Triples</li>
     * <li> If the URI end .bdb, it is assumed to be BerkeleyDB model [suppressed at present]</li>
     * </ul>
     * @param name    URL to base the guess on
     * @param otherwise Default guess
     * @return String   Guessed syntax - or the default supplied
     */

    public static String guessLang( String name, String otherwise )
    {
        if ( name.startsWith("jdbc:") || name.startsWith("JDBC:") )
            return langSQL ;
        
        String suffix = getFilenameExt( name );
        if (suffix.equals( "n3" )) return langN3;
        if (suffix.equals( "nt" )) return langNTriple;
        if (suffix.equals( "rdf" )) return langXML;
        if (suffix.equals( "owl" )) return langXML;
        return otherwise; 
    }    
   
   
    /** Guess the language/type of model data
     * 
     * <ul>
     * <li> If the URI of the model starts jdbc: it is assumed to be an RDB model</li>
     * <li> If the URI ends .rdf, it is assumed to be RDF/XML</li>
     * <li> If the URI ends .n3, it is assumed to be N3</li>
     * <li> If the URI ends .nt, it is assumed to be N-Triples</li>
     * <li> If the URI ends .bdb, it is assumed to be BerkeleyDB model</li>
     * </ul>
     * @param urlStr    URL to base the guess on
     * @return String   Guessed syntax - default is RDF/XML
     */

    public static String guessLang(String urlStr)
    {
        return guessLang(urlStr, langXML) ;
    }

    /** Turn a file: URL or file name into a plain file name */
    
    public static String toFilename(String filenameOrURI)
    {
        if ( !isFile(filenameOrURI) )
            return null ;

        String fn = filenameOrURI ;
        if ( fn.startsWith("file:") )
            fn = fn.substring("file:".length()) ;
        return fn ;
    }
    
    /** Check whether 'name' is possibly a file reference  
     * 
     * @param name
     * @return boolean False if clearly not a filename. 
     */
    public static boolean isFile(String name)
    {
        return name.startsWith("file:") || ! isURI(name) ;
    }
    
    /** Check whether a name is an absolute URI (has a scheme name)
     * 
     * @param name
     * @return boolean True if there is a scheme name 
     */
    public static boolean isURI(String name)
    {
        // Java 1.4+
        if ( name.matches("[^/:]*:.*") )
            return true ;
        return false ;
    }

    /**
     * Get the directory part of a filename
     * @param filename
     * @return Directory name
     */
    public static String getDirname(String filename)
    {
        File f = new File(filename) ;
        return f.getParent() ;
    }

    /** Get the basename of a filename
     * 
     * @param filename
     * @return Base filename.
     */
    public static String getBasename(String filename)
    {
        File f = new File(filename) ;
        return f.getName() ;
    }

    /**
     Get the suffix part of a file name or a URL in file-like format.
     */
    public static String getFilenameExt( String filename)
    {
        int iSlash = filename.lastIndexOf( '/' );      
        int iBack = filename.lastIndexOf( '\\' );
        int iExt = filename.lastIndexOf( '.' ); 
        if (iBack > iSlash) iSlash = iBack;
        return iExt > iSlash ? filename.substring( iExt+1 ).toLowerCase() : "";
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
