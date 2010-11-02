/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.request;

import static com.hp.hpl.jena.sparql.modify.request.Target.Decl.* ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class Target
{
    static enum Decl { DEFAULT$, NAMED$, ALL$, IRI$} ;
    
    public static final Target DEFAULT = new Target(DEFAULT$) ;
    public static final Target NAMED = new Target(NAMED$) ;
    public static final Target ALL = new Target(ALL$) ;

    public static Target create(String iri)
    { return new Target(Node.createURI(iri)) ; }

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
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */