/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: QueryMapper.java,v 1.3 2004-04-22 12:42:29 chris-dollin Exp $
*/

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;

/**
    Utility class for <code>ModelQueryUtil</code> which converts a query represented
    by a model with jqv: variables into a <code>.graph.query.Query</code> object.
    
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
        return Triple.create
            ( 
            toQueryNode( s.getSubject() ), 
            toQueryNode( s.getPredicate() ), 
            toQueryNode( s.getObject() )
            );
        }
    
    public Node [] toQueryVariables( Resource [] vars )
        {
        Node [] result = new Node[vars.length];
        for (int i = 0; i < vars.length; i += 1) result[i] = toQueryNode( vars[i] );
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
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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