/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.io.File ;
import java.io.IOException ;

import org.openjena.atlas.AtlasException ;

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
     *  gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
     * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                    / "*" / "+" / "," / ";" / "="
     */
    
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
    /** Encode using the rules for a path (e.g. ':' and'/' do not get encoded) */
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
    
    public static String IRIToFilename(String iri)
    {
        if ( ! iri.startsWith("file:") )
            throw new AtlasException("Not a file: URI: "+iri) ; 
        
        String fn = iri.substring("file:".length()) ;
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
        
        fn = encodeUriPath(fn) ;
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
            return encodeUriPath(fn) ;

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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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