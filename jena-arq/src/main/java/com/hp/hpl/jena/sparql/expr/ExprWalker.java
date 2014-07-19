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

package com.hp.hpl.jena.sparql.expr;
/** Walk the expression tree, bottom up */
public class ExprWalker 
{
    ExprVisitor visitor ;
    
    public ExprWalker(ExprVisitor visitor)
    {
        this.visitor = visitor ;
    }
    
    public void walk(Expr expr) { expr.visit(visitor) ; }

    static public void walk(ExprVisitor visitor, Expr expr)
    {
        if ( expr == null )
            return ;
        expr.visit(new WalkerBottomUp(visitor)) ;
    }
    
//    static public void walk(ExprVisitor visitor, Expr expr)
//    { expr.visit(new WalkerTopDown(visitor)) ; }

    static class Walker extends ExprVisitorFunction
    {
        ExprVisitor visitor ;
        boolean topDown = true ;
        
        private Walker(ExprVisitor visitor, boolean topDown)
        { 
            this.visitor = visitor ;
            this.topDown = topDown ;
        }
        
        @Override
        public void startVisit() {}
        
        @Override
        protected void visitExprFunction(ExprFunction func)
        {
            if ( topDown )
                func.visit(visitor) ;    
            for ( int i = 1 ; i <= func.numArgs() ; i++ )
            {
                Expr expr = func.getArg(i) ;
                if ( expr == null )
                    // Put a dummy in, e.g. to keep the transform stack aligned.
                    NodeValue.nvNothing.visit(this) ;
                else
                    expr.visit(this) ;
            }
            if ( !topDown )
                func.visit(visitor) ;
        }
        
//        @Override
//        public void visit(ExprFunction3 func)
//        {
//            if ( topDown )
//                func.visit(visitor) ; 
//            // These are 2 or 3 args.  Put a dummy in. 
//            func.getArg1().visit(this) ;
//            func.getArg2().visit(this) ;
//            if ( func.getArg3() == null )
//                NodeValue.nvNothing.visit(this) ;
//            else
//                func.getArg3().visit(this) ;
//            if ( !topDown )
//                func.visit(visitor) ;
//        }
        
        @Override
        public void visit(ExprFunctionOp funcOp)
        { funcOp.visit(visitor) ; }
        
        @Override
        public void visit(NodeValue nv)         { nv.visit(visitor) ; }
        @Override
        public void visit(ExprVar v)            { v.visit(visitor) ; }
        @Override
        public void visit(ExprAggregator eAgg)    { eAgg.visit(visitor) ; }
        
        @Override
        public void finishVisit() { }
    }
    
    // Visit current element then visit subelements
    public static class WalkerTopDown extends Walker
    {
        private WalkerTopDown(ExprVisitor visitor)
        { super(visitor, true) ; }
    }

    // Visit current element then visit subelements
    public static class WalkerBottomUp extends Walker
    {
        private WalkerBottomUp(ExprVisitor visitor)
        { super(visitor, false) ; }
    }

}
