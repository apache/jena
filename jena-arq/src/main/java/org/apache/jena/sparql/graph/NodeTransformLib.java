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

package org.apache.jena.sparql.graph;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Objects;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.algebra.table.TableData ;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.ExprTransform;

public class NodeTransformLib
{
    /** Do a node{@literal ->}node conversion of an Op - return original BGP for "no change" */
    public static Op transform(NodeTransform nodeTransform, Op op) {
        Transform opTransform = new NodeTransformOp(nodeTransform) ;
        ExprTransform exprTransform = new NodeTransformExpr(nodeTransform) ;
        return Transformer.transform(opTransform, exprTransform, op);
    }
    
    /** Do a node{@literal ->}node conversion of a BGP - return original BGP for "no change" */
    public static BasicPattern transform(NodeTransform nodeTransform, BasicPattern pattern) {
        BasicPattern bgp2 = new BasicPattern() ;
        boolean changed = false ;
        for ( Triple triple : pattern )
        {
            Triple t2 = transform(nodeTransform, triple) ;
            bgp2.add(t2) ;
            if ( t2 != triple )
                changed = true ;
        }
        if ( ! changed )
            return pattern ;
        return bgp2 ;
    }

    /** Do a node{@literal ->}node conversion of a QuadPattern - return original QuadPattern for "no change" */
    public static QuadPattern transform(NodeTransform nodeTransform, QuadPattern pattern) {
        QuadPattern qp2 = new QuadPattern() ;
        boolean changed = false ;
        for ( Quad quad : pattern )
        {
            Quad q2 = transform(nodeTransform, quad) ;
            qp2.add(q2) ;
            if ( q2 != quad )
                changed = true ;
        }
        if ( ! changed )
            return pattern ;
        return qp2 ;
    }

    /** Do a node{@literal ->}node conversion of a Triple - return original Triple for "no change" */
    public static Triple transform(NodeTransform nodeTransform, Triple triple) {
        boolean change = false ;
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        Node s1 = nodeTransform.apply(s) ;
        if ( s1 != s ) { change = true ; s = s1 ; }
        Node p1 = nodeTransform.apply(p) ;
        if ( p1 != p ) { change = true ; p = p1 ; }
        Node o1 = nodeTransform.apply(o) ;
        if ( o1 != o ) { change = true ; o = o1 ; }
    
        if ( ! change )
            return triple ;
        return new Triple(s,p,o) ;
    }

    /** Do a node{@literal ->}node conversion of a Quad - return original Quad for "no change" */
    public static Quad transform(NodeTransform nodeTransform, Quad quad) {
        boolean change = false ;
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;
        
        Node g1 = nodeTransform.apply(g) ;
        if ( g1 != g ) { change = true ; g = g1 ; }
        Node s1 = nodeTransform.apply(s) ;
        if ( s1 != s ) { change = true ; s = s1 ; }
        Node p1 = nodeTransform.apply(p) ;
        if ( p1 != p ) { change = true ; p = p1 ; }
        Node o1 = nodeTransform.apply(o) ;
        if ( o1 != o ) { change = true ; o = o1 ; }
    
        if ( ! change )
            return quad ;
        return new Quad(g,s,p,o) ;
    }
    
    public static Table transform(Table table, NodeTransform transform) {
        // Non-streaming rewrite 
        List<Var> vars = transformVars(transform, table.getVars()) ;
        Iterator<Binding> iter = table.rows() ; 
        List<Binding> newRows = new ArrayList<>() ;
        for ( ; iter.hasNext() ; ) { 
            Binding b = iter.next() ;
            Binding b2 = transform(b, transform) ;
            newRows.add(b2) ;
        }
        return new TableData(vars, newRows) ;
    }
    
    public static Binding transform(Binding b, NodeTransform transform) {
        BindingMap b2 = BindingFactory.create() ;
        List<Var> vars = Iter.toList(b.vars()) ;
        for ( Var v : vars ) {
            Var v2 = (Var)transform.apply(v) ;
            b2.add(v2, b.get(v));
        }
        return b2 ;
    }



    /** Do a node{@literal ->}node conversion of a List&lt;Quad&gt; - return original List&lt;Quad&gt; for "no change" */
    public static List<Quad> transformQuads(NodeTransform nodeTransform, List<Quad> quads) {
        List<Quad> x = new ArrayList<>() ;
        boolean changed = false ; 
        for ( Quad q : quads )
        {
            Quad q2 = NodeTransformLib.transform(nodeTransform, q) ;
            if ( q != q2 )
                changed = true ;
            x.add(q2) ;
        }
        if ( ! changed )
            return quads ;
        return x ;
    }
    
    /** Apply the NodeTransform to the variables of a VarExprList. */
    public static VarExprList transformVars(NodeTransform transform, VarExprList varExprList) {
        VarExprList varExprList2 = new VarExprList() ;
        boolean changed = false ;
        // Just variables.
        for ( Var v : varExprList.getVars() ) {
            Expr expr = varExprList.getExpr(v) ;
            Var v2 = (Var)transform.apply(v) ;
            if ( ! Objects.equals(v, v2) )
                changed = true ;
            varExprList2.add(v2, expr) ;
        }
        if ( ! changed )
            return varExprList ; 
        return varExprList2 ;
    }

    public static List<Var> transformVars(NodeTransform nodeTransform, List<Var> varList) {
        List<Var> varList2 = new ArrayList<>(varList.size()) ;
        boolean changed = false ;
        for ( Var v : varList ) {
            Var v2 = (Var)nodeTransform.apply(v) ;
            varList2.add(v2) ;
            if ( !Objects.equals(v, v2) )
                changed = true ;
        }
        if ( ! changed )
            return varList ; 
        return varList2 ;
    }

    public static ExprList transform(NodeTransform nodeTransform, ExprList exprList) {
          ExprList exprList2 = new ExprList() ;
          boolean changed = false ;
          for(Expr expr : exprList) {
              Expr expr2 = transform(nodeTransform, expr) ;
              if ( expr != expr2 )
                  changed = true ;
              exprList2.add(expr2) ;
          }
          if ( ! changed ) return exprList ;
          return exprList2 ;
    }

    public static Expr transform(NodeTransform nodeTransform, Expr expr) {
        return expr.applyNodeTransform(nodeTransform) ;
    }
}