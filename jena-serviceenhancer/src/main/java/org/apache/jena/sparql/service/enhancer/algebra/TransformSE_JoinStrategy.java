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
package org.apache.jena.sparql.service.enhancer.algebra;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.service.enhancer.impl.ServiceOpts;
import org.apache.jena.sparql.service.enhancer.impl.ServiceOptsSE;

/**
 * Checks for the presence of <code>SERVICE &lt;loop:&gt; { }</code>
 * transforms those into linear joins using {@link OpSequence} / {@link OpDisjunction}
 * and adjust variable scopes.
 *
 * All variables mentioned on the rhs which have the same reverse-renaming as variables
 * visible on the lhs will be substituted with the lhs variant.
 */
public class TransformSE_JoinStrategy extends TransformCopy
{
    public TransformSE_JoinStrategy()
    {}

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right)
    {
        boolean canDoLinear = false;
        Op effectiveRight = right;
        if (right instanceof OpService) {
            OpService op = (OpService)right;
            ServiceOpts opts = ServiceOptsSE.getEffectiveService(op);
            canDoLinear = opts.containsKey(ServiceOptsSE.SO_LOOP);
            if (canDoLinear) {
                String loopMode = opts.getFirstValue(ServiceOptsSE.SO_LOOP, "", null);
                switch (loopMode) {
                case "":
                    NodeTransform joinVarRename = renameForImplicitJoinVars(left);
                    effectiveRight = NodeTransformLib.transform(joinVarRename, right);
                    break;
                case ServiceOptsSE.SO_LOOP_MODE_SCOPED:
                    // Nothing to do
                    effectiveRight = right;
                    break;
                default:
                    throw new RuntimeException("Unsupported loop mode: " + loopMode);
                }
            }
        }

        Op result = canDoLinear
            ? OpSequence.create(left, effectiveRight)
            : super.transform(opJoin, left, effectiveRight)
            ;

        return result;
    }

    @Override
    public Op transform(OpSequence opSequence, List<Op> elts) {
        // Accumulated visible vars
        Set<Var> visibleVarsLeft = new LinkedHashSet<>();

        OpSequence result = OpSequence.create();
        for (Op right : elts) {
            Op newOp = right;
            if (right instanceof OpService) {
                OpService op = (OpService)right;
                ServiceOpts opts = ServiceOptsSE.getEffectiveService(op);
                boolean isLoop = opts.containsKey(ServiceOptsSE.SO_LOOP);
                if (isLoop) {
                    String loopMode = opts.getFirstValue(ServiceOptsSE.SO_LOOP, "", null);
                    switch (loopMode) {
                    case "":
                        NodeTransform joinVarRename = renameForImplicitJoinVars(visibleVarsLeft);
                        newOp = NodeTransformLib.transform(joinVarRename, right);
                        break;
                    case ServiceOptsSE.SO_LOOP_MODE_SCOPED:
                        // Nothing to do
                        break;
                    default:
                        throw new RuntimeException("Unsupported loop mode: " + loopMode);
                    }
                }
            }

            // Add the now visible vars as new ones
            Set<Var> visibleVarsRight = OpVars.visibleVars(newOp);
            visibleVarsLeft.addAll(visibleVarsRight);

            result.add(newOp);
        }

        return result;
    }

//    @Override
//    public Op transform(OpDisjunction opSequence, List<Op> elts) {
//        // Accumulated visible vars
//        Set<Var> visibleVarsLeft = new LinkedHashSet<>();
//
//        OpDisjunction result = OpDisjunction.create();
//        for (Op right : elts) {
//            Op newOp = right;
//            if (right instanceof OpService) {
//                OpService op = (OpService)right;
//                ServiceOpts opts = ServiceOptsSE.getEffectiveService(op);
//                boolean isLoop = opts.containsKey(ServiceOptsSE.SO_LOOP);
//                if (isLoop) {
//                    String loopMode = opts.getFirstValue(ServiceOptsSE.SO_LOOP, "", null);
//                    switch (loopMode) {
//                    case "":
//                        NodeTransform joinVarRename = renameForImplicitJoinVars(visibleVarsLeft);
//                        newOp = NodeTransformLib.transform(joinVarRename, right);
//                        break;
//                    case ServiceOptsSE.SO_LOOP_MODE_SCOPED:
//                        // Nothing to do
//                        newOp = right;
//                        break;
//                    default:
//                        throw new RuntimeException("Unsupported loop mode: " + loopMode);
//                    }
//                }
//            }
//
//            // Add the now visible vars as new ones
//            Set<Var> visibleVarsRight = OpVars.visibleVars(newOp);
//            visibleVarsLeft.addAll(visibleVarsRight);
//
//            result.add(newOp);
//        }
//
//        return result;
//    }

    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right)
    {
        boolean canDoLinear = false;
        Op effectiveRight = right;
        if (right instanceof OpService) {
            OpService op = (OpService)right;
            ServiceOpts opts = ServiceOptsSE.getEffectiveService(op);
            canDoLinear = opts.containsKey(ServiceOptsSE.SO_LOOP);
            if (canDoLinear) {
                String loopMode = opts.getFirstValue(ServiceOptsSE.SO_LOOP, "", null);
                switch (loopMode) {
                case "":
                    NodeTransform joinVarRename = renameForImplicitJoinVars(left);
                    effectiveRight = NodeTransformLib.transform(joinVarRename, right);

                    ExprList joinExprs = opLeftJoin.getExprs();
                    if (joinExprs != null) {
                        ExprList effectiveExprs = NodeTransformLib.transform(joinVarRename, joinExprs);
                        effectiveRight = OpFilter.filterBy(effectiveExprs, effectiveRight);
                    }
                    break;
                case ServiceOptsSE.SO_LOOP_MODE_SCOPED:
                    // Nothing to do
                    break;
                default:
                    throw new RuntimeException("Unsupported loop mode: " + loopMode);
                }
            }
        }

        Op result = canDoLinear
                ? new OpConditional(left, effectiveRight)
                : super.transform(opLeftJoin, left, effectiveRight)
                ;

        return result;
    }

    /**
     * Remove scoping of all mentioned rhs variables which implicitly join with those visible on the lhs:
     *
     * Join on all variables v that are visible in lhs where
     * there exists a mentioned variable v' in rhs where reverseRename(v) == reverseRename(v')
     */
    public static NodeTransform renameForImplicitJoinVars(Op left) {
        Set<Var> visibleInLhs = OpVars.visibleVars(left);
        return renameForImplicitJoinVars(visibleInLhs);
    }

    public static NodeTransform renameForImplicitJoinVars(Set<Var> visibleInLhs) {
        // Is it possible to have multiple _visible_ variables that map to same variable when reverse-renamed?!
        // The code assumes no
        Map<Var, Var> lhsPlainToScoped = visibleInLhs.stream()
                .collect(Collectors.toMap(
                        v -> (Var)Rename.reverseVarRename(v),
                        v -> v));

        Map<Node, Node> cache = new HashMap<>();
        NodeTransform joinVarRename = n -> {
            Node plain = cache.computeIfAbsent(n, Rename::reverseVarRename);
            Var scopedLhs = lhsPlainToScoped.get(plain);
            Node r = scopedLhs == null ? n : scopedLhs;
            return r;
        };

        return joinVarRename;
    }
}
