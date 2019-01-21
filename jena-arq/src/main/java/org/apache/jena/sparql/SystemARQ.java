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

package org.apache.jena.sparql;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.compose.Polyadic ;
import org.apache.jena.graph.impl.WrappedGraph ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.reasoner.InfGraph ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.GraphView ;
import org.apache.jena.sparql.graph.GraphWrapper ;
import org.apache.jena.sparql.mgt.ARQMgt ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.util.Symbol ;

public class SystemARQ
{
    // Various system wide settings, "constants" that might change e.g. test setups
    // ** This can be loaded before the rest of ARQ is initialized **

    // NodeValues work without the context so some things only have global settings.
    
    /** Control whether addition datatypes, over and above strict, minimal SPARQL compliance, are handled.
     *  Examples include xsd:date and simple literal/xsd:string.
     */
    public static boolean ValueExtensions       = true ;

    /**
     * Under strict {@literal F&O}, dateTimes and dates with no timezone have one magically applied. 
     * This default timezone is implementation dependent and can lead to different answers
     * to queries depending on the timezone. Normally, ARQ uses XMLSchema dateTime comparions,
     * which an yield "indeterminate", which in turn is an evaluation error. 
     * {@literal F&O} insists on true/false and so can lead to false positves and negatives. 
     */
    public static boolean StrictDateTimeFO      = false ;
    
    /** Whether support for roman numerals (datatype http://rome.example.org/Numeral).
     *  Mainly a test of datatype extension.
     */
    public static boolean EnableRomanNumerals   = true ;  
    
    /**
     * Use a plain graph (sameValueAs is term equality)
     */
    public static boolean UsePlainGraph         = false ;
    
    /**
     * Whether to use StAX or SAX XML parsing for result sets (StAX preferred). 
     */
    public static boolean UseSAX                = false ;

    /**
     * Sync a Model if it provides the underlying graph provides sync . Do nothing
     * otherwise.
     */
    public static void sync(Model model) {
        sync(model.getGraph()) ;
    }

    /** Sync if provided. Do nothing if not. */
    public static void sync(Graph graph) {
        syncGraph(graph) ;
    }

    private static void syncGraph(Graph graph) {
        // "Temporary" hack. Graph ought to implement sync and casade it down.
        if ( graph instanceof InfGraph )
            syncGraph(((InfGraph)graph).getRawGraph()) ;
        else if ( graph instanceof Polyadic ) // MultiUnion
            // Only the base graph is updatable.
            syncGraph(((Polyadic)graph).getBaseGraph()) ;
        else if ( graph instanceof GraphWrapper )
            syncGraph(((GraphWrapper)graph).get()) ;
        else if ( graph instanceof WrappedGraph )   
            syncGraph(((WrappedGraph)graph).getWrapped()) ;
        else
            syncObject(graph) ;
    }

    /** Sync a Dataset, if underlying storage provides sync. */
    public static void sync(Dataset dataset) {
        sync(dataset.asDatasetGraph()) ;
    }

    /** Sync carefully for compound objects */
    public static void sync(DatasetGraph dataset) {
        if ( dataset instanceof Sync ) {
            ((Sync)dataset).sync() ;
            return ;
        } else {
            Graph gDft = dataset.getDefaultGraph() ;
            syncIfNotView(gDft) ;
            // Go through each graph.
            dataset.listGraphNodes().forEachRemaining( gn->syncIfNotView(dataset.getGraph(gn) )) ;
        }
    }
    
    private static void syncIfNotView(Graph g) {
        // GraphView sync calls the DatasetGraph lead to possible recursion.
        if ( !( g instanceof GraphView) )
            sync(g) ;
    }

    /** Sync an object if synchronizable (model, graph, dataset). */
    public static void syncObject(Object object) {
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }
    
    private static List<SystemInfo> versions = new ArrayList<>() ;
    public static void registerSubSystem(SystemInfo systemInfo)
    {
        ARQMgt.register(systemInfo.getJmxPath() + ".system:type=SystemInfo", systemInfo) ;
        versions.add(systemInfo) ;
    }

    public static Iterator<SystemInfo> registeredSubsystems() {
        return versions.iterator() ;
    }

    public static Symbol allocSymbol(String shortName) {
        // This must work even if initialization is happening.
        // Touching final constant explicit strings in ARQ is fine (compile time constants).
        if ( shortName.startsWith(ARQ.arqSymbolPrefix) )
            throw new ARQInternalErrorException("Symbol short name begins with the ARQ namespace prefix: " + shortName) ;
        if ( shortName.startsWith("http:") )
            throw new ARQInternalErrorException("Symbol short name begins with http: " + shortName) ;
        return SystemARQ.allocSymbol(ARQ.arqParamNS, shortName) ;
    }
    public static Symbol allocSymbol(String base, String shortName) {
        return Symbol.create(base + shortName) ;
    }
}
