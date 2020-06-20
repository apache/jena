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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.compact.writer.CompactWriter;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestWriteShaclCompact {

    // Tests from the WG github area.
    @Test public void array_in()            { testWriter("array-in"); }
    @Test public void basic_shape_iri()     { testWriter("basic-shape-iri"); }
    @Test public void basic_shape()         { testWriter("basic-shape"); }
    @Test public void basic_shape_with_target()     { testWriter("basic-shape-with-target"); }
    @Test public void basic_shape_with_targets()    { testWriter("basic-shape-with-targets"); }
    @Test public void class_()              { testWriter("class"); }
    @Test public void comment()             { testWriter("comment"); }
    @Test public void complex1()            { testWriter("complex1"); }
    @Test public void complex2()            { testWriter("complex2"); }
    @Test public void count_0_1()           { testWriter("count-0-1"); }
    @Test public void count_0_unlimited()   { testWriter("count-0-unlimited"); }
    @Test public void count_1_2()           { testWriter("count-1-2"); }
    @Test public void count_1_unlimited()   { testWriter("count-1-unlimited"); }
    @Test public void datatype()            { testWriter("datatype"); }
    @Test public void directives()          { testWriter("directives"); }
    @Test public void empty()               { testWriter("empty"); }
    @Test public void nestedShape()         { testWriter("nestedShape"); }
    @Test public void nodeKind()            { testWriter("nodeKind"); }
    @Test public void node_or_2()           { testWriter("node-or-2"); }
    @Test public void node_or_3_not()       { testWriter("node-or-3-not"); }
    @Test public void path_alternative()    { testWriter("path-alternative"); }
    @Test public void path_complex()        { testWriter("path-complex"); }
    @Test public void path_inverse()        { testWriter("path-inverse"); }
    @Test public void path_oneOrMore()      { testWriter("path-oneOrMore"); }
    @Test public void path_sequence()       { testWriter("path-sequence"); }
    @Test public void path_zeroOrMore()     { testWriter("path-zeroOrMore"); }
    @Test public void path_zeroOrOne()      { testWriter("path-zeroOrOne"); }
    @Test public void property_empty()      { testWriter("property-empty"); }
    @Test public void property_not()        { testWriter("property-not"); }
    @Test public void property_or_2()       { testWriter("property-or-2"); }
    @Test public void property_or_3()       { testWriter("property-or-3"); }
    @Test public void shapeRef()            { testWriter("shapeRef"); }

    private final String DIR = "src/test/resources/shaclc-valid/";
    private final String BASE ="urn:x-base:default";

    private void testWriter(String string) {
        String fn = DIR+string+".shaclc";
        String ttl = DIR+string+".ttl"; // <<-- Unique base

        boolean DEV = false;

        // XXX Remove dev code.
        if ( DEV ) {
            System.out.println("---- "+fn);
            String x = IO.readWholeFileAsUTF8(fn);
            System.out.print(x);
        }

        // All shapes, not some.
        Shapes shapes = ShaclcParser.parse(fn, BASE);
        shapes = Shapes.parseAll(shapes.getGraph());
        //if ( DEV ) System.out.printf("R = %d : S = %d\n", shapes.numRootShapes(), shapes.numShapes());
        if ( DEV ) ShLib.printShapes(shapes);

        Graph expected = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(expected, ttl, BASE, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompactWriter.print(out, shapes);

        if ( DEV ) System.out.println(new String(out.toByteArray(), StandardCharsets.UTF_8));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Shapes shapes2 = ShaclcParser.parse(in, BASE);

        Graph graphGot = shapes2.getGraph();
        Graph graphOther = expected;

        // <urn:x-base:default>  a  owl:Ontology .
        // <http://example.org/datatype> a       owl:Ontology .

        // XXX TEMP
        remove(graphGot, null, RDF.type.asNode(), OWL.Ontology.asNode());
        remove(graphGot, null, OWL.imports.asNode(), null);
        remove(graphOther, null, RDF.type.asNode(), OWL.Ontology.asNode());
        remove(graphOther, null, OWL.imports.asNode(), null);

        boolean isomorphic = graphGot.isIsomorphicWith(graphOther);
        if ( ! isomorphic ) {
            System.err.println("---- "+fn);

            System.err.println("Different");
            System.err.println("graph(jena) = "+graphGot.size());
            System.err.println("graph(ref)  = "+graphOther.size());
            if ( true ) {
                RDFWriter.create().source(graphGot).format(RDFFormat.TURTLE_PRETTY).output(System.err);
                RDFWriter.create().source(graphOther).format(RDFFormat.TURTLE_PRETTY).output(System.err);
            }
        }
        assertTrue("test: "+string, isomorphic);
    }

    private void remove(Graph graph, Node s, Node p, Node o) {
        List<Triple> triples = graph.find(s,p,o).toList();
        triples.forEach(graph::delete);
    }
}

