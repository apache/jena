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
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.InternalErrorException ;

import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprTransform ;
import org.apache.jena.sparql.expr.ExprVar ;
import org.apache.jena.sparql.expr.NodeValue ;

public class TransformElementLib {
    public static Var applyVar(Var v, ExprTransform exprTransform) {
        if ( exprTransform == null )
            return v ;
        ExprVar expr = new ExprVar(v) ;
        Expr e = exprTransform.transform(expr) ;
        if ( e instanceof ExprVar )
            return ((ExprVar)e).asVar() ;
        throw new InternalErrorException("Managed to turn a variable " + v + " into " + e) ;
    }

    public static Node apply(Node n, ExprTransform exprTransform) {
        if ( exprTransform == null )
            return n ;
        Expr e = null ;
        if ( Var.isVar(n) ) {
            Var v = Var.alloc(n) ;
            ExprVar expr = new ExprVar(v) ;
            e = exprTransform.transform(expr) ;
        } else {
            NodeValue nv = NodeValue.makeNode(n) ;
            e = exprTransform.transform(nv) ;
        }

        if ( e instanceof ExprVar )
            return ((ExprVar)e).asVar() ;
        if ( e instanceof NodeValue )
            return ((NodeValue)e).asNode() ;
        throw new InternalErrorException("Managed to turn a node " + n + " into " + e) ;
    }

    public static Map<Var, Node> convert(Map<String, ? extends RDFNode> substitutions) {
        return substitutions.entrySet().stream()
                    .collect(Collectors.toMap(e -> Var.alloc(e.getKey()),
                                              e -> e.getValue().asNode()));
    }
}
