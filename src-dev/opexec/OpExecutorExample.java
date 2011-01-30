/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package opexec;

import java.io.ByteArrayInputStream ;
import java.io.StringReader ;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.RiotReader ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraphOne ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Example skeleton for a query engine.
 *  To just extend ARQ by custom basic graph pattern matching (a very common case)
 *  see the arq.examples.bgpmatching package */

public class OpExecutorExample //extends QueryEngineMain
{
    // UNFINISHED
    // Check where OpExecutorFactory.create happens.
    
    /* To install a custom OpExecutor, the application needs
     * 
     *  
     * The example MyQueryEngine shows how to take over the
     * execution of a SPARQL algebra expression.  This allows
     * customization of optimizations running before query execution
     * starts.  
     * 
     * An OpExecutor controls the running of an algebra expression.
     * An executor needs to cope with the fact a dataset might be composed
     * of a mixture of graphs, and that it might be be being called for any
     * kind of storage unit, not just one it is designed for. 
     * 
     * Thsi is done by having a chain (via subclassing) of OpExecutors,
     * with the base class being hthe general purpose one for ARQ that can
     * operate on any data storage layer.
     * 
     */
    
    
    static void init()
    {
        // Wire the new factory into the system.
        ARQ.init() ;
        // *** Where is the factory choosen?
        OpExecutorFactory current = QC.getFactory(ARQ.getContext()) ;
        // maybe null
        QC.setFactory(ARQ.getContext(), new MyOpExecutorFactory(current)) ;
    }
    
    
    public static void main(String ...argv)
    {
        Log.setLog4j() ;
        init() ;
        Model m = data() ;
        
        String s = "SELECT DISTINCT ?s { ?s ?p ?o FILTER (?o=12) } " ;
        Query query = QueryFactory.create(s) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, m) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
    }
    
    
    
    private static Model data()
    {
        String s = StrUtils.strjoinNL("<s> <p> 12 .",
                                      "<s> <p> 15 .") ;
        Model m = ModelFactory.createDefaultModel() ;
        m.read(new StringReader(s), null , "TTL") ;
        return m ; 
    }


    // This is a simple example.
    // For execution logging, see:
    //   http://openjena.org/wiki/ARQ/Explain
    // which printout more information. 
    static class MyOpExecutor extends OpExecutor
    {
        protected MyOpExecutor(ExecutionContext execCxt)
        {
            super(execCxt) ;
        }
        
        @Override
        protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
        {
            System.out.print("Execute: "+opBGP) ;
            // This is an illustration - it's a copy of the default implementation
            BasicPattern pattern = opBGP.getPattern() ;
            return StageBuilder.execute(pattern, input, execCxt) ;
        }
        
        @Override
        protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
        {
            System.out.print("Execute: "+opFilter) ;
            return super.execute(opFilter, input) ;
        }

    }
    
    /** A factory to make OpExecutors */
    static class MyOpExecutorFactory implements OpExecutorFactory
    {
        private final OpExecutorFactory other ;
        
        public MyOpExecutorFactory(OpExecutorFactory other) { this.other = other ; }
        public OpExecutor create(ExecutionContext execCxt)
        {
            return new MyOpExecutor(execCxt) ;
        }
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