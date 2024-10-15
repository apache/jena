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

package org.apache.jena.rfc3986;

import java.util.regex.Pattern;

/**
 * Implementation of RFC 3986 (URI), RFC 3987 (IRI).
 * <p>
 * See the <a href="{@docRoot}/iri/package-summary.html">Package Overview</a>.
 * <p>
 * As is common, these are referred to
 * as "3986" regardless just as {@code java.net.URI} covers IRIs. {@code java.net.URI}
 * parses and allocates and follows RFC 2396 with modifications (several of which are in
 * RFC 3986).
 * <p>
 * This provides a fast checking operation which does not copy the various parts of the
 * IRI and which creates a single object. The cost of extracting and allocating strings
 * happen when the getter for the component is called.
 * <p>
 * Implements the algorithms specified in RFC 3986 operations for:
 * <ul>
 * <li>Checking a string matches the IRI grammar.
 * <li>Extracting components of an IRI
 * <li>Resolving an IRI against a base IRI.
 * <li>Normalizing an IRI
 * <li>Relativize an IRI for a given base IRI.
 * <li>Building an IRI from components.
 * </ul>
 *
 * <h3>Usage</h3>
 *
 * <h4>Check</h4>
 * Check conformance with the RFC 3986 grammar:
 * <pre>
 *     RFC3986.checkSyntax(string);
 * </pre>
 * Check conformance with the RFC 3986 grammar and any applicable scheme specific rules:
 * <pre>
 *     IRI3986 iri = RFC3986.create(string);
 *     iri.hasViolations();
 * </pre>
 * <h4>Extract the components of IRI</h4>
 * <pre>
 *     IRI3986 iri = RFC3986.create(string);
 *     iri.path();
 *     ...
 * </pre>
 * <h4>Resolve</h4>
 * <pre>
 *     IRI3986 base = RFC3986.create(baseIRIString);
 *     IRI3986 iri = RFC3986.create(string);
 *     IRI3986 iri2 = RFC3986.resolve(base);
 * </pre>
 * <h4>Normalize</h4>
 * <pre>
 *     IRI3986 iri  = RFC3986.create(string);
 *     IRI3986 iri2 = RFC3986.normalize(iri);
 * </pre>
 * <h4>Relative IRI</h4>
 * <pre>
 *     IRI3986 base = RFC3986.create(baseIRIString);
 *     IRI3986 target = RFC3986.create(string);
 *     IRI3986 relative = RFC3986relativize(base, target);
 *     // then base.resolve(relative) equals target
 * </pre>
 * <h4>Build an IRI3986 from componets</h4>
 * <pre>
 *     IRI3986 iri = RFC3986.newBuilder()
 *                       .scheme("http")
 *                       .host("example.org")
 *                       .path("/dir/page.html")
 *                       .build();
 *     System.out.println(iri.str());
 * </pre>
 *
 * <h3>RFC Regular Expression</h3>
 *
 * An IRI can be created using the regular expression
 * of RFC 3986. This regular expression identifies the components
 * without checking for correct use of characters within components.
 * It may be useful when an IRI does to conform to the details of the
 * RFC 3986 syntax, for example spaces in the path component.
 */

public class RFC3986 {
    /**
     * Determine if the string conforms to the IRI syntax. If not, it throws an exception.
     * This operation checks the string against the RFC3986/7 grammar; it does not apply
     * scheme specific rules.
     */
    public static void checkSyntax(String iristr) {
        IRI3986.checkSyntax(iristr);
    }

    /**
     * Parse the string in accordance with the general IRI grammar.
     * If not, it throws an exception.
     * <p>
     * This reports schema-specific violations : see {@link IRI3986#hasViolations()} and {@link IRI3986#forEachViolation}.
     */
    public static IRI3986 create(String iristr) {
        return IRI3986.create(iristr);
    }

    /**
     * Create an {@link IRI3986} object; report errors and warnings.
     * This operation always returns an object; it does not throw an exception, nor return null.
     * The object may not be a valid IRI.
     * <p>
     * Errors and warning may be accessed with {@link IRI3986#hasViolations()} and {@link IRI3986#forEachViolation}.
     */
    public static IRI3986 createAny(String iristr) {
        IRI3986 iri = IRI3986.createAny(iristr);
        return iri;
    }

    /** Create an IRI builder */
    public static Builder newBuilder() {
        return new Builder();
    }

    /** Ensure an {@link IRI} is a {@link IRI3986} */
    public static IRI3986 create(IRI iriOther) {
        if ( iriOther instanceof IRI3986 ref3986 )
            return ref3986;
        return RFC3986.newBuilder()
                .scheme(iriOther.scheme()).authority(iriOther.authority())
                .path(iriOther.path())
                .query(iriOther.query()).fragment(iriOther.fragment())
                .build();
    }

    /** Normalize an IRI (RFC 3986 - Syntax-Based Normalization) */
    public static IRI3986 normalize(IRI3986 iri) { return iri.normalize(); }

    /** Resolve an IRI against a base. */
    public static IRI3986 resolve(IRI3986 base, IRI3986 iri) { return base.resolve(iri); }

    /**
     * For a given base, return (if possible) an IRI that is relative to base.
     * If input iri is relative, this is returned unchanged.
     */
    public static IRI3986 relativize(IRI3986 base, IRI3986 iri) { return iri.relativize(base); }

    /**
     * Create an IRI using the regular expression of RFC 3986.
     * Throws an exception of the regular expression does not match.
     * The regular expression assumes a valid RFC3986 IRI and splits
     * out the components.
     * This may be useful to extract components of an IRI with bad syntax.
     * This does not check the character rules of the syntax,
     * nor check scheme specific rules.
     * Use the resulting IRI3986 with care.
     */

    public static IRI3986 createByRegex(String iriStr) { return IRI3986.createByRegex(iriStr); }

    // Taken from jena-iri - more groups. Breaks apart authority.
    /*package*/ static final String altRFC3986regex =
             "(([^:/?#]*):)?" +               // scheme
             "(//((([^/?#@]*)@)?" +           // user info
             "(\\[[^/?#]*\\]|([^/?#:]*))?" +  // host
             "(:([^/?#]*))?))?" +             // port
             "([^#?]*)?" +                    // path
             "(\\?([^#]*))?" +                // query
             "(#(.*))?";                      // frag


//        1  -- http:
//        2  -- http
//        3  -- //u:p@host.com:2020
//        4  -- u:p@host.com:2020
//        5  -- u:p@
//        6  -- u:p
//        7  -- host.com
//        8  -- host.com
//        9  -- :2020
//        10 -- 2020
//        11 -- /path
//        12 -- ?query
//        13 -- query
//        14 -- #frag
//        15 -- frag


    /** RFC 3986 regular expression.
     * This assumes a well-formed URI reference; it will accept other mis-formed strings.
     * <ul>
     * <li>Group 2 : scheme without ':' (group 1, with ':')
     * <li>Group 4 : authority (group 3 is authority '//')
     * <li>Group 5 : path, with leading '/' if any.
     * <li>Group 7 : query, without '?' (group 6, with '?')
     * <li>Group 9 : fragment, without '#' (group 8, with '#')
     * </ul>
     * <ul>
     * <li>Group 1 : scheme with ':'
     * <li>Group 3 : authority with '//'
     * <li>Group 5 : path, with leading '/' if any.
     * <li>Group 6 : query, with '?'
     * <li>Group 8 : fragment, with '#'
     * </ul>
     *
     * <pre>
     * "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?"
     *     12           3  4          5       6   7        8 9
     * </pre>
     * <ul>
     * <li>scheme    = $2
     * <li>authority = $4
     * <li>path      = $5
     * <li>query     = $7
     * <li>fragment  = $9
     * </ul>
     */
    public static final Pattern rfc3986regex = Pattern.compile
            (//"^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?"
                "^(([^:/?#]+):)?"+      // scheme
                "(//([^/?#]*))?"+       // authority
                "([^?#]*)"+             // path
                "(\\?([^#]*))?"+        // query
                "(#(.*))?");            // fragment

    /*
   URI           = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

   hier-part     = "//" authority path-abempty
                 / path-absolute
                 / path-rootless
                 / path-empty

   URI-reference = URI / relative-ref

   absolute-URI  = scheme ":" hier-part [ "?" query ]

   relative-ref  = relative-part [ "?" query ] [ "#" fragment ]

   relative-part = "//" authority path-abempty
                 / path-absolute
                 / path-noscheme
                 / path-empty

   scheme        = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )

   authority     = [ userinfo "@" ] host [ ":" port ]
   userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
   host          = IP-literal / IPv4address / reg-name
   port          = *DIGIT

   IP-literal    = "[" ( IPv6address / IPvFuture  ) "]"

   IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )

   IPv6address   =                            6( h16 ":" ) ls32
                 /                       "::" 5( h16 ":" ) ls32
                 / [               h16 ] "::" 4( h16 ":" ) ls32
                 / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
                 / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
                 / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
                 / [ *4( h16 ":" ) h16 ] "::"              ls32
                 / [ *5( h16 ":" ) h16 ] "::"              h16
                 / [ *6( h16 ":" ) h16 ] "::"

   h16           = 1*4HEXDIG
   ls32          = ( h16 ":" h16 ) / IPv4address
   IPv4address   = dec-octet "." dec-octet "." dec-octet "." dec-octet

   dec-octet     = DIGIT                 ; 0-9
                 / %x31-39 DIGIT         ; 10-99
                 / "1" 2DIGIT            ; 100-199
                 / "2" %x30-34 DIGIT     ; 200-249
                 / "25" %x30-35          ; 250-255

   reg-name      = *( unreserved / pct-encoded / sub-delims )

   path          = path-abempty    ; begins with "/" or is empty
                 / path-absolute   ; begins with "/" but not "//"
                 / path-noscheme   ; begins with a non-colon segment
                 / path-rootless   ; begins with a segment
                 / path-empty      ; zero characters

   path-abempty  = *( "/" segment )
   path-absolute = "/" [ segment-nz *( "/" segment ) ]
   path-noscheme = segment-nz-nc *( "/" segment )
   path-rootless = segment-nz *( "/" segment )
   path-empty    = 0<pchar>

   segment       = *pchar
   segment-nz    = 1*pchar
   segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
                 ; non-zero-length segment without any colon ":"

   pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"



   pct-encoded   = "%" HEXDIG HEXDIG

   unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
   reserved      = gen-delims / sub-delims
   gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
   sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
                 / "*" / "+" / "," / ";" / "="
  RFC 3897 : IRIs
----
    NB "unreserved" used in
    IPvFuture      = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
----

   ipchar         = iunreserved / pct-encoded / sub-delims / ":" / "@"

   iquery         = *( ipchar / iprivate / "/" / "?" )

   iunreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar

   ucschar        = %xA0-D7FF / %xF900-FDCF / %xFDF0-FFEF
                  / %x10000-1FFFD / %x20000-2FFFD / %x30000-3FFFD
                  / %x40000-4FFFD / %x50000-5FFFD / %x60000-6FFFD
                  / %x70000-7FFFD / %x80000-8FFFD / %x90000-9FFFD
                  / %xA0000-AFFFD / %xB0000-BFFFD / %xC0000-CFFFD
                  / %xD0000-DFFFD / %xE1000-EFFFD

   iprivate       = %xE000-F8FF / %xF0000-FFFFD / %x100000-10FFFD


            ALPHA          =  %x41-5A / %x61-7A   ; A-Z / a-z
            DIGIT          =  %x30-39             ; 0-9


ABNF core rules: RFC 5234

         ALPHA          =  %x41-5A / %x61-7A   ; A-Z / a-z

         BIT            =  "0" / "1"

         CHAR           =  %x01-7F
                                ; any 7-bit US-ASCII character,
                                ;  excluding NUL

         CR             =  %x0D
                                ; carriage return

         CRLF           =  CR LF
                                ; Internet standard newline

         CTL            =  %x00-1F / %x7F
                                ; controls

         DIGIT          =  %x30-39
                                ; 0-9

         DQUOTE         =  %x22
                                ; " (Double Quote)

         HEXDIG         =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"

         HTAB           =  %x09
                                ; horizontal tab

         LF             =  %x0A
                                ; linefeed

         LWSP           =  *(WSP / CRLF WSP)
                                ; Use of this linear-white-space rule
                                ;  permits lines containing only white
                                ;  space that are no longer legal in
                                ;  mail headers and have caused
                                ;  interoperability problems in other
                                ;  contexts.
                                ; Do not use when defining mail
                                ;  headers and use with caution in
                                ;  other contexts.

         OCTET          =  %x00-FF
                                ; 8 bits of data

         SP             =  %x20

         VCHAR          =  %x21-7E
                                ; visible (printing) characters

         WSP            =  SP / HTAB
                                ; white space

     */
}
