/*
 * (c) Copyright Apache Software Foundation - Apache Software License 2.0
 */

package com.hp.hpl.jena.sparql.api ;

import java.util.concurrent.TimeUnit ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import static org.openjena.atlas.lib.Lib.sleep ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.function.FunctionRegistry ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestQueryExecutionTimeout extends BaseTest
{
    static Graph                g   = SSE.parseGraph("(graph (<s> <p> <o1>) (<s> <p> <o2>) (<s> <p> <o3>))") ;
    static DatasetGraph         dsg = DatasetGraphFactory.create(g) ;
    static Dataset              ds  = DatasetFactory.create(dsg) ;

    private static final String ns  = "http://example/ns#" ;

    @BeforeClass
    public static void beforeClass()
    {
        FunctionRegistry.get().put(ns + "wait", wait.class) ;
    }

    @AfterClass
    public static void afterClass()
    {
        FunctionRegistry.get().remove(ns + "wait") ;
    }

    static private String prefix = "PREFIX f: <http://example/ns#>" ;

    // Numbers all a bit iffy - but don't want test to be to slow ...

    @Test
    public void timeout_01()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(10, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        sleep(20) ;
        exceptionExpected(rs) ; 
    }

    @Test
    public void timeout_02()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        rs.next() ;
        sleep(75) ;
        exceptionExpected(rs) ; 
    }

    @Test
    public void timeout_03()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(100, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        qExec.close() ;
        qExec.abort() ;
    }

    @Test
    public void timeout_04()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        sleep(100) ;
        rs.hasNext() ;         // Query ended - calling rs.hasNext() is safe.
        qExec.close() ;
    }

    @Test
    public void timeout_05()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(50) }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        exceptionExpected(rs) ; 
        qExec.close() ;
    }
    
    @Test
    public void timeout_06()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ; // Sleep in execution to kick timer thread.
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(100, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        qExec.close() ;
    }
    
    @Test
    public void timeout_07()
    {
        // No timeout.
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        qExec.close() ;
    }

    @Test
    public void timeout_08()
    {
        // No timeout.
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(-1, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        qExec.close() ;
    }

    @Test
    public void timeout_09()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(100, TimeUnit.MILLISECONDS, -1, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        rs.next() ; // First timeout does not go off. Resets timers.
        rs.next() ; // Second timeout never goes off 
        assertTrue(rs.hasNext()) ;
        ResultSetFormatter.consume(rs) ; // Second timeout does not go off.
        qExec.close() ;
    }

    @Test
    public void timeout_10()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(50, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        rs.next() ; // First timeout does not go off. Resets timers.
        rs.next() ; // Second timeout never goes off 
        assertTrue(rs.hasNext()) ;
        sleep(200) ;
        exceptionExpected(rs) ; 
        qExec.close() ;
    }
    
    @Test
    public void timeout_11()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(-1, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        sleep(200) ;
        rs.next() ; // First timeout does not go off. Resets timer.
        rs.next() ; // Second timeout does not go off
        sleep(200) ;
        exceptionExpected(rs) ; 
        qExec.close() ;
    }
    
    private static void exceptionExpected(ResultSet rs)
    {
        try { ResultSetFormatter.consume(rs) ; fail("QueryCancelledException expected") ; } catch (QueryCancelledException ex) {}
    }
    
}
