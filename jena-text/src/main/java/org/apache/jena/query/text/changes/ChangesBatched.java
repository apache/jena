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

package org.apache.jena.query.text.changes ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Objects ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Quad;

/**
 * Collect a stream of DatasetChanges into batches. A batch is adjacent quads
 * changes with (same graph, same subject, same action).
 */
public abstract class ChangesBatched implements TextDatasetChanges {
    private TextQuadAction currentAction   = null ;
    private Node       currentSubject  = null ;
    private Node       currentGraph    = null ;
    private List<Quad> batchQuads      = null ;
    private boolean    mergeBlankNodes = false ;

    protected ChangesBatched() {
        this(false) ;
    }

    /*
     * Merge bNodes in a batch - i.e. include them in the current batch, not as
     * new entities
     */
    protected ChangesBatched(boolean mergeBNodes) {
        this.mergeBlankNodes = mergeBNodes ;
    }

    @Override
    public final void start() {
        startBatched() ;
        startBatch() ;
    }

    @Override
    public final void finish() {
        finishBatch() ;
        finishBatched() ;
    }

    @Override
    public final void reset() { }

    @Override
    public void change(TextQuadAction qaction, Node g, Node s, Node p, Node o) {
        if ( mergeBlankNodes && s.isBlank() ) {
            if ( batchQuads == null )
                // No active batch.
                startBatch() ;
            // Drop and through and include in the current batch.
        } else if ( !Objects.equals(currentAction, qaction) || !Objects.equals(currentGraph, g) || !Objects.equals(currentSubject, s) ) {
            finishBatch() ;
            startBatch() ;
            currentAction = qaction ;
            currentGraph = g ;
            currentSubject = s ;
        }

        batchQuads.add(new Quad(g, s, p, o)) ;
    }

    private void startBatch() {
        if ( batchQuads == null )
            batchQuads = new ArrayList<>() ;
    }

    protected void finishBatch() {
        if ( batchQuads == null || batchQuads.size() == 0 )
            return ;
        dispatch(currentAction, batchQuads) ;
        batchQuads = null ;
    }

    protected abstract void dispatch(TextQuadAction quadAction, List<Quad> batch) ;

    protected abstract void startBatched() ;

    protected abstract void finishBatched() ;

}
