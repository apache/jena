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
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;

public abstract class UpdateDropClear extends Update 
{
    protected final Target target ;
    protected final boolean silent ;
    
    protected UpdateDropClear(String iri, boolean silent)
    { this(Target.create(iri), silent) ; }
    
    protected UpdateDropClear(Target target, boolean silent)
    { this.target = target ; this.silent = silent ; }
    
    protected UpdateDropClear(Node target, boolean silent)
    { this(Target.create(target), silent) ; }

    public Target getTarget() { return target ; }
    public boolean isSilent() { return silent ; }
    
    public boolean isDefault()  { return target.isDefault() ; }
    public boolean isAll()      { return target.isAll() ; }
    public boolean isAllNamed() { return target.isAllNamed() ; }
    public boolean isOneGraph() { return target.isOneNamedGraph() ; }
    
    //public String getGraphIRI() { return target.getGraphIRI() ; }
    public Node getGraph()      { return target.getGraph() ; }
    
    @Override
    public boolean equalTo(Update obj, NodeIsomorphismMap isoMap) {
        if (this == obj)
            return true ;
        if (obj == null)
            return false ;
        if (getClass() != obj.getClass())
            return false ;
        UpdateDropClear other = (UpdateDropClear)obj ;
        if ( silent != other.silent )
            return false ;
        return target.equalTo(other.target, isoMap) ;
    }
}
