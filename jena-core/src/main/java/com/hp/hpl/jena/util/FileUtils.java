/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.util;

import java.io.* ;
import java.net.URL ;
import java.nio.charset.Charset ;

import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.JenaRuntime ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.WrappedIOException ;

public class FileUtils
{
    public static final String langXML          = "RDF/XML" ;
    public static final String langXMLAbbrev    = "RDF/XML-ABBREV" ;
    public static final String langNTriple      = "N-TRIPLE" ;
    public static final String langN3           = "N3" ;
    public static final String langTurtle       = "TURTLE" ;
    
    /** Java name for UTF-8 encoding */
    public static final String encodingUTF8     = "utf-8" ;
    
    static Charset utf8 = null ;
    static {
        try {
            utf8 = Charset.forName(encodingUTF8) ;
        } catch (Throwable ex)
        {
            LoggerFactory.getLogger(FileUtils.class).warn("Failed to get charset for UTF-8", ex) ;
        }
    }
    
    
    /** Create a reader that uses UTF-8 encoding */ 
    
    static public Reader asUTF8(InputStream in) {
        if ( JenaRuntime.runUnder(JenaRuntime.featureNoCharset) )
            return new InputStreamReader(in) ;
        // Not ,utf8 -- GNUClassPath (0.20) apparently fails on passing in a charset
        // but if passed not the decoder or the name of the charset.
        // Reported and fixed.
        return new InputStreamReader(in, utf8.newDecoder());
    }

    /** Create a buffered reader that uses UTF-8 encoding */ 
    
    static public BufferedReader asBufferedUTF8(InputStream in)
    {
        BufferedReader r = new BufferedReader(asUTF8(in)) ;
        return r ;
    }

    /** Create a writer that uses UTF-8 encoding */ 

    static public Writer asUTF8(OutputStream out) {
        if ( JenaRuntime.runUnder(JenaRuntime.featureNoCharset) )
            return new OutputStreamWriter(out) ;
        return new OutputStreamWriter(out, utf8.newEncoder());
    }

    /** Create a print writer that uses UTF-8 encoding */ 

    static public PrintWriter asPrintWriterUTF8(OutputStream out) {
        return new PrintWriter(asUTF8(out)); 
    }
    
    /** Guess the language/type of model data.
     * 
     * <ul>
     * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
     * <li> If the URI ends ".nt", it is assumed to be N-Triples</li>
     * <li> If the URI ends ".ttl", it is assumed to be Turtle</li>
     * <li> If the URI ends ".owl", it is assumed to be RDF/XML</li>
     * </ul>
     * @param name    URL to base the guess on
     * @param otherwise Default guess
     * @return String   Guessed syntax - or the default supplied
     */

    public static String guessLang( String name, String otherwise )
    {
        String suffix = getFilenameExt( name );
        if (suffix.equals( "n3" ))   return langN3;
        if (suffix.equals( "nt" ))   return langNTriple;
        if (suffix.equals( "ttl" ))  return langTurtle ;
        if (suffix.equals( "rdf" ))  return langXML;
        if (suffix.equals( "owl" ))  return langXML;
        return otherwise; 
    }    
   
   
    /** Guess the language/type of model data
     * 
     * <ul>
     * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
     * <li> If the URI ends ".nt", it is assumed to be N-Triples</li>
     * <li> If the URI ends ".ttl", it is assumed to be Turtle</li>
     * <li> If the URI ends ".owl", it is assumed to be RDF/XML</li>
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
        // Requirements of windows and Linux differ slightly here
        // Windows wants "file:///c:/foo" => "c:/foo"
        // but Linux only wants "file:///foo" => "/foo"
        // Pragmatically, a path of "/c:/foo", or "/foo" works everywhere.
        // but not "//c:/foo" or "///c:/foo" 
        // else IKVM thinks its a network path on Windows.
        
        // If it's a a file: we apply %-decoding.
        // If there is no scheme name, we don't.

        if ( !isFile(filenameOrURI) )
            return null ;
        // No scheme of file:
        String fn = filenameOrURI ;

        if ( ! fn.startsWith("file:") )
            return fn ;
        
        // file:
        // Convert absolute file names
        if ( fn.startsWith("file:///") )
            fn = fn.substring("file://".length()) ;
        else if ( fn.startsWith("file://localhost/") )
            // NB Leaves the leading slash on. 
            fn = fn.substring("file://localhost".length()) ;
        else
            // Just trim off the file:
            fn = fn.substring("file:".length()) ;

        return decodeFileName(fn) ;
    }
    
    public static String decodeFileName(String s)
    {
        if ( s.indexOf('%') < 0 ) 
            return s ;
        int len = s.length();
        StringBuilder sbuff = new StringBuilder(len) ;

        for ( int i =0 ; i < len ; i++ )
        {
            char c = s.charAt(i);
            switch (c)
            {
                case '%':
                    int codepoint = Integer.parseInt(s.substring(i+1,i+3),16) ;
                    char ch = (char)codepoint ;
                    sbuff.append(ch) ;
                    i = i+2 ;
                    break ;
                default:
                    sbuff.append(c);
            }
        }
        return sbuff.toString();
    }
    
    
    /** Turn a plain filename into a "file:" URL */
    public static String toURL(String filename)
    {
        if ( filename.length()>5
        		&& filename.substring(0,5).equalsIgnoreCase("file:") )
            return filename ;
        
        /**
         * Convert a File, note java.net.URI appears to do the right thing.
         * viz:
         *   Convert to absolute path.
         *   Convert all % to %25.
         *   then convert all ' ' to %20.
         *   It quite probably does more e.g. ? #
         * But has bug in only having one / not three at beginning
           
         */
		return "file://" + new File(filename).toURI().toString().substring(5);
    }
    
    /** Check whether 'name' is possibly a file reference  
     * 
     * @param name
     * @return boolean False if clearly not a filename. 
     */
    public static boolean isFile(String name)
    {
        String scheme = getScheme(name) ;
        
        if ( scheme == null  )
            // No URI scheme - treat as filename
            return true ;
        
        if ( scheme.equals("file") )
            // file: URI scheme
            return true ;
            
        // Windows: "c:" etc
        if ( scheme.length() == 1 )
            // file: URI scheme
            return true ;
        
        return false ;
    }
    
    /** Check whether a name is an absolute URI (has a scheme name)
     * 
     * @param name
     * @return boolean True if there is a scheme name 
     */
    public static boolean isURI(String name)
    {
        return (getScheme(name) != null) ;
    }

    public static String getScheme(String uri)
    {
        // Find "[^/:]*:.*"
        for ( int i = 0 ; i < uri.length() ; i++ )
        {
            char ch = uri.charAt(i) ;
            if ( ch == ':' )
                return uri.substring(0,i) ;
            if ( ! isASCIILetter(ch) )
                // Some illegal character before the ':' 
                break ;
        }
        return null ;
    }
    
    private static boolean isASCIILetter(char ch)
    {
        return ( ch >= 'a' && ch <= 'z' ) || ( ch >= 'A' && ch <= 'Z' ) ;
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
    { return JenaRuntime.getSystemProperty( "java.io.tmpdir" ); }

    private static int counter = 0;
    
    private static int randomNumber()
    {
        return ++counter;
    }

    // TODO Replace with a FileManager
    /**
     Answer a BufferedReader than reads from the named resource file as
     UTF-8, possibly throwing WrappedIOExceptions.
     */
    public static BufferedReader openResourceFile( String filename )  
    {
        try
        {
            InputStream is = FileUtils.openResourceFileAsStream( filename );
            return new BufferedReader(new InputStreamReader(is, "UTF-8"));
        }
        catch (IOException e)
        { throw new WrappedIOException( e ); }
    }
    
    /**
     * Open an resource file for reading.
     */
    public static InputStream openResourceFileAsStream(String filename)
    throws FileNotFoundException {
        InputStream is = ClassLoader.getSystemResourceAsStream(filename);
        if (is == null) {
            // Try local loader with absolute path
            is = FileUtils.class.getResourceAsStream("/" + filename);
            if (is == null) {
                // Try local loader, relative, just in case
                is = FileUtils.class.getResourceAsStream(filename);
                if (is == null) {
                    // Can't find it on classpath, so try relative to current directory
                    // Will throw security exception under and applet but there's not other choice left
                    is = new FileInputStream(filename);
                }
            }
        }
        return is;
    }

    // TODO Replace with FileManager
    public static BufferedReader readerFromURL( String urlStr ) 
    {
        try { return asBufferedUTF8( new URL(urlStr).openStream() ); }    
        catch (java.net.MalformedURLException e) 
        { // Try as a plain filename.
            try { return asBufferedUTF8( new FileInputStream( urlStr ) ); }
            catch (FileNotFoundException f) { throw new WrappedIOException( f ); }
        }
        catch (IOException e)
        { throw new WrappedIOException( e ); }
    }

    /** Read a whole file as UTF-8
     * @param filename
     * @return String
     * @throws IOException
     */
    
    public static String readWholeFileAsUTF8(String filename) throws IOException {
        InputStream in = new FileInputStream(filename) ;
        return readWholeFileAsUTF8(in) ;
    }

    /** Read a whole stream as UTF-8
     * 
     * @param in    InputStream to be read
     * @return      String
     * @throws IOException
     */
    public static String readWholeFileAsUTF8(InputStream in) throws IOException
    {
        try ( Reader r = new BufferedReader(asUTF8(in),1024) ) {
            return readWholeFileAsUTF8(r) ;
        }
    }
    
    /** Read a whole file as UTF-8
     * 
     * @param r
     * @return String The whole file
     * @throws IOException
     */
    
    // Private worker as we are trying to force UTF-8. 
    private static String readWholeFileAsUTF8(Reader r) throws IOException
    {
        try ( StringWriter sw = new StringWriter(1024) ) {
            char buff[] = new char[1024];
            int l ; 
            while ((l = r.read(buff))!=-1) {         // .ready does not work with HttpClient streams.
                if (l <= 0)
                    break;
                sw.write(buff, 0, l);
            }
            return sw.toString();
        }
    }

}
