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

package org.apache.jena.shacl.compact;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shacl.Shapes;
import org.junit.Test;

public class TestShaclCompact {

    // Tests from the WG github area.
    @Test public void array_in()            { testv("array-in"); }
    @Test public void basic_shape_iri()     { testv("basic-shape-iri"); }
    @Test public void basic_shape()         { testv("basic-shape"); }
    @Test public void basic_shape_with_target()     { testv("basic-shape-with-target"); }
    @Test public void basic_shape_with_targets()    { testv("basic-shape-with-targets"); }
    @Test public void class_()              { testv("class"); }
    @Test public void comment()             { testv("comment"); }
    @Test public void complex1()            { testv("complex1"); }
    @Test public void complex2()            { testv("complex2"); }
    @Test public void count_0_1()           { testv("count-0-1"); }
    @Test public void count_0_unlimited()   { testv("count-0-unlimited"); }
    @Test public void count_1_2()           { testv("count-1-2"); }
    @Test public void count_1_unlimited()   { testv("count-1-unlimited"); }
    @Test public void datatype()            { testv("datatype"); }
    @Test public void directives()          { testv("directives"); }
    @Test public void empty()               { testv("empty"); }
    @Test public void nestedShape()         { testv("nestedShape"); }
    @Test public void nodeKind()            { testv("nodeKind"); }
    @Test public void node_or_2()           { testv("node-or-2"); }
    @Test public void node_or_3_not()       { testv("node-or-3-not"); }
    @Test public void path_alternative()    { testv("path-alternative"); }
    @Test public void path_complex()        { testv("path-complex"); }
    @Test public void path_inverse()        { testv("path-inverse"); }
    @Test public void path_oneOrMore()      { testv("path-oneOrMore"); }
    @Test public void path_sequence()       { testv("path-sequence"); }
    @Test public void path_zeroOrMore()     { testv("path-zeroOrMore"); }
    @Test public void path_zeroOrOne()      { testv("path-zeroOrOne"); }
    @Test public void property_empty()      { testv("property-empty"); }
    @Test public void property_not()        { testv("property-not"); }
    @Test public void property_or_2()       { testv("property-or-2"); }
    @Test public void property_or_3()       { testv("property-or-3"); }
    @Test public void shapeRef()            { testv("shapeRef"); }

    private final String DIR = "testing/shaclc-valid/";
    private final String BASE ="urn:x-base:default";

    private void testv(String string) {
        String fn = DIR+string+".shaclc";
        String ttl = DIR+string+".ttl";

        //System.err.println("---- "+fn);

        Shapes shapes = ShaclcParser.parse(fn, BASE);
        Graph expected = RDFDataMgr.loadGraph(ttl);

        Graph graphGot = shapes.getGraph();
        Graph graphOther = expected;

        boolean isomorphic = graphGot.isIsomorphicWith(graphOther);
        if ( ! isomorphic ) {
            System.err.println("---- "+fn);
            System.err.println("Different");
            System.err.println("graph(jena) = "+graphGot.size());
            System.err.println("graph(ref)  = "+graphOther.size());
            if ( false ) {
                RDFWriter.create().source(graphGot).format(RDFFormat.TURTLE_PRETTY).output(System.err);
                RDFWriter.create().source(graphOther).format(RDFFormat.TURTLE_PRETTY).output(System.err);
            }
        }
        assertTrue("test: "+string, isomorphic);
    }
}

