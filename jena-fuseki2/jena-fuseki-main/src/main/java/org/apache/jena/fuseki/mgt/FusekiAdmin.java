/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.mgt;

public class FusekiAdmin {
    /**
     * Control whether to allow creating new dataservices by uploading a config file.
     * See {@link ActionDatasets}.
     *
     */
    public static final String allowConfigFileProperty = "fuseki:allowAddByConfigFile";

    /**
     * Return whether to allow service configuration files to be uploaded as a file.
     * See {@link ActionDatasets}.
     */
    public static boolean allowConfigFiles() {
        String value = System.getProperty(allowConfigFileProperty);
        if ( value != null )
            return "true".equals(value);
        return false;
    }
}
