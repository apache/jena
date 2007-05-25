/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.util.Context;


/** Factory to make QueryExecutionGraph objects from Query objects or a string.   
 *  
 * @author     Andy Seaborne
 * @version    $Id: QueryExecutionGraphFactory.java,v 1.3 2007/01/02 11:19:35 andy_seaborne Exp $
 */
public class QueryExecutionGraphFactory
{
    // XXX Merge into QueryExecutionFactory
    
    public static QueryExecutionGraph create(String queryStr, Graph graph)
    {
        return create(QueryFactory.create(queryStr), graph) ; 
    }
    
    public static QueryExecutionGraph create(String queryStr, DatasetGraph dataset)
    {
        return create(QueryFactory.create(queryStr), dataset) ; 
    }
     
    public static QueryExecutionGraph create(Query query, Graph graph)
    {
        return make(query, new DataSourceGraphImpl(graph)) ; 
    }
    
    public static QueryExecutionGraph create(Query query, DatasetGraph dataset)
    {
        return make(query, dataset) ;
    }

    public static QueryExecutionGraph create(Element pattern, Graph graph)
    {
        return create(toQuery(pattern), graph) ; 
    }
    
    public static QueryExecutionGraph create(Element pattern, DatasetGraph dataset)
    {
        return make(toQuery(pattern), dataset) ;
    }
    
    private static Query toQuery(Element pattern)
    {
        Query query = QueryFactory.make() ;
        query.setQueryPattern(pattern) ;
        return query ;
    }

    private static QueryExecutionGraph make(Query query, DatasetGraph dataset)
    {
        Context context = ARQ.getContext() ;
        for ( Iterator iter = QueryEngineRegistry.get().factories().iterator() ; iter.hasNext() ; )
        {
            QueryEngineFactory f = (QueryEngineFactory)iter.next();
            if ( f.accept(query, dataset, context) )
                return f.create(query, dataset, context) ;
        }
        return null ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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