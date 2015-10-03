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

package org.apache.jena.sparql.algebra.op;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.sse.Tags ;
import org.apache.jena.sparql.util.NodeIsomorphismMap ;

public class OpSlice extends OpModifier
{
    private long start ;
    private long length ;

    public OpSlice(Op subOp, long start, long length)
    {
        super(subOp) ;
        this.start = start ;
        this.length = length ;
    }
    
    public long getLength()         { return length ; }
    public long getStart()          { return start ; } 

    public Op copy()
    {
        return null ;
    }

    @Override
    public String getName()                 { return Tags.tagSlice ; }
    @Override
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op1 copy(Op subOp)                { return new OpSlice(subOp, start, length) ; }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }
    
    @Override
    public int hashCode()
    {
        return getSubOp().hashCode() ^ (int)(start&0xFFFFFFFF) ^ (int)(length&0xFFFFFFFF) ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpSlice) ) return false ;
        OpSlice opSlice = (OpSlice)other ;
        if ( opSlice.start != start || opSlice.length != length )
            return false;
        return getSubOp().equalTo(opSlice.getSubOp(), labelMap) ;
    }
}
