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
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Query;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.RDFNode;

/** Helper class for index creation from external content.
 *  
 *  Once completed, the index builder should be closed for writing,
 *  then the getIndex() called.
 *  
 *  To update the index once closed, the application should create a new index builder.
 *  Any index readers (e.g. IndexLARQ objects)
 *  need to be recreated and registered. 
 */

public class IndexBuilderNode extends IndexBuilderBase
{
    /** Create an in-memory index */
    public IndexBuilderNode() { super() ; }
    
    /** Manage a Lucene index that has already been created */
    public IndexBuilderNode(IndexWriter existingWriter) { super(existingWriter) ; }
    
    /** Create an on-disk index */
    public IndexBuilderNode(File fileDir) { super(fileDir) ; }
    
    /** Create an on-disk index */
    public IndexBuilderNode(String fileDir) { super(fileDir) ; }

    public void index(RDFNode rdfNode, String indexStr)
    {
        try {
        	if ( avoidDuplicates() ) unindex(rdfNode, indexStr);
            Document doc = new Document() ;
            LARQ.store(doc, rdfNode.asNode()) ;
            LARQ.index(doc, rdfNode.asNode(), indexStr) ;
            getIndexWriter().addDocument(doc) ;
        } catch (IOException ex)
        { throw new ARQLuceneException("index", ex) ; }
    }
   
    public void index(RDFNode rdfNode, Reader indexStream)
    {
        try {
        	if ( avoidDuplicates() ) unindex(rdfNode, indexStream);
            Document doc = new Document() ;
            LARQ.store(doc, rdfNode.asNode()) ;
            LARQ.index(doc, rdfNode.asNode(), indexStream) ;
            getIndexWriter().addDocument(doc) ;
        } catch (IOException ex)
        { throw new ARQLuceneException("index", ex) ; }
    }
    
    public void index(Node node, String indexStr)
    {
        try {
        	if ( avoidDuplicates() ) unindex(node, indexStr);
            Document doc = new Document() ;
            LARQ.store(doc, node) ;
            LARQ.index(doc, node, indexStr) ;
            getIndexWriter().addDocument(doc) ;
        } catch (IOException ex)
        { throw new ARQLuceneException("index", ex) ; }
    }
   
    public void index(Node node, Reader indexStream)
    {
        try {
        	if ( avoidDuplicates() ) unindex(node, indexStream);
            Document doc = new Document() ;
            LARQ.store(doc, node) ;
            LARQ.index(doc, node, indexStream) ;
            getIndexWriter().addDocument(doc) ;
        } catch (IOException ex)
        { throw new ARQLuceneException("index", ex) ; }
    }
    
    public void unindex(RDFNode rdfNode, String indexStr)
    {
        try {
            Query query = LARQ.unindex(rdfNode.asNode(), indexStr);
            getIndexWriter().deleteDocuments(query);
        } catch (Exception ex)
        { throw new ARQLuceneException("unindex", ex) ; } 
    }
    
    public void unindex(Node node, String indexStr)
    {
        try {
            Query query = LARQ.unindex(node, indexStr);
            getIndexWriter().deleteDocuments(query);
        } catch (Exception ex)
        { throw new ARQLuceneException("unindex", ex) ; } 
    }

    public void unindex(RDFNode rdfNode, Reader inputStream)
    {
    	unindex(rdfNode.asNode(), inputStream) ;
    }
    
    public void unindex(Node node, Reader inputStream)
    {
        try {
            Query query = LARQ.unindex(node, inputStream);
            getIndexWriter().deleteDocuments(query);
        } catch (Exception ex)
        { throw new ARQLuceneException("unindex", ex) ; } 
    }
    
}
