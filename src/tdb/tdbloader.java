/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import java.util.Iterator;
import java.util.concurrent.Semaphore;

import lib.Tuple;
import tdb.cmdline.ModLocation;
import arq.cmd.CmdException;
import arq.cmd.CmdUtils;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdARQ;
import arq.cmdline.ModAssembler;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.sparql.util.Timer;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.sparql.util.graph.GraphLoadMonitor;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.pgraph.NodeId;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;
import com.hp.hpl.jena.util.FileManager;

public class tdbloader extends CmdARQ
    {
    // Simple wrapper to update?
    // At least use update requests.
    //ModGraphStore modGraphStore = new ModGraphStore() ;
    
    PGraphBase graph ; 
    ModAssembler modAssembler =  new ModAssembler() ;
    ModLocation modLocation =  new ModLocation() ;
    ArgDecl argParallel = new ArgDecl(ArgDecl.NoValue, "parallel") ;
    
    boolean timing = true ;
    boolean doInParallel = false ;
    
    static public void main(String... argv)
    { 
        CmdUtils.setLog4j() ;
        new tdbloader(argv).main() ;
    }

    protected tdbloader(String[] argv)
    {
        super(argv) ;
        TDB.init() ;
        super.addModule(modAssembler) ;
        super.addModule(modLocation) ;
        super.add(argParallel) ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        doInParallel = super.contains(argParallel) ;
    }
    
    @Override
    protected String getSummary()
    {
        return getCommandName()+" [--desc DATASET | -loc DIR] FILE ..." ;
    }

    
    @Override
    protected String getCommandName()
    {
        return Utils.className(this) ;
    }

    private PGraphBase getGraph()
    {
        if ( graph != null )
            return graph ;
        
        if ( modLocation.getLocation() == null && modAssembler.getAssemblerFile() == null )
            throw new CmdException("No assembler file and no location") ;
             
        if ( modLocation.getLocation() != null && modAssembler.getAssemblerFile() != null )
            throw new CmdException("Both an assembler file and a location") ;
        
        Model model = null ;
        
        if ( modAssembler.getAssemblerFile() != null )
            model = TDBFactory.assembleModel(modAssembler.getAssemblerFile()) ;
        else
            model = TDBFactory.createModel(modLocation.getLocation()) ;
        graph = (PGraphBase)model.getGraph() ;
        return graph ;
    }
    
    @Override
    protected void exec()
    {
        if ( isVerbose() )
            timing = true ;
        if ( isQuiet() )
            timing = false ;
        
//        if ( modAssembler.getAssemblerFile() == null )
//            throw new CmdException("No assembler file") ;
        
//        Model model = TDBFactory.assembleModel(modAssembler.getAssemblerFile()) ;
        graph = getGraph() ;
        Model model = ModelFactory.createModelForGraph(graph) ;
        
        // SPO only.
        dropSecondaryIndexes() ;
        
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        // Side effect : with no files to load, it copies the SPO index to the secondary indexes.
        if ( timing && super.getPositional().size() > 0 )
            System.out.println("** Load data") ;
        long count = 0 ;
        
        @SuppressWarnings("unchecked")
        Iterator<String> iter = (Iterator<String>)super.getPositional().iterator() ;
        
        for ( ; iter.hasNext() ; )
        {
            String s = iter.next();
            count += loadOne(model, s) ;
        }
        
        // Especially the node table.
        graph.sync(true) ;
        // Close other resourses (node table).
        
        
        if ( timing )
            System.out.println("** Secondary indexes") ;
        // Now do secondary indexes.
        
        // Fork two separate processes? But it's disk bound!?
        createSecondaryIndexes(timing) ;

        if ( timing )
            System.out.println("** Close graph") ;
        graph.close() ;

        timer.endTimer() ;
        long time = timer.getTimeInterval() ;
        if ( timing )
        {
            long tps = 1000*count/time ;
            System.out.println() ;
            System.out.printf("Time for load: %.2fs [%,d triples/s]\n", time/1000.0, tps) ;
        }
    }

    private long loadOne(Model model, String s)
    {
        GraphLoadMonitor monitor = new GraphLoadMonitor(20000, false) ;
        if ( timing )
            model.getGraph().getEventManager().register(monitor) ;
        if ( ! s.equals("-") )
            FileManager.get().readModel(model, s) ;
        else
            // MUST BE N-TRIPLES
            model.read(System.in, null, "N-TRIPLES") ;
        
        if ( timing )
            model.getGraph().getEventManager().unregister(monitor) ;
        return timing ? monitor.getAddCount() : -1 ;
    }

    private void dropSecondaryIndexes()
    {
        if ( graph.getIndexPOS() != null )
        {
            graph.getIndexPOS().close();
            graph.setIndexPOS(null) ;
        }
        
        if ( graph.getIndexOSP() != null )
        {
            graph.getIndexOSP().close();
            graph.setIndexOSP(null) ;
        }
    }

    private void createSecondaryIndexes(boolean printTiming)
    {
        if ( doInParallel )
            createSecondaryIndexesParallel(printTiming) ;
        else
            createSecondaryIndexesSequential(printTiming) ;
    }
    
    private void createSecondaryIndexesParallel(boolean printTiming)
    {
        System.out.println("** Parallel index building") ;
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        TripleIndex indexSPO = graph.getIndexSPO() ;
        
        // ---- POS
        RangeIndex idxPOS = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "POS") ;
        TripleIndex triplesPOS = new TripleIndex("POS", idxPOS) ;
        
        Semaphore sema = new Semaphore(0) ;
        Runnable builder1 = setup(sema, indexSPO, triplesPOS, "POS", printTiming) ;
        
        // ---- OSP
        RangeIndex idxOSP = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "OSP") ;
        TripleIndex triplesOSP = new TripleIndex("OSP", idxOSP) ;
        Runnable builder2 = setup(sema, indexSPO, triplesOSP, "OSP", printTiming) ;

        new Thread(builder1).start() ;
        new Thread(builder2).start() ;
        
        try {  sema.acquire(2) ; } catch (InterruptedException ex) { ex.printStackTrace(); }

        long time = timer.readTimer() ;
        if ( printTiming )
            System.out.printf("Time for POS/OSP indexing: %.2fs\n", time/1000.0) ;
        timer.endTimer() ;
        
        graph.setIndexOSP(triplesOSP) ;
        graph.setIndexPOS(triplesPOS) ;
    }
    
    private Runnable setup(final Semaphore sema, final TripleIndex srcIndex, final TripleIndex destIndex, final String label, final boolean printTiming)
    {
        Runnable builder = new Runnable(){
            @Override
            public void run()
            {
                copyIndex(srcIndex, destIndex, label, printTiming) ;
                sema.release() ;
            }} ;
        
        return builder ;
    }

    private void createSecondaryIndexesSequential(boolean printTiming)
    {
        Timer timer = new Timer() ;
        timer.startTimer() ;
        
        TripleIndex indexSPO = graph.getIndexSPO() ;
        
        // ---- POS
        RangeIndex idxPOS = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "POS") ;
        TripleIndex triplesPOS = new TripleIndex("POS", idxPOS) ;
        
        long time1 = timer.readTimer() ;
        copyIndex(indexSPO, triplesPOS, "POS", printTiming) ;
        long time2 = timer.readTimer() ;
        if ( printTiming )
            System.out.printf("Time for POS indexing: %.2fs\n", (time2-time1)/1000.0) ;
        
        // ---- OSP
        RangeIndex idxOSP = graph.getIndexFactory().createRangeIndex(graph.getIndexRecordFactory(), "OSP") ;
        TripleIndex triplesOSP = new TripleIndex("OSP", idxOSP) ;
        copyIndex(indexSPO, triplesOSP, "OSP", printTiming) ;
        
        long time3 = timer.readTimer() ;
        if ( printTiming )
            System.out.printf("Time for OSP indexing: %.2fs\n", (time3-time2)/1000.0) ;
        timer.endTimer() ;
        
        graph.setIndexOSP(triplesOSP) ;
        graph.setIndexPOS(triplesPOS) ;

    }


    private static void copyIndex(TripleIndex srcIdx, TripleIndex destIdx, String label, boolean printTiming)
    {
        long quantum = 100000 ;

        Timer timer = new Timer() ;
        long cumulative = 0 ;
        long c = 0 ;
        long last = 0 ;
        timer.startTimer() ;
        
        Iterator<Tuple<NodeId>> iter = srcIdx.all() ;
        for ( int i = 0 ; iter.hasNext() ; i++ )
        {
            Tuple<NodeId> tuple = iter.next();
            destIdx.add(tuple.get(0), tuple.get(1), tuple.get(2)) ;
            c++ ;
            cumulative++ ;
            if ( printTiming && cumulative%quantum == 0 )
            {
                long t = timer.readTimer() ;
                long batchTime = t-last ;
                long elapsed = t ;
                last = t ;
                System.out.printf("Index %s: %,d slots (Batch: %,d slots/s / Run: %,d slots/s)\n", 
                                  label, cumulative, 1000*c/batchTime, 1000*cumulative/elapsed) ;
                c = 0 ;
            }
        } 
        destIdx.sync(true) ;
        long totalTime = timer.endTimer() ;
        
        if ( printTiming )
        {
            if ( cumulative > 0 )
            {
                if ( totalTime > 0 )
                    System.out.printf("Index %s: %,d triples indexed in %,.2fs [%,d slots/s]\n", 
                                      label, cumulative, totalTime/1000.0, 1000*cumulative/totalTime) ;
                else
                    System.out.printf("Index %s: %,d triples indexed in %,.2fs\n", label, cumulative, totalTime/1000.0) ;
            }
            else
                System.out.printf("Index %s: 0 triples indexed\n", label) ;
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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