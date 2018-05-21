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

package org.apache.jena.test.rdfconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.jena.graph.*;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;

/* Tests that blanknodes work over RDFConnectionFuseki
 * This consists of testing each of the necessary components,
 * and then a test of a connection itself.  
 */

public class TestBlankNodeBinary {
    private static Node n(String str) { return SSE.parseNode(str) ; }
    
    // Check RDF Thrift round-trips blank nodes.
    @Test public void binaryThrift() {
        Triple t = Triple.create(n(":s"), n(":p"), NodeFactory.createBlankNode("ABCD"));
        Node obj = t.getObject(); 
        Graph graph = Factory.createDefaultGraph();
        graph.add(t);
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFDataMgr.write(bout, graph, Lang.RDFTHRIFT);
        
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        Graph graph1 = Factory.createDefaultGraph();
        RDFDataMgr.read(graph1, bin, Lang.RDFTHRIFT);
        
        Node obj1 = graph1.find().next().getObject();
        assertEquals(obj, obj1);
        assertTrue(obj1.isBlank());
        assertEquals(obj.getBlankNodeLabel(), obj1.getBlankNodeLabel());  
    }
    
    // Check SPARQL parsing.
    @Test public void bNodeSPARQL_Query_1() {
        String qs = "SELECT * { ?s ?p <_:ABC>}";
        Query query = QueryFactory.create(qs);
        Element el = ((ElementGroup)query.getQueryPattern()).get(0);
        ElementPathBlock epb = (ElementPathBlock)el;
        TriplePath tp = epb.getPattern().get(0);
        Triple t = tp.asTriple();
        assertEquals("ABC", t.getObject().getBlankNodeLabel());  
    }
    
    @Test public void bNodeSPARQL_Query_2() {
        String qs = "SELECT * { ?s ?p <_:BCD>}";
        Query query = QueryFactory.create(qs);
        Op op = Algebra.compile(query);
        BasicPattern bp = ((OpBGP)op).getPattern();
        Triple t = bp.get(0);
        assertEquals("BCD", t.getObject().getBlankNodeLabel());  
    }

    @Test public void bNodeSPARQL_Update_1() {
        String str = "INSERT DATA { <x:s> <x:p> <_:789> }";
        UpdateRequest req = UpdateFactory.create(str);
        Update update = req.getOperations().get(0);
        UpdateDataInsert ins = (UpdateDataInsert)update;
        Node obj = ins.getQuads().get(0).getObject();
        assertEquals("789", obj.getBlankNodeLabel());
    }
}
