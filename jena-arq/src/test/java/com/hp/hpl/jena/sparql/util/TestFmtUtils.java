
package com.hp.hpl.jena.sparql.util ;

import static com.hp.hpl.jena.sparql.util.FmtUtils.stringForQuad ;
import static com.hp.hpl.jena.sparql.util.FmtUtils.stringForRDFNode ;
import static com.hp.hpl.jena.sparql.util.FmtUtils.stringForTriple ;
import static org.junit.Assert.assertEquals ;

import java.io.ByteArrayOutputStream ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl ;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;

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
    public void stringForObject_misc_versions() throws Exception {
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
        Node_Literal nl = (Node_Literal)NodeFactory.createUncachedLiteral("abc", "no", new XSDDatatype("string")) ;
        assertEquals("\"abc\"@no^^<http://www.w3.org/2001/XMLSchema#string>", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void integerLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createUncachedLiteral("2", new XSDDatatype("int")) ;
        assertEquals("\"2\"^^<http://www.w3.org/2001/XMLSchema#int>", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void doubleLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createUncachedLiteral("2.1e2", new XSDDatatype("double")) ;
        assertEquals("2.1e2", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void decimalLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createUncachedLiteral("2.4", new XSDDatatype("decimal")) ;
        assertEquals("2.4", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void booleanLiteral() throws Exception {
        Node_Literal nl = (Node_Literal)NodeFactory.createUncachedLiteral("false", new XSDDatatype("boolean")) ;
        assertEquals("false", FmtUtils.stringForLiteral(nl, getContext())) ;
    }

    @Test
    public void stringForRDFNode_resource() throws Exception {
        final ResourceImpl rdfNod = new ResourceImpl(aUriRemappableNode(), null) ;
        assertEquals("zz:abs", stringForRDFNode(rdfNod, getContext())) ;
    }

    @Test
    public void anonNode() {
        assertEquals("_:b0", FmtUtils.stringForNode(NodeFactory.createAnon())) ;
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
    public void testStringForURI() throws Exception {
        final String s = FmtUtils.stringForURI("zz:ü_fe-zz") ;
        assertEquals("<zz:ü_fe-zz>", s) ;

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
        return new Triple(n1, n2, l3) ;
    }

    private Node aNode() {
        return NodeFactory.createURI("n1") ;
    }

    private Triple getTriple2() {
        Node n1 = NodeFactory.createURI("nb1") ;
        Node n2 = NodeFactory.createURI("nb2") ;
        Node l3 = NodeFactory.createLiteral("lb3") ;
        return new Triple(n1, n2, l3) ;
    }

    private Triple getPrefixedTriple() {
        Node n1 = aUriRemappableNode() ;
        Node n2 = NodeFactory.createURI("n2") ;
        Node l3 = NodeFactory.createLiteral("l3") ;

        return new Triple(n1, n2, l3) ;
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
