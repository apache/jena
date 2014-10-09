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

package com.hp.hpl.jena.sparql.util;

import org.apache.jena.atlas.logging.Log;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;

public class Loader {
    static public Class<?> loadClass(String classNameOrURI) {
        return loadClass(classNameOrURI, null);
    }

    static public Class<?> loadClass(String classNameOrURI, Class<?> requiredClass) {
        if (classNameOrURI == null)
            throw new ARQInternalErrorException("Null classNameorIRI");

        if (classNameOrURI.startsWith("http:"))
            return null;
        if (classNameOrURI.startsWith("urn:"))
            return null;

        String className = classNameOrURI;

        if (classNameOrURI.startsWith(ARQConstants.javaClassURIScheme))
            className = classNameOrURI.substring(ARQConstants.javaClassURIScheme.length());

        Class<?> classObj = null;

        try {
            classObj = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            // It is possible that when coming from a URI we might have
            // characters which aren't valid as Java identifiers
            // We should see if we can load the class with the escaped class
            // name instead
            String baseUri = className.substring(0, className.lastIndexOf('.') + 1);
            String escapedClassName = escape(className.substring(className.lastIndexOf('.') + 1));
            try {
                classObj = Class.forName(baseUri + escapedClassName);
            } catch (ClassNotFoundException innerEx) {
                // Ignore, handled in the outer catch
            }

            if (classObj == null) {
                Log.warn(Loader.class, "Class not found: " + className);
                return null;
            }
        }

        if (requiredClass != null && !requiredClass.isAssignableFrom(classObj)) {
            Log.warn(Loader.class, "Class '" + className + "' found but not a " + Utils.classShortName(requiredClass));
            return null;
        }
        return classObj;
    }

    static public Object loadAndInstantiate(String uri, Class<?> requiredClass) {
        Class<?> classObj = loadClass(uri, requiredClass);
        if (classObj == null)
            return null;

        Object module = null;
        try {
            module = classObj.newInstance();
        } catch (Exception ex) {
            String className = uri.substring(ARQConstants.javaClassURIScheme.length());
            Log.warn(Loader.class, "Exception during instantiation '" + className + "': " + ex.getMessage());
            return null;
        }
        return module;
    }

    static public String escape(String className) {
        StringBuilder builder = new StringBuilder();
        boolean upgrade = false;

        for (int offset = 0; offset < className.length();) {
            int cp = className.codePointAt(offset);
            if (builder.length() == 0) {
                if (Character.isJavaIdentifierStart(cp)) {
                    // Start character is valid
                    builder.append(Character.toChars(cp));
                } else {
                    // Illegal start character so use F_ as prefix
                    builder.append("F_");
                }
            } else {
                if (Character.isJavaIdentifierPart(cp)) {
                    // Upgrade to upper case if previous character was illegal
                    if (upgrade) {
                        cp = Character.toUpperCase(cp);
                        upgrade = false;
                    }
                    // Valid character
                    builder.append(Character.toChars(cp));
                } else {
                    // Skip illegal characters, the next valid character will be
                    // upgraded to upper case
                    upgrade = true;
                }
            }

            offset += Character.charCount(cp);
        }

        return builder.toString();
    }
}
