/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.query.rewriter;

import java.util.Arrays;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OpRewriterTest {
    private OpRewriter rewriter;
    private Triple[] triples;

    public OpRewriterTest() {
    }

    @Before
    public void setup() {
        triples = new Triple[] {
                Triple.create(NodeFactory.createVariable("foo"), RDF.type.asNode(),
                        NodeFactory.createURI("http://example.com/class")),
                Triple.create(NodeFactory.createVariable("foo"), NodeFactory.createBlankNode(),
                        NodeFactory.createVariable("bar")),
                Triple.create(NodeFactory.createVariable("bar"), NodeFactory.createBlankNode(),
                        NodeFactory.createVariable("baz")), };
    }

    @Test
    public void testBGP() {
        SecurityEvaluator securityEvaluator = new MockSecurityEvaluator(true, true, true, true, true, true, true);
        rewriter = new OpRewriter(securityEvaluator, "http://example.com/dummy");

        rewriter.visit(new OpBGP(BasicPattern.wrap(Arrays.asList(triples))));
        Op op = rewriter.getResult();
        Assert.assertTrue("Should have been an OpFilter", op instanceof OpFilter);
        OpFilter filter = (OpFilter) op;
        ExprList eLst = filter.getExprs();
        Assert.assertEquals(1, eLst.size());
        Assert.assertTrue("Should have been a SecuredFunction", eLst.get(0) instanceof SecuredFunction);
        op = filter.getSubOp();
        Assert.assertTrue("Should have been a OpBGP", op instanceof OpBGP);
        BasicPattern basicPattern = ((OpBGP) op).getPattern();
        Assert.assertEquals(3, basicPattern.size());

        Triple t = basicPattern.get(0);
        Assert.assertEquals(NodeFactory.createVariable("foo"), t.getSubject());
        Assert.assertEquals(RDF.type.asNode(), t.getPredicate());
        Assert.assertEquals(NodeFactory.createURI("http://example.com/class"), t.getObject());

        t = basicPattern.get(1);
        Assert.assertEquals(NodeFactory.createVariable("foo"), t.getSubject());
        Assert.assertTrue("Should have been blank", t.getPredicate().isBlank());
        Assert.assertEquals(NodeFactory.createVariable("bar"), t.getObject());

        t = basicPattern.get(2);
        Assert.assertEquals(NodeFactory.createVariable("bar"), t.getSubject());
        Assert.assertTrue("Should have been blank", t.getPredicate().isBlank());
        Assert.assertEquals(NodeFactory.createVariable("baz"), t.getObject());
    }

    @Test
    public void testBGPNoReadAccess() {
        SecurityEvaluator securityEvaluator = new MockSecurityEvaluator(true, true, false, true, true, true, true);
        rewriter = new OpRewriter(securityEvaluator, "http://example.com/dummy");
        Triple[] triples = {
                Triple.create(NodeFactory.createVariable("foo"), RDF.type.asNode(),
                        NodeFactory.createURI("http://example.com/class")),
                Triple.create(NodeFactory.createVariable("foo"), NodeFactory.createBlankNode(),
                        NodeFactory.createVariable("bar")),
                Triple.create(NodeFactory.createVariable("bar"), NodeFactory.createBlankNode(),
                        NodeFactory.createVariable("baz")), };
        try {
            rewriter.visit(new OpBGP(BasicPattern.wrap(Arrays.asList(triples))));
            Assert.fail("Should have thrown AccessDeniedException");
        } catch (ReadDeniedException e) {
            // expected
        }
    }

}
