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

public class TestReadShaclCompact {

    // Tests from the WG github area.
    @Test public void array_in()            { testReader("array-in"); }
    @Test public void basic_shape_iri()     { testReader("basic-shape-iri"); }
    @Test public void basic_shape()         { testReader("basic-shape"); }
    @Test public void basic_shape_with_target()     { testReader("basic-shape-with-target"); }
    @Test public void basic_shape_with_targets()    { testReader("basic-shape-with-targets"); }
    @Test public void class_()              { testReader("class"); }
    @Test public void comment()             { testReader("comment"); }
    @Test public void complex1()            { testReader("complex1"); }
    @Test public void complex2()            { testReader("complex2"); }
    @Test public void count_0_1()           { testReader("count-0-1"); }
    @Test public void count_0_unlimited()   { testReader("count-0-unlimited"); }
    @Test public void count_1_2()           { testReader("count-1-2"); }
    @Test public void count_1_unlimited()   { testReader("count-1-unlimited"); }
    @Test public void datatype()            { testReader("datatype"); }
    @Test public void directives()          { testReader("directives"); }
    @Test public void empty()               { testReader("empty"); }
    @Test public void nestedShape()         { testReader("nestedShape"); }
    @Test public void nodeKind()            { testReader("nodeKind"); }
    @Test public void node_or_2()           { testReader("node-or-2"); }
    @Test public void node_or_3_not()       { testReader("node-or-3-not"); }
    @Test public void path_alternative()    { testReader("path-alternative"); }
    @Test public void path_complex()        { testReader("path-complex"); }
    @Test public void path_inverse()        { testReader("path-inverse"); }
    @Test public void path_oneOrMore()      { testReader("path-oneOrMore"); }
    @Test public void path_sequence()       { testReader("path-sequence"); }
    @Test public void path_zeroOrMore()     { testReader("path-zeroOrMore"); }
    @Test public void path_zeroOrOne()      { testReader("path-zeroOrOne"); }
    @Test public void property_empty()      { testReader("property-empty"); }
    @Test public void property_not()        { testReader("property-not"); }
    @Test public void property_or_2()       { testReader("property-or-2"); }
    @Test public void property_or_3()       { testReader("property-or-3"); }
    @Test public void shapeRef()            { testReader("shapeRef"); }

    private final String DIR = "src/test/resources/shaclc-valid/";
    private final String BASE ="urn:x-base:default";

    private void testReader(String string) {
        String fn = DIR+string+".shaclc";
        String ttl = DIR+string+".ttl";

        Shapes shapes = ShaclcParser.parse(fn, BASE);
        //System.out.printf("R = %d : S = %d\n", shapes.numRootShapes(), shapes.numShapes());
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

