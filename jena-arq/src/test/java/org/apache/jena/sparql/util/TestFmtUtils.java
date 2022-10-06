
package org.apache.jena.sparql.util ;

import static org.apache.jena.sparql.util.FmtUtils.stringForQuad ;
import static org.apache.jena.sparql.util.FmtUtils.stringForRDFNode ;
import static org.apache.jena.sparql.util.FmtUtils.stringForTriple ;
import static org.junit.Assert.assertEquals ;

import java.io.ByteArrayOutputStream ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Node_Literal ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.impl.LiteralImpl ;
import org.apache.jena.rdf.model.impl.ResourceImpl ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

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

public class TestFmtUtils
{
    @Test
    public void stringForTripleEncoding() {
        assertEquals("<n1> <n2> \"l3\"", stringForTriple(getTriple())) ;
    }

    @Test
    public void stringForTriple_WithPrefixMapping() {
        assertEquals("zz:abs <n2> \"l3\"", stringForTriple(getPrefixedTriple(), getPrefixMapping())) ;
    }
    
    @Test
    public void stringForQuadEncoding() {
        Node n1 = NodeFactory.createURI("q1") ;

        Quad quad = new Quad(n1, getTriple()) ;
        assertEquals("<q1> <n1> <n2> \"l3\"", stringForQuad(quad)) ;

    }

    @Test
    public void stringForQuad_WithPrefixMapping() {
        Node n1 = NodeFactory.createURI("q1") ;

        Quad quad = new Quad(n1, getPrefixedTriple()) ;
        assertEquals("<q1> zz:abs <n2> \"l3\"", stringForQuad(quad, getPrefixMapping())) ;
    }

    @Test
    public void formatPattern_2_triples() {
        BasicPattern basicPattern = new BasicPattern() ;
        basicPattern.add(getTriple()) ;
        basicPattern.add(getTriple2()) ;
        ByteArrayOutputStream os = new ByteArrayOutputStream() ;
        try(IndentedWriter iw = new IndentedWriter(os)) {
            SerializationContext sc = new SerializationContext() ;
            FmtUtils.formatPattern(iw, basicPattern, sc) ;
        }
        assertEquals("<n1> <n2> \"l3\" .\n" + "<nb1> <nb2> \"lb3\" .", new String(os.toByteArray())) ;
    }

    @Test
    public void stringForObject_misc_versions() {
        assertEquals("<<null>>", FmtUtils.stringForObject(null)) ;
        assertEquals("<n1>", FmtUtils.stringForObject(new LiteralImpl(aNode(), null))) ;
        assertEquals("<nzz1>", FmtUtils.stringForObject(new ResourceImpl(NodeFactory.createURI("nzz1"), null))) ;
        assertEquals("abc", FmtUtils.stringForObject("abc")) ;
    }

    @Test
    public void stringForRDFNode_literal() {
        assertEquals("<n1>", stringForRDFNode(new LiteralImpl(aNode(), null))) ;
    }

    @Test
    public void stringLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createLiteral("abc", "no", new XSDDatatype("string")) ;
        assertEquals("\"abc\"@no", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void integerLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createLiteral("2", new XSDDatatype("int")) ;
        assertEquals("\"2\"^^<http://www.w3.org/2001/XMLSchema#int>", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void doubleLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createLiteral("2.1e2", new XSDDatatype("double")) ;
        assertEquals("2.1e2", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void decimalLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createLiteral("2.4", new XSDDatatype("decimal")) ;
        assertEquals("2.4", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void booleanLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createLiteral("false", new XSDDatatype("boolean")) ;
        assertEquals("false", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void stringForRDFNode_resource() {
        final ResourceImpl rdfNod = new ResourceImpl(aUriRemappableNode(), null) ;
        assertEquals("zz:abs", stringForRDFNode(rdfNod, getContext())) ;
    }

    @Test
    public void anonNode1() {
        FmtUtils.resetBNodeLabels(); 
        assertEquals("_:b0", FmtUtils.stringForNode(NodeFactory.createBlankNode())) ;
    }

    @Test
    public void anonNode2() {
        FmtUtils.resetBNodeLabels(); 
        assertEquals("_:b0", FmtUtils.stringForNode(NodeFactory.createBlankNode())) ;
        assertEquals("_:b1", FmtUtils.stringForNode(NodeFactory.createBlankNode())) ;
        assertEquals("_:b2", FmtUtils.stringForNode(NodeFactory.createBlankNode())) ;
    }

    @Test
    public void variableNode() {
        assertEquals("?tt", FmtUtils.stringForNode(NodeFactory.createVariable("tt"))) ;
    }

    @Test
    public void anyNode() {
        assertEquals("ANY", FmtUtils.stringForNode(Node.ANY)) ;
    }

    @Test
    public void testStringForURI() {
        final String s = FmtUtils.stringForURI("zz:ü_fe-zz") ;
        assertEquals("<zz:ü_fe-zz>", s) ;

    }
    
    @Test
    public void stringForURI_colonInLocalname_shouldCompact() {
        String uri = aUri + "local:name";
        final String result = FmtUtils.stringForURI(uri, getPrefixMapping());
        assertEquals("zz:local:name", result);
    }
    
    @Test
    public void testStringEsc() {
        assertEquals("\\\\\\r\\n", FmtUtils.stringEsc("\\\r\n")) ;
    }

    @Test
    public void stringForString() {
        assertEquals("\"a\\rbt\"", FmtUtils.stringForString("a\rbt")) ;
    }

    @Test
    public void testFormatBGP_1() {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        BasicPattern bgp = SSE.parseBGP("(prefix ((zz: <"+aUri+">)) (bgp (zz:s zz:p zz:o)))") ;
        FmtUtils.formatPattern(b, bgp, getContext()) ;
        assertEquals("zz:s zz:p zz:o .", b.toString()) ;
    }

    @Test
    public void testFormatBGP_2() {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        BasicPattern bgp = SSE.parseBGP("(prefix ((zz: <"+aUri+">)) (bgp (zz:s zz:p zz:o) (zz:s zz:p 123) ))") ;
        FmtUtils.formatPattern(b, bgp, getContext()) ;
        assertEquals("zz:s zz:p zz:o .\nzz:s zz:p 123 .", b.toString()) ;
    }

    private Triple getTriple() {
        Node n1 = aNode() ;
        Node n2 = NodeFactory.createURI("n2") ;
        Node l3 = NodeFactory.createLiteral("l3") ;
        return Triple.create(n1, n2, l3) ;
    }

    private Node aNode() {
        return NodeFactory.createURI("n1") ;
    }

    private Triple getTriple2() {
        Node n1 = NodeFactory.createURI("nb1") ;
        Node n2 = NodeFactory.createURI("nb2") ;
        Node l3 = NodeFactory.createLiteral("lb3") ;
        return Triple.create(n1, n2, l3) ;
    }

    private Triple getPrefixedTriple() {
        Node n1 = aUriRemappableNode() ;
        Node n2 = NodeFactory.createURI("n2") ;
        Node l3 = NodeFactory.createLiteral("l3") ;

        return Triple.create(n1, n2, l3) ;
    }

    private Node aUriRemappableNode() {
        return NodeFactory.createURI(aUri + "abs") ;
    }

    private PrefixMapping getPrefixMapping() {
        PrefixMapping pmap = new PrefixMappingImpl() ;
        pmap.setNsPrefix("zz", aUri) ;
        return pmap ;
    }

    public static final String aUri = "http://www.zz.org/xx#" ;

    private SerializationContext getContext() {
        return new SerializationContext(getPrefixMapping()) ;
    }
}
