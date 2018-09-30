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

package org.apache.jena.sparql.util;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.ARQInternalErrorException ;

/**
 * Helper for loading class instances
 * <p>
 * This is primarily used as a helper by {@link MappedLoader} to dynamically
 * load in functions without a need to pre-register them. Since these class
 * names originate from URIs which may contain characters which are not valid in
 * Java class names this class implements a simple escaping scheme.
 * </p>
 * <h3>Escaping Scheme</h3>
 * <p>
 * Escaping is applied only to the last portion of the class name, typically
 * {@link MappedLoader} takes care of mapping a function library namespace
 * prefix into a java package name and likely only the last portion (the
 * function name) will require escaping.
 * </p>
 * <p>
 * If the first character of the class name is invalid it is replaced with
 * {@code F_}. If any subsequent characters are invalid they are ignored and the
 * subsequent valid character (if any) is promoted to upper case giving a camel
 * case style valid class name.
 * </p>
 * <p>
 * For example if the last portion of the class name were {@code foo-bar-faz}
 * then we would end up with an escaped class name of {@code fooBarFaz}.
 * </p>
 */
public class ClsLoader {
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
                Log.warn(ClsLoader.class, "Class not found: " + className);
                return null;
            }
        }

        if (requiredClass != null && !requiredClass.isAssignableFrom(classObj)) {
            Log.warn(ClsLoader.class, "Class '" + className + "' found but not a " + Lib.classShortName(requiredClass));
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
            Log.warn(ClsLoader.class, "Exception during instantiation '" + className + "': " + ex.getMessage());
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
