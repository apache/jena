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
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadAction ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class TextDocProducerTriples implements TextDocProducer
{
    private static Logger log = LoggerFactory.getLogger(TextDocProducerTriples.class) ;
    private final EntityDefinition defn ;
    private final TextIndex indexer ;
    private boolean started = false ;
    
    public TextDocProducerTriples(EntityDefinition defn, TextIndex indexer)
    {
        this.defn = defn ;
        this.indexer = indexer ;
    }
    
    @Override
    public void start()
    { indexer.startIndexing() ; started = true ;}

    @Override
    public void finish()
    { indexer.finishIndexing() ; }

    @Override
    public void change(QuadAction qaction, Node g, Node s, Node p, Node o)
    {
        // One document per triple/quad
        
        if ( qaction != QuadAction.ADD)
            return ;
        
        String field = defn.getField(p) ;
        if ( field == null )
            return ;

        String x = (s.isURI() ) ? s.getURI() : s.getBlankNodeLabel() ;
        Entity entity = new Entity(x) ;

        if ( ! o.isLiteral() )
        {
            log.warn("Not a literal value for mapped field-predicate: "+field+" :: "+FmtUtils.stringForString(field)) ;
            return ;
        }
        entity.put(field, o.getLiteralLexicalForm()) ;
        indexer.addEntity(entity) ;
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

