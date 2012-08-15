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

/** Super class for operators that operate on a single sub-operation (i.e. a table or sequence))*/

public abstract class Op1 extends OpBase
{
    private Op sub;

    public Op1(Op subOp)
    {
        this.sub = subOp ;
    }
    
    public Op getSubOp() { return sub ; }
    //public void setSubOp(Op op) { sub = op ; }
    
    public abstract Op apply(Transform transform, Op subOp) ;
    public abstract Op1 copy(Op subOp) ;
}
