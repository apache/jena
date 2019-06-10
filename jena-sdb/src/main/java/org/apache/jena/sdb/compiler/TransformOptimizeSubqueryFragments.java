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

package org.apache.jena.sdb.compiler;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.BasicPattern;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * For large datasets, the performance of the OPTIONAL joins and MINUS in SQL can suffer, as the subqueries
 * generate large views which are expensive to join.
 *
 * This optimizer transforms the algebra to insert the restrictions from outside of the optional subquery,
 * reducing the amount of data that has to be joined.
 *
 * By reducing the amount of data that is considered in the LEFT JOIN/MINUS operations, the
 * efficiency and performance of such constructs is greatly improved in most cases.
 */
public class TransformOptimizeSubqueryFragments extends TransformCopy {
    private Deque<Op> tracker;

    /**
     * Static method to run the transformer (needs to keep track of visited nodes)
     */
    public static Op transform(Op op) {
        final Deque<Op> stack = new ArrayDeque<>();
        OpVisitor before = new Pusher(stack);
        OpVisitor after = new Popper(stack);
        return Transformer.transform(new TransformOptimizeSubqueryFragments(stack), op, before, after);
    }

    private TransformOptimizeSubqueryFragments(Deque<Op> tracker) {
        this.tracker = tracker;
    }

    /**
     * Run transformation on joins
     */
    @Override
    public Op transform(OpJoin opJoin, Op opLeft, Op opRight) {
        OpTransformer transformer = new OpTransformer(opJoin, opLeft, opRight);
        return super.transform(opJoin, transformer.opLeft, transformer.opRight);
    }

    /**
     * Run transformation on leftjoins
     */
    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op opLeft, Op opRight) {
        OpTransformer transformer = new OpTransformer(opLeftJoin, opLeft, opRight);
        return super.transform(opLeftJoin, transformer.opLeft, transformer.opRight);
    }

    /**
     * Run transformation on minus
     */
    @Override
    public Op transform(OpMinus opMinus, Op opLeft, Op opRight) {
        OpTransformer transformer = new OpTransformer(opMinus, opLeft, opRight);
        return super.transform(opMinus, transformer.opLeft, transformer.opRight);
    }

    /**
     * Run transformation on unions
     */
    @Override
    public Op transform(OpUnion opUnion, Op opLeft, Op opRight) {
        OpTransformer transformer = new OpTransformer(opUnion, opLeft, opRight);
        return super.transform(opUnion, transformer.opLeft, transformer.opRight);
    }

    /**
     * Helper class to perform the transformation, as it is used for OpJoin, OpLeftJoin and OpMinus
     */
    private class OpTransformer {
        private Op opCurrent;
        private Op opLeft;
        private Op opRight;

        OpTransformer(Op opCurrent, Op opLeft, Op opRight) {
            // Store the parameters
            this.opCurrent = opCurrent;
            this.opLeft = opLeft;
            this.opRight = opRight;

            // Run the transformation
            transform();
        }

        private void transform() {
            // Holding place for any triples that might need to be added to the pattern,
            // But won't know until a later op in the tracker is processed
            BasicPattern potentialAdditions = new BasicPattern();

            if (tracker.contains(opCurrent)) {
                // Add statements from left to right, unless left is table op
                if (!(opLeft instanceof OpTable)) {
                    opRight = addStatements(opRight, opLeft, potentialAdditions);
                }
            }

            // If we have previous operations in the tracker
            if (needToProcess()) {
                // Iterate through the tracker
                Iterator<Op> iter = tracker.iterator();
                while (iter.hasNext()) {
                    Op op = iter.next();
                    // As long as the operation is not the one being transformed
                    if (op != opCurrent) {
                        // Copy statements from previous operations into the subpatterns
                        opLeft = addStatements(opLeft, op, potentialAdditions);
                        opRight = addStatements(opRight, op, potentialAdditions);
                    }
                }
            }
        }

        /**
         * Determine if there are entries in the tracker that need to be processed
         */
        private boolean needToProcess() {
            // If the current op is in the tracker
            if (tracker.contains(opCurrent)) {
                // Ensure there are at least 2 tracked items
                return tracker.size() > 1;
            }

            // The current op isn't tracked, so there needs to be at least one entry.
            return tracker.size() > 0;

        }

        // Add statements from src innto dest
        private Op addStatements(Op dest, Op src, BasicPattern potentialAdditions) {
            // Only needed if destination is a Basic Graph Pattern
            if (OpBGP.isBGP(dest)) {
                if (src instanceof Op2) {
                    // Get the left side of an Op2 from src
                    Op opLeft = ((Op2) src).getLeft();

                    // If the left side is a Basic Graph Pattern
                    if (OpBGP.isBGP(opLeft)) {
                        // Copy statements from the Basic Graph Pattern to the destination
                        dest = addStatements(dest, opLeft, potentialAdditions);
                    } else {
                        // Not a Basic Graph Pattern
                        // Determine if we should copy statements from the right hand side
                        if (shouldRecurseRight((Op2) src)) {
                            // Add statements from the right hand side of the op to the destination
                            dest = addStatements(dest, ((Op2) src).getRight(), potentialAdditions);
                        }

                        // Determine if we should copy statements from the left hand side
                        if (shouldRecurseLeft(dest, src)) {
                            // Add statements from the left hand side of the op to the destination
                            dest = addStatements(dest, opLeft, potentialAdditions);
                        }
                    }
                } else if (src instanceof Op1) {
                    if (!(src instanceof OpGraph)) {
                        dest = addStatements(dest, ((Op1) src).getSubOp(), potentialAdditions);
                    }
                } else if (OpBGP.isBGP(src)) {
                    // If the source is a Basic Graph Pattern we need to copy matching statements into the destination
                    BasicPattern destP = new BasicPattern( ((OpBGP)dest).getPattern() );
                    BasicPattern srcP  = ((OpBGP)src).getPattern();

                    // See if there are any potential additions to consider
                    if (potentialAdditions != null && potentialAdditions.size() > 0) {
                        // Get variables names in the source pattern
                        Set<String> srcNames = getVariableNames(srcP);
                        for (Triple potentialT : potentialAdditions) {
                            // If the potential triple is referenced in the source
                            if (srcNames.contains(potentialT.getObject().getName())) {
                                // Add it to the destination
                                addToPatternIfNotPresent(destP, potentialT);
                            }
                        }
                    }

                    // Record the current size of dest, so that we can check if any triples have been added
                    // after all loops are complete
                    int initialCount = destP.size();
                    int loopCount;
                    do {
                        // Record the current size of the destination pattern
                        loopCount = destP.size();

                        // Get any variable names that are in the destination
                        Set<String> destNames = getVariableNames(destP);

                        // Iterate through all of the statements in the source BGP
                        for (Triple srcT : srcP) {
                            if (srcT.getSubject().isVariable() && destNames.contains(srcT.getSubject().getName())) {
                                // If the subject in the source is a variable
                                // and the name is in the list of destination variables
                                if (srcT.getObject().isVariable()) {
                                    // And the object in the source is a variable
                                    // Check that the name binds to the destination
                                    if (destNames.contains(srcT.getObject().getName())) {
                                        // Add the triple to the destination if it is not already present
                                        addToPatternIfNotPresent(destP, srcT);
                                    } else {
                                        // Not currently bound to the destination, so add to the potential list
                                        addToPatternIfNotPresent(potentialAdditions, srcT);
                                    }
                                } else {
                                    // The object is a literal or resource
                                    // So add the triple to the destination if it is not already present
                                    addToPatternIfNotPresent(destP, srcT);
                                }
                            } else if (srcT.getObject().isVariable()) {
                                // If the object is a variable
                                // Check the name is in the list of destination variables
                                if (destNames.contains(srcT.getObject().getName())) {
                                    addToPatternIfNotPresent(destP, srcT);
                                } else {
                                    // Not currently bound to the destination, so add to the potential list
                                    addToPatternIfNotPresent(potentialAdditions, srcT);
                                }
                            }
                        }

                        // Loop if we have added any statements
                    } while (loopCount < destP.size());

                    // Only create a new OpBGP if we have added statements
                    if (initialCount < destP.size()) {
                        return new OpBGP(destP);
                    }
                }
            }

            // Return the destination
            return dest;
        }

        /**
         * Add a triple to the pattern if not already present
         */
        private void addToPatternIfNotPresent(BasicPattern pattern, Triple triple) {
            if (pattern != null) {
                if (!isInPattern(pattern, triple)) {
                    pattern.add(triple);
                }
            }
        }

        /**
         * Can we recurse the left side of the root op
         */
        private boolean shouldRecurseLeft(Op current, Op root) {
            // If the operation is a join, leftjoin or minus
            // We need to parse the Op if the the current operator is not immediately to the left or right of the root
            if (root instanceof OpJoin) {
                return current != ((OpJoin)root).getLeft() && current != ((OpJoin)root).getRight();
            } else if (root instanceof OpLeftJoin) {
                return current != ((OpLeftJoin)root).getLeft() && current != ((OpLeftJoin)root).getRight();
            } else if (root instanceof OpMinus) {
                return current != ((OpMinus)root).getLeft() && current != ((OpMinus)root).getRight();
            }

            // Not an operator that we are considering, so return false
            return false;
        }

        /**
         * Can we recurse the right side of the op
         */
        private boolean shouldRecurseRight(Op2 op) {
            // If the operator is a join
            if (op instanceof OpJoin) {
                Op opLeft = op.getLeft();
                // If the left side is an op2, and the left side of it is NOT an OpTable
                if (opLeft instanceof Op2) {
                    return (!(opLeft instanceof OpTable) && !(opLeft instanceof OpGraph));
                }
            }

            return false;
        }

        /**
         * Test whether the triple is currently in the basic pattern
         */
        private boolean isInPattern(BasicPattern bp, Triple t) {
            for (Triple bpt : bp) {
                if (bpt.matches(t)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Extract any variable names that are subjects in the basic pattern
         */
        private Set<String> getVariableNames(BasicPattern bp) {
            final Set<String> names = new HashSet<>();

            for (Triple triple : bp) {
                if (triple.getSubject().isVariable()) {
                    names.add(triple.getSubject().getName());
                }

                if (triple.getObject().isVariable()) {
                    names.add(triple.getObject().getName());
                }
            }
            return names;
        }
    }

    private static class Pusher extends OpVisitorBase {
        Deque<Op> stack ;

        Pusher(Deque<Op> stack) {
            this.stack = stack;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin) {
            stack.push(opLeftJoin);
        }

        @Override
        public void visit(OpJoin opJoin) {
            stack.push(opJoin);
        }

        @Override
        public void visit(OpMinus opMinus) {
            stack.push(opMinus);
        }
    }

    private static class Popper extends OpVisitorBase {
        Deque<Op> stack ;

        Popper(Deque<Op> stack) {
            this.stack = stack;
        }

        @Override
        public void visit(OpLeftJoin opLeftJoin) {
            stack.pop();
        }

        @Override
        public void visit(OpJoin opJoin) {
            stack.pop();
        }

        @Override
        public void visit(OpMinus opMinus) {
            stack.pop();
        }
    }
}
