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

package com.hp.hpl.jena.sparql.modify.request;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;


public class UpdateCreate extends Update
{
    protected final Node graphRef ;
    protected final boolean silent ;
    
    public UpdateCreate(String s)
    {
        this(NodeFactory.createURI(s), false) ;
    }
    
    public UpdateCreate(String s, boolean silent)
    {
        this(NodeFactory.createURI(s), silent) ;
    }
    
    public UpdateCreate(Node iri)
    {
        this(iri, false) ;
    }
    
    public UpdateCreate(Node iri, boolean silent)
    { 
        this.graphRef = iri ; 
        this.silent = silent ;
    }
    
    public boolean isSilent()   { return silent ; }
    
    public Node getGraph()      { return graphRef ; }


    @Override
    public void visit(UpdateVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public boolean equalTo(Update obj, NodeIsomorphismMap isoMap) {
        if (this == obj)
            return true ;
        if (obj == null)
            return false ;
        if (getClass() != obj.getClass())
            return false ;
        UpdateCreate other = (UpdateCreate)obj ;
        return silent == other.silent &&
               isoMap.makeIsomorphic(graphRef, other.graphRef) ; 
    }
}
