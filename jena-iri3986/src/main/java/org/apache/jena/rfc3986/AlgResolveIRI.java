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

import java.util.Objects;
import java.util.StringJoiner;

/** Algorithms for IRIs : resolution and relativize (reverse of resolution). */
public class AlgResolveIRI {

    /**
     * Resolve an IRI against a base.
     * <p>
     * This function includes resolving the path if the scheme of base and the scheme
     * of the reference is the same or both absent.
     */
    public static IRI3986 resolve(IRI base, IRI reference) {
        return transformReferencesNonStrict(reference, base);
    }

    /**
     * 5.2.2.  Transform References
     * <p>
     * <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.2">RFC 3986 section 5.2.2</a>.
     */
    private static IRI3986 transformReferencesStrict(IRI reference, IRI base) {
        if ( reference.hasScheme() )
            return RFC3986.create(reference);
        return transformReferencesNonStrict(reference, base);
    }

    /**
     * 5.2.2.  Transform References
     * <p>
     * <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.2">RFC 3986 section 5.2.2</a>.
     * <p>
     * "A non-strict parser" - this resolves (hosts and) paths if the reference and the base have the same scheme.
     */
    private static IRI3986 transformReferencesNonStrict(IRI reference, IRI base) {
        // Note the argument order is reverse from "resolve(base, relative)" to be more like RFC 3986.
        String t_scheme = null;
        String t_authority = "";
        String t_path = "";
        String t_query = null;
        String t_fragment = null;

//        -- The URI reference is parsed into the five URI components
//        --
//        (R.scheme, R.authority, R.path, R.query, R.fragment) = parse(R);
//
//        -- A non-strict parser may ignore a scheme in the reference
//        -- if it is identical to the base URI's scheme.
//        --
//        if ((not strict) and (R.scheme == Base.scheme)) then
//           undefine(R.scheme);
//        endif;
//
//        if defined(R.scheme) then
//           T.scheme    = R.scheme;
//           T.authority = R.authority;
//           T.path      = remove_dot_segments(R.path);
//           T.query     = R.query;
//        else
//           if defined(R.authority) then
//              T.authority = R.authority;
//              T.path      = remove_dot_segments(R.path);
//              T.query     = R.query;
//           else
//              if (R.path == "") then
//                 T.path = Base.path;
//                 if defined(R.query) then
//                    T.query = R.query;
//                 else
//                    T.query = Base.query;
//                 endif;
//              else
//                 if (R.path starts-with "/") then
//                    T.path = remove_dot_segments(R.path);
//                 else
//                    T.path = merge(Base.path, R.path);
//                    T.path = remove_dot_segments(T.path);
//                 endif;
//                 T.query = R.query;
//              endif;
//              T.authority = Base.authority;
//           endif;
//           T.scheme = Base.scheme;
//        endif;
//
//        T.fragment = R.fragment;

        // "not strict", including both missing schemes
        boolean sameScheme = Objects.equals(reference.scheme(), base.scheme());

        if ( reference.hasScheme() && ! sameScheme ) {
            t_scheme = reference.scheme();
            t_authority = reference.authority();
            t_path = remove_dot_segments(reference.path());
            t_query = reference.query();
        } else {
            if ( reference.hasAuthority() ) {
                t_authority = reference.authority();
                t_path = remove_dot_segments(reference.path());
                t_query = reference.query();
            } else {
                if ( reference.path().isEmpty() ) {
                    t_path = base.path();
                    if ( reference.hasQuery() )
                        t_query = reference.query();
                    else
                        t_query = base.query();
                } else {
                    if ( reference.path().startsWith("/") )
                        t_path = remove_dot_segments(reference.path());
                    else {
                        t_path = merge(base, reference.path());
                        t_path = remove_dot_segments(t_path);
                    }
                    t_query = reference.query();
                }
                t_authority = base.authority();
            }
            t_scheme = base.scheme();
        }
        t_fragment = reference.fragment();
        return RFC3986.newBuilder()
            .scheme(t_scheme).authority(t_authority).path(t_path).query(t_query).fragment(t_fragment)
            .build();
    }

    /** 5.2.3.  Merge Paths */
    private static String merge(IRI base, String ref) {
/*
     o  If the base URI has a defined authority component and an empty
        path, then return a string consisting of "/" concatenated with the
        reference's path; otherwise,

     o  return a string consisting of the reference's path component
        appended to all but the last segment of the base URI's path (i.e.,
        excluding any characters after the right-most "/" in the base URI
        path, or excluding the entire base URI path if it does not contain
        any "/" characters).
*/
        if ( base.hasAuthority() && base.path().isEmpty() ) {
            if ( ref.startsWith("/") )
                return ref;
            return "/"+ref;
        }
        String path = base.path();
        int j = path.lastIndexOf('/');
        if ( j < 0 )
            return ref;
        return path.substring(0, j)+"/"+ref;
    }

    /** 5.2.4.  Remove Dot Segments */
    static String remove_dot_segments(String path) {
        String s1 = remove_dot_segments$(path);
        if ( false ) {
            // Checking code.
            String s2 = LibParseIRI.jenaIRIremoveDotSegments(path);
            if ( ! Objects.equals(s1, s2) )
                System.err.printf("remove_dot_segments : IRI3986:%s  Jena IRI:%s\n", s1, s2);
        }
        return s1;
    }

    /*
     * Implement using a single segment stack, null out unwanted elements, then recombine into a string.
     */
    private static String remove_dot_segments$(String path) {
        if ( path == null || path.isEmpty() )
            return "";
        if ( path.equals("/") )
            return "/";
        // String.split -- "Trailing empty strings are not included in the results."
        String[] segments = path.split("/");
        int N = segments.length;
        if ( N == 0 ) {
            // If the path is two or more "/" and nothing else, then no segments.
            return path;
        }

        boolean initialSlash = segments[0].isEmpty();
        // path has a trailing slash, special case is "/"
        boolean trailingSlash = false;
        // Determine whether there is going to be a trailing slash.
        // *  if it isn't the initial "/" and it ends in "/"
        // *  there is "/." or "/.." which will be resolved below.
        if ( N > (initialSlash ? 1 : 0) ) {
            if ( segments[N-1].equals(".") || segments[N-1].equals("..") )
                // Not the initial slash, and the last segment is "." or ".."
                trailingSlash = true;
            else if ( path.charAt(path.length()-1) == '/' )
                // Not the initial slash, and the last character is "/"
                trailingSlash = true;
        }

        for ( int j = 0 ; j < N ; j++ ) {
            String s = segments[j];
            if ( s.equals(".") )
                // Remove.
                segments[j] = null;
            if ( s.equals("..") ) {
                // Remove the ".."
                segments[j] = null;
                // and remove first unset previous
                if ( j >= 1 ) {
                    for ( int j2 = j-1 ; j2 >= 0 ; j2-- ) {
                        if ( segments[j2] != null ) {
                            segments[j2] = null;
                            break;
                        }
                    }
                }
            }
        }

        // Build string again. Skip nulls.
        StringJoiner joiner = new StringJoiner("/");
        if ( initialSlash )
            joiner.add("");
        for ( int k = 0 ; k < segments.length ; k++ ) {
            if ( segments[k] == null )
                continue;
            // This turns "//" into "/".
            // The first segment is empty when parsing "/a/b/c" and we put in "/" already,
            // so do this for the top element and otherwise leave "//" in place.
            if ( k==0 && segments[k].isEmpty() )
                continue;
            joiner.add(segments[k]);
        }
        if ( trailingSlash )
            joiner.add("");
        String s = joiner.toString();
        return s;
    }

    /**
     * Calculate a relative IRI for a (base, target) pair.
     * Returns null if none generated.
     * The base IRI must not have query string.
     */
    public static IRI3986 relativize(IRI base, IRI iri) {
        Objects.requireNonNull(iri);
        if ( ! base.hasScheme() )
            return null;
        if ( base.hasQuery() )
            // Don't support relativize if the base has a query string.
            return null;
        if ( ! iri.hasScheme() || !iri.hasAuthority() )
            return null;
        if ( ! Objects.equals(iri.scheme(), base.scheme()) )
            return null;
        if ( ! Objects.equals(iri.authority(), base.authority()) )
            return null;
        // Authority covers host and port
//        if ( ! Objects.equals(base.getHost(), this.getHost()) )
//            return null;
//        if ( ! Objects.equals(base.getPort(), this.getPort()) )
//            return null;

        String basePath = base.path();
        String targetPath = iri.path();

        String relPath = relativePath(basePath, targetPath, true);

        if ( relPath == null ) {
            // At this point, we know it is same schema and authority, which just
            // leaves the query and fragment to adjust.
            relPath = targetPath;
        }
        IRI3986 relIRI = IRI3986.build(null, null, relPath, iri.query(), iri.fragment());
        return relIRI;
    }

    /**
     * Calculate a relative path so that resolve("base", relative path) = "path".
     * This is limited to the case where basePath is a prefix of path segments for
     * the path to be made relative.
     * <p>
     * If basePath == path, return "".
     * <p>
     * Optionally, look for relative paths going up one level "parent" paths starting "..".
     * This function does not consider two level "grandparent paths, starting "../..".
     * <p>
     * Return null for "no relative path".
     */
    private static String relativePath(String basePath, String targetPath, boolean withParentPath) {
        // Combine all relativization: avoid rework, choose in order.
        if ( basePath.equals(targetPath) )
            return "";
        if ( false ) {
            // Don't simply remove "/"
            if ( basePath.equals("/") )
                return null;
        }
        // Directory segment.
        int idx = basePath.lastIndexOf('/');

        if ( idx < 0 )
            return null;
        if ( false && idx == 0 ) {
            // The base is "/xyz", so basePrefix will be "/".
            // We might be able to do "a/b/c", i.e. remove first "/" but is it worth it?
            // Could prefer to use an absolute path for the relative reference.
            return null;
        }

        // Include the final "/"
        String basePrefix = basePath.substring(0,idx+1);

        boolean targetPathEndsInSlash = targetPath.endsWith("/");
        // Child.
        if ( targetPath.startsWith(basePrefix) ) {
            String relPath = targetPath.substring(basePrefix.length());
            if ( targetPathEndsInSlash && relPath.isEmpty() )
                return ".";
            relPath = safeInitalSegment(relPath);
            return relPath;
        }

        if ( ! withParentPath )
            return null;

        // Parent ../ : this is more expensive.
        // Generalize to multiple ..

        // Try parent. If the basePrefix and absPath have the first (n-1) segments in common.
        String[] baseSegs = basePrefix.split("/");
        //System.out.println("baseSegs = "+Arrays.asList(baseSegs));
        if ( baseSegs.length == 2)
            // Base is a single segment and ".." would be "/"
            return null;
        String[] targetSegs = targetPath.split("/");
        //System.out.println("targetSegs = "+Arrays.asList(targetSegs));

        int n = Math.min(baseSegs.length,  targetSegs.length);
        int j = 0;
        for ( ; j < n ; j++ ) {
            if ( Objects.equals(baseSegs[j], targetSegs[j]) ) {
                //System.out.println("Same: ["+j+"] = "+baseSegs[j]);
            } else
                break;
        }

        // Special case 1. target is one segment longer then base and does not end in slash.
        // This can be written "../xyz"
        if ( j == targetSegs.length && j == baseSegs.length && !targetPathEndsInSlash ) {
            return "../"+targetSegs[targetSegs.length-1];
        }

        if ( j+1 == baseSegs.length ) {
            // Special case 2.
            // Returning exactly ".."
            if ( j == targetSegs.length ) {
                if ( targetPathEndsInSlash )
                    return "..";
                return null;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("..");
            for ( int k = j ; k < targetSegs.length ; k++ ) {
                sb.append("/");
                sb.append(targetSegs[k]);
            }
            if ( targetPathEndsInSlash )
                sb.append("/");
            return sb.toString();
        }
        return null;
    }

    /** Ensure the relative path satisfies segment-no-nc in it's first segment. */
    public static String safeInitalSegment(String relPath) {
        // segment-no-nc handling.
        // Check: Does the initial segment have ':' in it? Test: A ':' before first '/'
        int rColon = relPath.indexOf(':');
        if ( rColon < 0 )
            // No ':' - OK.
            return relPath;

        int rPathSep = relPath.indexOf('/');
        if ( rPathSep < 0 || rColon < rPathSep )
            // Initial segment has ':' - add "./" so it is not initial
            relPath = "./"+relPath;
        return relPath;
    }
}
