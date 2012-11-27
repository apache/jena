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
import java.io.IOException ;

import org.apache.jena.atlas.AtlasException ;

/** Operations related to IRIs */
public class IRILib
{
    /** Encode using the rules for a component (e.g. ':' and '/' get encoded) 
     * Does not encode non-ASCII characters 
     */
    public static String encodeUriComponent(String string)
    {
        String encStr = StrUtils.encodeHex(string,'%', charsComponent) ;
        return encStr ;
    }
    
    /** Encode using the rules for a file: URL.  Same as encodeUriPath except
     * add "~" to the encoded set.
     *  Does not encode non-ASCII characters
     *   
     */
    public static String encodeFileURL(String string)
    {
        String encStr = StrUtils.encodeHex(string,'%', charsFilename) ;
        return encStr ;
    }
    
    /** Encode using the rules for a path (e.g. ':' and '/' do not get encoded) */
    public static String encodeUriPath(String uri)
    {
        // Not perfect.
        // Encode path.
        // %-encode chars.
        uri = StrUtils.encodeHex(uri, '%', charsPath) ;
        return uri ;
    }
    
    public static String decode(String string)
    {
        return StrUtils.decodeHex(string, '%') ;
    }
    
    
    private static boolean isWindows = (File.pathSeparatorChar == ';' ) ;
    //public static void setIsWindowsForTesting(boolean val) { isWindows = val ; }

    // http://www.w3.org/TR/xpath-functions/#func-encode-for-uri
    // Encodes delimiters.
    
    /* RFC 3986
     * 
     * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
     * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                    / "*" / "+" / "," / ";" / "="
     */
    
    // XXX Append(char[], char[])
    
    private static char uri_reserved[] = 
    { 
      '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
      '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;

    // No allowed in URIs
    private static char uri_non_chars[] = { '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t' } ;
    
    // RFC 2396
    //private static char uri_unwise[]    = { '{' , '}', '|', '\\', '^', '[', ']', '`' } ;


    private static char[] charsComponent =
    // reserved, + non-chars + nasties.
    { '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
      '=', '+', '$', ',', '/', '?', '%', '#', '[', ']',
      '{', '}', '|', '\\', '`', '^',
      ' ', '<', '>', '\n', '\r', '\t' } ;
    
    private static char[] charsFilename =
        // reserved, + non-chars + nasties.
        { '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
          '=', '+', '$', ',', /*'/',*/ '?', '%', '#', '[', ']',
          '{', '}', '|', '\\', '`', '^',
          ' ', '<', '>', '\n', '\r', '\t',
          '~'} ;

    private static char[] charsPath =  
    {
        // Reserved except leave the separators alone. 
        // Leave the path separator alone.
        // Leave the drive separator alone.
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
    /** Return a string that is a IRI for the filename.*/
    public static String fileToIRI(File f)
    {
        return filenameToIRI(f.getAbsolutePath()) ;
    }
    
    /** Create a string that is a IRI for the filename.
     *  Encode using the rules for a path (e.g. ':' and'/' do not get encoded)
     */
    public static String filenameToIRI(String fn)
    {
        if ( fn == null ) return cwdURL ;
        
        if ( fn.length() == 0 ) return cwdURL ;
        if ( fn.length() == 1 ) {}
        
        if ( fn.startsWith("file:") )
            return normalizeFilenameURI(fn) ;
        return plainFilenameToURL(fn) ;
        // Also: String fn2 = "file://" + new File(fn).toURI().toString().substring(5);
    }
    
    /** Convert an IRI to a filename */
    public static String IRIToFilename(String iri)
    {
        if ( ! iri.startsWith("file:") )
            throw new AtlasException("Not a file: URI: "+iri) ; 
        
        String fn ;
        if ( iri.startsWith("file:///") )
            fn = iri.substring("file://".length()) ;
        else
            fn = iri.substring("file:".length()) ;
        return decode(fn) ;
    }
    
    private static String plainFilenameToURL(String fn)
    {
        // No "file:"
        // Make Absolute filename.
        boolean trailingSlash = fn.endsWith("/") ;
        File file = new File(fn) ;
        
        try { fn = file.getCanonicalPath() ; }
        catch (IOException e)
        { fn = file.getAbsolutePath() ; } 
        if ( trailingSlash && ! fn.endsWith("/") )
            fn = fn + "/" ;
        
        // Temporary
        if ( isWindows )
        {
            // Char 2, 
            if ( fn.length() >= 2 && fn.charAt(1) == ':' )
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
    
    
    // Sanitize a "file:" URL.
    private static String normalizeFilenameURI(String fn)
    {
        // TODO
        String path = fn.substring("file:".length()) ;
        if ( ! fn.startsWith("file:/") )
        {
            String fn2 = fn.substring("file:".length()) ;
            return plainFilenameToURL(fn2) ;
        }
        // starts file:/
        
        if ( fn.startsWith("file:///") )
            // Good.
            return encodeFileURL(fn) ;

        if ( fn.startsWith("file://") )
        {
            String fn2 = fn.substring("file:/".length()) ;  // Leave one "/"
            return plainFilenameToURL(fn2) ;
        }
            
        // Must be file:/
        String fn2 = fn.substring("file:".length()) ;
        return plainFilenameToURL(fn2) ;
    }

    public static String encodeNonASCII(String string)
    {
        if ( ! containsNonASCII(string) )
            return string ;
        
        byte[] bytes = StrUtils.asUTF8bytes(string) ;
        StringBuilder sw = new StringBuilder() ;
        for ( int i = 0 ; i < bytes.length ; i++ )
        {
            byte b = bytes[i] ;
            // Signed bytes ...
            if ( b > 0 )
            {
                sw.append((char)b) ;
                continue ;
            }
            
            int hi = (b & 0xF0) >> 4 ;
            int lo = b & 0xF ;
            sw.append('%') ;
            sw.append(Chars.hexDigitsUC[hi]) ;
            sw.append(Chars.hexDigitsUC[lo]) ;
        }
        return sw.toString() ;
    }

    public static boolean containsNonASCII(String string)
    {
        boolean clean = true ;
        for ( int i = 0 ; i < string.length() ; i++ )
        {
            char ch = string.charAt(i) ;
            if ( ch >= 127 )
                return true;
        }
        return false ;
    } 
}
