/**
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

package org.apache.jena.riot.lang;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.system.SyntaxLabels ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

@RunWith(Parameterized.class)
public class TestLabelToNode extends BaseTest
{
    // See also TestNodeAlloc
    
    public interface LabelToNodeFactory { public LabelToNode create() ; }
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        LabelToNodeFactory fSyntaxLabels = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return SyntaxLabels.createLabelToNode() ; }
            @Override public String toString() { return "SyntaxLabels.createLabelToNode" ; }
        } ;
        
        LabelToNodeFactory fScopeDocumentHash = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createScopeByDocumentHash() ; }
            @Override public String toString() { return "ScopeByDocumentHash" ; }
        } ;

        LabelToNodeFactory fScopeByDocumentOld = new LabelToNodeFactory() {
                @Override public LabelToNode create() { return LabelToNode.createScopeByDocumentOld() ; }
                @Override public String toString() { return "ScopeByDocumentOld" ; }
        } ;
        LabelToNodeFactory fScopeByGraph = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createScopeByGraph() ; }
            @Override public String toString() { return "ScopeByGraph" ; }
        } ;
        LabelToNodeFactory fUseLabelAsGiven = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createUseLabelAsGiven() ; }
            @Override public String toString() { return "UseLabelAsGiven" ; }
        } ;
        LabelToNodeFactory fUseLabelEncoded = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createUseLabelEncoded() ; }
            @Override public String toString() { return "UseLabelEncoded" ; }
        } ;
        LabelToNodeFactory fIncremental = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createIncremental() ; }
            @Override public String toString() { return "Incremental" ; }
        } ;

        // (1) Factory, whether DocScoped, (2) whether unique in a document (or graph) (3) whether unique per run 
        x.add(new Object[]{fSyntaxLabels,       true,  true}) ;
        x.add(new Object[]{fScopeDocumentHash,  true,  true}) ;
        x.add(new Object[]{fScopeByDocumentOld, true,  true}) ;
        x.add(new Object[]{fScopeByGraph,       false, true}) ;
        x.add(new Object[]{fUseLabelAsGiven,    true,  false}) ;
        x.add(new Object[]{fUseLabelEncoded,    true,  false}) ;
        x.add(new Object[]{fIncremental,        true,  false}) ;
        return x ; 
    }

    private LabelToNodeFactory factory ;
    private Boolean unique ;
    private Boolean docScope ;

    public TestLabelToNode(LabelToNodeFactory factory, Boolean docScope, Boolean unique) 
    {
        this.factory = factory ;
        this.docScope = docScope ;      // Does this LabelToNode obey the per-document uniqueness?
        this.unique = unique ;          // Does this LabelToNode give uniqueness per initialization?
    }
    
    @Test public void label2node_Create1()
    {
        LabelToNode mapper = factory.create() ;
        Node n = mapper.create() ;
        assertNotNull(n) ;
    }
    
    @Test public void label2node_Create2()
    {
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.create() ;
        Node n2 = mapper.create() ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        assertNotEquals(n1, n2) ;
    }

    @Test public void label2node_Create3()
    {
        LabelToNode mapper1 = factory.create() ;
        LabelToNode mapper2 = factory.create() ;
        assertNotEquals(mapper1, mapper2) ;
        Node n1 = mapper1.create() ;
        Node n2 = mapper2.create() ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        if ( unique )
            assertNotEquals(n1, n2) ;
    }
    
    @Test public void label2node_Label1()
    {
        LabelToNode mapper = factory.create() ;
        Node n = mapper.get(null, "label") ;
        assertNotNull(n) ;
    }
    
    @Test public void label2node_Label2()
    {
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(null, "label1") ;
        Node n2 = mapper.get(null, "label2") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        assertNotEquals(n1,n2) ;
    }
    
    @Test public void label2node_Label3()
    {
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(null, "label1") ;
        Node n2 = mapper.get(null, "label1") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        assertEquals(n1,n2) ;
    }

    @Test public void label2node_Label4()
    {
        Node g = NodeFactory.createURI("g") ;
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(g, "label1") ;
        Node n2 = mapper.get(g, "label1") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        assertEquals(n1,n2) ;
    }

    @Test public void label2node_Label5()
    {
        Node g1 = NodeFactory.createURI("g1") ;
        Node g2 = NodeFactory.createURI("g2") ;
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(g1, "label1") ;
        Node n2 = mapper.get(g2, "label1") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        if ( docScope )
            assertEquals(n1,n2) ;
        else
            assertNotEquals(n1,n2) ;
    }

    @Test public void label2node_Label6()
    {
        Node g = NodeFactory.createURI("g") ;
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(g, "label1") ;
        Node n2 = mapper.get(null, "label1") ;
        if ( docScope )
            assertEquals(n1,n2) ;
        else
            assertNotEquals(n1,n2) ;
    }

    @Test public void label2node_Label7()
    {
        Node g1 = NodeFactory.createURI("g1") ;
        Node g2 = NodeFactory.createURI("g1") ;
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(g1, "label1") ;
        Node n2 = mapper.get(g2, "label2") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        assertNotEquals(n1,n2) ;
    }
    
    @Test public void label2node_Reset1()
    {
        LabelToNode mapper = factory.create() ;
        Node n1 = mapper.get(null, "label1") ;
        mapper.clear() ;
        Node n2 = mapper.get(null, "label1") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        if ( unique ) {
            if ( n1.equals(n2) )
                System.err.println("equals") ;
            
            assertNotEquals(n1,n2) ;
        }
        else
            assertEquals(n1,n2) ;
    }

    @Test public void label2node_Reset2()
    {
        LabelToNode mapper = factory.create() ;
        Node g = NodeFactory.createURI("g") ;
        Node n1 = mapper.get(g, "label1") ;
        mapper.clear() ;
        Node n2 = mapper.get(g, "label1") ;
        assertNotNull(n1) ;
        assertNotNull(n2) ;
        if ( unique )
            assertNotEquals(n1,n2) ;
        else
            assertEquals(n1,n2) ;
    }
}

