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
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.syntax.ElementService ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpService extends Op1
{
    private final Node serviceNode ;
    private final ElementService serviceElement ;  
    private final boolean silent ;
    
    public OpService(Node serviceNode, Op subOp, boolean silent)
    {
        this(serviceNode, subOp, null, silent) ;
    }

    public OpService(Node serviceNode, Op subOp, ElementService elt, boolean silent)
    {
        super(subOp) ;
        this.serviceNode = serviceNode ;
        this.serviceElement = elt ;
        this.silent = silent ;
    }
    
    @Override
    public Op apply(Transform transform, Op subOp)  { return transform.transform(this, subOp) ; }

    @Override
    public Op1 copy(Op newOp)                    { return new OpService(serviceNode, newOp, silent) ; }
    @Override
    public String getName()                     { return Tags.tagService ; }
    @Override
    public void visit(OpVisitor opVisitor)      { opVisitor.visit(this) ; }
    public Node getService()                    { return serviceNode ;  }
    public ElementService getServiceElement()   { return serviceElement ;  }
    public boolean getSilent()                  { return silent ; } 

    @Override
    public int hashCode()
    { return serviceNode.hashCode() ^ getSubOp().hashCode() ; }
    
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpService) )
            return false ;
        OpService opService = (OpService)other ;
        if ( ! ( serviceNode.equals(opService.serviceNode) ) )
            return false ;
        if ( opService.getSilent() != getSilent() )
            return false ;
        return getSubOp().equalTo(opService.getSubOp(), labelMap) ;
    }

}
