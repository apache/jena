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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/** 
 * Base class for indexing literals (i.e. index is a literal and the 
 * index returns the literal)
 */

public abstract class IndexBuilderLiteral extends IndexBuilderModel
{
    public IndexBuilderLiteral()
    { super() ; }

    public IndexBuilderLiteral(IndexWriter existingWriter)
    { super(existingWriter) ; }
    
    public IndexBuilderLiteral(File fileDir)
    { super(fileDir) ; }
    
    public IndexBuilderLiteral(String fileDir)
    { super(fileDir) ; }

    /** Test whether to index this literal */
    protected abstract boolean indexThisLiteral(Literal literal) ;
    
    /** Condition to filter statements passed to the indexStatement */
    protected abstract boolean indexThisStatement(Statement stmt) ;

    @Override
    public void unindexStatement(Statement s)
    { 
        if ( ! indexThisStatement(s) )
            return ;

        if ( s.getObject().isLiteral() )
        {
        	// we use the Model as reference counting
        	StmtIterator iter = s.getModel().listStatements((Resource)null, (Property)null, s.getObject());
        	if ( ! iter.hasNext() ) {
                Node node = s.getObject().asNode() ;
                if ( indexThisLiteral(s.getLiteral())) {
                	index.unindex(node, node.getLiteralLexicalForm()) ;
                }
        	}
        }
    }
    
    @Override
    public void indexStatement(Statement s)
    {
        if ( ! indexThisStatement(s) )
            return ;
        
        try {
            if ( s.getObject().isLiteral() )
            {
                Node node = s.getObject().asNode() ;
                
                if ( indexThisLiteral(s.getLiteral()))
                    index.index(node, node.getLiteralLexicalForm()) ;
            }
        } catch (Exception e)
        { throw new ARQLuceneException("indexStatement", e) ; }
    }
   
    /** Close the index - no more updates possible */
    @Override
    public void closeWriter()
    { 
        super.closeWriter() ;
    }
    
}
