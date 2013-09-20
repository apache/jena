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
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** General procedure in algebra evaluation (a stored procedure facility)
 *  Syntax (ARQ extension): CALL <iri>(?x, ?y+3)
 *  
 *  See also the similar algebra form for property functions.  The difference is in argument handling.
 *  A property function has a URI and two argment lists, one for subject, one for objects.
 *  A procedure is a URI and a list of arguments. */
public class OpProcedure extends Op1
{
    private Node procId ;
    private ExprList args = null ;

    public OpProcedure(Node procId, ExprList args, Op op)
    {
        super(op) ;   
        this.args = args ;
        this.procId = procId ;
    }
    
    public OpProcedure(String iri, ExprList args, Op op)
    {
        this(NodeFactory.createURI(iri), args, op) ;
    }
    
    @Override
    public String getName()
    {
        return Tags.tagProc ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if (other == this) return true;
        if ( ! (other instanceof OpProcedure) ) return false ;
        OpProcedure proc = (OpProcedure)other ;
        
        if ( ! procId.equals(proc.procId) ) return false ;
        if ( ! args.equals(proc.args) ) return false ;
        
        return getSubOp().equalTo(proc.getSubOp(), labelMap) ;
    }

    @Override
    public int hashCode()
    {
        int x = procId.hashCode() ;
        x ^= args.hashCode() ;
        x ^= getSubOp().hashCode() ;
        return x ;
    }

    @Override
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public Op apply(Transform transform, Op subOp)
    {
        return transform.transform(this, subOp) ;
    }

    @Override
    public Op1 copy(Op subOp)
    {
        return new OpProcedure(procId, args, getSubOp()) ;
    }

    public Node getProcId()
    {
        return procId ;
    }

    public String getURI()
    {
        return procId.getURI() ;
    }
    
    public ExprList getArgs()
    {
        return args ;
    }
}
