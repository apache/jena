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

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend ;
import com.hp.hpl.jena.sparql.core.VarExprList ;

/**
 * An optimizer that aims to combine multiple extend clauses together.
 * <p>
 * Since the semantics of extend are such that the expressions are expected to
 * be evaluated in order we can combine extends together. This can make
 * evaluation more efficient because all the assignments are done in a single
 * step though depending on the underlying store this may make little or no
 * difference.
 * </p>
 * <p>
 * Note that standard algebra construction will cause much of this to happen
 * naturally but sometimes it is useful to apply this as an additional
 * independent transform.
 * </p>
 * 
 */
public class TransformExtendCombine extends TransformCopy {

    public TransformExtendCombine() {}
    
    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        if (subOp instanceof OpAssign) {
            // If a variable is assigned twice, don't do anything.
            // (assign (?x 2)  (assign (?x 1) op)) => leave alone.
            // This is the safest option in a rare case.
            // It would be OK if addAll does a replacement without checking
            // but having it check and complain about duplicates adds robustness.
            // In OpExtend, it's actually illegal.

            OpAssign x = (OpAssign)subOp ;
            VarExprList outerVarExprList = opAssign.getVarExprList() ;
            VarExprList innerVarExprList = x.getVarExprList() ;
            
            Op r = OpAssign.assign(x.getSubOp(), innerVarExprList) ;
            // This contains an "if already assigned" test.
            r = OpAssign.assign(r, outerVarExprList) ;
            return r ;
        }
        return super.transform(opAssign, subOp);
    }

    @Override
    public Op transform(OpExtend opExtend, Op subOp) {
        if (subOp instanceof OpExtend) {
            // The case of (extend (?x e1) (extend (?x e2) ...op...))
            // is actually illegal in SPARQL.  ?x must be a fresh variable.
            OpExtend x = (OpExtend)subOp ;
            VarExprList outerVarExprList = opExtend.getVarExprList() ;
            VarExprList innerVarExprList = x.getVarExprList() ;
            // XXX Go back to "create"
            Op r = OpExtend.extend(x.getSubOp(), innerVarExprList) ;
            // This contains an "if already assigned" test.
            r = OpExtend.extend(r, outerVarExprList) ;
            return r ;
        }
        return super.transform(opExtend, subOp);
    }
}
