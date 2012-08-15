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
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpNull extends Op0
{
    // Only really need one.
    public static OpNull create() { return new OpNull() ; }
    
    private OpNull() { }
    
    @Override
    public Op apply(Transform transform)
    { return transform.transform(this) ; }

    @Override
    public Op0 copy() { return this ; }

    @Override
    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }

    @Override
    public String getName()
    {
        return Tags.tagNull ;
    }

    @Override
    public int hashCode()
    { return OpBase.HashNull ; }


    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    { return ( other instanceof OpNull ) ; }
}
