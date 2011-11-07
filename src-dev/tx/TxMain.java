/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tx;

import java.nio.ByteBuffer ;

import org.openjena.atlas.lib.Bytes ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkWrapper ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.RiotWriter ;
import org.openjena.riot.lang.SinkTriplesToGraph ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.QueryExecUtils ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.BufferChannelFile ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.transaction.Journal ;
import com.hp.hpl.jena.tdb.transaction.JournalControl ;
import com.hp.hpl.jena.tdb.transaction.JournalEntryType ;
import com.hp.hpl.jena.tdb.transaction.Transaction ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class TxMain
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
    
    static void exit(int rc)
    {
        System.out.println("EXIT") ;
        System.exit(rc) ;
    }
    
    static public String DBdir = "DB" ;
    static public Location LOC = new Location(DBdir) ;
    
    public static void main(String... args)
    {
        initFS() ;
        
        //String DATA = "/home/afs/Datasets/MusicBrainz/tracks.nt.gz" ;
        String DATA = "/home/afs/Datasets/MusicBrainz/tracks-10k.nt" ;
        
        StoreConnection sConn = StoreConnection.make(LOC) ;
        DatasetGraphTxn dsg = sConn.begin(ReadWrite.WRITE) ;
        Transaction txn = dsg.getTransaction() ;
        Journal journal = txn.getJournal() ;
        
        String blobFileBase = "BLOB" ;
        String blobExt = "blob" ;
        
        FileSet fs = new FileSet(LOC, blobFileBase) ;
        FileRef ref = FileRef.create(fs, blobExt) ;
        String blobfilename = fs.filename(blobExt) ;
        
        BufferChannel data = new BufferChannelFile(blobfilename) ;
        System.out.println(ref.getFilename()) ;
        
        dsg.getConfig().bufferChannels.put(ref, data) ;

        TransBlob blob = new TransBlob(data, ref, null) ;
        
        blob.begin(txn) ;
        blob.setValue(ByteBuffer.wrap(StrUtils.asUTF8bytes("Stringdata"))) ;
        blob.commitPrepare(txn) ;
        
        // Journal commit.
        journal.write(JournalEntryType.Commit, FileRef.Journal, null) ;
        journal.sync() ;        // Commit point.
        
        JournalControl.replay(journal, dsg) ;
        
        blob.commitEnact(txn) ;
        blob.commitClearup(txn) ;
        
        ByteBuffer bb2 = ByteBuffer.allocate(1024) ; 
        BufferChannel data2 = new BufferChannelFile(blobfilename) ;
        
        int x = data2.read(bb2) ;
        byte b[] = new byte[x] ;
        bb2.position(0) ;
        bb2.get(b, 0, x) ;
        String str = StrUtils.fromUTF8bytes(b) ;
        System.out.println(str) ;
        
        exit(0) ;
        
        Sink<Triple> sink = new SinkTriplesToGraph(dsg.getDefaultGraph()) ;
        RiotReader.parseTriples(DATA, sink) ;
        System.out.println(dsg.getDefaultGraph().size()) ;
        
        if ( true )
        {
            dsg.commit() ;
            dsg.close() ;
            dsg = sConn.begin(ReadWrite.WRITE) ;
        }
        dsg.getDefaultGraph().getBulkUpdateHandler().removeAll() ;
        System.out.println(dsg.getDefaultGraph().size()) ;
        dsg.commit() ;
        dsg.close() ;
        exit(0) ;
        
    }
    
    private static void write(Graph graph, String lang)
    {
        Model model = ModelFactory.createModelForGraph(graph) ;
        model.write(System.out, lang) ; 
    }

    private static void initFS()
    {
        if ( LOC.isMem() )
            return ;
        FileOps.ensureDir(LOC.getDirectoryPath()) ;
        FileOps.clearDirectory(LOC.getDirectoryPath()) ;
    }
    
    private static DatasetGraphTDB build()
    {
        //return DatasetBuilderStd.build() ;
        //DatasetGraphTDB dsg = TDBFactory.createDatasetGraph(DBdir) ;
        DatasetGraphTDB dsg = DatasetBuilderStd.build(new Location(DBdir)) ;
        return dsg ;
    }

    public static void dump(DatasetGraphTxn dsg)
    {
        RiotWriter.writeNQuads(System.out, dsg) ;
    }
    
    public static void query(String queryStr, DatasetGraphTxn dsg)
    {
        String x = "Query ("+dsg.getTransaction().getLabel()+")" ;
        query(x, queryStr, dsg) ;
    }
    
    public static void query(String label, String queryStr, DatasetGraphTxn dsg)
    {
        System.out.print("**** ") ;
        System.out.println(label) ;
        Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, DatasetFactory.create(dsg)) ;
        QueryExecUtils.executeQuery(query, qExec) ;
    }
    
    private static void load(String file, DatasetGraphTxn dsg)
    {
        String x = "Load ("+dsg.getTransaction().getLabel()+")" ;
        load(x, file, dsg) ;
    }
    
    private static void load(String label, String file, DatasetGraph dsg)
    {
        System.out.print("**** ") ;
        System.out.println(label) ;
        System.out.println("Load: "+file) ;
        
        Sink<Triple> sink = new SinkTriplesToGraph(dsg.getDefaultGraph()) ;
        
        sink = new SinkWrapper<Triple>(sink) {
            int count = 0 ;
            @Override
            public void send(Triple item)
            {
                try {
                sink.send(item) ;
                count++ ;
                } catch (RuntimeException ex)
                {
                    System.err.println("exception @"+count) ;
                    throw ex ;
                }
            }
        } ;
        
        RiotReader.parseTriples(file, sink) ;
    }

    public static void update(String updateStr, DatasetGraph dsg)
    {
        UpdateRequest req = UpdateFactory.create(updateStr) ;
        UpdateAction.execute(req, dsg) ;
    }
    
    static Record record(RecordFactory rf, int key, int val)
    {
        Record r = rf.create() ;
        Bytes.setInt(key, r.getKey()) ;
        if ( rf.hasValue() )
            Bytes.setInt(val, r.getValue()) ;
        return r ;
    }
}
