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

package org.apache.jena.riot.rowset.rw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import org.apache.commons.lang3.Strings;
import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetReader;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Addition tests for result readers.
 * The SPARQL test suite cover most usage. This class adds tests.
 */
public class TestRowSetReader {
    static { JenaSystem.init(); }

    // Check "abc"^^rdf:langString and "abc"^^rdf:dirLangString - i.e. incomplete forms.
    @Test public void resultSet_json_1() {
        String r = """
                {
                  "head": { "vars": [ "s" , "p" , "o" ] } ,
                  "results": {
                    "bindings": [
                      {
                        "s": { "type": "uri" , "value": "http://example/s" } ,
                        "p": { "type": "uri" , "value": "http://example/p" } ,
                        "o": { "type": "literal" , "datatype": "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString" , "value": "abc" }
                      } ,
                      {
                        "s": { "type": "uri" , "value": "http://example/s" } ,
                        "p": { "type": "uri" , "value": "http://example/p" } ,
                        "o": { "type": "literal" , "datatype": "http://www.w3.org/1999/02/22-rdf-syntax-ns#dirLangString" , "value": "abc" }
                      }
                    ]
                }}
                """;
        RowSet rowset = read(r, ResultSetLang.RS_JSON);
        assertNotNull(rowset);
        assertEquals(2, RowSetOps.count(rowset));
    }

    // Check error handling of bad JSON
    @Test public void resultSet_json_bad_vars() {
        // Trailing comma in "vars" array. This is only a warning.
        String r = """
                { "head": { "vars": [ "x", "y" , ] } ,
                  "results": {
                    "bindings": [
                      {
                        "x": { "type": "literal" , "value": "A" } ,
                        "y": { "type": "literal" , "value": "B" }
                      }
                    ]
                  }
                }
                """;
        Logger log = LoggerFactory.getLogger(RowSetReaderJSONStreaming.class);
        LogCtl.withLevel(log, "ERROR", ()->{
            RowSet rowset = read(r, ResultSetLang.RS_JSON);
            assertNotNull(rowset);
            assertEquals(1, RowSetOps.count(rowset));
        });
    }

    // Check error handling of bad JSON
    @Test public void resultSet_json_bad_bindings() {
        // Trailing comma in "bindings" array. This is an error. It might be file truncation.
        String r = """
                { "head": { "vars": [ "x", "y" ] } ,
                  "results": {
                    "bindings": [
                      {
                        "x": { "type": "literal" , "value": "A" } ,
                        "y": { "type": "literal" , "value": "B" } ,
                      }
                    ]
                  }
                }
                """;
        ResultSetException ex = assertThrows(ResultSetException.class, ()-> read(r, ResultSetLang.RS_JSON));
        String msg = ex.getMessage();
        assertTrue(Strings.CI.containsAny(msg, "JSON Syntax error:"));
    }

    @Test public void resultSet_xml_1() {
        String r = """
<?xml version="1.0"?>
<sparql xmlns="http://www.w3.org/2005/sparql-results#">
  <head>
    <variable name="s"/>
    <variable name="p"/>
    <variable name="o"/>
  </head>
  <results>
    <result>
      <binding name="s">
        <uri>http://example/s</uri>
      </binding>
      <binding name="p">
        <uri>http://example/p</uri>
      </binding>
      <binding name="o">
        <literal datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#langString">abc</literal>
      </binding>
    </result>
        <result>
      <binding name="s">
        <uri>http://example/s</uri>
      </binding>
      <binding name="p">
        <uri>http://example/p</uri>
      </binding>
      <binding name="o">
        <literal datatype="http://www.w3.org/1999/02/22-rdf-syntax-ns#dirLangString">abc</literal>
      </binding>
    </result>

  </results>
</sparql>
                """;
        RowSet rowset = read(r, ResultSetLang.RS_XML);
        assertNotNull(rowset);
        assertEquals(2, RowSetOps.count(rowset));
    }

    private static RowSet read(String x, Lang rsLang) {
        try ( InputStream input = new ByteArrayInputStream(Bytes.string2bytes(x)) ) {
            RowSet rowSet = RowSetReader.createReader(rsLang)
                    .read(input, ARQ.getContext().copy())
                    .materialize();
            return rowSet;
        } catch (IOException ex) { throw IOX.exception(ex); }
    }
}
