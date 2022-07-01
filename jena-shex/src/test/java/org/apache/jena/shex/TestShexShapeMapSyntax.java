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

package org.apache.jena.shex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.shex.parser.ShexParseException;
import org.apache.jena.shex.parser.ShExC;
import org.junit.Test;

public class TestShexShapeMapSyntax {
    @Test public void shape_map_1() {
        // The example in the doc.
        String s = StrUtils.strjoinNL
                ("<http://data.example/#n1> @ <http://data.example/#S2> ,"
                ,"\"chat\"@en-fr@<http://data.example/S3> ,"
                ,"{FOCUS a <http://schema.example/Some/Type>}@START ,"
                ,"{_ <http://data.example/p3> FOCUS}@START"
                 );
        ShexMap map = parseShapeMap(s);
        assertNotNull(map);
        assertNotNull(map.entries());
        assertEquals(4, map.entries().size());
    }

    @Test public void shape_map_2() {
        // Extensions.
        // Preamble.
        // Optional ","
        String s = StrUtils.strjoinNL
                ("BASE  <http://data.example/>"
                ,"PREFIX : <http://data.example/#>"
                ,""
                ,"<#n1> @ :S2 . "
                ,"'xyz'^^:datatype @ START"
                ,"{FOCUS a <Some/Type>} @ :Shape"
                ,"{_ :p3 FOCUS } @START . "
                );
        ShexMap map = parseShapeMap(s);
        assertNotNull(map);
        assertNotNull(map.entries());
        assertEquals(4, map.entries().size());
    }

    // Bad.

    @Test(expected=ShexParseException.class)
    public void shape_map_3() {
        String s = StrUtils.strjoinNL
                ("BASE  <http://data.example/>"
                ,"PREFIX : <http://data.example/#>"
                ,""
                ,"{_ :p3 FOCUS } @START ,"  // bad.
                );
        parseShapeMap(s);
    }

    private ShexMap parseShapeMap(String s) {
        InputStream input = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        ShexMap shapeMap = ShExC.parseShapeMap(input, null);
        return shapeMap;
    }
}
