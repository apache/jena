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

package org.apache.jena.sparql.syntax.syntaxtransform ;

import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryVisitor ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.DatasetDescription ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.graph.NodeTransform ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.ElementGroup ;

/** Support for transformation of query abstract syntax. */ 

public class QueryTransformOps {
    public static Query transform(Query query, Map<Var, Node> substitutions) {
        ElementTransform eltrans = new ElementTransformSubst(substitutions) ;
        NodeTransform nodeTransform = new NodeTransformSubst(substitutions) ;
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans) ;
        return transform(query, eltrans, exprTrans) ;
    }

    public static Query transform(Query query, ElementTransform transform, ExprTransform exprTransform) {
        Query q2 = QueryTransformOps.shallowCopy(query) ;

        transformVarExprList(q2.getProject(), exprTransform) ;
        transformVarExprList(q2.getGroupBy(), exprTransform) ;
        // Nothing to do about ORDER BY - leave to sort by that variable.

        Element el = q2.getQueryPattern() ;
        Element el2 = ElementTransformer.transform(el, transform, exprTransform) ;
        // Top level is always a group.
        if ( ! ( el2 instanceof ElementGroup ) ) {
            ElementGroup eg = new ElementGroup() ;
            eg.addElement(el2);
            el2 = eg ;
        }
        q2.setQueryPattern(el2) ;
        return q2 ;
    }

    public static Query transform(Query query, ElementTransform transform) {
        ExprTransform noop = new ExprTransformApplyElementTransform(transform) ;  
        return transform(query, transform, noop) ;
    }

    // Mutates the VarExprList
    private static void transformVarExprList(VarExprList varExprList, ExprTransform exprTransform)
    // , final Map<Var, Node> substitutions)
    {
        Map<Var, Expr> map = varExprList.getExprs() ;

        for (Var v : varExprList.getVars()) {
            Expr e = varExprList.getExpr(v) ;
            ExprVar ev = new ExprVar(v) ;
            Expr ev2 = exprTransform.transform(ev) ;
            if ( ev != ev2 ) {
                if ( e != null )
                    throw new ARQException("Can't substitute " + v + " because it's used as an AS variable") ;
                if ( ev2 instanceof NodeValue ) {
                    // Convert to (substit value AS ?var)
                    map.put(v, ev2) ;
                    continue ;
                } else
                    throw new ARQException("Can't substitute " + v + " because it's not a simple value: " + ev2) ;
            }
            if ( e == null )
                continue ;

            // Didn't change the variable.
            Expr e2 = ExprTransformer.transform(exprTransform, e) ;
            if ( e != e2 )
                // replace
                map.put(v, e2) ;
        }
    }

    static class QueryShallowCopy implements QueryVisitor {
        final Query newQuery = new Query() ;

        QueryShallowCopy() {}

        @Override
        public void startVisit(Query query) {
            newQuery.setSyntax(query.getSyntax()) ;

            if ( query.explicitlySetBaseURI() )
                newQuery.setBaseURI(query.getPrologue().getResolver()) ;

            newQuery.setQueryResultStar(query.isQueryResultStar()) ;

            if ( query.hasDatasetDescription() ) {
                DatasetDescription desc = query.getDatasetDescription() ;
                for (String x : desc.getDefaultGraphURIs())
                    newQuery.addGraphURI(x) ;
                for (String x : desc.getDefaultGraphURIs())
                    newQuery.addNamedGraphURI(x) ;
            }
            
            // Aggregators.
            newQuery.getAggregators().addAll(query.getAggregators()) ;
        }

        @Override
        public void visitPrologue(Prologue prologue) {
            // newQuery.setBaseURI(prologue.getResolver()) ;
            PrefixMapping pmap = new PrefixMappingImpl().setNsPrefixes(prologue.getPrefixMapping()) ;
            newQuery.setPrefixMapping(pmap) ;
        }

        @Override
        public void visitResultForm(Query q) {}

        @Override
        public void visitSelectResultForm(Query query) {
            newQuery.setQuerySelectType() ;
            newQuery.setDistinct(query.isDistinct()) ;
            VarExprList x = query.getProject() ;
            for (Var v : x.getVars()) {
                Expr expr = x.getExpr(v) ;
                if ( expr == null )
                    newQuery.addResultVar(v) ;
                else
                    newQuery.addResultVar(v, expr) ;
            }
        }

        @Override
        public void visitConstructResultForm(Query query) {
            newQuery.setQueryConstructType() ;
            newQuery.setConstructTemplate(query.getConstructTemplate()) ;
        }

        @Override
        public void visitDescribeResultForm(Query query) {
            newQuery.setQueryDescribeType() ;
            for (Node x : query.getResultURIs())
                newQuery.addDescribeNode(x) ;
        }

        @Override
        public void visitAskResultForm(Query query) {
            newQuery.setQueryAskType() ;
        }

        @Override
        public void visitDatasetDecl(Query query) {}

        @Override
        public void visitQueryPattern(Query query) {
            newQuery.setQueryPattern(query.getQueryPattern()) ;
        }

        @Override
        public void visitGroupBy(Query query) {
            if ( query.hasGroupBy() ) {
                VarExprList x = query.getGroupBy() ;

                for (Var v : x.getVars()) {
                    Expr expr = x.getExpr(v) ;
                    if ( expr == null )
                        newQuery.addGroupBy(v) ;
                    else
                        newQuery.addGroupBy(v, expr) ;
                }
            }
        }

        @Override
        public void visitHaving(Query query) {
            if ( query.hasHaving() ) {
                for (Expr expr : query.getHavingExprs())
                    newQuery.addHavingCondition(expr) ;
            }
        }

        @Override
        public void visitOrderBy(Query query) {
            if ( query.hasOrderBy() ) {
                for (SortCondition sc : query.getOrderBy())
                    newQuery.addOrderBy(sc) ;
            }
        }

        @Override
        public void visitLimit(Query query) {
            newQuery.setLimit(query.getLimit()) ;
        }

        @Override
        public void visitOffset(Query query) {
            newQuery.setOffset(query.getOffset()) ;
        }

        @Override
        public void visitValues(Query query) {
            if ( query.hasValues() )
                newQuery.setValuesDataBlock(query.getValuesVariables(), query.getValuesData()) ;
        }

        @Override
        public void finishVisit(Query query) {}
    }

    public static Query shallowCopy(Query query) {
        QueryShallowCopy copy = new QueryShallowCopy() ;
        query.visit(copy) ;
        Query q2 = copy.newQuery ;
        return q2 ;
    }

}
