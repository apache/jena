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

package org.apache.jena.riot.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import com.apicatalog.jsonld.JsonLdError;

import org.junit.jupiter.api.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;

public class TestLangJsonLD_prefixes {
    // GH-3335
    // https://github.com/apache/jena/issues/3335
    @Test
    public void prefixes_array_object() {
        String jsonStr = """
                [ { "@context": "http://schema.org",
                    "@id": "https://data.example.org/dataset/entity",
                    "@type": "Thing",
                    "description": "A value that ends with a colon:"
                   } ]
                """;
        parseTestPrefixes(jsonStr);
    }

    @Test
    public void prefixes_bad_uri() {
        // Bad prefix in the context - Titanium catches it.
        String jsonStr = """
                  { "@context": { "ns2": "A string that ends with a colon:" } ,
                    "@id": "https://data.example.org/dataset/entity",
                    "ns2:abc": "ABC"
                 }
                """;
        RiotException ex = assertThrows(RiotException.class, ()->parseTestPrefixes(jsonStr));
        assertTrue(ex.getCause() instanceof JsonLdError);
    }

    @Test
    public void prefixes_array_context() {
        String jsonStr = """
                  { "@context": [ { "ns2": "urn:jena:" } , null ] ,
                    "ns2:abc": "NS"
                  }
                """;
        parseTestPrefixes(jsonStr, "ns2");
    }

    @Test
    public void prefixes_array_context_2() {
        String jsonStr = """
          {
            "@context": [ { "ns1": "urn:jena:ns1#" } , { "ns2": "urn:jena:ns2#" } ] ,
            "ns1:abc": "abc1",
            "ns2:abc": "abc2"
          }
          """;
        parseTestPrefixes(jsonStr, "ns1", "ns2");
    }

    private void parseTestPrefixes(String json, String... namespaces) {
        Model model = ModelFactory.createDefaultModel();
        StringReader sr = new StringReader(json);
        RDFDataMgr.read(model, sr, null, Lang.JSONLD11);
        assertEquals(namespaces.length, model.getNsPrefixMap().size());
        for (String ns : namespaces ) {
            assertNotNull(model.getNsPrefixURI(ns), ()->"Failed to find prefix '"+ns+"'");
        }
    }
}
