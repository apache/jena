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

package org.apache.jena.sparql.expr;

import org.apache.jena.sparql.algebra.walker.Walker ;

public class ExprTransformer
{
    /** Transform an expression */
    public static Expr transform(ExprTransform transform, Expr expr)
    { return Walker.transform(expr, transform) ;}

    /** Transform an expression list */
    public static ExprList transform(ExprTransform transform, ExprList exprList) {
        ExprList exprList2 = new ExprList() ;
        boolean changed = false ;
        for ( Expr e : exprList ) {
            Expr e2 = transform(transform, e) ;
            exprList2.add(e2) ;
            if ( e != e2 )
                changed = true ;
        } ;
        if ( changed )
            return exprList2 ;
        else
            return exprList ;
    }
}
