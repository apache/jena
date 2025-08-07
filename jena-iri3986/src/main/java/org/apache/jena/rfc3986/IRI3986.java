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

import static java.lang.String.format;
import static org.apache.jena.rfc3986.Chars3986.EOF;
import static org.apache.jena.rfc3986.Chars3986.displayChar;
import static org.apache.jena.rfc3986.ParseErrorIRI3986.parseError;
import static org.apache.jena.rfc3986.URIScheme.*;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of RFC 3986 (URI), RFC 3987 (IRI). As is common, these are referred
 * to as "3986" regardless, just as {@code java.net.URI} covers IRIs. This provides a
 * fast checking operation which does not copy the various parts of the IRI and which
 * creates a single object. The cost of extracting and allocating strings happen when
 * the getter for the component is called. {@code java.net.URI} parses and allocates
 * and follows RFC 2396 with modifications (several of which are in RFC 3986). See
 * {@link RFC3986} for operations involving {@code IRI3986}. This package implements
 * the algorithms specified in RFC 3986 operations for:
 * <ul>
 * <li>Checking a string matches the IRI grammar.
 * <li>Extracting components of an IRI
 * <li>Normalizing an IRI
 * <li>Resolving an IRI against a base IRI.
 * <li>Rebuilding an IRI from components.
 * </ul>
 * Additions:
 * <ul>
 * <li>Scheme specific rules for Linked Data usage of HTTP IRIs and URNs.
 * </ul>
 * HTTP IRIs forbids the "user@" part which is strongly discouraged in IRIs and
 * requires a host name if the "//" is present.<br/>
 * Some additional check for RFC 8141 for URNs are included such as being of the form
 * {@code urn:NID:NSS}. Restrictions and limitations:
 * <ul>
 * <li>No normal form C checking when checking (currently). See
 * {@link Normalizer#isNormalized(CharSequence, java.text.Normalizer.Form)}.
 * </ul>
 * Usage:<br/>
 * Check conformance with the RFC 3986 grammar:
 *
 * <pre>
 * RFC3986.check(string);
 * </pre>
 *
 * Check conformance with the RFC 3986 grammar and any applicable scheme specific
 * rules:
 *
 * <pre>
 * RFC3986.check(string, true);
 * </pre>
 *
 * Validate and extract the components of IRI:
 *
 * <pre>
 *     IRI3986 iri = RFC3986.create(string);
 *     iri.getPath();
 *     ...
 * </pre>
 *
 * Resolve:
 *
 * <pre>
 *     IRI3986 base = ...
 *     IRI3986 iri = RFC3986.create(string);
 *     IRI3986 iri2 = iri.resolve(base);
 * </pre>
 *
 * Normalize:
 *
 * <pre>
 *     IRI3986 base = ...
 *     IRI3986 iri = RFC3986.create(string);
 *     IRI3986 iri2 = iri.normalize();
 * </pre>
 */
public class IRI3986 implements IRI {
    // RFC 3986, RFC 3987 grammars and the definition of ABNF names (RFC 5234) at the
    // end of the file.
    /**
     * Determine if the string conforms to the IRI syntax. If not, throw an
     * exception. This operation checks the string against the RFC3986/7 grammar; it
     * does not apply scheme specific rules
     */
    /* package */ static void checkSyntax(String iristr) {
        newAndParseEx(iristr);
    }

    /**
     * Create an {@code IRI3986} object or throw an exception if there is a syntax
     * error.
     * <p>
     * This operation also checks conformance to the rules of the IRI schemes for
     * some IRI schemes (http/https, urn:uuid, urn, file, did). These violations are
     * accessed with {@link #hasViolations()} and {@link #forEachViolation}.
     * <p>
     * See {@link Violations} for the mapping from issue to a warning or an error.
     * <p>
     * See {@link Issue} for the scheme specific issues covered.
     */
    public static IRI3986 create(String iristr) {
        IRI3986 iri = newAndParseAndCheck(iristr);
        return iri;
    }

    /**
     * Create an {@code IRI3986} object or throw an exception if there is a syntax
     * error.
     * <p>
     * This operation does not check conformance to the rules of IRI schemes.
     * <p>
     * Prefer {@link #create(String)} which records scheme-violations.
     * <p>
     * See {@link Violations} for the mapping from issue to a warning or an error.
     * <p>
     * See {@link Issue} for the scheme specific issues covered.
     */
    public static IRI3986 createSyntax(String iristr) {
        IRI3986 iri = newAndParseAndCheck(iristr);
        return iri;
    }

    /**
     * Create an {@code IRI3986} object; report errors and warnings. This operation
     * always returns an object; it does not throw an exception, nor return null.
     * <p>
     * Errors and warning may be accessed with {@link #hasViolations()} and
     * {@link #forEachViolation}.
     * <p>
     * This operation checks the resulting IRI conforms to URI scheme specific rules
     * only if the syntax as an IRI is valid.
     */
    public static IRI3986 createAny(String iristr) {
        IRI3986 iri = newAndCheck(iristr);
        return iri;
    }

    /**
     * Create an IRI3986, parsing the string to set all the members. If bad, by the
     * syntax defined by RFC 3986, throw an exception.
     * <p>
     * This operation does not check the resulting IRI conforms to URI scheme
     * specific rules - see {@link #newAndCheck(String)}.
     */
    private static IRI3986 newAndParseEx(String iristr) {
        IRI3986 iri = new IRI3986(iristr);
        iri.parse();
        return iri;
    }

    /**
     * Create an IRI3986, parsing the string to set all the members. If bad, by the
     * syntax defined by RFC 3986, throw an exception.
     * <p>
     * This operation does check the resulting IRI conforms to URI scheme specific
     * rules.
     * <p>
     * The result is a syntactically legal (by RFC 3986) IRI which may have
     * scheme-specific rule violations.
     */
    private static IRI3986 newAndParseAndCheck(String iristr) {
        IRI3986 iri = new IRI3986(iristr);
        iri.parse();
        iri.schemeSpecificRulesInternal();
        return iri;
    }

    /**
     * Create an IRI3986, parsing the string to set all the members. If bad, by the
     * syntax defined by RFC 3986, record this information; the state of the IRI3986
     * object only reflects the information parsed up until the error.
     * <p>
     * This operation does check the resulting IRI conforms to URI scheme specific
     * rules if the IRI string conforms to RFC 3986.
     */
    private static IRI3986 newAndCheck(String iriStr) {
        // The parser does not try to continue after it finds an error
        // in the RFC3986 syntax, so the components at the point of error
        // and later components are not recorded.
        IRI3986 iri = new IRI3986(iriStr);
        try {
            iri.parse();
            iri.schemeSpecificRulesInternal();
        } catch (IRIParseException ex) {
            String msg = ex.getMessage();
            addReportParseError(iri, iriStr, ex.getMessage());
        }
        return iri;
    }

    // Always set,
    private final String iriStr;
    private final int length;

    // Offsets of parsed components, together with cached value.
    // The value is not calculated until first used, so that pure checking
    // not need to create any extra objects.
    private int scheme0 = -1;
    private int scheme1 = -1;
    private String scheme = null;

    private int authority0 = -1;
    private int authority1 = -1;
    private String authority = null;

    private int userinfo0 = -1;
    private int userinfo1 = -1;
    // Do not retain.
    //private String userinfo = null;

    private int host0 = -1;
    private int host1 = -1;
    private String host = null;

    private int port0 = -1;
    private int port1 = -1;
    private String port = null;

    private int path0 = -1;
    private int path1 = -1;
    private String path = null;

    private int query0 = -1;
    private int query1 = -1;
    private String query = null;

    private int fragment0 = -1;
    private int fragment1 = -1;
    private String fragment = null;

    // Violations.
    private List<Violation> reports = null;

    private IRI3986(String iriStr) {
        this.iriStr = iriStr;
        this.length = iriStr.length();
    }

    /** The IRI in string form. This is guaranteed to parse to a ".equals" IRI. */
    @Override
    public final String str() {
        if ( iriStr != null )
            return iriStr;
        return rebuild();
    }

    /**
     * Does this IRI have any scheme specific issues?
     * <p>
     * The normal way to create IRIs, {@link #create} throws an
     * {@link IRIParseException}. Parse errors in IRI string are
     * {@link IRIParseException}s (unless {@link #createAny} is used). In addition to
     * parsing, IRIs are checked for some of the scheme-specific issues in the
     * standards. See enum {@link Issue}.
     * <p>
     * These are recorded in the IRI object; they do not automatically cause
     * exceptions. See {@link Violations} for mapping issues to warnings and errors.
     */
    public boolean hasViolations() {
        return reports != null && ! reports.isEmpty();
    }

    /**
     * Return true if this IRI has any violations greater than (and not equal to) the severity argument.
     */
    public boolean hasViolations(Severity levelSeverity) {
        if ( ! hasViolations() )
            return false;
        for ( var violation : reports ) {
            Severity severity = Violations.getSeverity(violation.issue());
            if ( severity.level() > levelSeverity.level() )
                return true;
        }
        return false;
    }

    /**
     * Call a consumer function for any violations recorded for this IRI.
     * <p>
     * The normal way to create IRIs, {@link #create}, throws an
     * {@link IRIParseException} if the IRI string does not match the grammar of RFC
     * 3986/3987 and other RFCs. In addition to parsing, IRIs are checked for
     * scheme-specific issues in the standards. See enum {@link Issue} for the issues
     * covered.
     * <p>
     * Issues are recorded as {@link Violations}.
     * These are recorded in the IRI object; they do not automatically cause
     * exceptions. See {@link Violations} for mapping issues to warnings and errors.
     * <p>
     * See {@link #createAny} for an operation to create a IRI that records parse
     * exceptions are does not throw exception.
     */
    public void forEachViolation(Consumer<Violation> action) {
        if ( reports == null )
            return;
        reports.forEach(action);
    }

    /**
     * Return an immutable list of the violations for this IRI.
     * See {@link #forEachViolation(Consumer)}.
     */
    public List<Violation> violations() {
        if ( reports == null )
            return List.of();
        return reports;
    }

    /** Human-readable appearance. Use {@link #str()} to a string to use in code. */
    @Override
    public String toString() {
        // Human readable form - may be overridden.
        return str();
    }

    @Override
    public boolean hasScheme() {
        return scheme0 != -1;
    }

    @Override
    public String scheme() {
        if ( hasScheme() && scheme == null )
            scheme = part(iriStr, scheme0, scheme1);
        return scheme;
    }

    @Override
    public boolean hasAuthority() {
        return authority0 != -1;
    }

    @Override
    public String authority() {
        if ( hasAuthority() && authority == null )
            authority = part(iriStr, authority0, authority1);
        return authority;
    }

    @Override
    public boolean hasUserInfo() {
        return userinfo0 != -1;
    }

    @Override
    public String userInfo() {
        // Do not retain.
        return part(iriStr, userinfo0, userinfo1);
    }

    @Override
    public boolean hasHost() {
        return host0 != -1;
    }

    @Override
    public String host() {
        if ( hasHost() && host == null )
            host = part(iriStr, host0, host1);
        return host;
    }

    @Override
    public boolean hasPort() {
        return port0 != -1;
    }

    @Override
    public String port() {
        if ( hasPort() && port == null )
            port = part(iriStr, port0, port1);
        return port;
    }

    @Override
    public boolean hasPath() {
        // Not "path0 != -1"
        // Rule path-abempty (or path-empty ) is "".
        // There is always a path, it may be "".
        return true;
    }

    @Override
    public String path() {
        // Assigning to a object member is atomic and even if two part/assignment
        // overlap, they are the same value-equals string.
        if ( hasPath() && path == null )
            path = part(iriStr, path0, path1);
        if ( path == null )
            path = "";
        return path;
    }

    @Override
    public String[] pathSegments() {
        String x = path();
        if ( x == null )
            return null;
        return x.split("/");
    }

    @Override
    public boolean hasQuery() {
        return query0 != -1;
    }

    @Override
    public String query() {
        if ( hasQuery() && query == null )
            query = part(iriStr, query0, query1);
        return query;
    }

    @Override
    public boolean hasFragment() {
        return fragment0 != -1;
    }

    @Override
    public String fragment() {
        if ( hasFragment() && fragment == null )
            fragment = part(iriStr, fragment0, fragment1);
        return fragment;
    }

    /**
     * <a href="https://tools.ietf.org/html/rfc3986#section-4.3">RFC 3986, Section 4.3</a>
     */
    @Override
    public boolean isAbsolute() {
        // With scheme, without fragment
        return hasScheme() && !hasFragment();
    }

    /**
     * <a href="https://tools.ietf.org/html/rfc3986#section-4.2">RFC 3986, Section 4.2</a>
     */
    @Override
    public boolean isRelative() {
        // No scheme.
        // This is not "! isAbsolute()"

        // @formatter:off
        // relative-part = "//" authority path-abempty
        //               / path-absolute
        //               / path-noscheme
        //               / path-empty
        // whereas:
        // hier-part = "//" authority path-abempty
        //           / path-absolute
        //           / path-rootless
        //           / path-empty
        //
        // @formatter:on
        //
        // Difference between "path-noscheme" and "path-rootless" is that
        // "path-noscheme" does not allow a colon in the first segment.
        // But we parsed it via the URI rule.
        return !hasScheme();
    }

    /**
     * <a href="https://tools.ietf.org/html/rfc3986#section-3">RFC 3986, Section 3</a>.
     * IRI has a scheme, no authority (no //) and is path-rootless (does not
     * start with /) e.g. URN's.
     */
    @Override
    public boolean isRootless() {
        return hasScheme() && !hasAuthority() && rootlessPath();
    }

    /**
     * <a href="https://tools.ietf.org/html/rfc3986#section-1.2.3">RFC 3986, Section 1.2.3 : Hierarchical Identifiers</a>.
     */
    @Override
    public boolean isHierarchical() {
        return hasScheme() && hasAuthority() && hierarchicalPath();
    }

    private boolean hierarchicalPath() {
        return hasPath() && firstChar(path0, path1) == '/';
    }

    private boolean rootlessPath() {
        return hasPath() && firstChar(path0, path1) != '/';
    }

    /**
     * Don't make the parts during parsing but wait until needed, if at all.
     */
    private static String part(String str, int start, int finish) {
        if ( start >= 0 ) {
            if ( finish > str.length() ) {
                // Safety.
                return str.substring(start);
            }
            return str.substring(start, finish);
        }
        return null;
    }

    /**
     * Find a character in a substring. Return -1 if no present.
     */
    private static int contains(String str, char character, int start, int finish) {
        for ( int i = start; i < finish; i++ ) {
                char ch = str.charAt(i);
                if ( ch == character )
                    return i;
        }
        return -1;
    }

    /**
     * Return the first char in segment x0..x1 or EOF if the segment is not defined
     * or of zero length. x0 = start index, x1 index after segment. x1 = x0 means no
     * segment, x1 = x0+1 is zero length.
     */
    private char firstChar(int x0, int x1) {
        if ( x0 < 0 )
            return EOF;
        if ( x1 < 0 )
            return EOF;
        if ( x0 > x1 )
            return EOF;
        return charAt(x0);
    }

    /** Test whether the IRI is RFC 3986 compatible;that is, has only ASCII characters. */
    public boolean isRFC3986() {
        // The URI is valid syntax so we just need to test for non-ASCII characters.
        return isASCII(iriStr, 0, iriStr.length());
    }

    /**
     * <a href="https://tools.ietf.org/html/rfc3986#section-6.2.2">RFC 3986, Section
     * 6.2.2 : Syntax-Based Normalization.</a>.
     */
    @Override
    public IRI3986 normalize() {
        String scheme = scheme();
        String authority = authority();
        String path = path();
        String query = query();
        String fragment = fragment();

        // 6.2.2. Syntax-Based Normalization
        //
        // Implementations may use logic based on the definitions provided by
        // this specification to reduce the probability of false negatives.
        // This processing is moderately higher in cost than character-for-
        // character string comparison. For example, an application using this
        // approach could reasonably consider the following two URIs equivalent:
        //
        // example://a/b/c/%7Bfoo%7D
        // eXAMPLE://a/./b/../b/%63/%7bfoo%7d
        //
        // Web user agents, such as browsers, typically apply this type of URI
        // normalization when determining whether a cached response is
        // available. Syntax-based normalization includes such techniques as
        // case normalization, percent-encoding normalization, and removal of
        // dot-segments.
        //
        // 6.2.2.1. Case Normalization
        //
        // For all URIs, the hexadecimal digits within a percent-encoding
        // triplet (e.g., "%3a" versus "%3A") are case-insensitive and therefore
        // should be normalized to use uppercase letters for the digits A-F.
        //
        // When a URI uses components of the generic syntax, the component
        // syntax equivalence rules always apply; namely, that the scheme and
        // host are case-insensitive and therefore should be normalized to
        // lowercase. For example, the URI <HTTP://www.EXAMPLE.com/> is
        // equivalent to <http://www.example.com/>. The other generic syntax
        // components are assumed to be case-sensitive unless specifically
        // defined otherwise by the scheme (see Section 6.2.3).

        scheme = toLowerCase(scheme);
        authority = toLowerCase(authority);

        // 6.2.2.2. Percent-Encoding Normalization
        //
        // The percent-encoding mechanism (Section 2.1) is a frequent source of
        // variance among otherwise identical URIs. In addition to the case
        // normalization issue noted above, some URI producers percent-encode
        // octets that do not require percent-encoding, resulting in URIs that
        // are equivalent to their non-encoded counterparts. These URIs should
        // be normalized by decoding any percent-encoded octet that corresponds
        // to an unreserved character, as described in Section 2.3.

        // percent encoding - to upper case.
        // percent encoding - remove unnecessary encoding.
        // Occurs in authority, path, query and fragment.
        authority = normalizePercent(authority);
        path = normalizePercent(path);
        query = normalizePercent(query);
        fragment = normalizePercent(fragment);

        // 6.2.2.3. Path Segment Normalization

        if ( path != null )
            path = AlgResolveIRI.remove_dot_segments(path);
        if ( path == null || path.isEmpty() )
            path = "/";

        // 6.2.3. Scheme-Based Normalization

        // HTTP and :80.
        // HTTPS and :443

        if ( authority != null && authority.endsWith(":") )
            authority = authority.substring(0, authority.length() - 1);

        if ( Objects.equals("http", scheme) ) {
            if ( authority != null && authority.endsWith(":80") )
                authority = authority.substring(0, authority.length() - 3);
        } else if ( Objects.equals("https", scheme) ) {
            if ( authority != null && authority.endsWith(":443") )
                authority = authority.substring(0, authority.length() - 4);
        }

        // 6.2.4. Protocol-Based Normalization
        // None.

        // Rebuild.
        if ( Objects.equals(scheme, scheme()) && Objects.equals(authority, authority()) && Objects.equals(path, path())
             && Objects.equals(query, query()) && Objects.equals(fragment, fragment()) ) {
            // No change and this has had all the elements calculated and substring
            // done.
            return this;
        }

        String s = rebuild(scheme, authority, path, query, fragment);
        return newAndCheck(s);
    }

    /**
     * Convert unnecessary %-encoding into the real character.
     * Convert %-encoding to upper case.
     */
    private String normalizePercent(String str) {
        if ( str == null )
            return str;
        int idx = str.indexOf('%');
        if ( idx < 0 )
            return str;
        final int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for ( int i = 0 ; i < len ; i++ ) {
            char ch = str.charAt(i);
            if ( !Chars3986.isPctEncoded(ch, str, i) ) {
                sb.append(ch);
                continue;
            }
            char ch1 = toUpperASCII(str.charAt(i + 1));
            char ch2 = toUpperASCII(str.charAt(i + 2));
            i += 2;
            char x = (char)(Chars3986.hexValue(ch1) * 16 + Chars3986.hexValue(ch2));

            if ( Chars3986.unreserved(x) ) {
                sb.append(x);
                continue;
            }
            sb.append('%');
            sb.append(ch1);
            sb.append(ch2);
        }
        return sb.toString();
    }

    /** Uppercase - ASCII only (used for percent encoding) */
    private char toUpperASCII(char ch) {
        if ( ch >= 'a' && ch <= 'z' )
            ch = (char)(ch + ('A' - 'a'));
        return ch;
    }

    /** Lowercase, locale insensitive */
    private String toLowerCase(String string) {
        if ( string == null )
            return null;
        return string.toLowerCase(Locale.ROOT);
    }

    /**
     * Return (if possible), an IRI that is relative to the base argument.
     * <p>
     * The base must have a scheme, and must not have a query string.
     * <p>
     * Any fragment on the base IRI is lost.
     * <p>
     * If no relative IRI can be found, return null.
     */
    public IRI3986 relativize(IRI iri) {
        // "this" is the base.
        return AlgResolveIRI.relativize(this, iri);
    }

    private static final boolean strictResolver = false;
    /**
     * Resolve an IRI, using this as the base.
     * <a href="https://tools.ietf.org/html/rfc3986#section-5">RFC 3986 section 5</a>
     */
    public IRI3986 resolve(IRI3986 other) {
        // Not isAbsolute here - absolute URIs do not allow a fragment.
        // "!isRelative" is not the same as "isAbsolute"

//        -- A non-strict parser may ignore a scheme in the reference
//        -- if it is identical to the base URI's scheme.

        if ( strictResolver && !other.isRelative() ) {
            // RFC 3986 section 5.2.2
            //          -- A non-strict parser may ignore a scheme in the reference
            //          -- if it is identical to the base URI's scheme.
            return other;
        }
//        if ( strictResolver && ! this.hasScheme() )
//            return other;
        // Be lax - don't require base to have scheme.
        // Relative path resolves against relative path.
        /* 5.2.2. Transform References */
        IRI3986 iri = AlgResolveIRI.resolve(this, other);
        if ( iri != other )
            // AlgResolveIRI.resolve only rebuilds to RFC 3986 syntax.
            iri.schemeSpecificRulesInternal();
        return iri;
    }

    /** Build a {@link IRI3986} from components. */
    public static IRI3986 build(String scheme, String authority, String path, String query, String fragment) {
        String s = rebuild(scheme, authority, path, query, fragment);
        return newAndParseEx(s);
    }

    /** RFC 3986 : 5.3. Component Recomposition */
    public String rebuild() {
        return rebuild(scheme(), authority(), path(), query(), fragment());
    }

    // 5.3. Component Recomposition
    private static String rebuild(String scheme, String authority, String path, String query, String fragment) {
        StringBuilder result = new StringBuilder();
        if ( scheme != null ) {
            result.append(scheme);
            result.append(":");
        }

        if ( authority != null ) {
            result.append("//");
            result.append(authority);
        }

        if ( path != null )
            result.append(path);

        if ( query != null ) {
            result.append("?");
            result.append(query);
        }

        if ( fragment != null ) {
            result.append("#");
            result.append(fragment);
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(iriStr);
    }

    // hashCode and equals.
    // Slots like "authority" are caches and only set when their value is needed
    // in the associated getter.
    // The positions in the iriStr (authority0, authority1) are set.
    // An IRI3986 always as the iriStr set.

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( !(obj instanceof IRI3986) )
            return false;
        IRI3986 other = (IRI3986)obj;
        return Objects.equals(iriStr, other.iriStr);
    }

    /** Detail comparison - includes internal fields but not reports. */
    /* package */ boolean identical(IRI3986 other, boolean includeComponentStrings) {
        if ( this == other )
            return true;
        if ( other == null )
            return false;
        // Force string creation by using ()
        // @formatter:off
        return
               Objects.equals(iriStr, other.iriStr) && length == other.length &&

               scheme0 == other.scheme0 && scheme1 == other.scheme1 &&
                   ( !includeComponentStrings || Objects.equals(scheme(), other.scheme())) &&

               authority0 == other.authority0 && authority1 == other.authority1 &&
                   ( !includeComponentStrings || Objects.equals(authority(), other.authority())) &&

               userinfo0 == other.userinfo0 && userinfo1 == other.userinfo1 &&
                   ( !includeComponentStrings || Objects.equals(userInfo(), other.userInfo())) &&

               host0 == other.host0 && host1 == other.host1 &&
                   ( !includeComponentStrings || Objects.equals(host(), other.host())) &&

               port0 == other.port0 && port1 == other.port1 &&
                   ( !includeComponentStrings || Objects.equals(port(), other.port())) &&

               path0 == other.path0 && path1 == other.path1 &&
                   ( !includeComponentStrings || Objects.equals(path(), other.path())) &&

               query0 == other.query0 && query1 == other.query1 &&
                   ( !includeComponentStrings || Objects.equals(query(), other.query())) &&

               fragment0 == other.fragment0 && fragment1 == other.fragment1 &&
                   ( !includeComponentStrings || Objects.equals(fragment(), other.fragment()))
               ;
        // @formatter:on
    }

    // ==== Regex

    private static final Pattern authorityRegex = Pattern.compile("(([^/?#@]*)@)?" +               // user
                                                                  "(\\[[^/?#]*\\]|([^/?#:]*))?" +  // host
                                                                  "(:([^/?#]*)?)?");               // port

    /**
     * Create an IRI using the regular expression of RFC 3986. Throws an exception of
     * the regular expression does not match. The regular expression assumes a valid
     * RFC3986 IRI and splits out the components. This may be useful to extract
     * components of an IRI with bad syntax. This does not check the character rules
     * of the syntax, nor check scheme specific rules. Use the resulting IRI3986 with
     * care.
     */
    static IRI3986 createByRegex(String iriStr) {
        Objects.requireNonNull(iriStr);
        Pattern pattern = RFC3986.rfc3986regex;
        Matcher m = pattern.matcher(iriStr);
        if ( !m.matches() )
            // Does not return.
            throw parseError(iriStr, "String does not match the regular expression for IRIs");
        final int length = iriStr.length();
        final int schemeGroup = 2;
        final int authorityGroup = 4;
        final int pathGroup = 5;
        final int queryGroup = 7;
        final int fragmentGroup = 9;

        // Offsets of parsed components, together with cached value.
        // The value is not calculated until first used, so that pure checking
        // not need to create any extra objects.

        IRI3986 iri = new IRI3986(iriStr);

        iri.scheme0 = m.start(schemeGroup);
        iri.scheme1 = m.end(schemeGroup);
        iri.scheme = m.group(schemeGroup);

        iri.authority0 = m.start(authorityGroup);
        iri.authority1 = m.end(authorityGroup);
        iri.authority = m.group(authorityGroup);

        // Unset values.
        iri.userinfo0 = -1;
        iri.userinfo1 = -1;
        //iri.userinfo = null;

        iri.host0 = -1;
        iri.host1 = -1;
        iri.host = null;

        iri.port0 = -1;
        iri.port1 = -1;
        iri.port = null;

        iri.path0 = m.start(pathGroup);
        iri.path1 = m.end(pathGroup);
        iri.path = m.group(pathGroup);
        if ( iri.path.isEmpty() ) {
            // The regex always matches path, including no characters.
            iri.path0 = -1;
            iri.path1 = -1;
        }

        iri.query0 = m.start(queryGroup);
        iri.query1 = m.end(queryGroup);
        iri.query = m.group(queryGroup);

        iri.fragment0 = m.start(fragmentGroup);
        iri.fragment1 = m.end(fragmentGroup);
        iri.fragment = m.group(fragmentGroup);

        m = null;

        if ( iri.authority != null ) {
            final int userinfoGroup = 2;
            final int hostGroup = 3;
            final int portGroup = 6;
            int offset = iri.authority0;

            Matcher m2 = authorityRegex.matcher(iri.authority);
            if ( m2.matches() ) {
                // Move indexes by start of authority if set.
                //iri.userinfo = m2.group(userinfoGroup);
                iri.userinfo0 = offset(offset, m2.start(userinfoGroup));
                iri.userinfo1 = offset(offset, m2.end(userinfoGroup));

                iri.host = m2.group(hostGroup);
                iri.host0 = offset(offset, m2.start(hostGroup));
                iri.host1 = offset(offset, m2.end(hostGroup));

                iri.port = m2.group(portGroup);
                iri.port0 = offset(offset, m2.start(portGroup));
                iri.port1 = offset(offset, m2.end(portGroup));
            }
        }

        return iri;
    }

    private static int offset(int offset, int index) {
        return index < 0 ? index : offset + index;
    }

    // ----------------------------------------------------------------------
    // ==== Parsing

    /** Parse (i.e. check) or create an IRI object. */
    private IRI3986 parse() {
        int x = scheme(0);
        if ( x > 0 ) {
            // URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
            // absolute-URI = scheme ":" hier-part [ "?" query ]
            scheme0 = 0;
            scheme1 = x;
            // and move over ':'
            x = withScheme(x + 1);
        } else {
            // relative-ref = relative-part [ "?" query ] [ "#" fragment ]
            x = withoutScheme(0);
        }

        // Did the process consume the whole string?
        if ( x != length ) {
            String label;
            if ( fragment0 >= 0 )
                label = "fragment";
            else if ( query0 >= 0 )
                label = "query";
            else
                label = "path";
            // System.err.printf("(x3=%d, length=%d)\n", x, length);
            throw parseError(iriStr, "Bad character in " + label + " component: " + displayChar(charAt(x)));
        }
        return this;
    }

    // scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
    private int scheme(int start) {
        int p = start;
        int end = length;
        while (p < end) {
            char c = charAt(p);
            if ( c == ':' )
                return p;
            if ( !Chars3986.isAlpha(c) ) {
                if ( p == start )
                    // Bad first character
                    return -1;
                if ( !(Chars3986.isDigit(c) || c == '+' || c == '-' || c == '.') )
                    // Bad subsequent character
                    return -1;
            }
            p++;
        }
        // Did not find ':'
        return 0;
    }

    /** Parse any scheme RFC 3986/3987 URI/IRI string. */
    private int withScheme(int start) {
        // @formatter:off
        //
        // URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]

        // absolute-URI = scheme ":" hier-part [ "?" query ]
        // hier-part    = "//" authority path-abempty
        //              / path-absolute
        //              / path-rootless
        //              / path-empty
        //
        // @formatter:on

        // Check for specific parsers: URN, UUID, OID, DID
        // While this class is general, directly parsing and checking may be advantageous.
        // Note that errors in scheme-specific schemes will need to be checked by the general code.
        // General code can throw IRIParseException or record scheme-specific violations.

        int p = maybeAuthority(start);
        return pathQueryFragment(p, true);
    }

    private int withoutScheme(int start) {
        // @formatter:off
        //
        // relative-ref = relative-part [ "?" query ] [ "#" fragment ]
        // relative-part = "//" authority path-abempty
        //                / path-absolute
        //                / path-noscheme
        //                / path-empty
        //
        // @formatter:on
        // Check not starting with ':' then path-noscheme is the same as
        // path-rootless.
        char ch = charAt(start);
        if ( ch == ':' )
            throw parseError(iriStr, "A URI without a scheme can't start with a ':'");
        int p = maybeAuthority(start);
        return pathQueryFragment(p, false);
    }

    // ---- Authority

    private int maybeAuthority(int start) {
        // "//" authority
        int p = start;
        char ch1 = charAt(p);
        char ch2 = charAt(p + 1);
        if ( ch1 == '/' && ch2 == '/' ) {
            p += 2;
            p = authority(p);
        }
        return p;
    }

    // @formatter:off
    /*
     * authority     = [ userinfo "@" ] host [ ":" port ]
     * userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
     * host          = IP-literal / IPv4address / reg-name
     * port          = *DIGIT
     *
     * IP-literal    = "[" ( IPv6address / IPvFuture  ) "]"
     * IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
     * IPv6address   = hex and ":", and "." for IPv4 in IPv6.
     * IPv4address   = dec-octet "." dec-octet "." dec-octet "." dec-octet
     *
     * reg-name = *( unreserved / pct-encoded / sub-delims )
     *
     * So the section is only unreserved / pct-encoded / sub-delims / ":" / "@" / "[" / "]".
     * isPChar includes ":" / "@" unreserved has "."
     *
     * iauthority     = [ iuserinfo "@" ] ihost [ ":" port ]
     * iuserinfo      = *( iunreserved / pct-encoded / sub-delims / ":" )
     * ihost          = IP-literal / IPv4address / ireg-name
     *
     * There are further restrictions on DNS names.
     * RFC 5890, RFC 5891, RFC 5892, RFC 5893
     */
    // @formatter:on
    private int authority(int start) {
        int end = length;
        int p = start;
        // Indexes for userinfo@host:port
        int endUserInfo = -1;
        int lastColon = -1;
        int countColon = 0;
        int startIPv6 = -1;
        int endIPv6 = -1;

        // Scan for whole authority then do some checking.
        // We need to know e.g. whether there is a userinfo section to check colons.
        while (p < end) {
            char ch = charAt(p);
            if ( ch == ':' ) {
                countColon++;
                lastColon = p;
            } else if ( ch == '/' ) {
                // Normal exit
                if ( startIPv6 >= 0 && endIPv6 == -1 )
                    throw parseError(iriStr, p + 1, "Bad IPv6 address - No closing ']'");
                break;
            } else if ( ch == '@' ) {
                if ( endUserInfo != -1 )
                    throw parseError(iriStr, p + 1, "Bad authority segment - multiple '@'");
                // Found userinfo end; reset counts and trackers.
                // Check for bad IPv6 []
                if ( startIPv6 != -1 || endIPv6 != -1 )
                    throw parseError(iriStr, p + 1, "Bad authority segment - contains '[' or ']'");
                endUserInfo = p;
                // Reset port colon tracking.
                countColon = 0;
                lastColon = -1;
            } else if ( ch == '[' ) {
                // Still to check whether user authority
                if ( startIPv6 >= 0 )
                    throw parseError(iriStr, p + 1, "Bad IPv6 address - multiple '['");
                startIPv6 = p;
            } else if ( ch == ']' ) {
                // Still to check whether user authority
                if ( startIPv6 == -1 )
                    throw parseError(iriStr, p + 1, "Bad IPv6 address - No '[' to match ']'");
                if ( endIPv6 >= 0 )
                    throw parseError(iriStr, p + 1, "Bad IPv6 address - multiple ']'");
                endIPv6 = p;
                // Reset port colon tracking.
                countColon = 0;
                lastColon = -1;
            } else if ( !isIPChar(ch, p) ) {
                break;
            }
            p++;
        }

        if ( startIPv6 != -1 ) {
            if ( endIPv6 == -1 )
                throw parseError(iriStr, startIPv6, "Bad IPv6 address - missing ']'");
            char ch1 = iriStr.charAt(startIPv6);
            char ch2 = iriStr.charAt(endIPv6);
            ParseIPv6Address.checkIPv6(iriStr, startIPv6, endIPv6 + 1);
        }

        // May not be valid but if tests fail there is an exception.
        authority0 = start;
        authority1 = p;
        int endAuthority = p;

        if ( endUserInfo != -1 ) {
            userinfo0 = start;
            userinfo1 = endUserInfo;
            host0 = endUserInfo + 1;
            if ( lastColon != -1 && lastColon < endUserInfo )
                // Not port, part of userinfo - ignore.
                lastColon = -1;
        } else {
            host0 = start;
        }

        // Check only one ":" in host.
        if ( countColon > 1 )
            throw parseError(iriStr, -1, "Multiple ':' in host:port section");

        if ( lastColon != -1 ) {
            host1 = lastColon;
            port0 = lastColon + 1;
            port1 = endAuthority;
            int x = port0;
            // check digits in port.
            while (x < port1) {
                char ch = charAt(x);
                if ( !Chars3986.isDigit(ch) )
                    break;
                x++;
            }
            if ( x != port1 )
                throw parseError(iriStr, -1, "Bad port");
        } else
            host1 = endAuthority;
        return endAuthority;
    }

    // ---- hier-part :: /path?query#fragment

    private int pathQueryFragment(int start, boolean withScheme) {
        // hier-part [ "?" query ] [ "#" fragment ]
        // relative-ref = relative-part [ "?" query ] [ "#" fragment ]

        // hier-part => path-abempty
        // relative-part = path-abempty
        //               / path-absolute
        //               / path-noscheme
        //               / path-empty
        // then [ "?" query ] [ "#" fragment ]

        int x1 = path(start, withScheme);

        if ( x1 < 0 ) {
            x1 = start;
        }

        int x2 = query(x1);
        if ( x2 < 0 ) {
            x2 = x1;
        }
        int x3 = fragment(x2);
        return x3;
    }

    // ---- Path
    // If not withScheme, then segment-nz-nc applies.
    private int path(int start, boolean withScheme) {
        // @formatter:off
        //
        // path = path-abempty  ; begins with "/" or is empty
        //      / path-absolute ; begins with "/" but not "//"
        //      / path-noscheme ; begins with a non-colon segment
        //      / path-rootless ; begins with a segment
        //      / path-empty    ; zero characters

        // path-abempty, path-absolute, path-rootless, path-empty
        //
        // path-abempty  = *( "/" segment )
        // path-absolute = "/" [ segment-nz *( "/" segment ) ]
        // path-noscheme = segment-nz-nc *( "/" segment )
        // path-rootless = segment-nz *( "/" segment )
        // path-empty = 0<pchar>

        // segment       = *pchar
        // segment-nz    = 1*pchar
        // segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )
        //
        // @formatter:on

        if ( start == length )
            return start;
        int segStart = start;
        int p = start;
        boolean allowColon = withScheme;

        while (p < length) {
            // skip segment-nz = 1*pchar
            char ch = charAt(p);

            int charLen = isIPCharLen(ch, p);
            if ( charLen == 1 ) {
                if ( !allowColon && ch == ':' ) {
                    // segment-nz-nc
                    throw parseError(iriStr, p + 1, "':' in initial segment of a scheme-less IRI");
                }
                p++;
                continue;
            }
            if ( charLen == 3 ) {
                // percent-encoded.
                p += 3;
                continue;
            }

            // End segment.
            // Maybe new one.
            if ( ch != '/' ) {
                if ( ch == ' ' )
                    throw parseError(iriStr, p + 1, "Space found in IRI");
                // ? or # else error
                if ( ch == '?' || ch == '#' )
                    break;
                // Not IPChar
                throw parseError(iriStr, p + 1, format("Bad character in IRI path: '%s' (U+%04X)", Character.toString((int)ch), (int)ch));
            }
            allowColon = true;
            segStart = p + 1;
            p++;
        }

        if ( p > start ) {
            path0 = start;
            path1 = Math.min(p, length);
        }
        return p;
    }

    // ---- Query & Fragment

    private int query(int start) {
        // query = *( pchar / "/" / "?" )
        // iquery = *( ipchar / iprivate / "/" / "?" )
        int x = trailer('?', start, true);

        if ( x >= 0 && x != start ) {
            query0 = start + 1;
            query1 = x;
        }
        if ( x < 0 )
            x = start;
        return x;
    }

    private int fragment(int start) {
        // fragment = *( pchar / "/" / "?" )
        // ifragment = *( ipchar / "/" / "?" )
        int x = trailer('#', start, false);
        if ( x >= 0 && x != start ) {
            fragment0 = start + 1;
            fragment1 = x;
        }
        if ( x < 0 )
            x = start;
        return x;
    }

    private int trailer(char startChar, int start, boolean allowPrivate) {
        if ( start >= length )
            return -1;
        if ( charAt(start) != startChar )
            return -1;
        int p = start + 1;
        while (p < length) {
            char ch = charAt(p);
            int charLen = isIPCharLen(ch, p);
            if ( charLen == 1 || charLen == 3 ) {
                p += charLen;
                continue;
            }
            // Trailer extra characters.
            if ( ch == '/' || ch == '?' ) {
                p++;
                continue;
            }

            if ( allowPrivate && Chars3986.isIPrivate(ch) ) {
                p++;
                continue;
            }
            // Not trailer.
            return p;
        }
        return p; // = length if correct IRI.
    }

    /** String.charAt except with an EOF character, not an exception. */
    private char charAt(int x) {
        if ( x < 0 )
            throw new IllegalArgumentException("Negative index");
        if ( x >= length )
            return EOF;
        return iriStr.charAt(x);
    }

    // ---- Character classification

    // Is the character at location 'x' percent-encoded? Looks at next two characters
    // if and only if ch is '%'. This function looks ahead 2 characters which will be
    // parsed but likely they are in the L1 or L2 cache and the alternative is more
    // complex logic. (return the read characters and new character position in some
    // way).
    private boolean isPctEncoded(char ch, int idx) {
        if ( ch != '%' )
            return false;
        char ch1 = charAt(idx + 1);
        char ch2 = charAt(idx + 2);
        return Chars3986.percentCheck(ch1, ch2, iriStr, idx);
    }

    // pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
    // pct-encoded = "%" HEXDIG HEXDIG
    //
    // unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
    // iunreserved = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar
    // reserved = gen-delims / sub-delims
    // gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
    // sub-delims = "!" / "$" / "&" / "'" / "(" / ")"
    // / "*" / "+" / "," / ";" / "="

    private boolean isPChar(char ch, int posn) {
        return Chars3986.unreserved(ch) || isPctEncoded(ch, posn) || Chars3986.subDelims(ch) || ch == ':' || ch == '@';
    }

    /**
     * Length expected in codepoints at location 'posn' for PChar. The function does
     * not account for surrogate pairs. Normally returns 1, except when '%' when it's
     * 3. Return -1 for error.
     */
    private int isPCharLen(char ch, int posn) {
        if ( Chars3986.unreserved(ch) || Chars3986.subDelims(ch) || ch == ':' || ch == '@' )
            return 1;
        if ( isPctEncoded(ch, posn) )
            return 3;
        return -1;
    }

    private boolean isIPChar(char ch, int posn) {
        return isPChar(ch, posn) || Chars3986.isUcsChar(ch);
    }

    /**
     * Length expected in codepoints at location 'posn' for IPChar. The function does
     * not account for combining chars. Normally returns 1, except when '%' when it's
     * 3. Return -1 for error.
     */
    private int isIPCharLen(char ch, int posn) {
        if ( Chars3986.unreserved(ch) || Chars3986.subDelims(ch) || ch == ':' || ch == '@' || Chars3986.isUcsChar(ch) )
            return 1;
        if ( isPctEncoded(ch, posn) )
            return 3;
        return -1;
    }

    private static boolean isASCII(String string, int start,int finish) {
        for ( int i = 0 ; i < finish ; i++ ) {
            char ch = string.charAt(i);
            if ( ch > 0x7F )
                return false;
        }
        return true;
    }

    // ==== Scheme specific checking.

    private IRI3986 schemeSpecificRulesInternal() {
        if ( reports != null ) {
            // Called on IRI that already has reports.
            return this;
        }

        checkGeneral();

        if ( !hasScheme() )
            // no scheme, no checks.
            return this;

        // Scheme is not necessarily lower case.
        // We could do dispatch twice, once fast path (assumes lower case) with a
        // switch statement.
        // Check accumulate errors and warnings.

        if ( fromScheme(iriStr, HTTPS) )
            checkHTTPS();
        else if ( fromScheme(iriStr, HTTP) )
            checkHTTP();
        else if ( fromScheme(iriStr, URN_UUID) )
            checkURN_UUID();
        else if ( fromScheme(iriStr, URN_OID) )
            checkURN_OID();
        // "urn" namespaces must go before this test.
        else if ( fromScheme(iriStr, URN) )
            checkURN();
        else if ( fromScheme(iriStr, FILE) )
            checkFILE();
        else if ( fromScheme(iriStr, UUID) )
            checkUUID();
        else if ( fromScheme(iriStr, DID) )
            checkDID();
        else if ( fromScheme(iriStr, OID) )
            checkOID();
        else if ( fromScheme(iriStr, EXAMPLE) )
            checkExample();

        if ( reports != null )
            // Immutable.
            reports = List.copyOf(reports);

        return this;
    }

    private void checkGeneral() {
        // RFC 3986   section 3,2.1
        // https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1
        /*
         * Use of the format "user:password" in the userinfo field is
         * deprecated.  Applications should not render as clear text any data
         * after the first colon (":") character found within a userinfo
         * subcomponent unless the data after the colon is the empty string
         * (indicating no password).  Applications may choose to ignore or
         * reject such data when it is received as part of a reference and
         * should reject the storage of such data in unencrypted form.
         */
        // See also rfc7230#section-2.7.1

        if ( hasUserInfo() ) {
            schemeReport(this,  Issue.iri_user_info_present, URIScheme.GENERAL, "Use of user info is deprecated");
            int idx = contains(iriStr, ':',  userinfo0, userinfo1);
            if ( idx >= 0 && idx < userinfo1-1 )
                schemeReport(this,  Issue.iri_password, URIScheme.GENERAL, "Non-empty password");
        }

        // RFC 3986   section 3,2.2
        // https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.2
        /*
         * Although host is case-insensitive,
         * case-insensitive, producers and normalizers should use lowercase
         * for registered names and hexadecimal addresses for the sake of
         * uniformity
         */

        if ( hasHost() ) {
            if ( containsUppercase(iriStr, host0, host1) )
                schemeReport(this, Issue.iri_host_not_lowercase, URIScheme.GENERAL, "Host name should be lowercase");
        }

        // RFC 3986 section 2.1
        /* If two URIs differ only in the case of hexadecimal digits used in
         * percent-encoded octets, they are equivalent. For consistency, URI
         * producers and normalizers should use uppercase hexadecimal digits for all
         * percent- encodings.
         */
        checkPercent();

        /*
         * The path segments "." and "..", also known as dot-segments, are
         * defined for relative reference within the path name hierarchy.  They
         * are intended for use at the beginning of a relative-path reference
         * (Section 4.2) to indicate relative position within the hierarchical
         * tree of names.
         * https://datatracker.ietf.org/doc/html/rfc3986#section-3.3
         */
        if ( hasPath() ) {
            boolean good = LibParseIRI.checkDotSegments(iriStr, path0,  path1);
            if ( ! good ) {
                schemeReport(this, Issue.iri_bad_dot_segments, URIScheme.GENERAL, "Dot segments should only appear at the start of a relative IRI");
            }
        }
    }

    private void checkPercent() {
        // Path onwards (lower case in host)
        // Legal syntax so percent encoded is hex.
        if ( path0 < 0 )
            return;
        int N = iriStr.length();
        for ( int i = path0 ; i < N ; i++ ) {
            char ch = iriStr.charAt(i);
            if ( ch == '%' ) {
                if ( i+2 > N ) {
                    // Too near the end.
                }
                char ch1 = iriStr.charAt(i+1);
                char ch2 = iriStr.charAt(i+2);
                if ( Chars3986.isHexDigitLC(ch1) || Chars3986.isHexDigitLC(ch2) ) {
                    schemeReport(this, Issue.iri_percent_not_uppercase, URIScheme.GENERAL, "Percent encoding should be uppercase");
                }
                i += 2;
            }
        }
    }

    // RFC 3986 section 3.1
    /* Although schemes are case-insensitive, the canonical form is lowercase and
     * documents that specify schemes must do so with lowercase letters. An
     * implementation should accept uppercase letters as equivalent to lowercase
     * in scheme names (e.g., allow "HTTP" as well as "http") for the sake of
     * robustness but should only produce lowercase scheme names for
     * consistency. */

    /**
     * Check scheme name.
     */
    private void checkSchemeName(URIScheme scheme) {
        String correctSchemeName = scheme.getSchemeName();

        if ( !hasScheme() ) {
            schemeReport(this, Issue.iri_scheme_expected, scheme, "No scheme name");
            return;
        }

        if ( !URIScheme.matchesExact(iriStr, scheme) ) {
            if ( URIScheme.matchesIgnoreCase(iriStr, scheme) )
                schemeReport(this, Issue.iri_scheme_name_is_not_lowercase, scheme, "Scheme name should be lowercase");
            else
                schemeReport(this, Issue.iri_scheme_unexpected, scheme, "Scheme name should be '" + correctSchemeName + "'");
        }
    }

    private boolean containsUppercase(String string, int start, int finish) {
        for ( int i = start ; i < finish ; i++ ) {
            char ch = string.charAt(i);
            if ( Character.isUpperCase(ch) )
                return true;
        }
        return false;
    }

    private void checkHTTP() {
        checkSchemeName(URIScheme.HTTP);
        checkHTTPx(URIScheme.HTTP);
    }

    private void checkHTTPS() {
        checkSchemeName(URIScheme.HTTPS);
        checkHTTPx(URIScheme.HTTPS);
    }

    private void checkHTTPx(URIScheme scheme) {
        // @formatter:off

        /*
         * https://datatracker.ietf.org/doc/html/rfc7230#section-2.7.1
         * http-URI = "http:" "//" authority path-abempty [ "?" query ] [ "#" fragment ]
         */

        /* https://tools.ietf.org/html/rfc7230#section-2.7.1
         * A sender MUST NOT generate an "http" URI with an empty host identifier.
         * A recipient that processes such a URI reference MUST reject it as invalid.
         */

        // @formatter:on

        if ( !hasHost() )
            schemeReport(this, Issue.http_no_host, scheme, "http and https URI schemes require //host/");
        else if ( /* hasHost() && */ (host0 == host1) )
            schemeReport(this, Issue.http_empty_host, scheme, "http and https URI schemes do not allow the host to be empty");

        // https://tools.ietf.org/html/rfc3986#section-3.2.3
        if ( hasPort() ) {
            if ( port0 == port1 ) {
                schemeReport(this, Issue.http_empty_port, scheme, "Port is empty - omit the ':'");
            } else {
                int port = Integer.parseInt(port());
                switch (scheme) {
                    case HTTP :
                        if ( port == 80 )
                            schemeReport(this, Issue.http_omit_well_known_port, scheme, "Default port 80 should be omitted");
                        else if ( port < 1024 && port != 80 )
                            schemeReport(this, Issue.http_port_not_advised, scheme, "An HTTP port under 1024 should only be 80, not "+port);
                        break;
                    case HTTPS :
                        if ( port == 443 )
                            schemeReport(this, Issue.http_omit_well_known_port, scheme, "Default port 443 should be omitted");
                        else if ( port < 1024 && port != 443 )
                            schemeReport(this, Issue.http_port_not_advised, scheme, "An HTTPS ports under 1024 should only be 443, not "+port);
                        break;
                    default :
                        throw new IllegalStateException();
                }
            }
        }

        // We generate an IRI violation for all use of

//        /* https://tools.ietf.org/html/rfc7230#section-2.7.1
//         *
//         * http scheme: (not https)
//         * A sender MUST NOT generate the userinfo subcomponent (and its "@"
//         * delimiter) when an "http" URI reference is generated within a message as a
//         * request target or header field value. Before making use of an "http" URI
//         * reference received from an untrusted source, a recipient SHOULD parse for
//         * userinfo and treat its presence as an error; it is likely being used to
//         * obscure the authority for the sake of phishing attacks.
//         *
//         * ---- And in linked data, any URI is a request target.
//         * Also treat is as a violation for https for linked data URIs.
//         */
//
//        if ( hasUserInfo() ) {
//            schemeReport(this, Issue.http_userinfo, scheme, "userinfo (e.g. user:password) in authority section");
//            if ( userInfo().contains(":") )
//                schemeReport(this, Issue.http_password, scheme, "userinfo contains password in authority section");
//        }
    }

    // URN specific.
    // "urn", ASCII, min 2 char NID min two char NSS (urn:NID:NSS)
    // Query string starts ?+ or ?=

    /**
     * <a href="https://datatracker.ietf.org/doc/html/rfc8089">RFC 8089</a>.
     *
     * Check "file:"
     */
    private void checkFILE() {
        checkSchemeName(URIScheme.FILE);

        // Must have authority and it must be empty. i.e. file:///
        if ( !hasAuthority() ) {
            // No authority means it does not start "//"

            if ( path().startsWith("/") )
                schemeReport(this, Issue.file_bad_form, URIScheme.FILE, "file: URLs are of the form file:///path/...");
            else
                schemeReport(this, Issue.file_relative_path, URIScheme.FILE,
                             "file: URLs are of the form file:///path/..., not file:filename");
        } else {
            // hasAuthority
            // We do not support file:// because file://path1/path2/ makes the host
            // "path1" (which is then ignored!)
            if ( authority0 != authority1 ) {
                // file://path1/path2/..., so path becomes the "authority"
                schemeReport(this, Issue.file_bad_form, URIScheme.FILE, "file: URLs are of the form file:///path/..., not file://path");
            } else {
                if ( path0 == path1 ) {
                    // Zerolength path;.IRI3986 It's "file://"
                    schemeReport(this, Issue.file_bad_form, URIScheme.FILE, "file: URLs are of the form file:///path/..., not file://path");
                }
            }
        }
    }

    // RFC 8141
    // @formatter:off
    /*
     * namestring    = assigned-name
     *                  [ rq-components ]
     *                  [ "#" f-component ]
     * assigned-name = "urn" ":" NID ":" NSS
     * NID           = (alphanum) 0*30(ldh) (alphanum)
     * ldh           = alphanum / "-"
     * NSS           = pchar *(pchar / "/")
     * rq-components = [ "?+" r-component ]
     *                 [ "?=" q-component ]
     * r-component   = pchar *( pchar / "/" / "?" )
     * q-component   = pchar *( pchar / "/" / "?" )
     * f-component   = fragment
     */
    /*
     * alphanum, fragment, and pchar from RFC 3986
     *
       pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
       alphanum      = ALPHA / DIGIT
       fragment      = *( pchar / "/" / "?" )

       pct-encoded   = "%" HEXDIG HEXDIG
       unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
       reserved      = gen-delims / sub-delims
       gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
       sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
                           / "*" / "+" / "," / ";" / "="
    */
    // @formatter:on

    // Strictly - requires 2 char NID and one char NSS.
    // NID must be ASCII
    // We allow NSS and components to include Unicode
    // Patterns called *PREFIX start with ^ and should be used with Matcher.find.

    /**
     * <a href="https://datatracker.ietf.org/doc/html/rfc8141">RFC 8141</a>.
     *
     * Check "urn:". Additional checks for "urn:uuid:" available in
     * {@link #checkURN_UUID(String)}.
     */
    private void checkURN() {
        checkSchemeName(URIScheme.URN);
        BiConsumer<Issue, String> handler = (issue, msg) -> schemeReport(this, issue, URIScheme.URN, msg);

        // Includes RFC 8141 section 5.1 (X-)
        // Includes RFC 8141 section 5.2 (urn-)
        int finishURN = ParseURN.validateAssignedName(iriStr, handler);
        if ( finishURN == -1 )
            return;
        checkURNComponents(URIScheme.URN, handler);

        if ( hasQuery() )
            urnCharCheck("URN components", iriStr, this.query0, iriStr.length());
        else if ( hasFragment() )
            urnCharCheck("URN components", iriStr, this.fragment0, iriStr.length());
    }

    // Whether to allow Unicode in portions of URNs
    private void urnCharCheck(String urnPart, String string, int start, int finish) {
//        if ( ! isASCII(string, start, finish) )
//            schemeReport(this, Issue.urn_non_ascii_character, URIScheme.URN, "Non-ASCII character in URN "+urnPart);
    }

    // URN r-component(?=), q-component(?+) and f-component(#)
    private void checkURNComponents(URIScheme scheme, BiConsumer<Issue, String> handler) {
        if ( ! hasQuery() && ! hasFragment() )
            return;
        if ( ! hasQuery() ) {
            // Fragment, not query string.
            return;
        }
        // Query string, maybe fragment.
        // Include the "?" at the start
        int idx = this.query0-1;
        ParseURNComponents.validateURNComponents(iriStr, idx, handler);
    }

    /*
     * Both "urn:uuid:" and the unofficial "uuid:"
     * URN r-component(?=), q-component(?+) and f-component(#) not allowed.
     *
     * For "uuid:", don't allow URN components.
     */
    // Unregistered
    private static Pattern UUID_PATTERN_LC = Pattern.compile("^uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    //private static Pattern UUID_PATTERN_UC_PREFIX = Pattern.compile("^uuid:[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$");

    // Correct URN.
    private static Pattern URN_UUID_PATTERN_LC = Pattern.compile("^urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    //private static Pattern URN_UUID_PATTERN_UC = Pattern.compile("^urn:uuid:[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$");

    // General shape of a UUID: either scheme, any case. No length check.
    private static Pattern UUID_PATTERN_AnyCase_PREFIX =
            Pattern.compile("^(?:urn:uuid|uuid):[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);

    private final int UUID_length = 36;
    // "uuid" is the scheme,the URI path is the 36 character of the UUID.
    private final int UUID_scheme_path_length = UUID_length;

    // "urn" is the scheme, the URI path is "uuid:" and the 36 character of the UUID.
    private final int URN_UUID_scheme_path_length = UUID_length+"uuid:".length();

    /**
     * <a href="https://datatracker.ietf.org/doc/html/rfc4122">RFC4122</a>
     * <p>
     * {@code <urn:uuid:...>} This is the correct way to have UUIDs as URIs.
     */
    private void checkURN_UUID() {
        checkSchemeName(URIScheme.URN_UUID);
        boolean matches = URN_UUID_PATTERN_LC.matcher(iriStr).matches();
        if ( matches )
            // Fast path - no string manipulation, lower case, no components.
            return;
        checkUUID(URIScheme.URN_UUID, iriStr, URN_UUID_scheme_path_length);
        BiConsumer<Issue, String> handler = (issue, msg) -> schemeReport(this, issue, URIScheme.URN, msg);
        checkURNComponents(URIScheme.URN_UUID, handler);
    }

    /**
     * {@code <uuid:...>} was never registered. The correct form is {@code <urn:uuid:...>}.
     * <p>
     * We allow the non-registered form, disallowing URN components.
     */
    private void checkUUID() {
        checkSchemeName(URIScheme.UUID);
        schemeReport(this, Issue.uuid_scheme_not_registered, URIScheme.UUID, "Use urn:uuid: -  'uuid:' is not a registered URI scheme.");
        boolean matches = UUID_PATTERN_LC.matcher(iriStr).matches();
        if ( matches )
            // Fast path - no string manipulation, lower case
            return;
        checkUUID(URIScheme.UUID, iriStr, UUID_scheme_path_length);
        // No query string, no URN components.
        if ( hasQuery() )
            schemeReport(this, Issue.uuid_has_query, URIScheme.UUID, "query component not allowed");
        if ( hasFragment() )
            schemeReport(this, Issue.uuid_has_fragment, URIScheme.UUID, "fragment not allowed");
    }

    // Checks for both urn:uuid: and uuid:
    private void checkUUID(URIScheme scheme, String iriStr, int uriPathLen) {
        // uuidPathLen : whole URI path : : 36 if uuid: ("uuid:" is the scheme), 41 is urn:uuid: (path is uuid:....)
        // It did not pass the fast-path regular expression.

        int actualPathLen = path1-path0;
        if (actualPathLen != uriPathLen ) {
            schemeReport(this, Issue.uuid_bad_pattern, scheme, "Bad UUID string (wrong length)");
            return;
        }

        if ( scheme == URIScheme.URN_UUID ) {
            if ( containsHexUC(iriStr, path0, path0+"uuid".length()) )
                schemeReport(this, Issue.uuid_not_lowercase, scheme, "Lowercase recommended for urn UUID namspace");
        }

        boolean matchesAnyCase = UUID_PATTERN_AnyCase_PREFIX.matcher(iriStr).find();
        if ( ! matchesAnyCase ) {
            // Didn't match as a UUID
            schemeReport(this, Issue.uuid_bad_pattern , scheme, "Not a valid UUID string");
            return;
        }
        // We know it is the right length, right shape so:
        int uuidStart = path1 - UUID_length;
        int uuidFinish = path1;
        if ( containsHexUC(iriStr,uuidStart, uuidFinish) )
            schemeReport(this, Issue.uuid_not_lowercase, scheme, "Lowercase recommended for UUID string");
    }

    private boolean containsHexUC(String iriStr2, int uuidStart, int uuidFinish) {
        for ( int i = uuidStart ; i < uuidFinish ; i++ ) {
            char ch = charAt(i);
            if ( Chars3986.range(ch, 'A', 'F') ) {
                return true;
            }
        }
        return false;
    }

    private void checkDID() {
        checkSchemeName(URIScheme.DID);
        try {
            ParseDID.parse(iriStr, true);
        } catch (RuntimeException ex) {
            schemeReport(this, Issue.did_bad_syntax, URIScheme.DID, "Invalid DID: " + ex.getMessage());
        }
    }

    private void checkURN_OID() {
        checkSchemeName(URIScheme.URN_OID);
        checkOID(URIScheme.URN_OID, iriStr);
    }

    // Incorrect by RFC (there was a a draft, but RFC 3061 is urn:oid:...)
    private void checkOID() {
        checkSchemeName(URIScheme.OID);
        schemeReport(this, Issue.oid_scheme_not_registered, URIScheme.OID, "Use 'urn:oid:' - 'oid:' is not a registered URI scheme.");
        checkOID(URIScheme.OID, iriStr);
    }

    // Check for both cases.
    private void checkOID(URIScheme scheme, String iriStr) {
        try {
            ParseOID.parse(iriStr);
        } catch (RuntimeException ex) {
            schemeReport(this, Issue.oid_bad_syntax, scheme, "Invalid OID: " + ex.getMessage());
        }
    }

    /**
     * URI scheme "example:" from RFC 7595
     */
    private void checkExample() {
        checkSchemeName(URIScheme.EXAMPLE);
    }

    /**
     * Violation of URI scheme specific rules.
     * <p>
     * The URI will be added to the beginning of the message.
     */

    private void schemeReport(IRI3986 iri, Issue issue, URIScheme scheme, String msg) {
        Objects.requireNonNull(issue);
        if ( issue == Issue.ParseError ) {
            // Should not happen.
            throw parseError(iri.str(), msg);
        }
        addReport(iri, iri.str(), scheme, issue, msg);
    }

    private static void addReport(IRI3986 iri, String iriStr, URIScheme uriScheme, Issue issue, String message) {
        String msg = "<"+iriStr+"> "+message;
        Violation v = new Violation(iriStr, uriScheme, issue, msg);
        addReport(iri, v);
    }

    private static void addReportParseError(IRI3986 iri, String iriStr, String message) {
        // The iri object is probably only partial populated.
        // Exception message already has the IRI string. But check.
        String msg = message;
        if ( ! message.startsWith("<"+iriStr+">") )
            msg = "'"+iriStr+"' : "+message;
        Violation v = new Violation(iriStr, null, Issue.ParseError, msg);
        addReport(iri, v);
    }

    private static void addReport(IRI3986 iri, Violation report) {
        if ( iri.reports == null )
            iri.reports = new ArrayList<Violation>(4);
        iri.reports.add(report);
    }
}

// @formatter:off

// RFC3986 Regex: ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?

/* RFC 3986
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

   query         = *( pchar / "/" / "?" )

   fragment      = *( pchar / "/" / "?" )

   pct-encoded   = "%" HEXDIG HEXDIG

   unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
   reserved      = gen-delims / sub-delims
   gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
   sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
                 / "*" / "+" / "," / ";" / "="
 */

/*
 * RFC 3897 : IRIs
IRI            = scheme ":" ihier-part [ "?" iquery ]
                         [ "#" ifragment ]

   ihier-part     = "//" iauthority ipath-abempty
                  / ipath-absolute
                  / ipath-rootless
                  / ipath-empty

   IRI-reference  = IRI / irelative-ref

   absolute-IRI   = scheme ":" ihier-part [ "?" iquery ]

   irelative-ref  = irelative-part [ "?" iquery ] [ "#" ifragment ]

   irelative-part = "//" iauthority ipath-abempty
                    / ipath-absolute
                    / ipath-noscheme
                    / ipath-empty

   iauthority     = [ iuserinfo "@" ] ihost [ ":" port ]
   iuserinfo      = *( iunreserved / pct-encoded / sub-delims / ":" )
   ihost          = IP-literal / IPv4address / ireg-name

   ireg-name      = *( iunreserved / pct-encoded / sub-delims )

   ipath          = ipath-abempty   ; begins with "/" or is empty
                  / ipath-absolute  ; begins with "/" but not "//"
                  / ipath-noscheme  ; begins with a non-colon segment
                  / ipath-rootless  ; begins with a segment
                  / ipath-empty     ; zero characters

   ipath-abempty  = *( "/" isegment )
   ipath-absolute = "/" [ isegment-nz *( "/" isegment ) ]
   ipath-noscheme = isegment-nz-nc *( "/" isegment )
   ipath-rootless = isegment-nz *( "/" isegment )
   ipath-empty    = 0<ipchar>

   isegment       = *ipchar
   isegment-nz    = 1*ipchar
   isegment-nz-nc = 1*( iunreserved / pct-encoded / sub-delims
                        / "@" )
                  ; non-zero-length segment without any colon ":"

   ipchar         = iunreserved / pct-encoded / sub-delims / ":"
                  / "@"

   iquery         = *( ipchar / iprivate / "/" / "?" )

   ifragment      = *( ipchar / "/" / "?" )

   iunreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar

   ucschar        = %xA0-D7FF / %xF900-FDCF / %xFDF0-FFEF
                  / %x10000-1FFFD / %x20000-2FFFD / %x30000-3FFFD
                  / %x40000-4FFFD / %x50000-5FFFD / %x60000-6FFFD
                  / %x70000-7FFFD / %x80000-8FFFD / %x90000-9FFFD
                  / %xA0000-AFFFD / %xB0000-BFFFD / %xC0000-CFFFD
                  / %xD0000-DFFFD / %xE1000-EFFFD

   iprivate       = %xE000-F8FF / %xF0000-FFFFD / %x100000-10FFFD
   */



/* ABNF core rules: (ABNF is RFC 5234)
 *
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
//@formatter:on
