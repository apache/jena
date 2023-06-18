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


import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.cache.CacheInfo ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.sparql.graph.NodeConst ;

/** Adds some caching of created nodes - the caching is tuned to RIOT parser usage */
public class FactoryRDFCaching extends FactoryRDFStd {
    public static final int DftNodeCacheSize = 5000 ;

    private final int cacheSize ;
    private final Cache<String, Node> cache ;

    public FactoryRDFCaching() {
        this(DftNodeCacheSize, SyntaxLabels.createLabelToNode());
    }

    public FactoryRDFCaching(int cacheSize, LabelToNode labelMapping) {
        super(labelMapping) ;
        this.cacheSize = cacheSize;
        this.cache = setCache(cacheSize) ;
    }

    private Cache<String, Node> setCache(int cacheSize) {
        return CacheFactory.createCache(cacheSize);
    }

    @Override
    public Node createURI(String uriStr) {
        return cache.get(uriStr, RiotLib::createIRIorBNode);
    }

    // A few constants

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
        return super.createTypedLiteral(lexical, datatype) ;
    }

    @Override
    public Node createStringLiteral(String lexical) {
        if ( lexical.isEmpty() )
            return NodeConst.emptyString ;
        return super.createStringLiteral(lexical) ;
    }

    public CacheInfo stats() {
        return cache.stats();
    }
}
