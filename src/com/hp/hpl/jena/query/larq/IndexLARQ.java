/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.larq;


import java.util.Iterator ;

import org.apache.lucene.analysis.Analyzer ;
import org.apache.lucene.analysis.standard.StandardAnalyzer ;
import org.apache.lucene.index.IndexReader ;
import org.apache.lucene.queryParser.QueryParser ;
import org.apache.lucene.search.Hit ;
import org.apache.lucene.search.Hits ;
import org.apache.lucene.search.IndexSearcher ;
import org.apache.lucene.search.Query ;
import org.apache.lucene.search.Searcher ;
import org.openjena.atlas.iterator.IteratorTruncate ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.NodeIterator ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;
import com.hp.hpl.jena.util.iterator.Map1 ;
import com.hp.hpl.jena.util.iterator.Map1Iterator ;


/** ARQ wrapper for a Lucene index. */
public class IndexLARQ
{
    protected final IndexReader reader ;
    protected final QueryParser luceneQueryParser ;
    protected final Analyzer analyzer ;

    public IndexLARQ(IndexReader r)
    { 
        this(r, new StandardAnalyzer()) ;
    }
        
    public IndexLARQ(IndexReader r, Analyzer a)
    { 
        //this(r, new QueryParser(LARQ.fIndex, a)) ;
        reader = r ;
        analyzer = a ;
        luceneQueryParser = null ;
        
    }
    
    @Deprecated
    /** Passing in a fixed QueryParser is not thread safe */
    public IndexLARQ(IndexReader r, QueryParser qp)
    { 
        reader = r ;
        analyzer = qp.getAnalyzer() ;
        luceneQueryParser = qp ;
    }
    
    /** Perform a free text Lucene search and return a NodeIterator.
     * 
     * @param queryString
     * @return NodeIterator
     */ 

    public NodeIterator searchModelByIndex(String queryString)
    { return searchModelByIndex(null, queryString) ; }
    
    /** Perform a free text Lucene search and return a NodeIterator. The RDFNodes
     * in the iterator are associated with the model supplied.
     * 
     * @param model
     * @param queryString
     * @return NodeIterator
     */ 

    public NodeIterator searchModelByIndex(Model model, String queryString)
    { return searchModelByIndex(model, queryString, 0.0f) ; }

    /** Perform a free text Lucene search and return a NodeIterator. The RDFNodes
     * in the iterator are associated with the model supplied.
     * 
     * @param model
     * @param queryString
     * @param scoreLimit    Minimum Lucene score
     * @return NodeIterator
     */ 

    public NodeIterator searchModelByIndex(final Model model, String queryString, final float scoreLimit)
    {
        Map1<HitLARQ, RDFNode> converter = new Map1<HitLARQ, RDFNode>(){
            public RDFNode map1(HitLARQ x)
            {
                return ModelUtils.convertGraphNodeToRDFNode(x.getNode(), model) ;
            }} ;
        
        Iterator<RDFNode> iter = new Map1Iterator<HitLARQ, RDFNode>(converter, search(queryString)) ;
        if ( scoreLimit > 0 )
            iter = new IteratorTruncate<RDFNode>(new ScoreTest(scoreLimit), iter) ;
        
        NodeIterator nIter = new NodeIteratorImpl(iter, null) ; 
        return nIter ;
    }
    
    /** test whether the index matches for the given Lucene query string */ 
    
    public boolean hasMatch(String queryString)
    {
        Iterator<HitLARQ> iter = search(queryString) ;
        return iter.hasNext(); 
    }
    
    /** Perform a free text Lucene search and returns an iterator of graph Nodes.   
     *  Applications normally call searchModelByIndex.
     *  @param queryString
     *  @return Iterator of hits (Graph node and score)
     */

    public Iterator<HitLARQ> search(String queryString)
    {    
        try{
            Searcher searcher = new IndexSearcher(reader);
            
            Query query = getLuceneQueryParser().parse(queryString) ;
            
            Hits hits = searcher.search(query) ;
            
            Map1<Hit,HitLARQ> converter = new Map1<Hit,HitLARQ>(){
                public HitLARQ map1(Hit object)
                {
                    return new HitLARQ(object) ;
                }} ;
            @SuppressWarnings("unchecked")
            Iterator<Hit> iterHits = hits.iterator() ;
            Iterator<HitLARQ> iter = new Map1Iterator<Hit, HitLARQ>(converter, iterHits) ;
            return iter ;
            
        } catch (Exception e)
        { throw new ARQLuceneException("search", e) ; }
    }
    
    /** Check whether an index recognizes a node.   
     * @param node
     * @param queryString
     * @return boolean 
     */
    public HitLARQ contains(Node node, String queryString)
    {
        try{
            Iterator<HitLARQ> iter = search(queryString) ;
            for ( ; iter.hasNext() ; )
            {
                HitLARQ x = iter.next();
                if ( x != null && x.getNode().equals(node)) 
                    return x ;
            }
            return null ;
        } catch (Exception e)
        { throw new ARQLuceneException("contains", e) ; }
    }
    
    public void close()
    {
        try{
            if ( reader != null )
                reader.close() ;
        } catch (Exception e)
        { throw new ARQLuceneException("close", e) ; }
    }

    /** Return the Lucene IndexReader for this LARQ index */ 
    public final IndexReader getLuceneReader()
    {
        return reader ;
    }

    /** Return the Lucene QueryParser for this LARQ index */ 
    public final QueryParser getLuceneQueryParser()
    {
        if ( luceneQueryParser != null )
            return luceneQueryParser ;
        // Creating a new parser makes this class thread safe for search()
        return new QueryParser(LARQ.fIndex, analyzer) ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * 
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