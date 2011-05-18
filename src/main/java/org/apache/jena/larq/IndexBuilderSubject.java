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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/** 
 * Class for indexing by subject (i.e. index is a literal and the 
 * index returns the subject).  Often the application can provide an
 * additional property to further restrict what gets indexed. */

public class IndexBuilderSubject extends IndexBuilderModel
{
    Property property ;
    
    public IndexBuilderSubject()
    { super() ; }

    public IndexBuilderSubject(IndexWriter existingWriter)
    { super(existingWriter) ; }
    
    
    public IndexBuilderSubject(File fileDir)
    { super(fileDir) ; }
    
    public IndexBuilderSubject(String fileDir)
    { super(fileDir) ; }

    public IndexBuilderSubject(Property p)
    {
        this() ;
        property = p ;
    }

    public IndexBuilderSubject(Property p, IndexWriter existingWriter)
    { 
        super(existingWriter) ;
        property = p ;
    }
    
    public IndexBuilderSubject(Property p, File fileDir)
    {
        this(fileDir) ;
        property = p ;
    }
    
    public IndexBuilderSubject(Property p, String fileDir)
    {
        this(fileDir) ;
        property = p ;
    }
    
    @Override
    public void unindexStatement(Statement s)
    {
        if ( ! indexThisStatement(s) )
            return ;

        try {
            Node subject = s.getSubject().asNode() ;

            if ( ! s.getObject().isLiteral() || ! LARQ.isString(s.getLiteral()) )
                return ;

        	StmtIterator iter = s.getModel().listStatements(s.getSubject(), (Property)null, s.getObject());
        	if ( ! iter.hasNext() ) {
                Node object  = s.getObject().asNode() ;
                index.unindex(subject, object.getLiteralLexicalForm()) ;
        	}
        } catch (Exception e)
        { throw new ARQLuceneException("unindexStatement", e) ; }
    }
    
    @Override
    public void indexStatement(Statement s)
    {
        if ( ! indexThisStatement(s) )
            return ;
        
        try {
            Node subject = s.getSubject().asNode() ;

            if ( ! s.getObject().isLiteral() ||
                 ! LARQ.isString(s.getLiteral()) )
                return ;
            
            Node object  = s.getObject().asNode() ;
            
            // Note: if a subject occurs twice with an indexable string,
            // there will be two hits later.
            index.index(subject, object.getLiteralLexicalForm()) ;
        } catch (Exception e)
        { throw new ARQLuceneException("indexStatement", e) ; }
    }

    protected boolean indexThisStatement(Statement s)
    {  
        if ( property == null ) 
            return true ;
        return s.getPredicate().equals(property) ;
    }
}
