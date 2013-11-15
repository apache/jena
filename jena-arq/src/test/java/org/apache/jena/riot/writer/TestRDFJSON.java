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

package org.apache.jena.riot.writer;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestRDFJSON extends BaseTest
{
    @Test public void rdfjson_00()
    {
        // Empty graph
        test ("(base <http://example/> (graph))") ;
    }
    
    @Test public void rdfjson_01()
    {
    	test ("(base <http://example/> (graph (<s> <p> 1)))") ;
    }

    @Test public void rdfjson_02()
    {
        // Different subjects
    	test ("(base <http://example/> (graph (<s1> <p> 1)(<s> <p> 2)))") ;
    }

    @Test public void rdfjson_03()
    {
        // Same subject, different predicates
        test ("(base <http://example/> (graph (<s> <p> 1)(<s> <q> 2)))") ;
    }

    @Test public void rdfjson_04()
    {
        // Same subject, same predicates
        test ("(base <http://example/> (graph (<s> <p> 1)(<s> <p> 2)))") ;
    }

    @Test public void rdfjson_05()
    {
        // Multiple subjects
        test ("(base <http://example/> (graph ",
              "(<s> <p> 1)" ,
              "(<s> <p> 2)" ,
              "(<s1> <p> 2)" ,
              "))") ;
    }

    @Test public void rdfjson_06()
    {
        // Blank nodes / subjects
        test ("(base <http://example/> (graph ",
              "(_:a <p> 1)" ,
              "(_:a <p> 2)" ,
              "(_:b <p> 3)" ,
              "))") ;
    }
    
    @Test public void rdfjson_07()
    {
        // Shared blank node objects
        test ("(base <http://example/> (graph ",
              "(<s> <p> _:abc)" ,
              "(<s> <p> 2)" ,
              "(<s1> <p> _:abc)" ,
              "))") ;
    }
    
    @Test public void rdfjson_08()
    {
        // Shared IRI objects
        test ("(base <http://example/> (graph ",
              "(<s> <p> <http://example.org/abc>)" ,
              "(<s> <p> 2)" ,
              "(<s1> <p> <http://example.org/abc>)" ,
              "))") ;
    }
    
    
    @Test public void rdfjson_09()
    {
        // Shared ...
        test ("(base <http://example/> (graph ",
              "(_:s <p> <http://example.org/abc>)" ,
              "(<http://example.org/abc> <p> _:s)" ,
              "))") ;
    }
    
    @Test public void rdfjson_literals()
    {
        // Literals, various
        test ("(base <http://example/> (graph ",
             "(<s> <p> 'abc')",
             "(<s> <p> 'abc'@en)",
             "(<s> <p> 'abc'^^xsd:string)",
             "(<s> <p> '1'^^xsd:integer)",
             "(<s> <p> '1e+100'^^xsd:double)",
             "(<s> <p> '1.05'^^xsd:decimal)",
            "))") ;
    }    
    
    @Test public void rdfjson_escapes()
    {
    	Graph g = GraphFactory.createGraphMem();
    	Node s = NodeFactory.createAnon();
    	Node p = NodeFactory.createURI("http://predicate");
    	g.add(new Triple(s, p, NodeFactory.createLiteral("quote \" character")));
    	g.add(new Triple(s, p, NodeFactory.createLiteral("new \n\r lines")));
    	g.add(new Triple(s, p, NodeFactory.createLiteral("tab \t character")));
    	test(g);
    }

    private void test (String... strings) 
    {
        String str = StrUtils.strjoinNL(strings) ;
        Graph g = SSE.parseGraph(str) ;
        test(g);
    }
    
    private void test (Graph g)
    {
        ByteArrayOutputStream bout = serializeAsJSON(g) ;
        parseAsJSON(bout) ; // make sure valid JSON
        Graph g2 = parseAsRDFJSON(bout) ; 

        assertTrue(g.isIsomorphicWith(g2)) ;  
    }

    private ByteArrayOutputStream serializeAsJSON (Graph graph) 
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream() ;
        RDFDataMgr.write(bout, graph, Lang.RDFJSON) ;
        return bout ;
    }

    private Graph parseAsRDFJSON (ByteArrayOutputStream bout) 
    {
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray()) ;
        Graph graph = GraphFactory.createGraphMem() ;
        RDFDataMgr.read(graph, bin, Lang.RDFJSON) ;
        return graph ;
    }

    private JsonObject parseAsJSON (ByteArrayOutputStream bout) 
    {
    	return JSON.parse(new ByteArrayInputStream(bout.toByteArray())) ;
    }

}
