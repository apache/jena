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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpMinus;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;

/**
 * Optimizer that ensures that <strong>table empty</strong> is promoted as high
 * up a query as is possible
 * <p>
 * <strong>table empty</strong> is an operator that may be introduced into the
 * algebra by other optimizations and represents the case where the optimizer
 * can a priori determine that some portion of a query will produce no results.
 * The classic example of this are {@code FILTER} clauses that may be determined
 * to always return false and thus negate the need to evaluate their inner
 * operations.
 * </p>
 * <p>
 * Other optimizers introduce <strong>table empty</strong> at the point in the
 * query where they are optimizing, often its presence in a query may render a
 * larger portion of even the entire query superfluous so this optimizer is
 * designed to promote it up through the query as necessary.
 * </p>
 * <p>
 * As detailed below this is not guaranteed to eliminate all portions of a query
 * rather it aims to eliminate portions where not doing so can cause expensive
 * and unnecessary evaluation to happen e.g. evaluating the left hand side of a
 * join (which may itself be a deeply nested operator) only to join it with
 * table empty and thus discard all the work that had been done.
 * </p>
 * <h3>Table Empty Promotions</h3>
 * <p>
 * The optimizer makes the following promotions:
 * </p>
 * <ul>
 * <li>Graph over table empty => table empty</li>
 * <li>Assign/Extend over table empty => table empty</li>
 * <li>Join where either side is table empty => table empty</li>
 * <li>Left Join:
 * <ul>
 * <li>If LHS is table empty => table empty</li>
 * <li>If RHS is table empty => LHS</li>
 * </ul>
 * </li>
 * <li>Union:
 * <ul>
 * <li>If both sides are table empty => table empty</li>
 * <li>If one side is table empty => other side</li>
 * </ul>
 * </li>
 * <li>Minus:
 * <ul>
 * <li>If LHS is table empty => table empty</li>
 * <li>If RHS is table empty => LHS</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * All other operators are left untouched either because it cannot be promoted
 * through them or because doing so has no clear benefit since applying them
 * over table empty should be minimal work anyway.
 * </p>
 */
public class TransformPromoteTableEmpty extends TransformCopy {

    @Override
    public Op transform(OpGraph opGraph, Op subOp) {
        if (isTableEmpty(subOp)) {
            return subOp;
        }
        return super.transform(opGraph, subOp);
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        if (isTableEmpty(subOp)) {
            return subOp;
        }
        return super.transform(opAssign, subOp);
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        if (isTableEmpty(subOp)) {
            return subOp;
        }
        return super.transform(opExtend, subOp);
    }

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        // If either side is table empty return table empty
        if (isTableEmpty(left) || isTableEmpty(right)) {
            return OpTable.empty();
        }
        return super.transform(opJoin, left, right);
    }

    @Override
    public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
        // If LHS is table empty return table empty
        // If RHS is table empty can eliminate left join and just leave LHS
        if (isTableEmpty(left)) {
            return OpTable.empty();
        } else if (isTableEmpty(right)) {
            return left;
        }
        return super.transform(opLeftJoin, left, right);
    }

    @Override
    public Op transform(OpMinus opMinus, Op left, Op right) {
        // If LHS is table empty return table empty
        // If RHS is table empty can eliminate minus and just leave LHS since no
        // shared variables means no effect
        if (isTableEmpty(left)) {
            return OpTable.empty();
        } else if (isTableEmpty(right)) {
            return left;
        }
        return super.transform(opMinus, left, right);
    }

    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        // If one and only one side is table empty return other side
        // If both are table empty return table empty
        if (isTableEmpty(left)) {
            if (isTableEmpty(right)) {
                return OpTable.empty();
            } else {
                return right;
            }
        } else if (isTableEmpty(right)) {
            return left;
        }
        return super.transform(opUnion, left, right);
    }

    private boolean isTableEmpty(Op op) {
        if (op instanceof OpTable) {
            return ((OpTable) op).getTable().isEmpty();
        } else {
            return false;
        }
    }

}
