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

package org.apache.jena.larq;

import java.io.File;

import org.apache.lucene.index.IndexWriter;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/** Root class for index creation from a graph or model.  This class
 *  can be used as a Model listener to index while loading data.  It also
 *  provides the ability to index from a StmtIterator.
 *  Once completed, the index builder should be closed for writing,
 *  then the getIndex() called.
 *  To update the index once closed, the application should create a new index builder.
 *  Any index readers (e.g. IndexLARQ objects)
 *  need to be recreated and registered. */

public abstract class IndexBuilderModel extends StatementListener
{
    // Multiple inheritance would be nice .
    protected IndexBuilderNode index ;
    
    /** Create an in-memory index */
    public IndexBuilderModel()
    { index = new IndexBuilderNode() ; }
    
    /** Manage a Lucene index that has already been created */
    public IndexBuilderModel(IndexWriter existingWriter)
    { index = new IndexBuilderNode(existingWriter) ; }

    /** Create an on-disk index */
    public IndexBuilderModel(File fileDir)
    { index = new IndexBuilderNode(fileDir) ; }
    
    /** Create an on-disk index */
    public IndexBuilderModel(String fileDir)
    { index = new IndexBuilderNode(fileDir) ; }

//    protected IndexWriter getIndexWriter() { return index.getIndexWriter() ; }
//    protected IndexReader getIndexReader() { return index.getIndexReader() ; }
    
    public boolean avoidDuplicates() {
        return index.avoidDuplicates() ;
    }

    public void setAvoidDuplicates(boolean avoid) {
        index.setAvoidDuplicates(avoid) ;
    }

    /** ModelListener interface : statement taken out of the model */
    @Override
    public void removedStatement(Statement s)
    { unindexStatement(s) ; }

    /** ModelListener interface : statement added to the model */
    @Override
    public void addedStatement(Statement s)
    { indexStatement(s) ; }
    
    /** Index all the statements from a StmtIterator */
    public void indexStatements(StmtIterator sIter)
    {
        while ( sIter.hasNext() )
            indexStatement(sIter.nextStatement()) ;
    }
    
    /** Update index based on one statement */
    public abstract void indexStatement(Statement s) ;
   
    /** Remove index information */
    public abstract void unindexStatement(Statement s) ;
    
    /** Flush the index, optimizing it, to allow a reader to be created */
    public void flushWriter() { index.flushWriter() ; }
    
    /** Close the index - no more updates possible */
    public void closeWriter() { index.closeWriter() ; }

    /** Get a search index used by LARQ. */
    public IndexLARQ getIndex()
    { return index.getIndex() ; }

}
