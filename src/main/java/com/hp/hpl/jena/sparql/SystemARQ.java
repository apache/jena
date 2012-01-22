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

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Sync ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;

public class SystemARQ
{
    /** Sync a Model if it provides the underlying graph provides sync . Do nothing otherwise. */
    public static void sync(Model model)
    {
        sync(model.getGraph()) ;
    }
    
    /** Sync a if provided. Do nothing if not TDB-backed. */
    public static void sync(Graph graph)
    {
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
        if ( dataset instanceof Sync )
        {
            ((Sync)dataset).sync() ;
            return ;
        }
        else
        {
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
     *  Do nothing otherwise 
     */
    public static void syncObject(Object object)
    {
        if ( object instanceof Sync )
            ((Sync)object).sync() ;
    }
    
    
    private static List<SystemInfo> versions = new ArrayList<SystemInfo>() ;
    public static void registerSubSystem(SystemInfo systemInfo)
    {
        versions.add(systemInfo) ;
    }
    
    public static Iterator<SystemInfo> registeredSubsystems()
    {
        return versions.iterator() ;
    }
}
