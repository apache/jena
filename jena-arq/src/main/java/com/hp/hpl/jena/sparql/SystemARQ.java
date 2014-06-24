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

package com.hp.hpl.jena.sparql;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Sync ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.compose.Polyadic ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.graph.GraphWrapper ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;

public class SystemARQ
{
    // Various system wide settings, "constants" that might change e.g. test setups

    // NodeValues work without the context so somethings only have global settings.
    
    /** Control whether additon datatypes, over and above strict, minimal SPARQL compliance, are handled.
     *  Examples incldue xsd;date and simple literal/xsd:string.
     */
    public static boolean ValueExtensions       = true ;
    /** Control whether simple literals, string literals without datatype orlanguage tag, 
     *  are created sameValueAs xsd:string.  Normally true. 
     *  Some testing for pre-RDF-1.1 assumes otherwise.    
     */
    public static boolean SameValueAsString     = true ;
    /**
     * Under strict F&O, dateTimes and dates with no timezone have one magically applied. 
     * This default timezone is implementation dependent and can lead to different answers
     * to queries depending on the timezone. Normally, ARQ uses XMLSchema dateTime comparions,
     * which an yield "indeterminate", which in turn is an evaluation error. 
     * F&O insists on true/false and so can lead to false positves and negatives. 
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

    /** Sync a Model if it provides the underlying graph provides sync . Do nothing otherwise. */
    public static void sync(Model model)
    {
        sync(model.getGraph()) ;
    }
    
    /** Sync if provided. Do nothing if not. */
    public static void sync(Graph graph)
    {
        syncGraph(graph) ;
    }
    
    private static void syncGraph(Graph graph)
    {
        // "Temporary" hack.  Graph ought to implement sync and casade it down.
        if ( graph instanceof InfGraph )
            syncGraph(((InfGraph)graph).getRawGraph()) ;
        else if ( graph instanceof Polyadic ) // MultiUnion
            // Only the base graph is updatable.
            syncGraph(((Polyadic)graph).getBaseGraph()) ;
        else if ( graph instanceof GraphWrapper )
            syncGraph(((GraphWrapper)graph).get()) ;
//        else if ( graph instanceof WrappedGraph )   
//            // Does not expose the WrappedGraph : checking, no subclass needs a sync().
//            syncGraph(((WrappedGraph)graph).get()) ;
        else
            syncObject(graph) ;
    }

    /** Sync a Dataset, if underlying storage provides sync. */
    public static void sync(Dataset dataset)
    { 
        sync(dataset.asDatasetGraph()) ;
    }
    
    /** Sync carefully for compound objects*/
    public static void sync(DatasetGraph dataset)
    { 
        // Let implementation declare Sync and decide what to do
        // For light-weight, not truely ACID transaction, implementations.
//        if ( dataset instanceof Transactional )
//            return ;
        
        if ( dataset instanceof Sync )
        {
            ((Sync)dataset).sync() ;
            return ;
        }
        else
        {
            sync(dataset.getDefaultGraph()) ;
            // Go through each graph.
            Iterator<Node> iter = Iter.iterator(dataset.listGraphNodes()) ;
            for ( ; iter.hasNext() ; )
            {
                Node n = iter.next();
                Graph g = dataset.getGraph(n) ;
                sync(g) ;
            }
        }
    }
    
    /** Sync an object if synchronizable (model, graph, dataset). 
     *  If force is true, synchronize as much as possible (e.g. file metadata)
     *  else make a reasonable attenpt at synchronization but does not gauarantee disk state. 
     *  Do nothing otherwise.
     */
    public static void syncObject(Object object)
    {
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }
    
    
    private static List<SystemInfo> versions = new ArrayList<>() ;
    public static void registerSubSystem(SystemInfo systemInfo)
    {
        ARQMgt.register(systemInfo.getJmxPath()+".system:type=SystemInfo", systemInfo) ;
        versions.add(systemInfo) ;
    }
    
    public static Iterator<SystemInfo> registeredSubsystems()
    {
        return versions.iterator() ;
    }

}
