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

/**
 * Methods and constants that define features of the current the environment.
 * Primarily for other parts of the Jena framework.
 */

public class JenaRuntime
{
    static final String lineSeparator = getSystemProperty("line.separator", "\n") ;
    public static String getLineSeparator() {
        return lineSeparator;
    }

    public static String getSystemProperty(String propName) {
        return getSystemProperty(propName, null);
    }

    public static String getSystemProperty(final String propName, String defaultValue) {
        return System.getProperty(propName, defaultValue);
    }
}
