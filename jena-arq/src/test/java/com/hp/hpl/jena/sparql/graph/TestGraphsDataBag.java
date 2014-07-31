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

package com.hp.hpl.jena.sparql.graph;

import org.apache.jena.atlas.data.ThresholdPolicy ;
import org.apache.jena.atlas.data.ThresholdPolicyFactory ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.DeleteDeniedException ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Tests for DataBag graphs */
public class TestGraphsDataBag extends BaseTest
{
    protected Graph distinct;
    protected Graph duplicates;
    
    @Before
    public void setup()
    {
        ThresholdPolicy<Triple> policy = ThresholdPolicyFactory.never();
        distinct = new GraphDistinctDataBag(policy);
        
        ThresholdPolicy<Triple> policy2 = ThresholdPolicyFactory.never();
        duplicates = new GraphDefaultDataBag(policy2);
    }
    
    @After
    public void shutdown()
    {
        if (null != distinct)
        {
            distinct.close() ;
        }
        
        if (null != duplicates)
        {
            duplicates.close();
        }
    }
    
    @Test
    public void add_1()
    {
        distinct.add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        assertEquals(1, count(distinct));
    }
    
    @Test
    public void add_2()
    {
        distinct.add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        distinct.add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        assertEquals(1, count(distinct));
    }
    
    @Test
    public void add_3()
    {
        duplicates.add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        duplicates.add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        assertEquals(2, count(duplicates));
    }
    
    @Test
    public void empty_0()
    {
        assertEquals(0, count(distinct));
    }
    
    @Test
    public void removeAll_1()
    {
        distinct.add(SSE.parseTriple("(<x> <p> 'ZZZ')"));
        assertEquals(1, count(distinct));
        distinct.clear();
        assertEquals(0, count(distinct));
    }
    
    @Test(expected=DeleteDeniedException.class)
    public void delete_1()
    {
        Triple t = SSE.parseTriple("(<x> <p> 'ZZZ')");
        distinct.add(t);
        distinct.delete(t);
    }
    
    @Test
    public void complexQuery_1()
    {
        for (int i=0; i<2; i++)
        {
            distinct.add(SSE.parseTriple("(<http://example.org/a> <http://example.org/p> 'YYY')"));
            distinct.add(SSE.parseTriple("(<http://example.org/a> <http://example.org/p2> 'ZZZ')"));
            distinct.add(SSE.parseTriple("(<http://example.org/b> <http://example.org/p> 'YYY')"));
            distinct.add(SSE.parseTriple("(<http://example.org/b> <http://example.org/p2> 'ZZZ')"));
        }
        
        assertEquals(2, query("select * where { ?a <http://example.org/p> ?v ; <http://example.org/p2> ?v2 }", distinct));
        assertEquals(2, query("select distinct * where { ?a <http://example.org/p> ?v ; <http://example.org/p2> ?v2 }", distinct));
    }
    
    @Test
    public void complexQuery_2()
    {
        for (int i=0; i<2; i++)
        {
            duplicates.add(SSE.parseTriple("(<http://example.org/a> <http://example.org/p> 'YYY')"));
            duplicates.add(SSE.parseTriple("(<http://example.org/a> <http://example.org/p2> 'ZZZ')"));
            duplicates.add(SSE.parseTriple("(<http://example.org/b> <http://example.org/p> 'YYY')"));
            duplicates.add(SSE.parseTriple("(<http://example.org/b> <http://example.org/p2> 'ZZZ')"));
        }
        
        assertEquals(8, query("select * where { ?a <http://example.org/p> ?v ; <http://example.org/p2> ?v2 }", duplicates));
        assertEquals(2, query("select distinct * where { ?a <http://example.org/p> ?v ; <http://example.org/p2> ?v2 }", duplicates));
    }
    
    private int query(String str, Graph g)
    {
        Model model = ModelFactory.createModelForGraph(g);
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        try(QueryExecution qexec = QueryExecutionFactory.create(q, model)) {
        ResultSet rs = qexec.execSelect() ;
        return ResultSetFormatter.consume(rs) ;
        }
    }
    
    private int count(Graph g)
    {
        int toReturn = 0;
        ExtendedIterator<Triple> it = g.find(null, null, null);
        try
        {
            while (it.hasNext())
            {
                it.next();
                toReturn++;
            }
        }
        finally
        {
            it.close();
        }
        
        return toReturn;
    }
}
