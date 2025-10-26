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

/**
 * More relativization algorithms on IRI3986's.
 */
public class AlgRelativizeIRI {

    /**
     * Calculate a "same scheme" relative URI, if possible.
     * <p>
     * The IRIs have the same scheme.<br/>
     * The relative URI is the "//" part onwards of the target.
     */
    public static IRI3986 relativeScheme(IRI base, IRI target) {
        if ( ! validBase(base) )
            return null;
        if ( ! Objects.equals(base.scheme(), target.scheme()) )
            return null;
        // No scheme.
        return IRI3986.build(null, target.authority(), target.path(), target.query(), target.fragment());
    }

    /**
     * Calculate a relative URI as a same-place (scheme, authority) resource, if possible.
     * <p>
     * The IRIs have the same scheme and authority.<br/>
     * The base does not have a query string.<br/>
     * The relative URI is the path, query and fragment of the target.<br/>
     */
    public static IRI3986 relativeResource(IRI base, IRI target) {
        if ( ! validBase(base) )
            return null;
        if ( ! Objects.equals(base.scheme(), target.scheme()) )
            return null;
        if ( ! Objects.equals(base.authority(), target.authority()) )
            return null;
        return IRI3986.build(null, null, target.path(), target.query(), target.fragment());
    }

    /**
     * Calculate a "same document" relative URI, if possible.
     * <p>
     * That is, the IRIs refer to the same document (same scheme, authority, path and
     * query string) and the relative URI is the fragment of the target if it has one.
     * <p>
     * The base must have a scheme and not have a query string.
     * <p>
     * Return null if the target does not have a fragment.
     */
    public static IRI3986 relativeSameDocument(IRI base, IRI target) {
        // Necessary conditions.
        // Same scheme, same authority, same path, same query
        if ( ! validBase(base) )
            return null;
        if ( ! Objects.equals(base.scheme(), target.scheme()) )
            return null;
        if ( ! Objects.equals(base.authority(), target.authority()) )
            return null;
        if ( Objects.equals(base.path(), target.path()) ) {
            if ( ! target.hasFragment() && ! target.hasQuery() )
                return IRI3986.build(null, null, "", null, null);
        }

        if ( ! Objects.equals(base.path(), target.path()) )
            return null;
        if ( ! Objects.equals(base.query(), target.query()) )
            return null;
        if ( ! target.hasFragment() )
            // Could be "#".
            return null;
        return IRI3986.build(null, null, null, null, target.fragment());
    }

    /**
     * Calculate a relative URI as a relative child path of the base.
     * <p>
     * The base must have scheme.<br/>
     * The IRIs have the same scheme and authority.<br/>
     * The base has a path which is a prefix of the target.<br/>
     * The relative URI is the remainder of the path, query and fragment of the target.<br/>
     * The relative path may be "" leading to "?query#frag" or "#frag"
     * The relative path does go up: it does not start with "..". See {@link #relativeParentPath(IRI, IRI)}.
     */
    public static IRI3986 relativePath(IRI base, IRI target) {
        // Includes return of "?query" and "#frag" (= same document)
        validBase(base);
        if ( ! Objects.equals(target.scheme(), base.scheme()) )
            return null;
        if ( ! Objects.equals(target.authority(), base.authority()) )
            return null;

        String basePath = base.path();
        String targetPath = target.path();

        if ( basePath.equals(targetPath) ) {
            if ( target.hasQuery() ) {
                String x = targetPath.isEmpty() ? "." : "";
                return IRI3986.build(null, null, x, target.query(), target.fragment());
            }

            // Both "" and "." are possible when the two paths end "/".
            String pathRel = targetPath.endsWith("/") ? "." : "";
            return IRI3986.build(null, null, pathRel, null, target.fragment());
        }

        String relPath = relativeChildPath(basePath, targetPath);
        if ( relPath == null )
            return null;
        if ( relPath.equals(".") && target.hasQuery() )
            relPath = "";
        return IRI3986.build(null, null, relPath, target.query(), target.fragment());
    }

    private static String lastSegment(String path) {
        // Directory segment.
        int idx = path.lastIndexOf('/');
        if ( idx < 0 )
            return path;
        // Exclude the final "/"
        String seg = path.substring(idx+1);
        return seg;
    }

    private static String relativeChildPath(String basePath, String targetPath) {
        // Need to be careful about /a/b/c and a/b/c/.
        String basePrefix = pathPrefix(basePath);
        boolean targetPathEndInSlash = targetPath.endsWith("/");
        if ( ! targetPath.startsWith(basePrefix) )
            return null;
        String relPath = targetPath.substring(basePrefix.length());
        if ( targetPathEndInSlash && relPath.isEmpty() )
            return ".";
        relPath = AlgResolveIRI.safeInitalSegment(relPath);
        return relPath;
    }

    public static IRI3986 relativeParentPath(IRI base, IRI target) {
        if ( ! Objects.equals(target.scheme(), base.scheme()) )
            return null;
        if ( ! Objects.equals(target.authority(), base.authority()) )
            return null;
        String relPath = relativeParentPath(base.path(), target.path());
        if ( relPath == null )
            return null;
        return IRI3986.build(null, null, relPath, target.query(), target.fragment());
    }

    /**
     * Calculate a relative URI as a parent then path of the base. That is, the result starts "..".
     * <p>
     * The base must have scheme.<br/>
     * The IRIs have the same scheme and authority.<br/>
     * The base has a path which is a prefix of the target.<br/>
     * The relative URI is the remainder of the path, query and fragment of the target.
     */
    private static String relativeParentPath(String basePath, String targetPath) {
        String basePrefix = pathPrefix(basePath);
        boolean targetPathEndsInSlash = targetPath.endsWith("/");

        // Try parent. If the basePrefix and absPath have the first (n-1) segments in common.
        String[] baseSegs = basePrefix.split("/");
        String[] targetSegs = targetPath.split("/");
        int n = Math.min(baseSegs.length,  targetSegs.length);
        int j = 0;
        for ( ; j < n ; j++ ) {
            if ( Objects.equals(baseSegs[j], targetSegs[j]) ) {
                //System.out.println("Same: ["+j+"] = "+baseSegs[j]);
            } else
                break;
        }

        // Special case 1. target is one or more segments longer than the base
        // This can be written "../xyz/..."

        // This is the child path case but done as up, then down again.
        if ( j == baseSegs.length && j <= targetSegs.length && j > 0 ) {
            String x = "../"+slice(targetSegs, j-1, "/");
            if ( targetPathEndsInSlash )
                x = x+"/";
            return x;
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

    private static String slice(String[] segs, int start, String sep) {
        StringJoiner sj = new StringJoiner(sep);
        for ( int i = start ; i < segs.length ; i++ ) {
            sj.add(segs[i]);
        }
        return sj.toString();
    }

    /**
     * Path, upto and including the final '/'.
     * Returns null of there is no '/'.
     */
    private static String pathPrefix(String basePath) {
        Objects.requireNonNull(basePath);
        // Directory segment.
        int idx = basePath.lastIndexOf('/');
        if ( idx < 0 )
            return null;
        if ( idx == 0 )
            return "/";
        // Include the final "/"
        String basePrefix = basePath.substring(0,idx+1);
        return basePrefix;
    }

    /** Validate for use as a base IRI. */
    /*package*/ static boolean validBase(IRI base) {
        return AlgIRI.validBase(base);
    }
}
