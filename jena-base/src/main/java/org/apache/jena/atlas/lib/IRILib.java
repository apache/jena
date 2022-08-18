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
import java.net.URLEncoder;
import java.nio.file.Path;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.base.Sys ;

/** Operations related to IRIs.
 *  <p>
 *  The encoding operations are for Linked Data use, not network encoding - e.g. use
 *  {@linkplain URLEncoder#encode} or {@code org.apache.http.client.utilsURLEncodedUtils}
 *  for encoding query string name/value pairs for the network.
 */
public class IRILib
{
    // Tests - see also TestFilenameProcessing and TestIRILib

    // http://www.w3.org/TR/xpath-functions/#func-encode-for-uri
    // Encodes delimiters.

    // Encoding - does not encode non-ASCII unless otherwise mentioned.

    /* RFC 3986
     *
     * unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * reserved    = gen-delims / sub-delims
     * gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
     * sub-delims  = "!" / "$" / "&" / "'" / "(" / ")"
                    / "*" / "+" / "," / ";" / "="
     * segment       = *pchar
     * segment-nz    = 1*pchar
     * segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
     *                  ; non-zero-length segment without any colon ":"
     * pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
     * query         = *( pchar / "/" / "?" )
     * fragment      = *( pchar / "/" / "?" )
     */

    private static char uri_reserved[] = {
        // reserved : sub-delims
        '!', '$',  '&', '\'', '(', ')', '*', '+', ',', ';', '=',
        // reserved : gen-delims
        ':', '/', '?', '#', '[', ']',  '@'
    };

    // Not allowed in URIs, and '%'
    private static char uri_non_chars[] = {
        '%', '"', '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t', '£'
    } ;

    // RFC 2396
    //private static char uri_unwise[]    = { '{' , '}', '|', '\\', '^', '[', ']', '`' } ;

    // Javascript: A-Z a-z 0-9 - _ . ! ~ * ' ( )
    // URLEncoder.encode(string)
    // But this is not the strict set.
    // "component" is a name of value in a query string so pchar, with "/" and without "? and "="
    // We over-encode, partly legacy and partly experience.
    // Common use in Jena is for GSP URIs - so ?graph=uri

    /**
     * See also {@link URLEncoder} for {@code application/x-www-form-urlencoded}:
     * {@link URLEncoder#encode(String, String)}
     * which is strict ASCII.
     * Include ':' (segment-nc) and '/' (segment separator).
     */
    private static char[] charsComponent = {
        //
        '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=', ':', '/', '?', '#', '[', ']', '@',
        // Other
        '%', '"', '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t', '£'
    };

    private static char[] charsFilename = {
        // reserved, + non-chars + nasties.
        // Leave : (Windows drive character) and / (separator) alone
        // include SPC.
        // Should this include "~"?
        '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=',/* ':', '/',*/ '?', '#', '[', ']', '@',
        // Other
        '%', '"', '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t', '£'
    } ;

    // segment       = *pchar
    // pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
    private static char[] charsPath = {
        // sub-delims
        '!', '$',  '&', '\'', '(', ')', '*', '+', ',', ';', '=',
        // gen-delims that aren't in paths segments. Allow '/'
        /*':', '/',*/ '?', '#', '[', ']',/* '@',*/
        // Other
        '%', '"', '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t', '£'
    };


    // Character for a query string or fragment:
    // sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
    //               / "*" / "+" / "," / ";" / "="
    // pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
    // query         = *( pchar / "/" / "?" )
    // fragment      = *( pchar / "/" / "?" )

    private static char[] charsQueryFrag = {
        // sub-delims
        '!', '$',  '&', '\'', '(', ')', '*', '+', ',', ';', '=',
        // gen-delims that aren't in paths, allow '/', '?'
        /* ':', '/', '?', */ '#', '[', ']',/* '@',*/
        // Other
        '"', '%',
        '<', '>', '{', '}', '|', '\\', '`', '^', ' ',  '\n', '\r', '\t', '£'
    };

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
     *  <ul>
     *  <li>The file name may already have {@code file:}.
     *  <li>The file name may be relative.
     *  <li>Encode using the rules for a path (e.g. ':' and'/' do not get encoded)
     *  <li>Non-IRI characters get %-encoded.
     *  </ul>
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

        return decodeHex(fn) ;
    }

    /** Convert a plain file name (no file:) to a file: URL */
    private static String plainFilenameToURL(String fn) {
        // No "file:"
        // Make Absolute filename.

        boolean trailingSlash = fn.endsWith("/") ;

        // To get Path.toAbsolutePath to work, we need to convert /C:/ to C:/
        // then back again.
        fn = fixupWindows(fn) ;
        try {
            // Windows issue
            // Drive letter may not exists in which case it has no working directory "x:"
            fn = Path.of(fn).toAbsolutePath().normalize().toString() ;
        } catch (java.io.IOError ex) {
            // Any IO problems - > ignore.
        }
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
     * Apply to a name/value of a query string.
     * Does not encode non-ASCII characters
     */
    public static String encodeUriComponent(String string) {
        String encStr = StrUtils.encodeHex(string,'%', charsComponent) ;
        return encStr ;
    }

    /**
     * Encode using the rules for a query string or fragment
     * (e.g. ':' and '/' do not encoded).
     * It does not encode non-ASCII characters.
     * '?' is not encoded - in RFC 3986, the first '?' triggers the
     * query part but it is then a legal, character.
     */
    public static String encodeUriQueryFrag(String string) {
        String encStr = StrUtils.encodeHex(string,'%', charsQueryFrag) ;
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

    /**
     * Decode a string that may have %-encoded sequences.
     * <p>
     * This function will reverse
     * {@link #encodeNonASCII(String)},
     * {@link #encodeUriPath(String)},
     * {@link #encodeFileURL(String)} and
     * {@link #encodeUriComponent(String)}.
     *
     * It will not decode '+' used for space (application/x-www-form-urlencoded).
     */
    public static String decodeHex(String string) {
        return StrUtils.decodeHex(string, '%') ;
    }
}
