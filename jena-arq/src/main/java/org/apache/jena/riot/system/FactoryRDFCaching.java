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

package org.apache.jena.riot.system;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.atlas.lib.cache.CacheGuava ;
import org.apache.jena.atlas.lib.cache.CacheInfo ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.sparql.graph.NodeConst ;

/** Adds some caching of created nodes - the caching is tuned to RIOT parser usage */ 
public class FactoryRDFCaching extends FactoryRDFStd {

    // Double caching
    public final int NodeCacheSize = 10000 ; 
    public final int PredicateCacheSize = 1000 ;
    
    public FactoryRDFCaching() {
        super() ;
    }
    
    public FactoryRDFCaching(LabelToNode labelMapping) {
        super(labelMapping) ; 
    }

    // Better by S,P,O?
    // Better by Node->Node in triple?
    
    // By role?
    // By policy key?
    
    Cache<String, Node> cache = CacheFactory.createCache(NodeCacheSize) ;
    
    @Override
    public Node createURI(String uriStr) {
        Node n = cache.getOrFill(uriStr, ()->RiotLib.createIRIorBNode(uriStr)) ;
        return n ;
    }

    // Constants
    
    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype) {
        if ( XSDDatatype.XSDinteger.equals(datatype) ) {
            switch(lexical) {
                case "0" : return NodeConst.nodeZero ; 
                case "1" : return NodeConst.nodeOne ;
                case "2" : return NodeConst.nodeTwo ;
                case "-1" : return NodeConst.nodeMinusOne ;
            }
            // fallthrough.
        } else if ( XSDDatatype.XSDboolean.equals(datatype) ) {
            switch(lexical) {
                case "true" : return NodeConst.nodeTrue ; 
                case "false" : return NodeConst.nodeFalse ;
            }
            // fallthrough.
        }
        return NodeFactory.createLiteral(lexical, datatype) ;
    }

    @Override
    public Node createStringLiteral(String lexical) {
        if ( lexical.isEmpty() )
            return NodeConst.emptyString ;
        return NodeFactory.createLiteral(lexical) ;
    }
    
//    // A predicate cache.  No significant addional effect.
//    Cache<Node, Node> cachePredicate = CacheFactory.createCache(PredicateCacheSize) ;
//    
//    @Override
//    public Triple createTriple(Node subject, Node predicate, Node object) {
//        Node p = cachePredicate.getOrFill(predicate, ()->predicate) ; 
//        return super.createTriple(subject, p, object) ;
//    }
//
//    // A graph name cache.
//    Cache<Node, Node> cacheGraphName = CacheFactory.createCache(100) ;
//
//    @Override
//    public Quad createQuad(Node graph, Node subject, Node predicate, Node object) {
//        Node g = 
//            graph != null ? cacheGraphName.getOrFill(graph, ()->graph) : null ;
//        Node p = cachePredicate.getOrFill(predicate, ()->predicate) ;
//        return super.createQuad(g, subject, p, object) ;
//    }
    
    public CacheInfo stats() {
        return new CacheInfo(NodeCacheSize, ((CacheGuava<?, ?>)cache).stats()) ;
    }
    
//    public void details() {
//        details("Node cache", (CacheGuava<?, ?>)cache, NodeCacheSize) ;
////        details("Predicate Cache", (CacheGuava<?, ?>)cachePredicate, PredicateCacheSize) ;
//    }
//        
//    private void details(String label, CacheGuava<?, ?> cache, int cacheSize) {
//        System.out.printf("%s [%,d]\n", label, cacheSize) ;
//        CacheStats stats = ((CacheGuava<?, ?>)cache).stats() ;
////        System.out.printf("  Cache usage:      %,d\n", cache.size()) ;
//        System.out.printf("  Requests:         %,d\n", stats.requestCount()) ;
//        System.out.printf("  Hit rate:         %.1f%%\n", 100*stats.hitRate()) ; 
////        System.out.printf("  Hits:             %,d\n", stats.hitCount()) ;
////        System.out.printf("  Misses:           %,d\n", stats.missCount()) ;
////        if ( stats.loadSuccessCount() != stats.missCount() ) {
////            System.out.printf("  Load success:     %,d\n", stats.loadSuccessCount()) ;
////            System.out.printf("  Load ex:          %,d\n", stats.loadExceptionCount()) ;
////        }
}