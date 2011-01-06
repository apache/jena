/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

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
        sync(graph, true) ;
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
            ((Sync)dataset).sync(true) ;
            return ;
        }
        else
        {
            // Go through each graph.
            Iterator<Node> iter = dataset.listGraphNodes() ;
            for ( ; iter.hasNext() ; )
            {
                Node n = iter.next();
                Graph g = dataset.getGraph(n) ;
                sync(g, true) ;
            }
        }
    }
 
    
    /** Sync an object if synchronizable (model, graph, dataset). 
     *  If force is true, synchronize as much as possible (e.g. file metadata)
     *  else make a reasonable attenpt at synchronization but does not gauarantee disk state. 
     *  Do nothing otherwise 
     */
    private static void sync(Object object, boolean force)
    {
        if ( object instanceof Sync )
            ((Sync)object).sync(force) ;
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */