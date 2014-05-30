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

import static com.hp.hpl.jena.sparql.modify.request.Target.Decl.ALL$ ;
import static com.hp.hpl.jena.sparql.modify.request.Target.Decl.DEFAULT$ ;
import static com.hp.hpl.jena.sparql.modify.request.Target.Decl.IRI$ ;
import static com.hp.hpl.jena.sparql.modify.request.Target.Decl.NAMED$ ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class Target
{
    static enum Decl { DEFAULT$, NAMED$, ALL$, IRI$}
    
    public static final Target DEFAULT = new Target(DEFAULT$) ;
    public static final Target NAMED = new Target(NAMED$) ;
    public static final Target ALL = new Target(ALL$) ;

    public static Target create(String iri)
    { return new Target(NodeFactory.createURI(iri)) ; }

    public static Target create(Node graphName)
    { return new Target(graphName) ; }
    
    private final Decl decl ;
    private final Node graphIRI ;
    
    private Target(Decl decl)           { this.graphIRI = null ; this.decl = decl ; } 
    private Target(Node iri)            { this.graphIRI = iri ; this.decl = Decl.IRI$ ; }
    
    public boolean isDefault()          { return decl == DEFAULT$ ; }
    public boolean isAll()              { return decl == ALL$ ; }
    public boolean isAllNamed()         { return decl == NAMED$ ; }
    public boolean isOneNamedGraph()    { return decl == IRI$ ; }
    
    public Node getGraph()              { return graphIRI ; }
    
    @Override
    public String toString()
    {
        if ( isOneNamedGraph() )
            return decl.toString()+" "+FmtUtils.stringForNode(graphIRI) ;
        else    
            return decl.toString() ;
    }

    public boolean equalTo(Target other, NodeIsomorphismMap isoMap) {
        return equals(other) ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((decl == null) ? 0 : decl.hashCode()) ;
        // OK since a URI.
        result = prime * result + ((graphIRI == null) ? 0 : graphIRI.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        Target other = (Target)obj ;
        if (decl != other.decl) return false ;
        if (graphIRI == null)
        {
            if (other.graphIRI != null) return false ;
        } else
            if (!graphIRI.equals(other.graphIRI)) return false ;
        return true ;
    }
}
