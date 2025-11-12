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

package org.apache.jena.sparql.resultset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.ResultSetStream;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sparql.util.ResultSetUtils;
import org.apache.jena.sys.JenaSystem;

public class TestResultSet {
    static {
        JenaSystem.init();
    }

    @BeforeAll
    public static void setup() {
        // Disable warnings these tests will produce
        ResultSetPeeking.warnOnSyncErrors = false;
    }

    @AfterAll
    public static void teardown() {
        // Re-enable warnings
        ResultSetPeeking.warnOnSyncErrors = true;
    }

    // Test reading, writing and comparison
    @Test
    public void test_RS_1() {
        ResultSetRewindable rs1 = new ResultSetMem();
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(arr, rs1);
        rs1.reset();

        String x = StrUtils.fromUTF8bytes(arr.toByteArray());

        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray());
        ResultSet rs2 = ResultSetFactory.fromXML(ins);
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_1_str() {
        ResultSetRewindable rs1 = new ResultSetMem();
        String x = ResultSetFormatter.asXMLString(rs1);
        rs1.reset();
        InputStream in = new ByteArrayInputStream(StrUtils.asUTF8bytes(x));
        ResultSet rs2 = ResultSetFactory.fromXML(in);
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_2() {
        ResultSetRewindable rs1 = makeRewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsXML(arr, rs1);
        rs1.reset();
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray());
        ResultSet rs2 = ResultSetFactory.fromXML(ins);
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_2_str() {
        ResultSetRewindable rs1 = makeRewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        String x = ResultSetFormatter.asXMLString(rs1);
        rs1.reset();
        InputStream in = new ByteArrayInputStream(StrUtils.asUTF8bytes(x));
        ResultSet rs2 = ResultSetFactory.fromXML(in);
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    // RDF

    @Test
    public void test_RS_3() {
        ResultSetRewindable rs1 = new ResultSetMem();
        Model model = RDFOutput.encodeAsModel(rs1);
        rs1.reset();
        ResultSet rs2 = RDFInput.fromRDF(model);
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_4() {
        ResultSetRewindable rs1 = makeRewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        Model model = RDFOutput.encodeAsModel(rs1);
        rs1.reset();
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(RDFInput.fromRDF(model));
        boolean b = ResultsCompare.equalsByTerm(rs1, rs2);
        if ( !b ) {
            rs1.reset();
            rs2.reset();
            ResultSetFormatter.out(rs1);
            ResultSetFormatter.out(rs2);
        }

        assertTrue(b);
    }

    // JSON

    @Test
    public void test_RS_5() {
        ResultSetRewindable rs1 = new ResultSetMem();
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(arr, rs1);
        rs1.reset();
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray());
        ResultSet rs2 = ResultSetFactory.fromJSON(ins);
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_6() {
        ResultSetRewindable rs1 = make2Rewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(arr, rs1);
        rs1.reset();
        ByteArrayInputStream ins = new ByteArrayInputStream(arr.toByteArray());
        ResultSet rs2 = ResultSetFactory.fromJSON(ins);    // Test using the DAWG
                                                           // examples
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    // Into some format.
    private static String DIR = "testing/ResultSet/";

    @Test
    public void test_RS_7() {
        ResultSet rs = ResultSetFactory.load(DIR + "output.srx");
        test_RS_fmt(rs, ResultsFormat.XML, true);
    }

    @Test
    public void test_RS_8() {
        ResultSet rs = ResultSetFactory.load(DIR + "output.srx");
        test_RS_fmt(rs, ResultsFormat.JSON, true);
    }

    @Test
    public void test_RS_9() {
        ResultSet rs = ResultSetFactory.load(DIR + "output.srx");
        test_RS_fmt(rs, ResultsFormat.XML, false);
    }

    @Test
    public void test_RS_10() {
        ResultSet rs = ResultSetFactory.load(DIR + "output.srx");
        for (; rs.hasNext(); rs.next() ) {}
        // We should be able to call hasNext() as many times as we want!
        assertFalse(rs.hasNext());
    }

    // Test reading "variations". Things that are accepted but not in the form Jena
    // writes.

    // JENA-1563: xml:lang= and datatype=rdf:langString
    @Test
    public void rs_xmllang_datatype_1() {
        ResultSetFactory.load(DIR + "rs-xmllang-datatype-1.srj");
    }

    // JENA-1563: xml:lang= and incompatible datatype
    @Test
    public void rs_xmllang_datatype_2() {
        // Bad: datatype is not rdf:langString (it is xsd:string in the test data)
        assertThrows(ResultSetException.class,()-> ResultSetFactory.load(DIR + "rs-xmllang-datatype-2.srj") );
    }

    // Explicit (unnecessary) datatype=xsd:string
    @Test
    public void rs_datatype_string() {
        ResultSet rs = ResultSetFactory.load(DIR + "rs-datatype-string.srj");
    }

    @Test
    public void test_RS_union_1() {
        ResultSet rs1 = make("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ResultSet rs2 = make("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ResultSet rs3 = make2("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        assertTrue(ResultsCompare.equalsByTerm(rs3, ResultSetUtils.merge(rs1, rs2)));
    }

    private void test_RS_fmt(ResultSet rs, ResultsFormat fmt, boolean ordered) {
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(rs);
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ResultSetFormatter.output(arr, rs1, fmt);
        byte bytes[] = arr.toByteArray();
        rs1.reset();
        ByteArrayInputStream ins = new ByteArrayInputStream(bytes);
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(ResultSetFactory.load(ins, fmt));

        // Ordered? Unordered?
        boolean b = ResultsCompare.equalsByTerm(rs1, rs2);
        if ( ordered ) {
            rs1.reset();
            rs2.reset();
            b = b & ResultsCompare.equalsByTerm(rs1, rs2);
        }

        if ( !b ) {
            System.out.println(new String(bytes));
            rs1.reset();
            rs2.reset();
            ResultSetFormatter.out(rs1);
            ResultSetFormatter.out(rs2);
        }

        assertTrue(b);
    }

    // Test comparison
    @Test
    public void test_RS_cmp_1() {
        ResultSetRewindable rs1 = new ResultSetMem();
        ResultSetRewindable rs2 = new ResultSetMem();
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
        rs1.reset();
        rs2.reset();
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_cmp_2() {
        ResultSet rs1 = make("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ResultSet rs2 = new ResultSetMem();
        assertFalse(ResultsCompare.equalsByTerm(rs1, rs2));

    }

    @Test
    public void test_RS_cmp_3() {
        ResultSet rs1 = make("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ResultSet rs2 = new ResultSetMem();
        assertFalse(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_cmp_4() {
        ResultSet rs1 = make("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ResultSet rs2 = make("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_cmp_5() {
        // Same variable, different values
        ResultSetRewindable rs1 = makeRewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local:1"));
        ResultSetRewindable rs2 = makeRewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local:2"));

        assertFalse(ResultsCompare.equalsByTerm(rs1, rs2));
        rs1.reset();
        rs2.reset();

        assertTrue(rs1.hasNext());
        assertTrue(rs2.hasNext());
        assertFalse(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    @Test
    public void test_RS_cmp_6() {
        // Different variable, same values
        ResultSetRewindable rs1 = makeRewindable("x", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        ResultSetRewindable rs2 = makeRewindable("y", org.apache.jena.graph.NodeFactory.createURI("tag:local"));
        assertFalse(ResultsCompare.equalsByTermAndOrder(rs1, rs2));
        rs1.reset();
        rs2.reset();
        assertTrue(rs1.hasNext());
        assertTrue(rs2.hasNext());
        assertFalse(ResultsCompare.equalsByTerm(rs1, rs2));
    }

    // Value based
    @Test
    public void test_RS_cmp_value_1() {
        ResultSetRewindable rs1 = makeRewindable("x", NodeFactoryExtra.parseNode("123"));
        ResultSetRewindable rs2 = makeRewindable("x", NodeFactoryExtra.parseNode("0123"));
        assertFalse(ResultsCompare.equalsByTerm(rs1, rs2));
        assertTrue(ResultsCompare.equalsByValue(rs1, rs2));
    }

    // Peeking
    @Test
    public void test_RS_peeking_1() {
        ResultSetPeekable rs = makePeekable("x", NodeFactory.createURI("tag:local"));
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Peeking should not move the result set onwards so hasNext() should still
        // report true
        assertTrue(rs.hasNext());

        assertNotNull(rs.next());
        assertFalse(rs.hasNext());
    }

    @Test
    public void test_RS_peeking_2() {
        ResultSetPeekable rs = makePeekable("x", NodeFactory.createURI("tag:local"));
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Peeking should not move the result set onwards so hasNext() should still
        // report true
        assertTrue(rs.hasNext());

        assertNotNull(rs.next());
        assertFalse(rs.hasNext());

        // Peeking beyond end of results throws an error
        assertThrows(NoSuchElementException.class,()-> rs.peek() );
    }

    @Test
    public void test_RS_peeking_3() {
        // Expect that a rewindable result set will be peekable
        ResultSetPeekable rs = (ResultSetPeekable)makeRewindable("x", NodeFactory.createURI("tag:local"));
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Peeking should not move the result set onwards so hasNext() should still
        // report true
        assertTrue(rs.hasNext());

        assertNotNull(rs.next());
        assertFalse(rs.hasNext());
    }

    @Test
    public void test_RS_peeking_4() {
        // Expect that a rewindable result set will be peekable
        ResultSetPeekable rs = (ResultSetPeekable)makeRewindable("x", NodeFactory.createURI("tag:local"));
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Peeking should not move the result set onwards so hasNext() should still
        // report true
        assertTrue(rs.hasNext());

        assertNotNull(rs.next());
        assertFalse(rs.hasNext());

        // Peeking beyond end of results throws an error
        assertThrows(NoSuchElementException.class,()-> rs.peek() );
    }

    @Test
    public void test_RS_peeking_5() {
        // Peeking should be able to cope with people moving on the underlying result
        // set independently
        ResultSet inner = make(List.of("x"), row("x", NodeFactory.createURI("tag:local")), row("x", NodeFactory.createURI("tag:local")));
        ResultSetPeekable rs = ResultSetFactory.makePeekable(inner);
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Move on the inner result set independently
        inner.next();

        // Since we fiddled with the underlying result set there won't be further
        // elements available anymore
        assertFalse(rs.hasNext());
    }

    @Test
    public void test_RS_peeking_6() {
        // Peeking should be able to cope with people moving on the underlying result
        // set independently
        ResultSet inner = make(List.of("x"), row("x", NodeFactory.createURI("tag:local")), row("x", NodeFactory.createURI("tag:local")),
                               row("x", NodeFactory.createURI("tag:local")));
        ResultSetPeekable rs = ResultSetFactory.makePeekable(inner);
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Move on the inner result set independently
        inner.next();

        // Since we fiddled with the underlying result set we'll be out of sync
        // but there should still be further data available
        assertTrue(rs.hasNext());
    }

    private static Binding row(String var, Node node) {
        return BindingFactory.binding(Var.alloc(var), node);
    }

    @Test
    public void test_RS_peeking_7() {
        // Peeking may fail if someone moves backwards in the result set
        // If we hadn't moved pass the first item this should be safe
        ResultSetRewindable inner = makeRewindable("x", NodeFactory.createURI("tag:local"));
        ResultSetPeekable rs = ResultSetFactory.makePeekable(inner);
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());

        // Reset the inner result set independently
        inner.reset();

        // Since we moved the underlying result set backwards but we hadn't gone
        // anywhere
        // we should still be able to safely access the underlying results
        assertTrue(rs.hasNext());
    }

    @Test
    public void test_RS_peeking_8() {
        // Peeking may fail if someone moves backwards in the result set
        // If we had moved past the first item this should be an error
        ResultSet resultSet0 = make(List.of("x"), row("x", NodeFactory.createURI("tag:local")),
                                    row("x", NodeFactory.createURI("tag:local")));
        ResultSetRewindable inner = ResultSetFactory.makeRewindable(resultSet0);
        ResultSetPeekable rs = ResultSetFactory.makePeekable(inner);
        assertTrue(rs.hasNext());
        assertNotNull(rs.peek());
        assertNotNull(rs.next());

        // Reset the inner result set independently
        inner.reset();

        // Since we moved the underlying result set backwards and had
		// moved somewhere we are now in an illegal state
        assertThrows(IllegalStateException.class,()-> rs.hasNext() );
    }

    @Test
    public void test_RS_peeking_9() {
        // Check that peeking causes the correct row to be returned when we actually
        // access the rows
        Node first = NodeFactory.createURI("tag:first");
        Node second = NodeFactory.createURI("tag:second");
        Var x = Var.alloc("x");

        ResultSet inner = make(List.of("x"), row("x", first), row("x", second));
        ResultSetPeekable rs = ResultSetFactory.makePeekable(inner);
        assertTrue(rs.hasNext());

        // Peek and check row is as expected
        Binding peeked = rs.peekBinding();
        assertNotNull(peeked);
        assertTrue(first.equals(peeked.get(x)));

        // Check first row is as expected
        Binding next = rs.nextBinding();
        assertNotNull(next);
        assertTrue(first.equals(next.get(x)));

        // Repeat for second row
        peeked = rs.peekBinding();
        assertNotNull(peeked);
        assertTrue(second.equals(peeked.get(x)));
        next = rs.nextBinding();
        assertNotNull(next);
        assertTrue(second.equals(next.get(x)));
    }

    // ---- Isomorphism.

    private static String[] rs1$ = {
        "(resultset (?x ?y)",
        "   (row (?x _:b0) (?y _:b1))",
        "   (row (?x _:b2) (?y _:b3))",
        "   (row (?x _:b1) (?y _:b0))",
        ")"
    };
    private static String[] rs2$ = {
        "(resultset (?x ?y)",
        "   (row (?x _:c1) (?y _:c0))",
        "   (row (?x _:c3) (?y _:c2))",
        "   (row (?x _:c2) (?y _:c3))",
        ")"};

    @Test
    public void test_RS_iso_1() {
        isotest(rs1$, rs2$);
    }

    private void isotest(String[] rs1$2, String[] rs2$2) {
        ResultSetRewindable rs1 = make(StrUtils.strjoinNL(rs1$));
        ResultSetRewindable rs2 = make(StrUtils.strjoinNL(rs2$));
        assertTrue(ResultsCompare.equalsByTerm(rs1, rs2));
        rs1.reset();
        rs2.reset();
        assertTrue(ResultsCompare.equalsByValue(rs1, rs2));
    }

    private static ResultSetRewindable make(String x) {
        return ResultSetFactory.makeRewindable(SSE.parseRowSet(x));
    }

    // -- BNode preservation

    static Context cxt;
    static {
        cxt = new Context();
        cxt.set(ARQ.inputGraphBNodeLabels, true);
        cxt.set(ARQ.outputGraphBNodeLabels, true);
    }

    @Test
    public void preserve_bnodes_1() {
        preserve_bnodes(ResultSetLang.RS_JSON, cxt, true);
        preserve_bnodes(ResultSetLang.RS_JSON, ARQ.getContext(), false);
    }

    @Test
    public void preserve_bnodes_2() {
        preserve_bnodes(ResultSetLang.RS_XML, cxt, true);
        preserve_bnodes(ResultSetLang.RS_XML, ARQ.getContext(), false);
    }

    @Test
    public void preserve_bnodes_3() {
        preserve_bnodes(ResultSetLang.RS_Thrift, cxt, true);
        preserve_bnodes(ResultSetLang.RS_Thrift, ARQ.getContext(), true);
    }

    private static void preserve_bnodes(Lang sparqlresultlang, Context cxt, boolean same) {
        ResultSetRewindable rs1 = make(StrUtils.strjoinNL(rs1$));
        ByteArrayOutputStream x = new ByteArrayOutputStream();

        ResultsWriter.create().context(cxt).lang(sparqlresultlang).write(x, rs1);
        ByteArrayInputStream y = new ByteArrayInputStream(x.toByteArray());

        ResultSetRewindable rs2 = ResultSetFactory.copyResults(ResultsReader.create().context(cxt).lang(sparqlresultlang).read(y));

        rs1.reset();
        rs2.reset();

        if ( same )
            assertTrue(ResultsCompare.equalsExact(rs1, rs2));
        else
            assertFalse(ResultsCompare.equalsExact(rs1, rs2));
    }

    // -------- Support functions

    static class RowSetBuilder {
    }

    private ResultSet make(String var, Node val) {
        return make(List.of(var), row(var, val));
    }

    private ResultSet make(List<String> varNames, Binding...rows) {
        List<Binding> listRows = Arrays.asList(rows);
        List<Var> vars = varNames.stream().map(Var::alloc).toList();
        return new ResultSetMem(ResultSetStream.create(vars, listRows.iterator()));
    }

    private ResultSet make2(String var, Node val) {
        return make(List.of(var), row(var, val), row(var, val));
    }

    private ResultSetRewindable makeRewindable(String var, Node val) {
        ResultSet rs = make(var, val);
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs);
        return rsw;
    }

    private ResultSetRewindable make2Rewindable(String var, Node val) {
        ResultSet rs = make2(var, val);
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs);
        return rsw;
    }

    private ResultSet make(String var1, Node val1, String var2, Node val2) {
        Binding b = BindingFactory.binding(Var.alloc(var1), val1, Var.alloc(var2), val2);

        List<String> vars = new ArrayList<>();
        vars.add(var1);
        vars.add(var2);

        QueryIterator qIter = QueryIterSingleton.create(b, null);
        ResultSet rs = ResultSetStream.create(vars, null, qIter);
        return rs;
    }

    private ResultSetRewindable makeRewindable(String var1, Node val1, String var2, Node val2) {
        ResultSet rs = make(var1, val1, var2, val2);
        ResultSetRewindable rsw = ResultSetFactory.makeRewindable(rs);
        return rsw;
    }

    private ResultSetPeekable makePeekable(String var, Node val) {
        ResultSet rs = make(var, val);
        ResultSetPeekable rsp = ResultSetFactory.makePeekable(rs);
        return rsp;
    }

    private ResultSetPeekable make2Peekable(String var1, Node val1, String var2, Node val2) {
        ResultSet rs = make(var1, val1, var2, val2);
        ResultSetPeekable rsp = ResultSetFactory.makePeekable(rs);
        return rsp;
    }
}
