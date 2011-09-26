/**
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

package com.hp.hpl.jena.query.larq;

import java.io.File ;
import java.io.IOException ;

import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.index.IndexReader ;
import org.apache.lucene.index.IndexWriter ;
import org.apache.lucene.store.Directory ;
import org.apache.lucene.store.FSDirectory ;
import org.apache.lucene.store.RAMDirectory ;

/** Root class for index creation. */

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
