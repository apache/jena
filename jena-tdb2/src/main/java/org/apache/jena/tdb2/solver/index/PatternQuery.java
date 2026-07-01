/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.tdb2.solver.index;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.Vars;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.JoinKey;
import org.apache.jena.sparql.engine.join.JoinKey.Builder;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

/**
 * Single tuple access pattern.
 *
 * Captures variants such as: SELECT DISTINCT ?g ?p { GRAPH ?g { ?s ?p ?o } }
 */
public record PatternQuery(boolean distinct, JoinKey project, Node[] tuple) {

    @Override
    public String toString() {
        return "PatternQuery [distinct=" + distinct + ", project=" + project + ", tuple=" + Arrays.toString(tuple) + "]";
    }

    /** Transform an array of nodes. Akin to {@link NodeTransformLib}. */
    private static Node[] transform(NodeTransform nodeTransform, Node[] tuple) {
        int n = tuple.length;
        Node[] newTuple = new Node[n];
        for (int i = 0; i < n; ++i) {
            Node node = tuple[i];
            newTuple[i] = nodeTransform.apply(node);
        }
        return newTuple;
    }

    /** Substitute all variables in a PatternQuery. Akin to {@link Substitute}. */
    public static PatternQuery substitute(PatternQuery patternQuery, Binding binding) {
        Node[] newTuple = transform(n -> Var.lookup(binding, n), patternQuery.tuple);
        Builder builder = JoinKey.newBuilder();
        for (Var v : patternQuery.project) {
            if (!binding.contains(v)) {
                builder.add(v);
            }
        }
        JoinKey newProject = builder.build();
        return new PatternQuery(patternQuery.distinct, newProject, newTuple);
    }

    /**
     * Note: Quads in the default graph are returned as triples.
     *
     * @param inputOp
     * @return
     */
    public static PatternQuery createOrNull(Op inputOp) {
        PatternQuery result = null;

        Node g = null;
        JoinKey projectVars = null;
        boolean distinct = false;
        Node[] tuple = null;

        Op op = inputOp;
        if (op instanceof OpDistinct opD) {
            distinct = true;
            op = opD.getSubOp();
        }

        if (op instanceof OpProject opP) {
            OpProject opProject = opP;
            projectVars = JoinKey.create(opProject.getVars());
            op = opP.getSubOp();
        }

        BasicPattern bp = null;
        if (op instanceof OpQuadPattern opQuadPattern) {
            bp = opQuadPattern.getBasicPattern();
            g = opQuadPattern.getGraphNode();
        } else if (op instanceof OpBGP opBgp) {
            bp = opBgp.getPattern();
        }

        if (bp != null && bp.size() == 1) {
            Triple t = bp.get(0);
            tuple = g == null || Quad.isDefaultGraph(g)
                ? new Node[] { t.getSubject(), t.getPredicate(), t.getObject() }
                : new Node[] { g, t.getSubject(), t.getPredicate(), t.getObject() };

            if (projectVars == null) {
                Set<Var> varSet = new LinkedHashSet<>();
                Vars.addVarsFromTriple(varSet, t);
                if (g != null && g.isVariable()) {
                    varSet.add(Var.alloc(g));
                }
                projectVars = JoinKey.create(varSet); // new ArrayList<>(varSet);
            }

            result = new PatternQuery(distinct, projectVars, tuple);
        }
        return result;
    }
}
