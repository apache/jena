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

package org.apache.jena.tdb2.solver.skipscan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarAlloc;
import org.apache.jena.sparql.core.Vars;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.join.ImmutableUniqueList;
import org.apache.jena.sparql.engine.join.ImmutableUniqueList.Builder;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

/**
 * Single tuple access pattern.
 *
 * Captures variants such as: SELECT DISTINCT ?g ?p { GRAPH ?g { ?s ?p ?o } }
 */
public record PatternQuery(boolean distinct, List<Var> project, Node[] tuple) {

    /**
     * Create a PatternOp structure from the argument. Returns null on failure.
     *
     * Tries to match the structure below. [Op] indicates optional presence.
     * <pre>{@code
     * OpDistinct
     *   [OpProject]
     *     OpQuadPattern|OpBGP
     * }</pre>
     *
     * And returns non-null iff it can be safely rewritten as follows:
     * <pre>{@code
     * OpDistinct
     *   [OpProject]
     *     OpQuadPattern|OpBGP
     * }</pre>
     *
     * <p>
     * Notes:
     * <ul>
     *   <li>Quads in the default graph are returned as triples.</li>
     *   <li>The union default graph constant {@link Quad#unionGraph} is converted to a non-distinguished variable.</li>
     * </ul>
     *
     * @param inputOp The input Op.
     * @return A PatternOp instance or null.
     */
    static PatternQuery createOrNull$(Op inputOp) {
        PatternQuery result = null;

        Node g = null;
        List<Var> projectVars = null;
        boolean distinct = false;
        Node[] tuple = null;

        Op op = inputOp;
        if (op instanceof OpDistinct opD) {
            distinct = true;
            op = opD.getSubOp();
        }

        if (op instanceof OpProject opP) {
            projectVars = ImmutableUniqueList.createUniqueList(opP.getVars());
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

            if (projectVars == null) {
                Set<Var> varSet = new LinkedHashSet<>();
                Vars.addVarsFromTriple(varSet, t);
                if (g != null && g.isVariable()) {
                    varSet.add(Var.alloc(g));
                }
                projectVars = ImmutableUniqueList.createUniqueList(varSet);
            }

            if (g == null || Quad.isDefaultGraph(g)) {
                tuple = new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
            } else if (Quad.isUnionGraph(g)) {
                VarAlloc varAlloc = new VarAlloc("G");
                tuple = new Node[] { varAlloc.allocVar(), t.getSubject(), t.getPredicate(), t.getObject() };
            } else {
                tuple = new Node[] { g, t.getSubject(), t.getPredicate(), t.getObject() };
            }

            result = new PatternQuery(distinct, projectVars, tuple); //, filter, extend, reapplyProject);
        }
        return result;
    }

    public Op effectiveOp() {
        Op result = tupleToOp(tuple);
        Set<Var> tupleVars = OpVars.visibleVars(result);

        if (!project.containsAll(tupleVars)) {
            result = new OpProject(result, project);
        }

        if (distinct) {
            result = new OpDistinct(result);
        }
        return result;
    }

    private static Op tupleToOp(Node[] t) {
        Op result = switch (t.length) {
        case 3 -> new OpBGP(new BasicPattern(List.of(Triple.create(t[0], t[1], t[2]))));
        case 4 -> OpQuadBlock.create(t[0], new BasicPattern(List.of(Triple.create(t[1], t[2], t[3]))));
        default -> throw new IllegalStateException();
        };
        return result;
    }

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
        Builder<Var> builder = ImmutableUniqueList.newUniqueListBuilder();
        for (Var v : patternQuery.project) {
            if (!binding.contains(v)) {
                builder.add(v);
            }
        }
        ImmutableUniqueList<Var> newProject = builder.build();
        return new PatternQuery(patternQuery.distinct, newProject, newTuple);
    }

    private static final Node CONST = NodeFactory.createLiteralString("C");


    /**
     * Create a cache key representation of the argument:
     * <ul>
     *   <li>Each unique variable is deterministically mapped to a fresh one in the order of their occurrence in the tuple.</li>
     *   <li>All constants are re-mapped to the same special constant.</li>
     * </ul>
     *
     * For example (project (?p) (quad ?g :s ?p :o)) becomes (project (?V1) quad(?V0 :C ?V1 :C))
     */
    public static PatternQuery toCacheKey(PatternQuery patternQuery) {
        Map<Node, Node> map = new HashMap<>();
        VarAlloc varAlloc = new VarAlloc("V");

        NodeTransform xform = n -> {
            Node r;
            if (n.isVariable()) {
                r = map.computeIfAbsent(n, k -> varAlloc.allocVar());
            } else if (n.isConcrete()) {
                r = map.computeIfAbsent(n, k -> CONST);
            } else {
                throw new IllegalStateException("Should never come here");
            }
            return r;
        };

        Node[] newTuple = transform(xform, patternQuery.tuple());

        List<Var> newProject = new ArrayList<>(patternQuery.tuple().length);
        for (Var v : patternQuery.project()) {
            Var newV = (Var)map.get(v);
            if (newV != null) {
                newProject.add(newV);
            }
        }

        return new PatternQuery(patternQuery.distinct(), newProject, newTuple);
    }
}
