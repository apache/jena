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

package org.apache.jena.riot.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jena.riot.system.jsonld.JenaToTitanium;
import org.apache.jena.riot.system.jsonld.TitaniumJsonLdOptions;
import org.apache.jena.riot.system.jsonld.TitaniumToJena;

import org.junit.jupiter.api.Test;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.JsonDocument;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.IsoMatcher;

public class TestJenaTitaniumConvert {

    private JsonObject findInArray(JsonArray array, String key, String value) {
        return array.stream()
                .filter(jsonValue -> jsonValue.getValueType() == JsonValue.ValueType.OBJECT
                                     && jsonValue.asJsonObject().containsKey(key)
                                     && jsonValue.asJsonObject().getString(key).equals(value))
                .map(JsonValue::asJsonObject).findFirst().orElse(null);
    }

    @Test
    public void test_convert_titanium() throws JsonLdError {
        JsonLdOptions options = TitaniumJsonLdOptions.get(null, Context.emptyContext());
        ParserProfile profile = RiotLib.createParserProfile(RiotLib.factoryRDF(), ErrorHandlerFactory.errorHandlerStd, false);

        String dsStr = StrUtils.strjoinNL
                ("(dataset"
                 , "  (_ :s :p :o)"
                 , "  (_ :s :p 123)"
                 , "  (_ :s :p 123.5)"
                 , "  (_ :s :p 1e10)"
                 , "  (_ :s :p '2021-08-10'^^xsd:date)"
                 , "  (_ :s :p 'foo')"
                 , "  (:g1 :s :p :o)"
                 , "  (:g1 _:x :p :o)"
                 , "  (:g2 _:x :p 123)"
                 , "  (:g2 _:x :p 'abc'@en)"
                 , "  (_:x _:x :p _:x)"
                 ,")"
                );
        DatasetGraph dsg1 = SSE.parseDatasetGraph(dsStr);

        JsonArray jsonld = JenaToTitanium.convert(dsg1, options);

        // Should contain s, g1, g2, x
        assertEquals(4, jsonld.size());

        // Checking s
        JsonObject s = findInArray(jsonld, "@id", "http://example/s");
        // Contains @id and p
        assertEquals(2, s.size());
        assertEquals("http://example/s", s.getString("@id"));
        assertTrue(s.containsKey("http://example/p"));

        // s -> http://example/p
        JsonArray sp = s.getJsonArray("http://example/p");
        assertNotNull(sp);

        JsonObject dateValue = findInArray(sp, "@value", "2021-08-10");
        assertEquals(2, dateValue.size());
        assertEquals(XSDDatatype.XSDdate.getURI(), dateValue.getString("@type"));

        // Checking g1
        JsonObject g1 = findInArray(jsonld, "@id", "http://example/g1");
        assertTrue(g1.containsKey("@graph"));

        // Checking g2
        JsonObject g2 = findInArray(jsonld, "@id", "http://example/g2");
        assertTrue(g2.containsKey("@graph"));

        // g2 -> @graph -> http://example/p
        JsonArray g2p = g2.getJsonArray("@graph").getJsonObject(0).getJsonArray("http://example/p");
        assertEquals(2, g2p.size());

        JsonObject stringLangValue = findInArray(g2p, "@value", "abc");
        assertEquals("en", stringLangValue.getString("@language"));

        // Converting back to Jena and checking that the result is isomorphic with the initial one
        JsonDocument document = JsonDocument.of(jsonld);
        DatasetGraph dsg2      = DatasetFactory.create().asDatasetGraph();
        StreamRDF output = StreamRDFLib.dataset(dsg2);
        TitaniumToJena.convert(document, options, output, profile);
        assertTrue(IsoMatcher.isomorphic(dsg1, dsg2), "Datasets should be isomorphic");
    }
}
