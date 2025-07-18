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

package org.apache.jena.riot.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.SyntaxLabels;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestLabelToNode {

    public interface LabelToNodeFactory { public LabelToNode create(); }

    private static Stream<Arguments> provideArgs() {
        LabelToNodeFactory fSyntaxLabels = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return SyntaxLabels.createLabelToNode(); }
            @Override public String toString() { return "SyntaxLabels.createLabelToNode"; }
        };

        LabelToNodeFactory fScopeDocumentHash = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createScopeByDocumentHash(); }
            @Override public String toString() { return "ScopeByDocumentHash"; }
        };

        LabelToNodeFactory fScopeByDocumentOld = new LabelToNodeFactory() {
                @Override public LabelToNode create() { return LabelToNode.createScopeGlobal(); }
                @Override public String toString() { return "ScopeByDocumentOld"; }
        };
        LabelToNodeFactory fScopeByGraph = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createScopeByGraph(); }
            @Override public String toString() { return "ScopeByGraph"; }
        };
        LabelToNodeFactory fUseLabelAsGiven = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createUseLabelAsGiven(); }
            @Override public String toString() { return "UseLabelAsGiven"; }
        };
        LabelToNodeFactory fUseLabelEncoded = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createUseLabelEncoded(); }
            @Override public String toString() { return "UseLabelEncoded"; }
        };
        LabelToNodeFactory fIncremental = new LabelToNodeFactory() {
            @Override public LabelToNode create() { return LabelToNode.createIncremental(); }
            @Override public String toString() { return "Incremental"; }
        };

        // (1) Factory, (2) whether DocScoped, (3) whether unique in a document (or graph) (4) whether unique per run
        List<Arguments> x = List.of
                (Arguments.of(fSyntaxLabels,       true,  true),
                 Arguments.of(fScopeDocumentHash,  true,  true),
                 Arguments.of(fScopeByDocumentOld, true,  true),
                 Arguments.of(fScopeByGraph,       false, true),
                 Arguments.of(fUseLabelAsGiven,    true,  false),
                 Arguments.of(fUseLabelEncoded,    true,  false),
                 Arguments.of(fIncremental,        true,  false)
                        );
        return x.stream();
    }

    @Parameter(0)
    private LabelToNodeFactory factory;
    @Parameter(1)
    private Boolean docScope;
    @Parameter(2)
    private Boolean unique;

    @Test
    public void label2node_Create1() {
        LabelToNode mapper = factory.create();
        Node n = mapper.create();
        assertNotNull(n);
    }

    @Test
    public void label2node_Create2() {
        LabelToNode mapper = factory.create();
        Node n1 = mapper.create();
        Node n2 = mapper.create();
        assertNotNull(n1);
        assertNotNull(n2);
        assertNotEquals(n1, n2);
    }

    @Test
    public void label2node_Create3() {
        LabelToNode mapper1 = factory.create();
        LabelToNode mapper2 = factory.create();
        assertNotEquals(mapper1, mapper2);
        Node n1 = mapper1.create();
        Node n2 = mapper2.create();
        assertNotNull(n1);
        assertNotNull(n2);
        if ( unique )
            assertNotEquals(n1, n2);
    }

    @Test
    public void label2node_Label1() {
        LabelToNode mapper = factory.create();
        Node n = mapper.get(null, "label");
        assertNotNull(n);
    }

    @Test
    public void label2node_Label2() {
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(null, "label1");
        Node n2 = mapper.get(null, "label2");
        assertNotNull(n1);
        assertNotNull(n2);
        assertNotEquals(n1, n2);
    }

    @Test
    public void label2node_Label3() {
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(null, "label1");
        Node n2 = mapper.get(null, "label1");
        assertNotNull(n1);
        assertNotNull(n2);
        assertEquals(n1, n2);
    }

    @Test
    public void label2node_Label4() {
        Node g = NodeFactory.createURI("g");
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(g, "label1");
        Node n2 = mapper.get(g, "label1");
        assertNotNull(n1);
        assertNotNull(n2);
        assertEquals(n1, n2);
    }

    @Test
    public void label2node_Label5() {
        Node g1 = NodeFactory.createURI("g1");
        Node g2 = NodeFactory.createURI("g2");
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(g1, "label1");
        Node n2 = mapper.get(g2, "label1");
        assertNotNull(n1);
        assertNotNull(n2);
        if ( docScope )
            assertEquals(n1, n2);
        else
            assertNotEquals(n1, n2);
    }

    @Test
    public void label2node_Label6() {
        Node g = NodeFactory.createURI("g");
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(g, "label1");
        Node n2 = mapper.get(null, "label1");
        if ( docScope )
            assertEquals(n1, n2);
        else
            assertNotEquals(n1, n2);
    }

    @Test
    public void label2node_Label7() {
        Node g1 = NodeFactory.createURI("g1");
        Node g2 = NodeFactory.createURI("g1");
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(g1, "label1");
        Node n2 = mapper.get(g2, "label2");
        assertNotNull(n1);
        assertNotNull(n2);
        assertNotEquals(n1, n2);
    }

    @Test
    public void label2node_Reset1() {
        LabelToNode mapper = factory.create();
        Node n1 = mapper.get(null, "label1");
        mapper.clear();
        Node n2 = mapper.get(null, "label1");
        assertNotNull(n1);
        assertNotNull(n2);
        if ( unique )
            assertNotEquals(n1, n2);
        else
            assertEquals(n1, n2);
    }

    @Test
    public void label2node_Reset2() {
        LabelToNode mapper = factory.create();
        Node g = NodeFactory.createURI("g");
        Node n1 = mapper.get(g, "label1");
        mapper.clear();
        Node n2 = mapper.get(g, "label1");
        assertNotNull(n1);
        assertNotNull(n2);
        if ( unique )
            assertNotEquals(n1, n2);
        else
            assertEquals(n1, n2);
    }
}
