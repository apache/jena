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

package org.apache.jena.sparql.algebra;

import java.util.Iterator ;

import org.apache.jena.sparql.algebra.op.* ;

/** Apply a visitor to the whole structure of Ops, recursively.
 *  Visit sub Op before the current level
 */

public class OpWalker
{
    // Predates org.apache.jena.sparql.algebra.walker.Walker
    // Need to convert but OpVars depends on OpWalkerVisitorFixed, OpWalkerVisitorVisible
    
    /* OpVars::
     public static void visibleVars(Op op, Set<Var> acc) {
        OpVarsPattern visitor = new OpVarsPattern(acc, true) ;
        // Does not work.
        //new WalkerVisitorVisible(visitor, null, null, null).walk(op);
        //OpWalker.walk(new OpWalkerVisitorVisible(visitor, acc), op) ;
    }
     */
    
    // **** Replace with org.apache.jena.sparql.algebra.walker.Walker
    public static void walk(Op op, OpVisitor visitor)
    {
        // new walker :: this works
        // Walker.walk(op, visitor);
        walk(new OpWalkerVisitor(visitor, null, null), op) ;
    }
    
//    public static void walk(Op op, OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor)
//    {
//        walk(new OpWalkerVisitor(visitor, beforeVisitor, afterVisitor), op) ;
//    }
    
    public static void walk(OpWalkerVisitor walkerVisitor, Op op)
    {
        // Stil needed for OpWalkerVisitorVisible, OpWalkerVisitorFixed
        op.visit(walkerVisitor) ;
    }
    
    protected static class OpWalkerVisitor extends OpVisitorByType {
        private final OpVisitor   beforeVisitor ;
        private final OpVisitor   afterVisitor ;
        protected final OpVisitor visitor ;

        public OpWalkerVisitor(OpVisitor visitor, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
            this.visitor = visitor ;
            this.beforeVisitor = beforeVisitor ;
            this.afterVisitor = afterVisitor ;
        }

        public OpWalkerVisitor(OpVisitor visitor) {
            this(visitor, null, null) ;
        }

        protected final void before(Op op) {
            if ( beforeVisitor != null )
                op.visit(beforeVisitor) ;
        }

        protected final void after(Op op) {
            if ( afterVisitor != null )
                op.visit(afterVisitor) ;
        }

        @Override
        protected void visit0(Op0 op) {
            before(op) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visit1(Op1 op) {
            before(op) ;
            if ( op.getSubOp() != null )
                op.getSubOp().visit(this) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visitFilter(OpFilter op) {
            visit1(op) ;
        }

        @Override
        protected void visitLeftJoin(OpLeftJoin op) {
            visit2(op) ;
        }

        @Override
        protected void visit2(Op2 op) {
            before(op) ;
            if ( op.getLeft() != null )
                op.getLeft().visit(this) ;
            if ( op.getRight() != null )
                op.getRight().visit(this) ;
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visitN(OpN op) {
            before(op) ;
            for (Iterator<Op> iter = op.iterator(); iter.hasNext();) {
                Op sub = iter.next() ;
                sub.visit(this) ;
            }
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }

        @Override
        protected void visitExt(OpExt op) {
            before(op) ;
            if ( op.effectiveOp() != null )
                // Walk the effective op, if present.
                op.effectiveOp().visit(this);
            if ( visitor != null )
                op.visit(visitor) ;
            after(op) ;
        }
    }
}
