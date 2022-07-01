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

import java.util.Collections ;
import java.util.LinkedList ;
import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Quad;

/** Capture a record of quad actions
 */
public class ChangesCapture implements TextDatasetChanges {
    // ArrayLists have an annoying issue that they grow by copying the internal
    // []-array.
    // This growth is by a fixed factor of adding 50% which for an array
    // with little guidance as to likely size, can lead to undesirable GC
    // and copy-time issues.
    // Using a LinkedList avoids this although it adds overhead for list
    // entries.
    private List<Pair<TextQuadAction, Quad>> actions ;
    final private boolean                captureAdd ;
    final private boolean                captureDelete ;
    final private boolean                captureNoAdd ;
    final private boolean                captureNoDelete ;

    /** Capture quad actions, excluding no-ops */
    public ChangesCapture() {
        this(true, true, false, false) ;
    }

    /**
     * Capture quad actions, either including or excluding the "no ops"
     *
     * @param recordNoOps
     *            Whether to record {@link TextQuadAction#NO_ADD} and
     *            {@link TextQuadAction#NO_DELETE}
     */
    public ChangesCapture(boolean recordNoOps) {
        this(true, true, recordNoOps, recordNoOps) ;
    }

    /** Capture quad actions, selectively by category */
    public ChangesCapture(boolean captureAdd, boolean captureDelete, boolean captureNoAdd, boolean captureNoDelete) {
        this.captureAdd = captureAdd ;
        this.captureDelete = captureDelete ;
        this.captureNoAdd = captureNoAdd ;
        this.captureNoDelete = captureNoDelete ;
        this.actions = new LinkedList<>() ;
    }

    /** The actions recorded.
     *  Only valid until the next {@code start} call.
     */
    public List<Pair<TextQuadAction, Quad>> getActions() {
        return Collections.unmodifiableList(actions) ;
    }

    @Override
    public void start() {
        if ( actions == null )
            actions = new LinkedList<>() ;
    }

    @Override
    public void change(TextQuadAction qaction, Node g, Node s, Node p, Node o) {
        Quad q = new Quad(g, s, p, o) ;
        Pair<TextQuadAction, Quad> pair = Pair.create(qaction, q) ;

        switch (qaction) {
            case ADD :
                if ( captureAdd )
                    actions.add(pair) ;
                break ;
            case DELETE :
                if ( captureDelete )
                    actions.add(pair) ;
                break ;
            case NO_ADD :
                if ( captureNoAdd )
                    actions.add(pair) ;
                break ;
            case NO_DELETE :
                if ( captureNoDelete )
                    actions.add(pair) ;
                break ;
        }
    }

    @Override
    public void finish() {}

    @Override
    public void reset() {
        if ( actions != null )
            actions.clear() ;
        actions = null ;
    }
}
