/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Iterator ;

import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.TDBLoader ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.vocabulary.RDF ;

public class RunTDB
{
    static { Log.setLog4j() ; }
    static String divider = "----------" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = divider ;
    }

    public static void main(String[] args) throws Exception
    {
        TDBFactory.createDataset("/home/afs/Projects/TDB/a'b/c") ;
        System.out.println("DONE") ;
        System.exit(0) ;
        
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph("/home/afs/Desktop/BWQ/DB-0.8.6") ;
        TupleIndex index = dsg.getTripleTable().getNodeTupleTable().getTupleTable().getIndex(1) ;
        
        TupleIndexRecord idxRec = (TupleIndexRecord)index ;
        BPlusTree bpt = (BPlusTree)idxRec.getRangeIndex() ;
        Record rec = bpt.minKey() ;

        System.out.println(index.getLabel()) ;
        
        System.out.println("LOOKUP rdf:type") ; System.out.flush();
        NodeId n = dsg.getTripleTable().getNodeTupleTable().getNodeTable().getNodeIdForNode(RDF.type.asNode()) ;
        Tuple<NodeId> pattern = Tuple.create(null, n, null) ;
        
        System.out.println("Iterator") ; System.out.flush();
        Iterator<Tuple<NodeId>> iter = index.find(pattern) ;// index.all() ;
        
        int count = 0 ;
        for ( ; iter.hasNext() ; )
        {
            if ( count == 0 )
            { System.out.println("Loop - 0") ; System.out.flush(); }
            Tuple<NodeId> tuple = iter.next();
            count++ ;
            //System.out.println(tuple) ;
        }
        System.out.println("DONE") ;
        System.exit(0) ;
        
        if ( true )
        {
            Dataset ds = TDBFactory.createDataset() ;
            TDBLoader.load((DatasetGraphTDB)(ds.asDatasetGraph()), "/home/afs/Desktop/main.nt") ;
            TDB.sync(ds) ;
            System.out.println("DONE") ;
            System.exit(0) ;
            
            
            Model model = ds.getDefaultModel() ;
            model.read("file:main.rdf", "RDF/XML") ;
            TDB.sync(ds) ;
            System.out.println("DONE") ;
            System.exit(0) ;
        }

        FileOps.clearDirectory("DB") ;
        tdb.tdbloader.main("--loc=DB", "D.nt" ) ;
        System.exit(0) ;
        
        {
            //FileOps.clearDirectory("DB") ;
            Dataset ds = TDBFactory.createDataset("DB") ;
            boolean b = ds.containsNamedModel("http://example/") ;
            TDB.sync(ds) ;
            System.out.println(b) ;
            System.exit(0) ;
        }
        
        
        FileOps.clearDirectory("DB") ;
        arq.update.main("--desc=tdb.ttl", "--file=up1.ru") ;
        arq.sparql.main("--desc=tdb.ttl", "--file=Q.rq") ;
        arq.update.main("--desc=tdb.ttl", "--file=up2.ru") ;
        arq.sparql.main("--desc=tdb.ttl", "--file=Q.rq") ;
        System.exit(0) ;
        
        // Config union graph
        //tdb.tdbquery.main("--tdb=tdb.ttl", "SELECT * { ?s ?p ?o}") ;
//        System.exit(0) ;

        // Working on API models ..
        Dataset ds = TDBFactory.createDataset() ;
//        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;
//        ds.asDatasetGraph().getContext().setTrue(TDB.symUnionDefaultGraph) ;
        Model model = ds.getDefaultModel() ;
        
        ds.getDefaultModel().getNsPrefixURI("") ;
        System.out.println("DONE") ;
        System.exit(0) ;
        
        Model ng1 = ds.getNamedModel("http://example/ng1") ;
        Model ng2 = ds.getNamedModel("http://example/ng2") ;
        
        Model ug = ds.getNamedModel(Quad.unionGraph.getURI()) ;
        Model ndft1 = ds.getNamedModel(Quad.defaultGraphIRI.getURI()) ;
        Model ndft2 = ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI()) ;
        
        Resource r = model.createResource("http://example/") ; 
        Property p = model.createProperty("http://example/prop") ;
        
//        model.add(r, p, "default") ;
//        ng1.add(r, p, "ng1-a") ;
//        ng1.add(r, p, "ng1-b") ;
//        ng2.add(r, p, "ng2-a") ;

        //ug.add(r, p, "union") ;   //Thou shalt not ...
        ndft1.add(r, p, "ndft1") ;
        ndft2.add(r, p, "ndft2") ;
        
        SSE.write(ds) ;
        //SSE.write(model.getGraph()) ;
        System.out.println();
        
        QueryExecution qExec = QueryExecutionFactory.create("SELECT ?o { ?s ?p ?o}", ds) ;
        ResultSetFormatter.out(qExec.execSelect()) ;
        qExec.close() ;
        
        System.exit(0) ;
        
        tdb.tdbquery.main("--set=tdb:logExec=true", 
                          "--set=tdb:unionDefaultGraph=true", 
                          "--query=Q.rq") ;
        System.exit(0) ;
    }
   
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd
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