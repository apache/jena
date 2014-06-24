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
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.SyntaxLabels ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;

@RunWith(Parameterized.class)
public class TestNodeToLabel extends BaseTest
{
    public interface NodeToLabelFactory { public NodeToLabel create() ; }
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        
        NodeToLabelFactory f0 = new NodeToLabelFactory() {
            @Override public NodeToLabel create() { return SyntaxLabels.createNodeToLabel() ; }
            @Override public String toString() { return "SyntaxLabels.createNodeToLabel" ; }
        } ;
        NodeToLabelFactory f1 = new NodeToLabelFactory() {
            @Override public NodeToLabel create() { return NodeToLabel.createBNodeByIRI() ; }
            @Override public String toString() { return "NodeToLabel.createBNodeByIRI()" ; }
        } ;
        NodeToLabelFactory f2 = new NodeToLabelFactory() {
            @Override public NodeToLabel create() { return NodeToLabel.createBNodeByLabelAsGiven() ; }
            @Override public String toString() { return "NodeToLabel.createBNodeByLabelAsGiven()" ; }
        } ;
        NodeToLabelFactory f3 = new NodeToLabelFactory() {
            @Override public NodeToLabel create() { return NodeToLabel.createBNodeByLabelEncoded() ; }
            @Override public String toString() { return "NodeToLabel.createBNodeByLabelEncoded()" ; }
        } ;
        NodeToLabelFactory f4 = new NodeToLabelFactory() {
            @Override public NodeToLabel create() { return NodeToLabel.labelByInternal() ; }
            @Override public String toString() { return "NodeToLabel.labelByInternal()" ; }
        } ;

        x.add(new Object[]{f0}) ;
        x.add(new Object[]{f1}) ;
        x.add(new Object[]{f2}) ;
        x.add(new Object[]{f3}) ;
        x.add(new Object[]{f4}) ;
        return x ; 
    }

    private NodeToLabelFactory factory ;

    public TestNodeToLabel(NodeToLabelFactory factory) 
    {
        this.factory = factory ;
    }
    
    @Test public void node2label_01()
    {
        NodeToLabel mapper = factory.create() ;
        String x1 = mapper.create() ;
        String x2 = mapper.create() ;
        assertNotNull(x1) ;
        assertNotNull(x2) ;
        assertNotEquals(x1, x2) ;
    }

    @Test public void node2label_02()
    {
        NodeToLabel mapper = factory.create() ;
        Node x = NodeFactory.createAnon() ;
        String s1 = mapper.get(null, x) ;
        String s2 = mapper.get(null, x) ;
        assertNotNull(s1) ;
        assertNotNull(s2) ;
        assertEquals(s1, s2) ;
    }

    @Test public void node2label_03()
    {
        NodeToLabel mapper = factory.create() ;
        Node x1 = NodeFactory.createAnon() ;
        Node x2 = NodeFactory.createAnon() ;
        String s1 = mapper.get(null, x1) ;
        String s2 = mapper.get(null, x2) ;
        assertNotNull(s1) ;
        assertNotNull(s2) ;
        assertNotEquals(s1, s2) ;
    }
}

