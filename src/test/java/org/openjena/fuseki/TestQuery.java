/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.fuseki.client.DatasetUpdater ;
import org.openjena.fuseki.client.DatasetUpdaterHTTP ;

import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;

public class TestQuery extends BaseServerTest 
{
    protected static final String gn1       = "http://graph/1" ;
    protected static final Model graph1     = 
        ModelFactory.createModelForGraph(SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))")) ;
    protected static final Model graph2     = 
        ModelFactory.createModelForGraph(SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))")) ;
    
    protected static ResultSet rs1 = null ; 
    static {
        Item item = SSE.parseItem("(resultset (?s ?p ?o) (row (?s <x>)(?p <p>)(?o 1)))") ;
        rs1 = BuilderResultSet.build(item) ;
    }
    
    
    @BeforeClass public static void beforeClass()
    {
        serverReset() ;
        // Load some data.
        DatasetUpdater du = new DatasetUpdaterHTTP(serviceREST) ;
        du.putModel(graph1) ;
        du.putModel(gn1, graph2) ;
    }
    
    @AfterClass public static void afterClass()
    {
        DatasetUpdater du = new DatasetUpdaterHTTP(serviceREST) ;
        du.deleteDefault() ;
    }
    
    @Test public void query_01()
    {
        execQuery("SELECT * {?s ?p ?o}", 1) ;
    }

    private void execQuery(String queryString, int exceptedRowCount)
    {
        QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery, queryString) ;
        ResultSet rs = qExec.execSelect() ;
        int x = ResultSetFormatter.consume(rs) ;
        assertEquals(exceptedRowCount, x) ;
    }
    
    private void execQuery(String queryString, ResultSet expectedResultSet)
    {
        QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery, queryString) ;
        ResultSet rs = qExec.execSelect() ;
        boolean b = ResultSetCompare.equalsByTerm(rs, expectedResultSet) ;
        assertTrue("Result sets different", b) ;
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