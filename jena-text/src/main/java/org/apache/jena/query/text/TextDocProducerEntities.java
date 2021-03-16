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

package org.apache.jena.query.text ;

import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.text.changes.ChangesBatched;
import org.apache.jena.query.text.changes.TextQuadAction;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.util.FmtUtils ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

// Currently unused
// This would index multiple quads at a time from batched stream of changes (e.g. rdf-patch)
public class TextDocProducerEntities extends ChangesBatched implements TextDocProducer {
    private static Logger          log     = LoggerFactory.getLogger(TextDocProducer.class) ;
    private final EntityDefinition defn ;
    private final TextIndex        indexer ;

    // Have to have a ThreadLocal here to keep track of whether or not we are in a transaction,
    // therefore whether or not we have to do autocommit
    private final ThreadLocal<Boolean> inTransaction = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE ;
        }
    } ;

    public TextDocProducerEntities(TextIndex indexer) {
        this.defn = indexer.getDocDef() ;
        this.indexer = indexer ;
        inTransaction.set(false) ;
    }

    @Override
    protected void startBatched() {
        inTransaction.set(true) ;
    }

    @Override
    protected void finishBatched() {
        inTransaction.set(false) ;
    }

    @Override
    protected void dispatch(TextQuadAction quadAction, List<Quad> batch) {
        if ( !inTransaction.get() )
            throw new IllegalStateException("Not started") ;
        if ( !TextQuadAction.ADD.equals(quadAction) )
            return ;
        if ( batch.size() == 0 )
            return ;
        if ( false ) {
            // One document per entity - future possibility.
            Quad q = batch.get(0) ;
            Node g = q.getGraph() ;
            Node s = q.getSubject() ;
            docEntity(g, s, batch) ;
            return ;
        }
        docQuads(batch) ; // Does not need batching.
    }

    private void docEntity(Node g, Node s, List<Quad> batch) {
        // One document per entity
        String x = TextQueryFuncs.subjectToString(s) ;
        String gx = TextQueryFuncs.graphNodeToString(g) ;
        Entity entity = new Entity(x, gx) ;
        String graphField = defn.getGraphField() ;
        if ( defn.getGraphField() != null )
            entity.put(graphField, gx) ;

        for ( Quad quad : batch ) {
            Node p = quad.getPredicate() ;
            String field = defn.getField(p) ;
            if ( field == null )
                continue ;
            Node o = quad.getObject() ;
            String val = null ;
            if ( o.isURI() )
                val = o.getURI() ;
            else if ( o.isLiteral() )
                val = o.getLiteralLexicalForm() ;
            else {
                log.warn("Not a literal value for mapped field-predicate: " + field + " :: "
                         + FmtUtils.stringForString(field)) ;
                continue ;
            }
            entity.put(field, val) ;
        }
        indexer.addEntity(entity) ;
    }

    private void docQuads(List<Quad> batch) {

        // One document per triple/quad
        for ( Quad quad : batch ) {
            Entity entity = TextQueryFuncs.entityFromQuad(defn, quad) ;
            if ( entity != null )
                indexer.addEntity(entity) ;
        }
    }
}
