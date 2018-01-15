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

package org.apache.jena.atlas.lib;

import java.io.File ;
import java.nio.file.Paths ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.base.Sys ;

/** Operations related to IRIs */
public class IRILib
{
    // Tests - see also TestFilenameProcessing
    
    // http://www.w3.org/TR/xpath-functions/#func-encode-for-uri
    // Encodes delimiters.
    
    /* RFC 3986
     * 
     * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
     * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                    / "*" / "+" / "," / ";" / "="
     */
    
    private static char uri_reserved[] = 
    { '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
      '=', '+', '$', ',', '/', '?', '%', '#', '[', ']' } ;

    // No allowed in URIs
    private static char uri_non_chars[] = { '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t', '£' } ;
    
    // RFC 2396
    //private static char uri_unwise[]    = { '{' , '}', '|', '\\', '^', '[', ']', '`' } ;


    private static char[] charsComponent =
    // reserved, + non-chars + nasties.
    { '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
      '=', '+', '$', ',', '/', '?', '%', '#', '[', ']',
      '{', '}', '|', '\\', '`', '^',
      ' ', '<', '>', '\n', '\r', '\t', '£' } ;
    
    private static char[] charsFilename =
        // reserved, + non-chars + nasties.
        // Leave : (Windows drive charcater) and / (separator) alone
        // include SPC.
        // Should this include "~"?
        { '!', '*', '"', '\'', '(', ')', ';', /*':',*/ '@', '&', 
          '=', '+', '$', ',', /*'/',*/ '?', '%', '#', '[', ']',
          '{', '}', '|', '\\', '`', '^',
          ' ', '<', '>', '\n', '\r', '\t'} ;

    private static char[] charsPath =  
    {   // Reserved except leave the separators alone. 
        '!', '*', '"', '\'', '(', ')', ';', /*':',*/ '@', '&',
        '=', '+', '$', ',', /*'/',*/ '?', '%', '#', '[', ']',
        '{', '}', '|', '\\', '`', '^',
        // Other junk 
        ' ', '<', '>', '\n', '\r', '\t' } ;

    // The initializers must have run.
    static final String cwd ; 
    static final String cwdURL ;
    
    // Current directory, with trailing "/"
    // This matters for resolution.
    static { 
        String x = new File(".").getAbsolutePath() ;
        x = x.substring(0, x.length()-1) ;
        cwd = x ;
        cwdURL = plainFilenameToURL(cwd) ;
    }
    
    // See also IRIResolver
    /** Return a string that is an IRI for the filename.*/
    public static String fileToIRI(File f) {
        return filenameToIRI(f.getAbsolutePath()) ;
    }
    
    /** Create a string that is a IRI for the filename.
     *  <li>The file name may already have {@code file:}.
     *  <li>The file name may be relative. 
     *  <li>Encode using the rules for a path (e.g. ':' and'/' do not get encoded)
     *  <li>Non-IRI characters get %-encoded. 
     */
    public static String filenameToIRI(String fn) {
        if ( fn == null ) return cwdURL ;
        
        if ( fn.length() == 0 ) return cwdURL ;
        
        if ( fn.startsWith("file:") )
            return normalizeFilenameURI(fn) ;
        return plainFilenameToURL(fn) ;
    }
    
    /** Convert a file: IRI to a filename */
    public static String IRIToFilename(String iri) {
        if ( ! iri.startsWith("file:") )
            throw new AtlasException("Not a file: URI: "+iri) ; 
        
        String fn ;
        if ( iri.startsWith("file:///") )
            fn = iri.substring("file://".length()) ;
        else
            fn = iri.substring("file:".length()) ;
        // MS Windows: we can have 
        //  file:///C:/path or file:/C:/path
        // At this point, we have a filename of /C:/
        // so need strip the leading "/"
        fn = fixupWindows(fn);
        
        return decode(fn) ;
    }
    
    /** Convert a plain file name (no file:) to a file: URL */
    private static String plainFilenameToURL(String fn) {
        // No "file:"
        // Make Absolute filename.

        boolean trailingSlash = fn.endsWith("/") ;

        // To get Path.toAbsolutePath to work, we need to convert /C:/ to C:/
        // then back again.
        fn = fixupWindows(fn) ;
        fn = Paths.get(fn).toAbsolutePath().normalize().toString() ;
        
        if ( trailingSlash && ! fn.endsWith("/") )
            fn = fn + "/" ;
        
        if ( Sys.isWindows )
        {
            // C:\ => file:///C:/... 
            if ( windowsDrive(fn, 0) )
                // Windows drive letter - already absolute path.
                // Make "URI" absolute path
                fn = "/"+fn ;
            // Convert \ to /
            // Maybe should do this on all platforms? i.e consistency.
            fn = fn.replace('\\', '/' ) ;
        }
        
        fn = encodeFileURL(fn) ;
        return "file://"+fn ;
    }
    
    // Case of Windows /C:/ which can come from URL.toString 
    // giving file:/C:/ and decoding file:///C:/ 
    private static String fixupWindows(String fn) {
        if ( Sys.isWindows && 
             fn.length() >= 3 && fn.charAt(0) == '/' && windowsDrive(fn, 1))
             fn = fn.substring(1) ;
        return fn;
    }
    
    /** Does filename {@code fn} look like a windows-drive rooted file path?
     * The test is can we find "C:" at location {@code i}. 
     */
    private static boolean windowsDrive(String fn, int i) {
        return 
            fn.length() >= 2+i && 
            fn.charAt(1+i) == ':' && 
            isA2Z(fn.charAt(i)) ;    
    }
    
    private static boolean isA2Z(char ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') ;  
    }
    
    /** Sanitize a "file:" URL. Must start "file:" */
    private static String normalizeFilenameURI(String fn) {
        if ( ! fn.startsWith("file:/") ) {
            // Relative path.
            String fn2 = fn.substring("file:".length()) ;
            return plainFilenameToURL(fn2) ;
        }
        
        // Starts file:// or file:/// 
        if ( fn.startsWith("file:///") )
            // Assume it's good and return as-is.
            return fn ;

        if ( fn.startsWith("file://") ) {
            // file: URL with host name (maybe!)
            return fn ;
        }

        // Must be file:/
        String fn2 = fn.substring("file:".length()) ;
        return plainFilenameToURL(fn2) ;
    }

    /** Encode using the rules for a component (e.g. ':' and '/' get encoded) 
     * Does not encode non-ASCII characters 
     */
    public static String encodeUriComponent(String string) {
        String encStr = StrUtils.encodeHex(string,'%', charsComponent) ;
        return encStr ;
    }

    /** Encode using the rules for a file: URL.  
     *  Does not encode non-ASCII characters
     */
    public static String encodeFileURL(String string) {
        String encStr = StrUtils.encodeHex(string,'%', charsFilename) ;
        return encStr ;
    }

    /** Encode using the rules for a path (e.g. ':' and '/' do not get encoded) */
    public static String encodeUriPath(String uri) {
        // Not perfect.
        // Encode path.
        // %-encode chars.
        uri = StrUtils.encodeHex(uri, '%', charsPath) ;
        return uri ;
    }

    public static String decode(String string) {
        return StrUtils.decodeHex(string, '%') ;
    }

    public static String encodeNonASCII(String string) {
        if ( ! containsNonASCII(string) )
            return string ;
        
        byte[] bytes = StrUtils.asUTF8bytes(string) ;
        StringBuilder sw = new StringBuilder() ;
        for ( byte b : bytes ) {
            // Signed bytes ...
            if ( b > 0 ) {
                sw.append( (char) b );
                continue;
            }

            int hi = ( b & 0xF0 ) >> 4;
            int lo = b & 0xF;
            sw.append( '%' );
            sw.append( Chars.hexDigitsUC[hi] );
            sw.append( Chars.hexDigitsUC[lo] );
        }
        return sw.toString() ;
    }

    public static boolean containsNonASCII(String string){
        for ( int i = 0 ; i < string.length() ; i++ ) {
            char ch = string.charAt(i) ;
            if ( ch >= 127 )
                return true;
        }
        return false ;
    } 
}
