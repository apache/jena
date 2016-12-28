/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package parallel;

import java.util.List ;
import java.util.Spliterators ;
import java.util.concurrent.Executor ;
import java.util.concurrent.Executors ;
import java.util.function.Consumer ;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.lib.Timer ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.op.OpBGP ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.main.OpExecutor ;
import org.apache.jena.sparql.engine.main.StageGenerator ;
import org.apache.jena.sparql.engine.main.StageGeneratorGeneric ;
import org.apache.jena.sparql.sse.SSE ;

public class DevSplit {
    static { LogCtl.setLog4j(); }
    
    // streams
    
    public static void main(String ...argv) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;
        String y = StrUtils.strjoinNL(
                                      "(prefix ((bsbm-inst: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/>)"
                                      ,"        (bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>)"
                                      ,"        (rdfs: <http://www.w3.org/2000/01/rdf-schema#>)"
                                      ,"        (rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>)"
                                      ,"       )"
                                      ,"  (sequence"
                                      ,"    (bgp (bsbm-inst:ProductType1 ?p ?o))"
                                      ,"    (bgp (?s1 ?p1 ?o))"
                                      ,"  )"
                                      ,")"
                                      ) ;
        Op op = SSE.parseOp(y) ;
        
        {
            Timer timer = new Timer() ;
            timer.startTimer() ;
            StreamRDF s1 = StreamRDFLib.dataset(dsg) ; 
            StreamRDFCounting s2 = StreamRDFLib.count(s1) ;

            RDFDataMgr.parse(s2, "/home/afs/Datasets/BSBM/bsbm-250k.nt.gz") ;
            long N = s2.count() ;
            long x = timer.endTimer() ;
            double z = x/1000.0 ;
            System.out.printf("Load: N=%,d : Time = %.2fs : %,.1f TPS\n", N, z, N/z) ;
        }
        ExecutionContext execCxt = new ExecutionContext(ARQ.getContext(), dsg.getDefaultGraph(), dsg, OpExecutor.stdFactory) ;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        Parallel p = new Parallel(execCxt, true) ;
        System.out.println(op) ;
        QueryIterator qIter = p.executeOp(op, QueryIterRoot.create(execCxt)) ;
        long N = Iter.count(qIter) ;
        long x = timer.endTimer() ;
        double z = x/1000.0 ;
        System.out.printf("BGP:  N=%,d : Time = %.2fs\n", N, z) ;
        System.out.println("DONE") ;
    }
    
    static class Parallel extends OpExecutor {
        // c.f StreamRDFBatchSplit
        //@Override
        
//        BlockingDeque<Binding> pipe = new ArrayBlockingQueue<>() ;
//        
//        QueryIterator result = new QueryIteratorP
//        
//        private Consumer<List<Binding>> proc = (list->{}) ;
        
        //Stream<Binding>
        
        // Parallelism
        static Executor executor = Executors.newFixedThreadPool(4) ;
        
        final boolean parallel ;
        final boolean stream = false ;

        
        public Parallel(ExecutionContext execCxt, boolean parallel) { 
            super(execCxt) ;
            this.parallel = parallel ;
        }
        
        StageGenerator gen = new StageGeneratorGeneric() ;
        
        protected QueryIterator executeDirect(OpBGP opBGP, QueryIterator input) {
            return gen.execute(opBGP.getPattern(), input, execCxt) ;
        }
        
        static Binding END = BindingFactory.create() ;
        
        protected QueryIterator executeStream(OpBGP opBGP, QueryIterator input) {
            // Split
            Stream<Binding> start = stream(input, true) ;
            // Or stream of list of 
            Stream<Binding> result = start.flatMap(binding ->solveStream(binding, opBGP.getPattern())) ;
            
            //start.collect(null) // Blocks.
            //.map(null) // To blocks.
            return new QueryIterPlainWrapper(result.iterator()) ;
        }
        
 
        
        public Stream<Binding> solveStream(Binding binding, BasicPattern pattern) {
            // Overhead?
            QueryIterator qiter = gen.execute(pattern, QueryIterSingleton.create(binding, execCxt), execCxt) ;
            return stream(qiter, false) ;
        }

        private Stream<Binding> stream(QueryIterator qIter,boolean parallel) {
            return StreamSupport.stream(
                                 Spliterators.spliteratorUnknownSize(qIter, 0), parallel);
        }

        @Override
        protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
            if ( parallel )
                return executeParallel(opBGP, input) ;
            if ( stream )
                executeStream(opBGP, input) ;
            return super.execute(opBGP, input) ;
        }
        
        protected QueryIterator executeParallel(OpBGP opBGP, QueryIterator input) {
            BasicPattern bgp = opBGP.getPattern() ;
            if (input instanceof QueryIterRoot && bgp.size() <= 1) {
                QueryIterator qIter = gen.execute(bgp, input, execCxt) ;
                return qIter ;
            }
            //Join.
            
            // check for "small"
            Consumer<List<Binding>> proc = (list-> {
                System.out.println("List: "+list.size()) ;
                // Parallel Hash Join.
                // Need the hash key.
            }) ;
            //Scatter
            Splitter<Binding> splitter = new Splitter<>(proc, 10) ;
            splitter.start() ;
            input.forEachRemaining(splitter::item) ;
            splitter.finish();
            
            // ......
            
            return QueryIterNullIterator.create(execCxt) ;
        }

    }
}

