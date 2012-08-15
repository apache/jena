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

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Super class for operators that combine two sub-operators */

public abstract class Op2 extends OpBase
{
    private Op left ;
    private Op right ;

    public Op2(Op left, Op right)
    {
        this.left = left ; this.right = right ;
    }
    
    public Op getLeft() { return left ; }
    public Op getRight() { return right ; }

    public abstract Op apply(Transform transform, Op left, Op right) ;
    public abstract Op2 copy(Op left, Op right) ;

    @Override
    public int hashCode()
    {
        return left.hashCode()<<1 ^ right.hashCode() ^ getName().hashCode() ;
    }
    
    // equalsTo worker
    protected final boolean sameArgumentsAs(Op2 op2, NodeIsomorphismMap labelMap)
    {
        return left.equalTo(op2.left, labelMap) && 
               right.equalTo(op2.right, labelMap) ;
    }
}
