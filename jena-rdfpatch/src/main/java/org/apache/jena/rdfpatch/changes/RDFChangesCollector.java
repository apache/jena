/*
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

package org.apache.jena.rdfpatch.changes;

import java.util.*;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfpatch.PatchHeader;
import org.apache.jena.rdfpatch.RDFChanges;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.items.*;

/** Capture a stream of changes, then play it to another {@link RDFChanges} */
public class RDFChangesCollector implements RDFChanges {
    // NB begin - then set headers correctly becomes headers then begin.
    // This is intentional so headers can be set after the patch log starts.
    // But use with care.
    private static final boolean RECORD_HEADER = false;
    private Map<String, Node> header = new LinkedHashMap<>();
    private List<ChangeItem> actions = new LinkedList<>();

    public static class RDFPatchStored implements RDFPatch {
        private final PatchHeader header ;
        private final List<ChangeItem> actions;

        public RDFPatchStored(Map<String, Node> header, List<ChangeItem> actions) {
            this.header = new PatchHeader(header);
            this.actions = actions;
        }

        @Override
        public PatchHeader header() {
            return header;
        }

        @Override
        public void apply(RDFChanges changes) {
            if ( ! RECORD_HEADER )
                header.apply(changes);
            actions.forEach(a -> enact(a, changes));
        }

        @Override
        public boolean repeatable() {
            return true;
        }

        public List<ChangeItem> getActions() {
            return actions;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((actions == null) ? 0 : actions.hashCode());
            result = prime * result + ((header == null) ? 0 : header.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            RDFPatchStored other = (RDFPatchStored)obj;
            if ( actions == null ) {
                if ( other.actions != null )
                    return false;
            } else if ( !actions.equals(other.actions) )
                return false;
            if ( header == null ) {
                if ( other.header != null )
                    return false;
            } else if ( !header.equals(other.header) )
                return false;
            return true;
        };
    }

    public RDFChangesCollector() { }

    public RDFPatch getRDFPatch() {
        return new RDFPatchStored(new HashMap<>(header), new ArrayList<>(actions));
    }

//    /** Play backwards, swapping adds for deletes and delete for adds */
//    public void playReverse(RDFChanges target) {
//        System.err.println("playReverse: Partially implemented");
//        // More complicated - turn into transaction chunks then ...
//
//        ListIteratorReverse.reverse(actions.listIterator()).forEachRemaining(a-> enactFlip(a, target));
//    }

    private void enactFlip(ChangeItem a, RDFChanges target) {
        if ( a instanceof AddQuad ) {
            AddQuad a2 = (AddQuad)a;
            target.delete/*add*/(a2.g, a2.s, a2.p, a2.o);
            return;
        }
        if ( a instanceof DeleteQuad ) {
            DeleteQuad a2 = (DeleteQuad)a;
            target.add/*delete*/(a2.g, a2.s, a2.p, a2.o);
            return;
        }
//        if ( a instanceof AddPrefix ) {
//            AddPrefix a2 = (AddPrefix)a;
//            target.deletePrefix(a2.gn, a2.prefix, a2.uriStr);
//            return;
//        }
//        if ( a instanceof DeletePrefix ) {
//            DeletePrefix a2 = (DeletePrefix)a;
//            target.addPrefix(a2.gn, a2.prefix);
//            return;
//        }
        // Transaction.
        enact(a, target);
    }

    private static void enact(ChangeItem item, RDFChanges target) {
        if ( item instanceof HeaderItem ) {
            HeaderItem h = (HeaderItem)item;
            target.header(h.field, h.value);
            return;
        }
        if ( item instanceof AddQuad ) {
            AddQuad a2 = (AddQuad)item;
            target.add(a2.g, a2.s, a2.p, a2.o);
            return;
        }
        if ( item instanceof DeleteQuad ) {
            DeleteQuad a2 = (DeleteQuad)item;
            target.delete(a2.g, a2.s, a2.p, a2.o);
            return;
        }
        if ( item instanceof AddPrefix ) {
            AddPrefix a2 = (AddPrefix)item;
            target.addPrefix(a2.gn, a2.prefix, a2.uriStr);
            return;
        }
        if ( item instanceof DeletePrefix ) {
            DeletePrefix a2 = (DeletePrefix)item;
            target.deletePrefix(a2.gn, a2.prefix);
            return;
        }
        if ( item instanceof TxnBegin ) {
            target.txnBegin();
            return;
        }
        if ( item instanceof TxnCommit ) {
            target.txnCommit();
            return;
        }
        if ( item instanceof TxnAbort ) {
            target.txnAbort();
            return;
        }
        if ( item instanceof Segment ) {
            target.segment();
            return;
        }
        FmtLog.warn(RDFChangesCollector.class,  "Unrecognized action: %s : %s", Lib.className(item), item);
    }

    private void collect(ChangeItem object) {
        actions.add(object);
    }

    @Override
    public void start() {
        internalReset();
    }

    @Override
    public void finish() {
        // Do not call internalReset() here.
        // The collected patch may be used after .finish() happens.
    }

    @Override
    public void segment() {
        actions.add(new Segment());
    }

    public void reset() {
        internalReset();
    }

    private void internalReset() {
        header.clear();
        actions.clear();
    }

    @Override
    public void header(String field, Node value) {
        if ( RECORD_HEADER )
            collect(new HeaderItem(field, value));
        // And keep a copy.
        header.put(field, value);
    }

    protected Node header(String field) {
        return header.get(field);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        collect(new AddQuad(g, s, p, o));
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        collect(new DeleteQuad(g, s, p, o));
    }

    @Override
    public void addPrefix(Node gn, String prefix, String uriStr) {
        collect(new AddPrefix(gn, prefix, uriStr));
    }

    @Override
    public void deletePrefix(Node gn, String prefix) {
        collect(new DeletePrefix(gn, prefix)) ;
    }

    @Override
    public void txnBegin() {
        collect(TxnBegin.object());
    }

    @Override
    public void txnCommit() {
        collect(TxnCommit.object());
    }

    @Override
    public void txnAbort() {
        collect(TxnAbort.object());
    }
}
