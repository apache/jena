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

package org.apache.jena.fuseki.mod.shiro;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jakarta.servlet.ServletContext;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.shiro.lang.io.ResourceUtils;
import org.apache.shiro.web.env.EnvironmentLoaderListener;

/*package*/ class FusekiShiroLib {
    static void shiroEnvironment(ServletContext servletContext, List<String> possibleShiroIniFiles) {
        // Shiro environment initialization, done here because we don't have webapp listeners.
        EnvironmentLoaderListener shiroListener = new ShiroEnvironmentLoaderListener(possibleShiroIniFiles);
        try {
            shiroListener.initEnvironment(servletContext);
        } catch (org.apache.shiro.config.ConfigurationException ex) {
            ShiroEnvironmentLoaderListener.shiroConfigLog.error("Failed to initialize Shiro: "+ex.getMessage());
            throw new FusekiConfigException(ex.getMessage(), ex);
        }
    }

    private static final String fileShiroPrefix = "file:";
    private static final int fileShiroPrefixLength = fileShiroPrefix.length();

    /**
     * Look for a Shiro ini file, returning the first found, or return null.
     * The input is a IRI ("file:" encoded for spaces etc).
     * The returned resource will be "Shiro" style - starts "file:", "url:", "classpath:"
     * but no encoding of the path.
     * In Shiro, no prefix causes it to use {@link ServlectContext#getResourceAsStream}.
     * <p>
     * See {@link ResourceUtils#hasResourcePrefix} and {@link ResourceUtils#getInputStreamForPath}.
     */
    static String huntForShiroIni(List<String> locations) {
        for ( String loc : locations ) {
            // If file:, look for that file.
            if ( loc.startsWith(fileShiroPrefix) ) {
                // Shiro format resource name.
                String fn = loc.substring(fileShiroPrefixLength);
                Path p = Path.of(fn);
                if ( Files.exists(p) )
                    return loc;
                // Ignore.
                continue;
            }
            // No scheme. May be a classpath resource.
            if ( ResourceUtils.resourceExists(loc) )
                return loc;
        }
        return null;
    }

    public static String withResourcePrefix(String shiroFileName) {
        if ( shiroFileName.startsWith(fileShiroPrefix) )
            return shiroFileName;
        // How Shiro likes it. file:, unencoded filename.
        return fileShiroPrefix+shiroFileName;
    }

    public static String removeResourcePrefix(String shiroFileResource) {
        if ( shiroFileResource.startsWith(fileShiroPrefix) ) {
            // Shiro format resource name.
            // Convert (back) to a filesystem path.
            String fn = shiroFileResource.substring(fileShiroPrefixLength);
            return fn;
        }
        return shiroFileResource;
    }

}
