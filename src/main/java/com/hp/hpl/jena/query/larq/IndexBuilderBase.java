/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

/** Root class for index creation.
 *  
 * @author Andy Seaborne
 */

public class IndexBuilderBase implements IndexBuilder 
{
    private Directory dir = null ;

    // Use this for incremental indexing?
    //private IndexModifier modifier ;

    private IndexWriter indexWriter = null ;
    //private IndexReader indexReader = null ;

    //private boolean isClosed ;

    /** Create an in-memory index */
    
    public IndexBuilderBase()
    {
        dir = new RAMDirectory() ;
        makeIndex() ;
    }
    
    /** Manage a Lucene index that has already been created */
    
    public IndexBuilderBase(IndexWriter existingWriter)
    {
        dir = existingWriter.getDirectory() ;
        indexWriter = existingWriter ;
    }
    
    /** Create an on-disk index */
    
    public IndexBuilderBase(File fileDir)
    {
        try {
            dir = FSDirectory.getDirectory(fileDir);
            makeIndex() ;
        } catch (Exception ex)
        { throw new ARQLuceneException("IndexBuilderLARQ", ex) ; }
        
    }
    
    /** Create an on-disk index */

    public IndexBuilderBase(String fileDir)
    {
        try {
            dir = FSDirectory.getDirectory(fileDir);
            makeIndex() ;
        } catch (Exception ex)
        { throw new ARQLuceneException("IndexBuilderLARQ", ex) ; }
    }

    private void makeIndex()
    {
        try {
            indexWriter = new IndexWriter(dir, new StandardAnalyzer()) ;
        } catch (Exception ex)
        { throw new ARQLuceneException("IndexBuilderLARQ", ex) ; }
    }

    protected IndexWriter getIndexWriter() { return indexWriter ; }
    
    protected IndexReader getIndexReader()
    {
        // Always return a new reader.  Write may have changed.
        try {
            flushWriter() ;
            return IndexReader.open(dir) ;
        } catch (Exception e) { throw new ARQLuceneException("getIndexReader", e) ; }
    }
    
    /** Close the writing index permanently.  Optimizes the index. */ 
    
    public void closeWriter()    { closeWriter(true) ; }

    /** Close the writing index permanently.
     * @param optimize  Run Lucene optimize on the index before closing.
     */ 
    
    public void closeWriter(boolean optimize)
    {
        if ( optimize ) 
            flushWriter() ;
        try {
            if ( indexWriter != null ) indexWriter.close();
        }
        catch (IOException ex) { throw new ARQLuceneException("closeIndex", ex) ; }
        indexWriter = null ;
   }
    
    public void flushWriter()
    { 
        try { if ( indexWriter != null ) indexWriter.optimize(); }
        catch (IOException ex) { throw new ARQLuceneException("flushWriter", ex) ; }
    }
    
    /** Get a search index used by LARQ */
    
    public IndexLARQ getIndex()
    {
    	// In Lucene, an index reader sees the index at a point in time.
    	// This wil not see later updates.
        //ARQ 2.2 : no longer close the index.  closeForWriting() ;
        return new IndexLARQ(getIndexReader()) ;
    }
    

}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
