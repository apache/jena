/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.out.NQuadsWriter ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.migrate.DynamicDatasets ;
import com.hp.hpl.jena.tdb.migrate.TransformDynamicDataset_Imperfect ;

public class ReportDynamicDatasetAndPaths
{
    // GraphDynamicUnion for paths.
    // Cross graph does each triple pattern as a distinct-union over graphs.
    // But let's be general.
    
    public static void main(String ...argv)
    {
        //main2() ; System.exit(0) ;
        
        // One - create a graph that does the FROM thing properly.  Use as defaul graph.
        // two - rewrite quads (BGP?) to  

        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        //DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        Dataset ds = DatasetFactory.create(dsg) ;
        Quad q1 = SSE.parseQuad("(<http://example/g1> <http://example/x1> <http://example/p1> <http://example/x2>)") ;
        Quad q2 = SSE.parseQuad("(<http://example/g2> <http://example/x2> <http://example/p2> <http://example/x3>)") ;
        Quad q3 = SSE.parseQuad("(_ <http://example/x2> <http://example/p2> <http://example/x3>)") ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.add(q1) ;
        dsg.add(q3) ;
        divider() ;
        NQuadsWriter.write(System.out, dsg) ;
        divider() ;
        
        String qs = StrUtils.strjoinNL(
            "PREFIX : <http://example/>", 
            "SELECT ?s ?o",
            // This breaks the query.
            //"FROM <urn:x-arq:DefaultGraph> FROM <urn:x-arq:UnionGraph>",
            "FROM :g1 FROM :g2 FROM <urn:x-arq:DefaultGraph>",
            // Any unconverted paths (e.g. :p*) remain as (graph .. (path ...))
            //" { ?s :p1/:p2 ?o }"
            "{ ?s ?p ?o }"
            //" { ?s :p1 ?z . ?z :p1* ?x . ?x :p2 ?o }"
            // Fixed.
            //" { ?s :p1 ?x . ?x :p2 ?o }"
        ) ;
        
        
        Query query = QueryFactory.create(qs) ;
        Dataset ds2 = DynamicDatasets.dynamicDataset(query, ds, false ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds2) ;
        // No effect if using dynamic datasets.
        //qExec.getContext().set(TDB.symUnionDefaultGraph, true) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        System.exit(0) ;
        
        
        //ARQ.setExecutionLogging(InfoLevel.ALL) ;
        Op op = Algebra.compile(query) ;
        //op = Algebra.optimize(op) ;
        output("** Compiled", op) ;
        // Unoptimized - no path flattening.
        op = Algebra.toQuadForm(op) ;
        output("** Quad form", op) ;
        op = TransformDynamicDataset_Imperfect.transform(query, op) ; 
        output("** DynDS transform", op) ;
       
        divider() ;
        QueryExecUtils.executeAlgebra(op, dsg, ResultsFormat.FMT_TEXT) ;
        System.exit(0) ;
        
    }
    
    public static void main2(String ...argv)
    {
        DatasetGraph dsg = TDBFactory.createDatasetGraph() ;
        Quad q1 = SSE.parseQuad("(<http://example/g1> <http://example/x1> <http://example/p1> <http://example/x2>)") ;
        Quad q2 = SSE.parseQuad("(<http://example/g2> <http://example/x2> <http://example/p2> <http://example/x3>)") ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        
        query(dsg) ;
    }
    
    public static void query(DatasetGraph dsg)
    {
        NQuadsWriter.write(System.out, dsg) ;
        Dataset ds = DatasetFactory.create(dsg) ;
        String qs = StrUtils.strjoinNL(
            "PREFIX : <http://example/>", 
            "SELECT ?s ?o",
            //"FROM :g1 FROM :g2",
            " { ?s :p1/:p2 ?o }"
        ) ;
        Query query = QueryFactory.create(qs) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        // No effect if using dynamic datasets.
        qExec.getContext().set(TDB.symUnionDefaultGraph, true) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        
    }
    
    private static void output(String label, Op op)
    {
        divider() ;
        
        System.out.println(label) ;
        System.out.flush() ;
        System.out.print(op) ;
        System.out.flush() ;
    }

    static String divider = "----------------------------------------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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