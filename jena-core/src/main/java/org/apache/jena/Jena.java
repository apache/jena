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

package org.apache.jena;

import org.apache.jena.atlas.lib.Version;

/**
 * <p>
 * Provides various meta-data constants about the Jena package.
 * </p>
 */
public interface Jena
{
	/** The root package name for Jena */
    public static final String PATH = "org.apache.jena";

    /** The root name for metadata */
    public static final String MPATH = "org.apache.jena";

    /** The product name */
    public static final String NAME = "Apache Jena";

    /** The Jena web site */
    public static final String WEBSITE = "https://jena.apache.org/";

    /** The full name of the current Jena version */
    public static final String VERSION = Version.versionForClass(Jena.class).orElse("<development>");
}
