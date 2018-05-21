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

package org.apache.jena.graph.impl;

import java.util.function.Predicate;

import org.apache.jena.graph.* ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * A version of Graph that enforces term equality even if the base graph uses value-indexing.
 * With value-indexing, one value may have several object terms that represent it when the graph store
 * RDF terms, and but matches by value.
 * 
 * This only affects the object field of a triple in RDF but in 
 * <a href="https://www.w3.org/TR/rdf11-concepts/#section-generalized-rdf">"generalized RDF"</a> 
 * literals can appear in any triple slot.
 * <p>
 */ 
public class GraphPlain extends WrappedGraph
{
    /** Return a graph that only has term-equality
     * and storage in the {@code base} graph.
     * Update affects the base graph. 
     */ 
    public static Graph plain(Graph base) {
        if ( ! base.getCapabilities().handlesLiteralTyping() )
            return base;
        return new GraphPlain(base);
    }
    
    /** Return a graph that only has term-equality. */ 
    public static Graph plain() { 
        return plain(Factory.createDefaultGraph());
    }

    private final Capabilities capabilities;

    protected GraphPlain(Graph other) {
        super(other);
        capabilities = new WrappedCapabilities(base.getCapabilities()) {
            @Override public boolean handlesLiteralTyping() { return false; }
        };
    }
    
    @Override
	public Capabilities getCapabilities() {
		return capabilities;
	}

    @Override
    public void remove( Node s, Node p, Node o ) {
        GraphUtil.remove(this, s, p, o) ;
    }
    
//    @Override
//    public void clear() {
//        GraphUtil.remove(this, Node.ANY, Node.ANY, Node.ANY);
//    }
    
    @Override
    public boolean contains( Triple t ) {
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        // Do direct for efficiency.
        if ( ! base.contains(s,p,o) )
            return false;
        // May have matched by value.  Do a term test find to restrict to RDF terms.
        ExtendedIterator<Triple> iter = find(s, p, o);
        boolean b = iter.hasNext();
        iter.close();
        return b;
    }
    
    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getSubject(), m.getPredicate(), m.getObject());
    }

    @Override
    public ExtendedIterator<Triple> find(Node subj, Node pred, Node obj) {
        // Use the base graph and it's indexing (which presumably is efficient)
        // then filter for term match.  If two terms match they must be same-value
        // so retrieve-by-value will find matches, and maybe some more.
        ExtendedIterator<Triple> iter = base.find(subj, pred, obj) ;

        // Add a term filter.
        // Whole triple.
        Predicate<Triple> predicate = (dataTriple) -> sameTermMatch(subj, pred, obj, dataTriple);
        iter = iter.filterKeep(predicate) ;
//        // For reference - just object 
//        if ( !obj.isConcrete() || obj.isLiteral() ) {
//            Predicate<Triple> predicate = (t) -> sameTermMatch(obj, t.getObject()) ;
//            iter = iter.filterKeep(predicate) ;
//        }
        return iter;
    }
    
    @Override 
    public String toString() {
        return this.getClass().getSimpleName()+" "+base.toString();
    }
    
    /**
     * Match a ground triple (even ANY and variablesare considered ground terms in the
     * data triple) with S/P/O which can be wildcards (ANY or null).
     */
    private static boolean sameTermMatch(Node matchSubj, Node matchPred, Node matchObj, Triple dataTriple) {
        return
            sameTermMatch(matchSubj, dataTriple.getSubject()) &&
            sameTermMatch(matchPred, dataTriple.getPredicate()) &&
            sameTermMatch(matchObj,  dataTriple.getObject());
    }
    
    /**
     * Match a ground RDF Term (ANY and variables are considered ground terms in the
     * data term) with a node which can be a wildcard (ANY or null).
     * Language tags compare case-insensitively. 
     */
    private static boolean sameTermMatch(Node match, Node data) {
        if ( ! Util.isLangString(data) || ! Util.isLangString(match) )
            // No lang tag
            return (match==null) || (match == Node.ANY) || match.equals(data) ;

        // Concrete match, which is a lang tag literal.
        // Language tags compare case insensitively.
        String lex1 = data.getLiteralLexicalForm();
        String lex2 = data.getLiteralLexicalForm();
        String lang1 = data.getLiteralLanguage();
        String lang2 = data.getLiteralLanguage();
        return lex1.equals(lex2) && lang1.equalsIgnoreCase(lang2);
    }
}
