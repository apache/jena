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

import org.apache.jena.graph.Node ;
import org.apache.jena.query.text.changes.TextQuadAction;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TextDocProducerTriples implements TextDocProducer {
    private static Logger          log     = LoggerFactory.getLogger(TextDocProducerTriples.class) ;
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

    public TextDocProducerTriples(TextIndex indexer) {
        this.defn = indexer.getDocDef() ;
        this.indexer = indexer ;
    }

    @Override
    public void start() {
        inTransaction.set(true) ;
    }

    @Override
    public void finish() {
        inTransaction.set(false) ;
    }

    @Override
    public void reset() { }

    @Override
    public void change(TextQuadAction qaction, Node g, Node s, Node p, Node o) {
        // One document per triple/quad

        if ( qaction != TextQuadAction.ADD &&
             qaction != TextQuadAction.DELETE )
            return ;

        Entity entity = TextQueryFuncs.entityFromQuad(defn, g, s, p, o) ;
        // Null means does not match defn
        if ( entity != null ) {
            if (qaction == TextQuadAction.ADD) {
                indexer.addEntity(entity);

                // Auto commit the entity if we aren't in a transaction
                if (!inTransaction.get())
                    indexer.commit();
            }
            else if (qaction == TextQuadAction.DELETE) {
                indexer.deleteEntity(entity);

                // Auto commit the entity if we aren't in a transaction
                if (!inTransaction.get())
                    indexer.commit();
            }
        }
    }
}
