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

import java.io.Reader ;

import org.apache.lucene.document.Document ;
import org.apache.lucene.document.Field ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Node_Blank ;
import com.hp.hpl.jena.graph.Node_Literal ;
import com.hp.hpl.jena.graph.Node_URI ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class LARQ
{
    public static void init()   {} 
    
    // The field that is the index
    public static final String fIndex               = "index" ;
    
    // Object literals
    public static final String fLex                 = "lex" ;
    public static final String fLang                = "lang" ;
    public static final String fDataType            = "datatype" ;
    // Object URI
    public static final String fURI                 = "uri" ;
    // Object bnode
    public static final String fBNodeID             = "bnode" ;

    // The symbol used to register the index in the query context
    public static final Symbol indexKey     = ARQConstants.allocSymbol("lucene") ;

    public static void setDefaultIndex(IndexLARQ index)
    { setDefaultIndex(ARQ.getContext(), index) ; }
    
    public static void setDefaultIndex(Context context, IndexLARQ index)
    { context.set(LARQ.indexKey, index) ; }
    
    public static IndexLARQ getDefaultIndex()
    { return getDefaultIndex(ARQ.getContext()) ; }
    
    public static IndexLARQ getDefaultIndex(Context context)
    { return (IndexLARQ)context.get(LARQ.indexKey) ; }
    
    public static void removeDefaultIndex()
    { removeDefaultIndex(ARQ.getContext()) ; }
    
    public static void removeDefaultIndex(Context context)
    { context.unset(LARQ.indexKey) ; }

    public static void index(Document doc, Node indexNode)
    {
        if ( ! indexNode.isLiteral() )
            throw new ARQLuceneException("Not a literal: "+indexNode) ;
        index(doc, indexNode.getLiteralLexicalForm()) ;
    }        
     
    public static void index(Document doc, String indexContent)
    {
        Field indexField = new Field(LARQ.fIndex, indexContent,
                                     Field.Store.NO, Field.Index.TOKENIZED) ;
        doc.add(indexField) ;
    }        
     
    public static void index(Document doc, Reader indexContent)
    {
        Field indexField = new Field(LARQ.fIndex, indexContent) ;
        doc.add(indexField) ;
    }        

    public static void store(Document doc, Node node)
    {
        // Store.
        if ( node.isLiteral() )
            storeLiteral(doc, (Node_Literal)node) ;
        else if ( node.isURI() )
            storeURI(doc, (Node_URI)node) ;
        else if ( node.isBlank() )
            storeBNode(doc, (Node_Blank)node) ;
        else
            throw new ARQLuceneException("Can't store: "+node) ;
    }

    public static Node build(Document doc)
    {
        String lex = doc.get(LARQ.fLex) ;
        if ( lex != null )
            return buildLiteral(doc) ;
        String uri = doc.get(LARQ.fURI) ;
        if ( uri != null )
            return Node.createURI(uri) ;
        String bnode = doc.get(LARQ.fBNodeID) ;
        if ( bnode != null )
            return Node.createAnon(new AnonId(bnode)) ;
        throw new ARQLuceneException("Can't build: "+doc) ;
    }

    public static boolean isString(Literal literal)
    {
        RDFDatatype dtype = literal.getDatatype() ;
        if ( dtype == null )
            return true ;
        if ( dtype.equals(XSDDatatype.XSDstring) )
            return true ;
        return false ;
    }
    
    private static void storeURI(Document doc, Node_URI node)
    { 
        String x = node.getURI() ;
        Field f = new Field(LARQ.fIndex, x, Field.Store.NO, Field.Index.TOKENIZED) ;
        doc.add(f) ;
        f = new Field(LARQ.fURI, x, Field.Store.YES, Field.Index.NO) ;
        doc.add(f) ;
    }

    private static void storeBNode(Document doc, Node_Blank node)
    { 
        String x = node.getBlankNodeLabel() ;
        Field f = new Field(LARQ.fIndex, x, Field.Store.NO, Field.Index.TOKENIZED) ;
        doc.add(f) ;
        f = new Field(LARQ.fBNodeID, x, Field.Store.YES, Field.Index.NO) ;
        doc.add(f) ;
    }
    
    private static void storeLiteral(Document doc, Node_Literal node)
    {
        String lex = node.getLiteralLexicalForm() ;
        String datatype = node.getLiteralDatatypeURI() ;
        String lang = node.getLiteralLanguage() ;

        Field f = new Field(LARQ.fLex, lex, Field.Store.YES, Field.Index.NO) ;
        doc.add(f) ;
        
        if ( lang != null )
        {
            f = new Field(LARQ.fLang, lang, Field.Store.YES, Field.Index.NO) ;
            doc.add(f) ;
        }

        if ( datatype != null )
        {       
            f = new Field(LARQ.fDataType, datatype, Field.Store.YES, Field.Index.NO) ;
            doc.add(f) ;
        }
    }
    
    private static Node buildLiteral(Document doc)
    {
        String lex = doc.get(LARQ.fLex) ;
        if ( lex == null )
            return null ;
        String datatype = doc.get(LARQ.fDataType) ;
        String lang = doc.get(LARQ.fLang) ;
        return NodeFactory.createLiteralNode(lex, lang, datatype) ;
    }
}
