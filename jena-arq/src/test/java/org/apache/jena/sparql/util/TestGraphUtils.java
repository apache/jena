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

import static org.junit.Assert.*;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.junit.Test;

public class TestGraphUtils {

    private static String text = """
            PREFIX :     <http://example/>
            PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>

            :r1 :property1 'string' .
            :r2 :property2 'PT1S'^^xsd:duration .

            """;

    private static Model model = RDFParser.fromString(text, Lang.TTL).toModel();

    private static Resource r1 = model.createResource("http://example/r1");
    private static Property p1 = model.createProperty("http://example/property1");

    private static Resource r2 = model.createResource("http://example/r2");
    private static Property p2 = model.createProperty("http://example/property2");

    @Test
    public void string_1() {
        String x = GraphUtils.getStringValue(r1, p1);
        assertEquals("string", x);
    }

    @Test
    public void string_2() {
        // No datatype check.
        String x = GraphUtils.getStringValue(r2, p2);
        assertEquals("PT1S", x);
    }

    @Test
    public void string_datatype_1() {
        String x = GraphUtils.getStringValue(r2, p2, XSDDatatype.XSDduration);
        assertEquals("PT1S", x);
    }

    @Test(expected = JenaException.class)
    public void string_datatype_2() {
        String x = GraphUtils.getStringValue(r2, p2, XSDDatatype.XSDdouble);
        assertEquals("PT1S", x);
    }
}
