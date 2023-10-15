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

package org.apache.jena.rdfs.setup;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsDomain;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsRange;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsSubClassOf;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfsSubPropertyOf;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
//import org.apache.jena.riot.other.Transitive;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.system.G;
import org.apache.jena.system.Transitive;

/**
 * Core datastructures needed for RDFS for one vocabulary.
 * To be general, this is in {@code <X>} space (e.g. {@link Node}, {@code NodeId}).
 */
public abstract class BaseSetupRDFS<X> implements ConfigRDFS<X>{
    public final Graph vocabGraph;

    // Variants for with and without the key in the value side.
    private final Map<X, Set<X>> superClasses         = new HashMap<>();
    private final Map<X, Set<X>> superClassesInc      = new HashMap<>();
    private final Map<X, Set<X>> subClasses           = new HashMap<>();
    private final Map<X, Set<X>> subClassesInc        = new HashMap<>();

    private final Map<X, Set<X>> superPropertiesInc   = new HashMap<>();
    private final Map<X, Set<X>> superProperties      = new HashMap<>();
    private final Map<X, Set<X>> subPropertiesInc     = new HashMap<>();
    private final Map<X, Set<X>> subProperties        = new HashMap<>();

    // Predicate -> type
    private final Map<X, Set<X>> propertyRange        = new HashMap<>();
    private final Map<X, Set<X>> propertyDomain       = new HashMap<>();

    // Type -> predicate
    private final Map<X, Set<X>> rangeToProperty      = new HashMap<>();
    private final Map<X, Set<X>> domainToProperty     = new HashMap<>();

    private final boolean hasAnyRDFS;
    private final boolean hasOnlyPropertyDeclarations;

    private static String preamble = StrUtils.strjoinNL
        ("PREFIX  rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
         "PREFIX  rdfs:   <http://www.w3.org/2000/01/rdf-schema#>",
         "PREFIX  xsd:    <http://www.w3.org/2001/XMLSchema#>",
         "PREFIX  owl:    <http://www.w3.org/2002/07/owl#>",
         "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>");

    protected BaseSetupRDFS(Graph vocab) {
        vocabGraph = vocab;
        // Fast path flags.
        hasAnyRDFS = setup();
        hasOnlyPropertyDeclarations = ! hasClassDeclarations() && ! hasDomainDeclarations() && ! hasRangeDeclarations();
    }

    private boolean setup() {
        // Calculate a different way and see if the answers are the same.
        final boolean CHECK = false;

        // Find super/sub classes
        execTransitive(vocabGraph, rdfsSubClassOf, superClasses, subClasses);
        if ( CHECK )
            execCheck("rdfs:subClassOf+", vocabGraph, superClasses, subClasses);

        // Find super/sub properties
        execTransitive(vocabGraph, rdfsSubPropertyOf, superProperties, subProperties);
        if( CHECK )
            execCheck("rdfs:subPropertyOf+", vocabGraph, superProperties, subProperties);

        // Find domain
        execSingle(vocabGraph, rdfsDomain, propertyDomain, domainToProperty);
        if( CHECK )
            execCheck("rdfs:domain", vocabGraph, propertyDomain, domainToProperty);

        // Find range
        execSingle(vocabGraph, rdfsRange, propertyRange, rangeToProperty);
        if ( CHECK )
            execCheck("rdfs:range", vocabGraph, propertyRange, rangeToProperty);

        deepCopyInto(superClassesInc, superClasses);
        addKeysToValues(superClassesInc);
        deepCopyInto(subClassesInc, subClasses);
        addKeysToValues(subClassesInc);
        deepCopyInto(superPropertiesInc, superProperties);
        addKeysToValues(superPropertiesInc);
        deepCopyInto(subPropertiesInc, subProperties);
        addKeysToValues(subPropertiesInc);

        return hasClassDeclarations() || hasPropertyDeclarations() || hasRangeDeclarations() || hasDomainDeclarations();
    }

    /**
     * Go from Node space to X space for a node that is in the RDFS vocabulary.
     * This function is only passed Nodes that exist in the dataset.
     * Must not return null or "don't know".
     */
    protected abstract X fromNode(Node node);

    @Override
    public Map<X, Set<X>> getSubClassHierarchy()    { return superClassesInc; }
    @Override
    public Map<X, Set<X>> getSubPropertyHierarchy() { return superPropertiesInc; }

    @Override
    public Map<X, Set<X>> getPropertyRanges()       { return propertyRange; }
    @Override
    public Map<X, Set<X>> getPropertyDomains()      { return propertyDomain; }

    // get* : return the Set corresponding to element elt
    // get*Inc : return the Set corresponding to element elt incluinge self.

    @Override
    public Set<X> getSuperClasses(X elt) {
        return result(superClasses, elt);
    }

    @Override
    public Set<X> getSuperClassesInc(X elt) {
        return result(superClassesInc, elt);
    }

    @Override
    public Set<X> getSubClasses(X elt) {
        return result(subClasses, elt);
    }

    @Override
    public Set<X> getSubClassesInc(X elt) {
        return result(subClassesInc, elt);
    }

    @Override
    public Set<X> getSuperProperties(X elt) {
        return result(superProperties, elt);
    }

    @Override
    public Set<X> getSuperPropertiesInc(X elt) {
        return result(superPropertiesInc, elt);
    }

    @Override
    public Set<X> getSubProperties(X elt) {
        return result(subProperties, elt);
    }

    @Override
    public Set<X> getSubPropertiesInc(X elt) {
        return result(subPropertiesInc, elt);
    }

    @Override
    public boolean hasClassDeclarations() {
        return ! subClasses.isEmpty();
    }

    @Override
    public boolean hasPropertyDeclarations() {
        return ! subProperties.isEmpty();
    }

    @Override
    public boolean hasRangeDeclarations() {
        return ! propertyRange.isEmpty();
    }

    @Override
    public boolean hasDomainDeclarations() {
        return ! propertyDomain.isEmpty();
    }

    @Override
    public boolean hasOnlyPropertyDeclarations() {
        return hasOnlyPropertyDeclarations;
    }

    @Override
    public boolean hasRDFS() {
        return hasAnyRDFS;
    }

    @Override
    public Set<X> getRange(X elt) {
        return result(propertyRange, elt);
    }

    @Override
    public Set<X> getDomain(X elt) {
        return result(propertyDomain, elt);
    }

    @Override
    public Set<X> getPropertiesByRange(X elt) {
        return result(rangeToProperty, elt);
    }

    @Override
    public Set<X> getPropertiesByDomain(X elt) {
        return result(domainToProperty, elt);
    }

    // Calculate using SPARQL and see if we get the same answer.
    private void execCheck(String path, Graph vocab, Map<X, Set<X>> supers, Map<X, Set<X>> subs) {
        Map<X, Set<X>> mSupers  = new HashMap<>();
        Map<X, Set<X>> mSubs    = new HashMap<>();
        String queryString      = "SELECT ?x ?y { ?x "+path+" ?y }";
        exec(queryString, vocab, mSupers, mSubs);
        if ( ! mSupers.equals(supers) || ! mSubs.equals(subs) )
            throw new InternalErrorException(path);
    }

    /** Calculate super/sub mapping */
    private void execTransitive(Graph vocab, Node property, Map<X, Set<X>> superMap, Map<X, Set<X>> subMap) {
        Map<Node, Collection<Node>> map = Transitive.transitive(vocab, property);
        map.forEach((n,c)->{
            c.forEach(nc->{
                X a = fromNode(n);
                X b = fromNode(nc);
                put(superMap, a, b);
                put(subMap, b, a);
            });
        });
    }

    private void execSingle(Graph vocab, Node predicate, Map<X, Set<X>> propertyMap, Map<X, Set<X>> reversePropertyMap) {
        G.find(vocab, Node.ANY, predicate, Node.ANY).forEach(t->{
            Node s = t.getSubject();
            Node o = t.getObject();
            X a = fromNode(s);
            X b = fromNode(o);
            put(propertyMap, a, b);
            put(reversePropertyMap, b, a);
        });
    }

    // The copy does not share the Set structure with the source.
    private void deepCopyInto(Map<X, Set<X>> dest, Map<X, Set<X>> src) {
        src.entrySet().forEach(e -> {
            Set<X> x = new HashSet<>(e.getValue());
            dest.put(e.getKey(), x);
        });
    }

    //  For each entry, add the key in the value set.
    private void addKeysToValues(Map<X, Set<X>> map) {
        map.entrySet().forEach(e -> e.getValue().add(e.getKey()) );
        ensureValuesAsKeys(map);
    }

    // Ensure every value is also a key, if it isn't add (x,x).
    private void ensureValuesAsKeys(Map<X, Set<X>> map) {
        Set<X> free = map.values().stream()
                .flatMap(setx -> setx.stream())
                .filter(x -> !map.containsKey(x))
                .collect(Collectors.toSet());
        free.forEach(x -> {
            Set<X> set = map.get(x);
            if ( set == null ) {
                set = new HashSet<>();
                map.put(x, set);
            }
            set.add(x);
        });
    }

    private void exec(String qs, Graph graph, Map<X, Set<X>> multimap1, Map<X, Set<X>> multimap2) {
        Query query = QueryFactory.create(preamble + "\n" + qs, Syntax.syntaxARQ);
        try ( QueryExecution qexec = QueryExecutionFactory.create(query, DatasetGraphFactory.wrap(graph)) ) {
            ResultSet rs = qexec.execSelect();
            for ( ; rs.hasNext() ; ) {
                Binding soln = rs.nextBinding();
                Node x = soln.get(Var.alloc("x"));
                Node y = soln.get(Var.alloc("y"));
                X a = fromNode(x);
                X b = fromNode(y);
                put(multimap1, a, b);
                put(multimap2, b, a);
            }
        }
    }

    private static <X> void put(Map<X, Set<X>> multimap, X n1, X n2) {
        if ( !multimap.containsKey(n1) )
            multimap.put(n1, new HashSet<X>());
        multimap.get(n1).add(n2);
    }

    // Return empty set, not null.
    private Set<X> result(Map<X, Set<X>> map, X elt) {
        Set<X> x = map.get(elt);
        return x != null ? x : Collections.emptySet();
    }
}
