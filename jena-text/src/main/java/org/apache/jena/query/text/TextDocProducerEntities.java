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

package org.apache.jena.query.text;

import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetChangesBatched ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadAction ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class TextDocProducerEntities extends DatasetChangesBatched implements TextDocProducer
{
    private static Logger log = LoggerFactory.getLogger(TextDocProducer.class) ;
    private final EntityDefinition defn ;
    private final TextIndex indexer ;
    private boolean started = false ;
    
    public TextDocProducerEntities(EntityDefinition defn, TextIndex indexer)
    {
        this.defn = defn ;
        this.indexer = indexer ;
    }
    
    @Override
    protected void startBatched()
    { indexer.startIndexing() ; started = true ;}

    @Override
    protected void finishBatched()
    { indexer.finishIndexing() ; }

    @Override
    protected void dispatch(QuadAction quadAction, List<Quad> batch)
    {
        if ( ! started )
            throw new IllegalStateException("Not started") ;
        if ( ! QuadAction.ADD.equals(quadAction) )
            return ;
        if ( batch.size() == 0 )
            return ;
        Quad q = batch.get(0) ;
        Node g = q.getGraph() ;
        Node s = q.getSubject() ;
        List<Triple> triples = quadsToTriples(batch) ;
        //docEntity(s, triples) ;   // One docment per entity - future possibility.
        docTriples(s,triples) ; // Does not need batching.
    }

    private void docEntity(Node s, List<Triple> batch)
    {
        // One document per entity
        
        String x = (s.isURI() ) ? s.getURI() : s.getBlankNodeLabel() ;
        Entity entity = new Entity(x) ;
        for ( Triple triple : batch )
        {
            Node p = triple.getPredicate() ;
            String field = defn.getField(p) ;
            if ( field == null )
                continue ;
            Node o = triple.getObject() ;
            String val = null ;
            if ( o.isURI() )
                val = o.getURI() ;
            else if ( o.isLiteral() )
                val = o.getLiteralLexicalForm() ;
            else
            {
                log.warn("Not a literal value for mapped field-predicate: "+field+" :: "+FmtUtils.stringForString(field)) ;
                continue ;
            }
            entity.put(field, val) ;
        }
        indexer.addEntity(entity) ;
    }

    private void docTriples(Node s, List<Triple> batch)
    {
        String x = (s.isURI() ) ? s.getURI() : s.getBlankNodeLabel() ;
        // One document per triple.
        for ( Triple triple : batch )
        {
            Entity entity = new Entity(x) ;
            Node p = triple.getPredicate() ;
            String field = defn.getField(p) ;
            if ( field == null )
                continue ;
            Node o = triple.getObject() ;
            if ( ! o.isLiteral() )
            {
                log.warn("Not a literal value for mapped field-predicate: "+field+" :: "+FmtUtils.stringForString(field)) ;
                continue ;
            }
            entity.put(field, o.getLiteralLexicalForm()) ;
            indexer.addEntity(entity) ;
        }
    }

    static Transform<Quad, Triple> QuadsToTriples = new Transform<Quad, Triple>() 
    {
        @Override
        public Triple convert(Quad item)
        {
            return item.asTriple() ;
        }
        
    } ;
    
    static private List<Triple> quadsToTriples(List<Quad> quads) { return Iter.map(quads, QuadsToTriples) ; } 
}

