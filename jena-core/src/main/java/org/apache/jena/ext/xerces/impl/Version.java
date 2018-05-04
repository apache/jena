/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.impl;

/**
 * This class defines the version number of the parser.
 *
 * @version $Id: Version.java 446761 2006-09-15 21:59:29Z mrglavas $
 */
public class Version {

    /** Version string. */
    private static String fVersion = "2.11.0-jena";

    /* Print out the version information.
     * @return the version of the parser.
     */
    public static String getVersion() {
        return fVersion;
    }

}
