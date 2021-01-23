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

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;

import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Literal ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.ExprTransform ;
import org.apache.jena.sparql.graph.NodeTransform ;
import org.apache.jena.sparql.modify.request.* ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.update.Update ;
import org.apache.jena.update.UpdateRequest ;

/** Support for transformation of update abstract syntax. */ 
public class UpdateTransformOps {
    
    /** Transform an {@link Update} based on a mapping from {@link Var} variable to replacement {@link Node}. */ 
    public static Update transform(Update update, Map<Var, Node> substitutions) {
        ElementTransform eltrans = new ElementTransformSubst(substitutions) ;
        NodeTransform nodeTransform = new NodeTransformSubst(substitutions) ;
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans) ;
        return transform(update, eltrans, exprTrans) ;
    }

    /** Transform an {@link UpdateRequest} based on a mapping from {@link Var} variable to replacement {@link Node}. */ 
    public static UpdateRequest transform(UpdateRequest update, Map<Var, Node> substitutions) {
        ElementTransform eltrans = new ElementTransformSubst(substitutions) ;
        NodeTransform nodeTransform = new NodeTransformSubst(substitutions) ;
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans) ;
        return transform(update, eltrans, exprTrans) ;
    }

    /**
     * Transform an {@link Update} based on a mapping from variable name to replacement
     * {@link RDFNode} (a {@link Resource} (or blank node) or a {@link Literal}).
     */
    public static Update transformUpdate(Update update, Map<String, ? extends RDFNode> substitutions) {
        Map<Var, Node> map = TransformElementLib.convert(substitutions);
        return transform(update, map);
    }

    /**
     * Transform an {@link UpdateRequest} based on a mapping from variable name to replacement
     * {@link RDFNode} (a {@link Resource} (or blank node) or a {@link Literal}).
     */
    public static UpdateRequest transformUpdate(UpdateRequest update, Map<String, ? extends RDFNode> substitutions) {
        Map<Var, Node> map = TransformElementLib.convert(substitutions);
        return transform(update, map);
    }
    
    public static Update transform(Update update, ElementTransform transform, ExprTransform exprTransform) {
        UpdateTransform upParam = new UpdateTransform(transform, exprTransform) ;
        update.visit(upParam) ;
        Update update1 = upParam.result ;
        return update1 ;
    }

    public static UpdateRequest transform(UpdateRequest update, ElementTransform transform, ExprTransform exprTransform) {
        UpdateRequest req = new UpdateRequest() ;
        req.getPrefixMapping().setNsPrefixes(update.getPrefixMapping()) ;
        
        for (Update up : update.getOperations()) {
            up = transform(up, transform, exprTransform) ;
            req.add(up) ;
        }

        return req ;
    }

    static class UpdateTransform implements UpdateVisitor {
        ElementTransform elTransform ;
        ExprTransform    exprTransform ;
        Update           result = null ;

        public UpdateTransform(ElementTransform transform, ExprTransform exprTransform) {
            this.elTransform = transform ;
            this.exprTransform = exprTransform ;
        }

        @Override
        public void visit(UpdateDrop update) {
            result = update ;
        }

        @Override
        public void visit(UpdateClear update) {
            result = update ;
        }

        @Override
        public void visit(UpdateCreate update) {
            result = update ;
        }

        @Override
        public void visit(UpdateLoad update) {
            result = update ;
        }

        @Override
        public void visit(UpdateAdd update) {
            result = update ;
        }

        @Override
        public void visit(UpdateCopy update) {
            result = update ;
        }

        @Override
        public void visit(UpdateMove update) {
            result = update ;
        }

        @Override
        public void visit(UpdateDataInsert update) {
            result = update ;
        }

        @Override
        public void visit(UpdateDataDelete update) {
            result = update ;
        }

        @Override
        public void visit(UpdateDeleteWhere update) {
            List<Quad> quads = update.getQuads() ;
            List<Quad> quads2 = transform(quads) ;
            if ( quads == quads2 )
                result = update ;
            else {
                QuadAcc acc = new QuadAcc() ;
                addAll(acc, quads2) ;
                result = new UpdateDeleteWhere(acc) ;
            }
        }

        @Override
        public void visit(UpdateModify update) {
            Element el = update.getWherePattern() ;
            Element el2 = ElementTransformer.transform(el, elTransform, exprTransform) ;

            List<Quad> del = update.getDeleteQuads() ;
            List<Quad> del1 = transform(del) ;
            List<Quad> ins = update.getInsertQuads() ;
            List<Quad> ins1 = transform(ins) ;

            UpdateModify mod = new UpdateModify() ;

            addAll(mod.getDeleteAcc(), del1) ;
            addAll(mod.getInsertAcc(), ins1) ;
            mod.setElement(el2); 
            result = mod ;
        }

        private void addAll(QuadAcc acc, List<Quad> quads) {
            for (Quad q : quads)
                acc.addQuad(q) ;
        }

        public List<Quad> transform(List<Quad> quads) {
            List<Quad> x = new ArrayList<>() ;
            boolean changed = false ;
            for (Quad q : quads) {
                Quad q1 = transform(q) ;
                changed = changed || q1 != q ;
                x.add(q1) ;
            }
            if ( changed )
                return x ;
            return quads ;
        }

        private Quad transform(Quad q) {
            Node g = q.getGraph() ;
            Node g1 = transform(g) ;
            Node s = q.getSubject() ;
            Node s1 = transform(s) ;
            Node p = q.getPredicate() ;
            Node p1 = transform(p) ;
            Node o = q.getObject() ;
            Node o1 = transform(o) ;
            if ( g == g1 && s == s1 && p == p1 && o == o1 )
                return q ;
            return Quad.create(g1, s1, p1, o1) ;
        }

        private Node transform(Node n) {
            if ( Var.isVar(n) )
                return TransformElementLib.apply(Var.alloc(n), exprTransform) ;
            else
                return TransformElementLib.apply(n, exprTransform) ;
        }
    }

}
