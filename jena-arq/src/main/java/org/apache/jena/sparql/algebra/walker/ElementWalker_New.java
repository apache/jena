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

package org.apache.jena.sparql.algebra.walker;

import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.syntax.* ;

/** An element visitor that walks the graph pattern tree for one query level. 
 *  applying a visitor at each Element traversed.<br/>
 *  Does not process subqueries.<br/>      
 *  Does not process (NOT)EXISTS in filters.<br/>
 *  These will need to call down themselves if it is meaningful for the visitor.
 *  Bottom-up walk - apply to subelements before applying to current element.
 */

public class ElementWalker_New {
    public static void walk(Element el, ElementVisitor visitor) {
        walk(el, visitor, null) ;
    }

    public static void walk(Element el, ElementVisitor elVisitor, ExprVisitor exprVisitor) {
        EltWalker w = new EltWalker(elVisitor, exprVisitor) ;
        el.visit(w) ;
    }

//    protected static void walk$(Element el, Walker walker) {
//        el.visit(walker) ;
//    }

    static public class EltWalker implements ElementVisitor, ExprVisitorFunction {
        protected final ElementVisitor elementVisitor ;
        protected final ExprVisitor exprVisitor ;

        protected EltWalker(ElementVisitor visitor, ExprVisitor exprVisitor) {
            this.elementVisitor = visitor ;
            this.exprVisitor = exprVisitor ;
        }

        @Override
        public void visit(ElementTriplesBlock el) {
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementFilter el) {
            el.getExpr().visit(this);
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementAssign el) {
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementBind el) {
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementData el) {
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementUnion el) {
            for ( Element e : el.getElements() )
                e.visit(this) ;
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementGroup el) {
            for ( Element e : el.getElements() )
                e.visit(this) ;
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementOptional el) {
            if ( el.getOptionalElement() != null )
                el.getOptionalElement().visit(this) ;
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementDataset el) {
            if ( el.getElement() != null )
                el.getElement().visit(this) ;
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementNamedGraph el) {
            if ( el.getElement() != null )
                el.getElement().visit(this) ;
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementService el) {
            if ( el.getElement() != null )
                el.getElement().visit(this) ;
            elementVisitor.visit(el) ;
        }

        // EXISTs, NOT EXISTs also occur in FILTERs via expressions.

        @Override
        public void visit(ElementExists el) {
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementNotExists el) {
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementMinus el) {
            if ( el.getMinusElement() != null )
                el.getMinusElement().visit(this) ;
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementSubQuery el) {
            // This does not automatically walk into the subquery.
            elementVisitor.visit(el) ;
        }

        @Override
        public void visit(ElementPathBlock el) {
            elementVisitor.visit(el) ;
        }
        
        @Override
        public void visit(ExprFunction0 func) { visitExprFunction(func) ; }
        @Override
        public void visit(ExprFunction1 func) { visitExprFunction(func) ; }
        @Override
        public void visit(ExprFunction2 func) { visitExprFunction(func) ; }
        @Override
        public void visit(ExprFunction3 func) { visitExprFunction(func) ; }
        @Override
        public void visit(ExprFunctionN func) { visitExprFunction(func) ; }
        
        @Override
        public void visitExprFunction(ExprFunction func) {
            for ( int i = 1 ; i <= func.numArgs() ; i++ )
            {
                Expr expr = func.getArg(i) ;
                if ( expr == null )
                    // Put a dummy in, e.g. to keep the transform stack aligned.
                    Expr.NONE.visit(this) ;
                else
                    expr.visit(this) ;
            }
            func.visit(exprVisitor) ;
        }
        
        @Override
        public void visit(ExprFunctionOp funcOp) {
            // Walk the op
            funcOp.getElement().visit(this);
            funcOp.visit(exprVisitor) ;
        }
        
        @Override
        public void visit(NodeValue nv)         { nv.visit(exprVisitor) ; }
        @Override
        public void visit(ExprVar v)            { v.visit(exprVisitor) ; }
        @Override
        public void visit(ExprNone v)            { v.visit(exprVisitor) ; }
        @Override
        public void visit(ExprAggregator eAgg)  {
            //eAgg.getAggVar().visit(visitorExpr);
            // XXX XXX Hack for varsMentioned
            eAgg.visit(exprVisitor) ; 
        }
    }
}
