/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: QueryMapper.java,v 1.1 2003-05-23 15:25:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

/**
 	@author kers
*/
public class QueryMapper
    {
    private Query query;
    private Node [] variables;
    private Graph graph;
    
    public QueryMapper( Model m, Resource [] variables )
        {
        super();
        this.graph = toQueryGraph( m );
        this.query = new Query( this.graph );
        this.variables = toQueryVariables( variables );
        }
        
    public Node [] getVariables()
        { return variables; }
        
    public Query getQuery()
        { return query; }
        
    public Graph getGraph()
        { return graph; }

    public Graph toQueryGraph( Model m )
        {
        StmtIterator st = m.listStatements();
        Graph result = Factory.createDefaultGraph();
        while (st.hasNext()) result.add( toQueryTriple( st.nextStatement() ) );
        return result;
        }
    
    public Triple toQueryTriple( Statement s )
        {
        return new Triple
            ( 
            toQueryNode( s.getSubject() ), 
            toQueryNode( s.getPredicate() ), 
            toQueryNode( s.getObject() )
            );
        }
    
    public Node [] toQueryVariables( Resource [] variables )
        {
        Node [] result = new Node[variables.length];
        for (int i = 0; i < variables.length; i += 1) result[i] = toQueryNode( variables[i] );
        return result;
        }
    
    static final String varPrefix = "jqv:";
    
    public Node toQueryNode( RDFNode rn )
        {
        Node n = rn.asNode();
        return n.isURI() && n.getURI().startsWith( varPrefix )
            ? Node.createVariable( n.getURI().substring( varPrefix.length() ) )
            : n;
        }
        
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/