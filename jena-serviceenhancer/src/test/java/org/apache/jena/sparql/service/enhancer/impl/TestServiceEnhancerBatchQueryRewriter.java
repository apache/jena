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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.junit.Assert;
import org.junit.Test;

public class TestServiceEnhancerBatchQueryRewriter {

    /**
     * Test that the generation of a bulk request from a query that uses limit and order by uses the correct rewrite.
     * Specifically, ORDER BY must be retained on the union members even if they appear without slice because
     * it might make a difference when the result set is cut off by the service's result set limit.
     */
    @Test
    public void testOrderBy_01() {
        OpService op = (OpService)Algebra.compile(QueryFactory.create(String.join("\n",
            "SELECT * {",
            "  SERVICE <http://example.org/> { SELECT * { ?s ?p ?o } ORDER BY ?s LIMIT 1 }",
            "}"), Syntax.syntaxARQ).getQueryPattern());

        Batch<Integer, PartitionRequest<Binding>> batch = BatchImpl.forInteger();
        Var o = Var.alloc("o");
        batch.put(0, new PartitionRequest<>(0, BindingFactory.binding(o, NodeFactory.createLiteralString("x1")), 1, 5));
        batch.put(1, new PartitionRequest<>(1, BindingFactory.binding(o, NodeFactory.createLiteralString("x2")), 2, 6));

        Query actualQuery = defaultRewrite(op, batch);
        Query expectedQuery = QueryFactory.create(String.join("\n",
            "SELECT  *",
            "WHERE",
            "   {   { { SELECT  *",
            "          WHERE",
            "             { ?s  ?p  \"x1\" }",
            "          ORDER BY ?s",
            "          OFFSET  1",
            "          LIMIT   5",
            "         }",
            "        BIND(0 AS ?idx)",
            "       }",
            "    UNION",
            "       {   { { SELECT  *",
            "              WHERE",
            "                 { ?s  ?p  \"x2\" }",
            "              ORDER BY ?s",
            "              OFFSET  2",
            "              LIMIT   6",
            "             }",
            "            BIND(1 AS ?idx)",
            "           }",
            "        UNION",
            "           { BIND(1000000000 AS ?idx) }",
            "       }",
            "   }",
            "ORDER BY ASC(?idx) ?s"));

        Assert.assertEquals(expectedQuery, actualQuery);
    }

    /** GH-2992: Blank nodes must be relabeled wher building batch unions. */
    @Test
    public void testReport_01() {
        OpService op = (OpService)Algebra.compile(QueryFactory.create("""
            PREFIX sachem: <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#>
            SELECT * {
              SERVICE <http://example.org/> { ?COMPOUND sachem:substructureSearch [ sachem:query ?STRUCTURE ] }
            }
            """, Syntax.syntaxARQ).getQueryPattern());

        Batch<Integer, PartitionRequest<Binding>> batch = BatchImpl.forInteger();
        Var o = Var.alloc("STRUCTURE");
        batch.put(0, new PartitionRequest<>(0, BindingFactory.binding(o, NodeFactory.createLiteralString("[He]")), 0, Long.MAX_VALUE));
        batch.put(1, new PartitionRequest<>(1, BindingFactory.binding(o, NodeFactory.createLiteralString("[Ar]")), 0, Long.MAX_VALUE));

        Query actualQuery = defaultRewrite(op, batch);
        Query expectedQuery = harmonizeBnodes(QueryFactory.create("""
            SELECT  *
            WHERE
              {   { ?COMPOUND  <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#substructureSearch>  _:b0 .
                    _:b0      <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#query>  "[He]"
                    BIND(0 AS ?idx)
                  }
                UNION
                  {   { ?COMPOUND  <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#substructureSearch>  _:b1 .
                        _:b1      <http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#query>  "[Ar]"
                        BIND(1 AS ?idx)
                      }
                    UNION
                      { BIND(1000000000 AS ?idx) }
                  }
              }
            ORDER BY ASC(?idx)
            """));

        Assert.assertEquals(expectedQuery, actualQuery);
    }

    private static Query defaultRewrite(OpService op , Batch<Integer, PartitionRequest<Binding>> batch) {
        BatchQueryRewriter rewriter = new BatchQueryRewriter(new OpServiceInfo(op), Var.alloc("idx"), false, false, false);
        BatchQueryRewriteResult rewrite = rewriter.rewrite(batch);
        Op resultOp = rewrite.getOp();
        Query result = harmonizeBnodes(OpAsQuery.asQuery(resultOp));
        return result;
    }

    /** Relabel blank nodes in the order of their occurrence in the query. */
    private static Query harmonizeBnodes(Query query) {
        Op op = Algebra.compile(query);
        Set<Var> vars = new LinkedHashSet<>();
        OpVars.mentionedVars(op, vars);

        int nextBnodeId[] = {0};
        Map<Var, Node> varMap = vars.stream()
            .filter(v -> Var.isBlankNodeVar(v))
            .collect(Collectors.toMap(v -> v, v -> NodeFactory.createBlankNode("b" + (++nextBnodeId[0]))));

        Query result = QueryTransformOps.replaceVars(query, varMap);
        return result;
    }
}
