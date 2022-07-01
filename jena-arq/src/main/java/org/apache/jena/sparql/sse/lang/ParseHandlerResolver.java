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

package org.apache.jena.sparql.sse.lang;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.sse.Item;
import org.apache.jena.sparql.sse.ItemList;
import org.apache.jena.sparql.sse.builders.BuilderPrefixMapping;

/**
 * Resolve syntactic forms like (base ...) and (prefix...) where the syntax modifies
 * the enclosed sub term. Forms: (FORM DECL... TERM) {@literal =>} where TERM is the
 * result. Examples (prefix (PREFIXES) TERM) {@literal =>} TERM with prefix names
 * expanded (base IRI TERM) {@literal =>} TERM with IRIs resolved to absolute IRIs
 * The DECL part can not itself have nested, independent forms unless a subclass
 * (carefully) manages that.
 */

public class ParseHandlerResolver extends ParseHandlerForm {
    private static class State extends Pair<IRIx, PrefixMapping> {
        public State(IRIx a, PrefixMapping b) {
            super(a, b);
        }
    }

    private static final String prefixTag = "prefix";
    private static final String baseTag = "base";
    private IRIx baseURI = null;
    private PrefixMapping prefixes = null;
    private PrefixMapping topMap = null;
    private String topBase = null;
    private ItemList declList = null;
    private Deque<State> state = new ArrayDeque<>(); // Previous state (not the current one)

    public ParseHandlerResolver(String baseStr, PrefixMapping prefixMapping) {
        this.prefixes = prefixMapping;
        if ( baseStr != null )
            this.baseURI = IRIs.resolveIRI(baseStr);
    }

    @Override
    protected void declItem(ItemList list, Item item) {
        if ( list != declList )
            // Deeper
            return;

        // Prefix - deeper than one.
        boolean isBase = list.get(0).isSymbol(baseTag);
        boolean isPrefix = list.get(0).isSymbol(prefixTag);

        // Old state has already been saved.
        if ( isBase ) {
            if ( !item.isNode() )
                throwException("(base ...): not an RDF node for the base.", item);
            if ( !item.getNode().isURI() )
                throwException("(base ...): not an IRI for the base.", item);

            String newBase = item.getNode().getURI();
            if ( baseURI != null )
                baseURI = baseURI.resolve(newBase);
            else
                baseURI = IRIs.resolveIRI(newBase);
            // Remember first base seen
            if ( topBase == null )
                topBase = newBase;
            return;
        }

        if ( isPrefix ) {
            PrefixMapping newPrefixes = new PrefixMappingImpl();
            PrefixMapping itemMappings = BuilderPrefixMapping.build(item);
            // Add exising, overwrite with new
            newPrefixes.setNsPrefixes(prefixes);
            newPrefixes.setNsPrefixes(itemMappings);
            prefixes = newPrefixes;
            // Remember first prefix mapping seen.
            if ( topMap == null )
                topMap = itemMappings;
            return;
        }
        throwException("Inconsistent: " + list.shortString(), list);
    }

    @Override
    protected boolean endOfDecl(ItemList list, Item item) {
        // Both (base...) and (prefix...) have one decl item
        if ( declList == list && list.size() == 2 ) {
            declList = null;
            return true;
        }
        return false;
    }

    @Override
    protected boolean isForm(Item tag) {
        return tag.isSymbol(baseTag) || tag.isSymbol(prefixTag);
    }

    @Override
    protected void startForm(ItemList list) {
        // Remember the top of declaration
        declList = list;
        state.push(new State(baseURI, prefixes));
    }

    @Override
    protected void finishForm(ItemList list) {
        // Check list length

        // Restore state
        State oldState = state.pop();
        baseURI = oldState.getLeft();
        prefixes = oldState.getRight();

        // Choose the result.
        if ( list.size() > 2 ) {
            Item item = list.getLast();
            super.setFormResult(item);
        }
    }

    @Override
    public void emitIRI(int line, int column, String iriStr) {
        iriStr = resolveIRI(iriStr, line, column);
        super.emitIRI(line, column, iriStr);
    }

    @Override
    public void emitPName(int line, int column, String pname) {
        if ( inFormDecl() ) {
            // Record a faked PName. Works with BuilderPrefixMapping
            Item item = Item.createSymbol(pname, line, column);
            listAdd(item);
            return;
        }
        String iriStr = resolvePrefixedName(pname, line, column);
        super.emitIRI(line, column, iriStr);
    }

    @Override
    protected String resolvePrefixedName(String pname, int line, int column) {
        if ( prefixes == null )
            throwException("No prefix mapping for prefixed name: " + pname, line, column);

        if ( !StrUtils.contains(pname, ":") )
            throwException("Prefixed name does not have a ':': " + pname, line, column);

        String uri = prefixes.expandPrefix(pname);
        if ( uri == null )
            throwException("Can't resolve prefixed name: " + pname, line, column);
        return uri;
    }

    private String resolveIRI(String iriStr, int line, int column) {
        if ( baseURI == null )
            // return IRIOps.resolve(iriStr)
            return iriStr;
        return IRIs.resolve(baseURI, iriStr);
    }

}
