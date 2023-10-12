/**
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes;

import static org.apache.jena.ext.io.github.galbiston.rdf_tables.datatypes.DatatypeController.HTTP_PREFIX;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gerg
 */
public class PrefixController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final HashMap<String, String> PREFIXES = new HashMap<>();
    public static final String PREFIX_DELIMITER = ":";

    public static HashMap<String, String> getPrefixes() {
        if (PREFIXES.isEmpty()) {
            loadPrefixes();
        }
        return PREFIXES;
    }

    private static void loadPrefixes() {
        PREFIXES.put("olo", "http://purl.org/ontology/olo/core#");
        PREFIXES.put("owl", "http://www.w3.org/2002/07/owl#");
        PREFIXES.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        PREFIXES.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        PREFIXES.put("time", "http://www.w3.org/2006/time#");
        PREFIXES.put("xsd", "http://www.w3.org/2001/XMLSchema#");
    }

    public static void addPrefixes(Map<String, String> prefixes) {
        PREFIXES.putAll(prefixes);
    }

    public static void addPrefix(String prefix, String uri) {
        PREFIXES.put(prefix, uri);
    }

    public static String lookupURI(String classLabel, String baseURI) {
        getPrefixes();
        //Check property URI for HTTP prefix.
        if (checkURI(classLabel)) {
            return classLabel;
        } else if (PREFIXES.containsKey(classLabel)) {
            return PREFIXES.get(classLabel);
        } else {

            String[] parts = classLabel.split(PREFIX_DELIMITER);

            if (parts.length > 1) {
                if (PREFIXES.containsKey(parts[0])) {
                    return PREFIXES.get(parts[0]) + parts[1];
                } else {
                    LOGGER.error("Prefix unknown for {}", classLabel);
                    throw new AssertionError();
                }
            }
            return baseURI + classLabel;
        }
    }

    public static boolean checkURI(String candidateURI) {
        return candidateURI.startsWith(HTTP_PREFIX);
    }

}
