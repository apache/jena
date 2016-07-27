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

import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.expr.ExprVisitor ;

/** Walk algebra and expressions, but don't walk into SERVICE */
public class WalkerVisitorSkipService extends WalkerVisitor {

    public WalkerVisitorSkipService(OpVisitor opVisitor, ExprVisitor exprVisitor, OpVisitor before, OpVisitor after) {
        super(opVisitor, exprVisitor, before, after) ;
    }
    
    @Override
    public void visit(OpService op) {
        // Visit the op itself but not the subOp. 
        if ( opVisitor != null )
            op.visit(opVisitor) ;
    }
}


