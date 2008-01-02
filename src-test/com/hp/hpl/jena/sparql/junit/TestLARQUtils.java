/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import java.util.Iterator;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.larq.IndexBuilderModel;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.query.larq.LARQ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.NiceIterator;

public class TestLARQUtils
{
    public static QueryExecution query(Model model, String pattern)
    { return query(model, pattern, null) ; }

    public static QueryExecution query(Model model, String pattern, IndexLARQ index)
    {
        String queryString = StringUtils.join("\n", new String[]{
            "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>" ,
            "PREFIX :       <http://example/>" ,
            "PREFIX pf:     <http://jena.hpl.hp.com/ARQ/property#>",
            "PREFIX  dc:    <http://purl.org/dc/elements/1.1/>",
            "SELECT *",
            pattern,
        }) ;
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, model) ;
        if ( index != null )
            LARQ.setDefaultIndex(qExec.getContext(), index) ;
        return qExec ;
    }
    
    public static int count(ResultSet rs)
    {
        return ResultSetFormatter.consume(rs) ;
    }
    
    public static int count(Iterator iter)
    {
        int count = 0 ; 
        for ( ; iter.hasNext() ; )
        {
            iter.next();
            count++ ;
        }
        NiceIterator.close(iter) ;
        return count ;
    }
    
    public static IndexLARQ createIndex(String datafile, IndexBuilderModel indexBuilder)
    { return createIndex(ModelFactory.createDefaultModel(), datafile, indexBuilder) ; }
    
    public static IndexLARQ createIndex(Model model, String datafile, IndexBuilderModel indexBuilder)
    {
        model.register(indexBuilder) ;
        FileManager.get().readModel(model, datafile) ;
        model.unregister(indexBuilder) ;
        indexBuilder.closeForWriting() ;
        return indexBuilder.getIndex() ;
    }

}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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