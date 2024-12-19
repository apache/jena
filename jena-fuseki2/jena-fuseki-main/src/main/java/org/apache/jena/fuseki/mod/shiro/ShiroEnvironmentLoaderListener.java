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

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.ResourceBasedWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;

/**
 * A Shiro {@link EnvironmentLoaderListener} that supports multiple possible
 * locations for a {@code shiro.ini} file. It will return the first found in a list
 * of possible file names.
 */
class ShiroEnvironmentLoaderListener extends EnvironmentLoaderListener{

    public static final Logger shiroConfigLog = FMod_Shiro.shiroConfigLog;

    private List<String> locations;

    /*package*/ ShiroEnvironmentLoaderListener(List<String> locations) {
        this.locations = locations;
    }

    /**
     * When given multiple locations for the shiro.ini file, and
     * if a {@link ResourceBasedWebEnvironment}, check the list of configuration
     * locations, testing whether the name identified an existing resource.
     * For the first resource name found to exist, reset the {@link ResourceBasedWebEnvironment}
     * to name that resource alone so the normal Shiro initialization executes.
     */
    @Override
    protected void customizeEnvironment(WebEnvironment environment) {
        if ( locations == null )
            return;

        // Look for shiro.ini
        if ( environment instanceof ResourceBasedWebEnvironment ) {
            ResourceBasedWebEnvironment env = (ResourceBasedWebEnvironment)environment;
            String[] configLocations = env.getConfigLocations();
            if ( configLocations != null && configLocations.length > 0 ) {
                // Set some other way.
                shiroConfigLog.info(format("Shiro file resource %s", Arrays.asList(configLocations)));
                return;
            }
            String loc = FusekiShiroLib.huntForShiroIni(locations);
            if  ( loc == null ) {
                shiroConfigLog.info(format("No Shiro file found (tried: %s)", locations));
                return;
            }
            shiroConfigLog.info("Shiro INI: "+loc);
            String[] configLocationsHere = new String[] {loc};
            env.setConfigLocations(configLocationsHere);
        }
    }
}