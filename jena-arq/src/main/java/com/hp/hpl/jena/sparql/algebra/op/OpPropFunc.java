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

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Property functions (or any OpBGP replacement)
 *  Execution will be per-engine specific */
public class OpPropFunc extends Op1
{
    // c.f. OpProcedure which is similar except for the handling of arguments.
    // Safer to have two (Ops are mainly abstract syntax, not executional).
    private Node uri ;
    private PropFuncArg subjectArgs ;
    private PropFuncArg objectArgs2 ;

    public OpPropFunc(Node uri, PropFuncArg args1 , PropFuncArg args2, Op op)
    {
        super(op) ;
        this.uri = uri ;
        this.subjectArgs = args1 ;
        this.objectArgs2 = args2 ;
    }
    
    public PropFuncArg getSubjectArgs()
    {
        return subjectArgs ;
    } 
    
    public PropFuncArg getObjectArgs()
    {
        return objectArgs2 ;
    } 
    
    @Override
    public Op apply(Transform transform, Op subOp)
    {
        return transform.transform(this, subOp) ;
    }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    public Node getProperty() { return uri ; }
    
    @Override
    public Op1 copy(Op op)
    {
        return new OpPropFunc(uri, subjectArgs, objectArgs2, op) ;
    }

    @Override
    public int hashCode()
    {
        return uri.hashCode() ^ getSubOp().hashCode() ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpPropFunc ) ) return false ;
        OpPropFunc procFunc = (OpPropFunc)other ;
        
        
        return getSubOp().equalTo(procFunc.getSubOp(), labelMap) ;
    }

    @Override
    public String getName()
    {
        return Tags.tagPropFunc ;
    }
}
