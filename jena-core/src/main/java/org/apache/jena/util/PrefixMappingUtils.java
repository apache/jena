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

package org.apache.jena.util;

import java.util.* ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.lib.SetUtils ;
import org.apache.jena.atlas.lib.Trie ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;

public class PrefixMappingUtils {
    /**
     * Return a read-only graph that has the same data (RDF triples) as the one given, but has a
     * prefix mapping that only includes "in use" prefixes as calculated by
     * {@link #calcInUsePrefixMapping(Graph, PrefixMapping)}.
     * <p>
     * The prefix mappings of the two graphs are not connected.
     * Later changes to the prefix mapping of the original graph are not reflected in the returned graph.
     * Modifications to the triples contained in the underlying graph are reflected.
     */
    public static Graph graphxInUsePrefixMapping(Graph graph) {
        final PrefixMapping prefixMapping = calcInUsePrefixMapping(graph) ;
        prefixMapping.lock() ;
        Graph graph2 = new WrappedGraph(graph) {
            @Override
            public void performAdd(Triple triple)
            { throw new UnsupportedOperationException() ; }

            @Override
            public void performDelete(Triple triple)
            { throw new UnsupportedOperationException() ; }

            @Override
            public PrefixMapping getPrefixMapping() {
                return prefixMapping ;
            }
        } ;
        return graph2 ;
    }

    /**
     * Analyse the graph to see which prefixes of the graph are in use.
     * <p>
     * In the case of overlapping prefixes (where one prefix declaration is has an initial
     * URI string which matches another prefix declaration), all are included, though
     * they may not be used when printing (that depends on the output process). In effect,
     * this process has "false positives".
     * <p>
     * This function does not calculate new prefixes.
     *
     * @see #calcInUsePrefixMappingTTL(Graph)
     */
    public static PrefixMapping calcInUsePrefixMapping(Graph graph) {
        PrefixMapping prefixMapping = graph.getPrefixMapping() ;
        if ( prefixMapping == null )
            return null ;
        return calcInUsePrefixMapping(graph, prefixMapping) ;
    }

    /**
     * Analyse the graph to see which prefixes of the given {@link PrefixMapping} are in
     * use.
     * <p>
     * In the case of overlapping prefixes (where one prefix declaration is has an initial
     * URI string which matches another prefix declaration), all are included, though
     * they may not be used when printing (that depends on the output process). In effect,
     * this process has "false positives".
     * <p>
     * This function does not calculate new prefixes.
     *
     * @see #calcInUsePrefixMappingTTL(Graph, PrefixMapping)
     */
    public static PrefixMapping calcInUsePrefixMapping(Graph graph, PrefixMapping prefixMapping) {
        /* Method:
         *
         * For each URI in the data, look it up in the trie.
         * to see if has a declared prefix.
         *
         * Exit early if every prefix is accounted for.
         */

        // Map prefix to URI.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;

        // Map URI to prefix, with partial lookup (all uri keys that partly match the URI)
        Trie<String> trie = new Trie<>() ;
        // Change this to "add(uri, uri)" to calculate the uris.
        pmap.forEach((prefix,uri)-> trie.add(uri, prefix)) ;
        Iterator<Triple> iter = graph.find(null, null, null) ;
        // Prefixes in use.
        // (URIs if "add(uri, uri)")
        Set<String> inUse = new HashSet<>() ;

        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            process(triple, inUse, trie);
            if ( pmap.size() == inUse.size() )
                break ;
        }

        if ( pmap.size() == inUse.size() )
            return prefixMapping ;

        // Build result.
        PrefixMapping pmap2 = new PrefixMappingImpl() ;
        inUse.forEach((prefix)-> pmap2.setNsPrefix(prefix, prefixMapping.getNsPrefixURI(prefix)) ) ;
        return pmap2 ;
    }

    // Step for each Triple
    private static void process(Triple triple, Set<String> inUse, Trie<String> trie) {
        process(triple.getSubject(),   inUse, trie);
        process(triple.getPredicate(), inUse, trie);
        process(triple.getObject(),    inUse, trie);
    }

    // Step for each Node.
    // Process to apply to each node
    // Accumulate any prefixes into 'inUse' if the data URI
    // is partially matched by a prefix URIs in the trie.
    private static void process(Node node, Set<String> inUse, Trie<String> trie) {
        String uri;
        if ( node.isURI() )
            uri = node.getURI();
        else if ( node.isLiteral() )
            uri = node.getLiteralDatatypeURI();
        else if ( node.isNodeTriple() ) {
            process(node.getTriple(), inUse, trie);
            return ;
        }
        else
            return;
        // Get all prefixes whose URIs are candidates
        List<String> hits = trie.partialSearch(uri) ;
        if ( hits.isEmpty() )
            return ;
        inUse.addAll(hits) ;
    }

    /**
     * Analyse the graph to see which prefixes of the graph are in use.
     * <p>
     * This function attempts to process each URI in the graph as if it were to be printed
     * in Turtle. Only prefixes that lead to valid output strings are returned. This is
     * more expensive than {@link #calcInUsePrefixMapping(Graph)}.
     * <p>
     * This function does not calculate new prefixes.
     *
     * @see #calcInUsePrefixMappingTTL(Graph)
     */
    public static PrefixMapping calcInUsePrefixMappingTTL(Graph graph) {
        PrefixMapping prefixMapping = graph.getPrefixMapping() ;
        if ( prefixMapping == null )
            return null ;
        return calcInUsePrefixMappingTTL(graph, prefixMapping) ;
    }

    /**
     * Analyse the graph to see which prefixes of the given {@link PrefixMapping} are used
     * by the graph triples.
     * <p>
     * This function attempts to process each URI in the graph as if it were to be printed
     * in Turtle. Only prefixes that lead to valid output strings are returned. This is
     * more expensive than {@link #calcInUsePrefixMapping(Graph, PrefixMapping)}.
     * <p>
     * This function does not calculate new prefixes.
     *
     * @see #calcInUsePrefixMapping(Graph, PrefixMapping)
     */
    public static PrefixMapping calcInUsePrefixMappingTTL(Graph graph, PrefixMapping prefixMapping) {

        /* Method:
         *
         * For each URI, split in in the usual place, after "/" or "#" for http URIs, and
         * after the last ":" for URNs, then see if that is a declared prefix.
         *
         * Exit early if every prefix is accounted for.
         */
        // Map prefix -> URI.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;

        // All URIs used as prefixes in the prefix mapping.
        Set<String> prefixURIs = new HashSet<>(pmap.values()) ;

        // Prefixes used.
        Set<String> inUse = new HashSet<>() ;

        Iterator<Triple> iter = graph.find(null, null, null) ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            processTTL(triple, inUse, prefixMapping);
            if ( inUse.size() == prefixURIs.size() )
                // Fast exit.
                break ;
        }

        if ( pmap.size() == inUse.size() )
            return prefixMapping ;

        // Build result.
        PrefixMapping pmap2 = new PrefixMappingImpl() ;
        inUse.forEach((prefix)-> pmap2.setNsPrefix(prefix, prefixMapping.getNsPrefixURI(prefix)) ) ;
        return pmap2 ;
    }

    // Step for each Triple
    private static void processTTL(Triple triple, Set<String> inUse, PrefixMapping prefixMapping) {
        processTTL(triple.getSubject(),   inUse, prefixMapping);
        processTTL(triple.getPredicate(), inUse, prefixMapping);
        processTTL(triple.getObject(),    inUse, prefixMapping);
    }

    // Step for each Node.
    private static void processTTL(Node node, Set<String> inUse, PrefixMapping prefixMapping) {
        String uri;
        if ( node.isURI() )
            uri = node.getURI();
        else if ( node.isLiteral() )
            uri = node.getLiteralDatatypeURI();
        else if ( node.isNodeTriple() ) {
            processTTL(node.getTriple(), inUse, prefixMapping);
            return ;
        }
        else
            return;
        // URI case.
        int idx = SplitIRI.splitpoint(uri) ;
        if ( idx < 0 )
            return ;
        String nsURI = SplitIRI.namespaceTTL(uri) ;
        String prefix = prefixMapping.getNsURIPrefix(nsURI) ;
        if ( prefix != null )
            inUse.add(prefix) ;
    }

    /** Check every URI as a possible use of a prefix */
    private static Set<String> fullMethod(Model m) {
        /* Method: Covers prefixes not based on "/", "#" or final ":" splitting.
         *
         * Build a trie to use as a partial lookup matcher.
         * For each URI in the data, look it up as a partial match in the trie
         * to get all URIs in the prefix map that apply.
         */

        // Map prefix to URI.
        Map<String, String> pmap = m.getNsPrefixMap() ;
        // Map URI to prefix, with partial lookup (all uri keys that partly match the URI)
        Trie<String> trie = new Trie<>() ;

        // change to add(uri, prefix) to get prefixes.
        pmap.forEach((prefix,uri)-> trie.add(uri, uri)) ;

        Iterator<Triple> iter = m.getGraph().find(null, null, null) ;
        // Prefix URIs in use.
        Set<String> inUseURIs = new HashSet<>() ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            processFull(trie, inUseURIs, triple.getSubject()) ;
            processFull(trie, inUseURIs, triple.getPredicate()) ;
            processFull(trie, inUseURIs, triple.getObject()) ;
            if ( pmap.size() == inUseURIs.size() )
                break ;
        }
        return inUseURIs ;
    }

    private static void processFull(Trie<String> trie, Set<String> prefixesInUse, Node node) {
        if ( ! node.isURI() )
            return ;
        String uri = node.getURI() ;
        // Shorten to "/" or "#" or ":"
        String pref = uri ;

        // Get all under the pref
        List<String> hits = trie.partialSearch(pref) ;
        if ( hits == null || hits.isEmpty() )
            return ;
        //System.out.println(pref+" => ("+hits.size()+")"+hits) ;
        prefixesInUse.addAll(hits) ;
    }

    // -------------------------------------------

    /** Assume that prefixes:localName are in the normal places (after/ or #).
     * i.e. node.getNameSpace() ; makes sense.
     * @param m
     */
    private static Set<String> splitMethod(Model m) {
        /* Method:
         *
         * For each URI, split in in the usual place, after "/" or "#" for http URIs, and
         * after the last ":" for URNs, then see if that is a declared prefix.
         *
         * Exit early if every prefix is accounted for.
         */

        PrefixMapping prefixMapping = m ;
        // Map prefix -> URI.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;

        // All URIs used as prefixes in the prefix mapping.
        Set<String> prefixURIs = new HashSet<>(pmap.values()) ;

        // Prefix URIs used.
        Set<String> inUsePrefixURIs = new HashSet<>() ;

        Iterator<Triple> iter = m.getGraph().find(null, null, null) ;
        while(iter.hasNext()) {
            Triple triple = iter.next() ;
            processBySplit(prefixURIs, inUsePrefixURIs, triple.getSubject()) ;
            processBySplit(prefixURIs, inUsePrefixURIs, triple.getPredicate()) ;
            processBySplit(prefixURIs, inUsePrefixURIs, triple.getObject()) ;
            if ( inUsePrefixURIs.size() == prefixURIs.size() )
                // Fast exit.
                break ;
        }
        return inUsePrefixURIs ;
    }

    private static void processBySplit(Collection<String> prefixURIs, Collection<String> inUse, Node node) {
        if ( ! node.isURI() )
            return ;
        String uri = node.getURI() ;

        int idx = SplitIRI.splitpoint(uri) ;
        if ( idx < 0 )
            return ;
        String prefixUri = uri.substring(0,idx) ;
        String localname = uri.substring(idx) ;

        if ( prefixURIs.contains(prefixUri) )
            inUse.add(prefixUri) ;
//        String ns = node.getNameSpace() ;
//        if ( ns == null )
//            return ;
//        if ( prefixURIs.contains(ns) )
//            inUse.add(ns) ;
    }

    // Development assistance in seeing into the algorithms.
    private static void print(Set<String> inUsePrefixURIs, PrefixMapping prefixMapping) {
        // Convert to prefixes.
        Set<String> inUsePrefixes = urisToPrefixes(prefixMapping, inUsePrefixURIs) ;
        // ----- Analysis

        System.out.println("In use: "+inUsePrefixURIs) ;
        System.out.println("In use: "+inUsePrefixes) ;

        inUsePrefixURIs.forEach((u)->System.out.printf("    %s: -> <%s>\n", prefixMapping.getNsURIPrefix(u), u)) ;

        // Calc not needed to be efficient.
        Map<String, String> pmap = prefixMapping.getNsPrefixMap() ;
        Set<String> prefixURIs = new HashSet<>(pmap.values()) ;
        Set<String> notInUseURIs = SetUtils.difference(prefixURIs, inUsePrefixURIs) ;
        Set<String> notInUsePrefixes = SetUtils.difference(pmap.keySet(), inUsePrefixes) ;
        System.out.println("Not in use: "+notInUseURIs) ;
        System.out.println("Not in use: "+notInUsePrefixes) ;
        notInUseURIs.forEach((u)->System.out.printf("    %s: -> <%s>\n", prefixMapping.getNsURIPrefix(u), u)) ;
    }

    /** Find the prefixes from a set of prefix uri */
    private static Set<String> urisToPrefixes(PrefixMapping prefixMapping, Set<String> inUsePrefixURIs) {
        return inUsePrefixURIs.stream()
            .map(prefixMapping::getNsURIPrefix)
            .collect(Collectors.toSet()) ;
    }
}
