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
import org.apache.jena.rfc3986.URIScheme;
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
            throw new FusekiConfigException(ex.getMessage());
        }
    }

    private static String fileSchemePrefix = URIScheme.FILE.getPrefix();

    /** Look for a Shiro ini file, returning the first found, or return null */
    static String huntForShiroIni(List<String> locations) {
        for ( String loc : locations ) {
            // If file:, look for that file.
            if ( loc.startsWith(fileSchemePrefix) ) {
                Path p = Path.of(loc.substring(fileSchemePrefix.length()));
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
}
