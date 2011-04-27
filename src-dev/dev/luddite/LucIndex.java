/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.luddite;

import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.query.larq.ARQLuceneException;
import com.hp.hpl.jena.tdb.index.TupleIndex;
import com.hp.hpl.jena.tdb.store.NodeId;

public class LucIndex implements TupleIndex
{
    static NodeId n1 = new NodeId(1) ; 
    static NodeId n2 = new NodeId(2) ; 
    static NodeId n3 = new NodeId(3) ; 
    static NodeId n4 = new NodeId(4) ; 
    static NodeId n5 = new NodeId(5) ; 
    static NodeId n6 = new NodeId(6) ; 

    
    public static void main(String ...args) throws Exception
    {
        LucIndex idx = new LucIndex() ;
        idx.find(Tuple.create(n1, n2, null)) ;
        
    }
    
    
//    static Document encode(Record record)
//    { 
//        // tuple to byte[].  Or a string?
//        // ???? TupleIndex and Tuple<NodeId) => String.
//        Field fTuple = new Field("tuple", record.getKey().toString(), Store.YES, Index.UN_TOKENIZED) ; // ==> NOT_ANALYZED 
//        
//        return null ;
//    }
//    
//    
//    static Record decode(Document document) { return null ; }

    static Document encode(Tuple<NodeId> tuple)
    { 
        StringBuilder sbuf = new StringBuilder() ;
        for ( NodeId n : tuple )
            // Fixed width.
            sbuf.append(String.format(" %016X", n.getId())) ;
        
        String x = "" ;
        // tuple to byte[].  Or a string?
        // ???? TupleIndex and Tuple<NodeId) => String.
        Field fTuple = new Field("tuple", x, Store.YES, Index.UN_TOKENIZED) ; // ==> NOT_ANALYZED 
        Document d = new Document() ;
        d.add(fTuple) ;
        return d ;
    }
    
    
    static Tuple<NodeId> decode(Document document)
    {
        Field f = document.getField("tuple") ;
        String x = f.stringValue() ;
        int len = x.length()/16 ;
        NodeId t[] = new NodeId[len] ;
        for ( int i = 0 ; i < len ; i++ )
        {
            String z = x.substring(i*16, (i+1)*16) ;
            long v = Long.parseLong(z, 16) ;
            t[i] = new NodeId(v) ; 
        }
        return Tuple.create(t) ;
    }

    
    IndexReader reader = null ;

    public LucIndex() throws Exception
    {
        Directory d = new RAMDirectory() ;
        IndexWriter w = new IndexWriter(d, null) ;
        
        
        w.addDocument(encode(Tuple.create(n1,n2,n3))) ;
        w.addDocument(encode(Tuple.create(n1,n2,n4))) ;
        w.addDocument(encode(Tuple.create(n2,n5,n6))) ;
        w.close() ;
        reader = IndexReader.open(d) ;
    }
    
    //@Override
    public boolean add(Tuple<NodeId> tuple)
    {
        return false ;
    }

    //@Override
    public Iterator<Tuple<NodeId>> all()
    {
        return null ;
    }

    //@Override
    public boolean delete(Tuple<NodeId> tuple)
    {
        return false ;
    }


    //@Override
    public Iterator<Tuple<NodeId>> find(Tuple<NodeId> pattern)
    {
        try{
            Searcher searcher = new IndexSearcher(reader);
            
            String x1 = String.format("%016X", pattern.get(0).getId()) ;
            String x2 = String.format("%016X", pattern.get(0).getId()) ;
            
            Term term1  = new Term("tuple", x1) ;
            Term term2  = new Term("tuple", x2) ;
            Query query = new RangeQuery(term1, term2, false) ;
            
            Hits hits = searcher.search(query) ;
            @SuppressWarnings("unchecked")
            Iterator<Hit> iterHits = hits.iterator() ;
            for ( ; iterHits.hasNext(); )
            {
                System.out.println(iterHits.next().toString()) ;
            }

        } catch (Exception e)
        { throw new ARQLuceneException("search", e) ; }
        return null ;
    }


    //@Override
    public String getLabel()
    {
        return null ;
    }


    //@Override
    public int getTupleLength()
    {
        return 0 ;
    }


    //@Override
    public boolean isEmpty()
    {
        return false ;
    }


    //@Override
    public void clear()
    {}


    //@Override
    public long size()
    {
        return 0 ;
    }


    //@Override
    public int weight(Tuple<NodeId> pattern)
    {
        return 0 ;
    }

    //@Override
    public void sync()
    {}


    //@Override
    public void sync(boolean force)
    {}


    //@Override
    public void close()
    {}
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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