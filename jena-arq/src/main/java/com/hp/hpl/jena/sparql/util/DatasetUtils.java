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

package com.hp.hpl.jena.sparql.util;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.IRIResolver ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

/** Internal Dataset/DataSource factory + graph equivalents. */

public class DatasetUtils
{
    
    public static Dataset createDataset(String uri, List<String> namedSourceList)
    {
        return createDataset(uri, namedSourceList, null) ;
    }
    
    public static Dataset createDataset(String uri, List<String> namedSourceList, String baseURI)
    {
        List<String> uriList = Arrays.asList(uri) ;
        return createDataset(uriList, namedSourceList, baseURI) ;
    }

    public static Dataset createDataset(List<String> uriList, List<String> namedSourceList)
    {
        return createDataset(uriList, namedSourceList, null) ;
    }

    public static Dataset createDataset(DatasetDescription datasetDesc)
    {
        return createDataset(datasetDesc, null) ;
    }

    public static Dataset createDataset(DatasetDescription datasetDesc, String baseURI)
    {
        return createDataset(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), baseURI) ;
    }
    
    public static Dataset createDataset(List<String> uriList, List<String> namedSourceList, String baseURI)
    {
        Dataset ds = DatasetFactory.createMem() ;
        return addInGraphs(ds, uriList, namedSourceList, baseURI) ;
    }
    
    /** add graphs into an existing DataSource */
    public static Dataset addInGraphs(Dataset ds, List<String> uriList, List<String> namedSourceList)
    {
        return addInGraphs(ds, uriList, namedSourceList, null) ;
    }
    
    /** add graphs into an existing DataSource */
    public static Dataset addInGraphs(Dataset ds, List<String> uriList, List<String> namedSourceList, String baseURI)
    {
        if ( ds.getDefaultModel() == null )
            // Merge into background graph
            ds.setDefaultModel(GraphFactory.makeDefaultModel()) ;
        
        if ( uriList != null )
        {
            for (Iterator<String> iter = uriList.iterator() ; iter.hasNext() ; )
            {
                String sourceURI = iter.next() ;
                String absURI = null ;
                if ( baseURI != null )
                    absURI = IRIResolver.resolveString(sourceURI, baseURI) ;
                else
                    absURI = IRIResolver.resolveString(sourceURI) ;
                RDFDataMgr.read(ds.getDefaultModel(), sourceURI, absURI, null) ;
            }
        }
        
        if ( namedSourceList != null )
        {
            for (Iterator<String> iter = namedSourceList.iterator() ; iter.hasNext() ; )
            {
                String sourceURI = iter.next() ;
                String absURI = null ;
                if ( baseURI != null )
                    absURI = IRIResolver.resolveString(sourceURI, baseURI) ;
                else
                    absURI = IRIResolver.resolveString(sourceURI) ;
                Model m = GraphFactory.makeDefaultModel() ;
                RDFDataMgr.read(m, sourceURI, absURI, null) ;
                ds.addNamedModel(absURI, m) ;
            }
        }
        return ds ;
    }
    
    // ---- DatasetGraph level.
    
    public static DatasetGraph createDatasetGraph(DatasetDescription datasetDesc)
    {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), null) ;
    }

    public static DatasetGraph createDatasetGraph(DatasetDescription datasetDesc, String baseURI)
    {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), baseURI) ;
    }
        
    public static DatasetGraph createDatasetGraph(String uri, List<String> namedSourceList, String baseURI)
    {
        List<String> uriList = new ArrayList<String>() ;
        uriList.add(uri) ;
        return createDatasetGraph(uriList, namedSourceList, baseURI) ;
    }

    public static DatasetGraph createDatasetGraph(List<String> uriList, List<String> namedSourceList, String baseURI)
    {
        DatasetGraph ds = DatasetGraphFactory.createMem() ;
        
        // Merge into background graph
        if ( uriList != null )
        {
            Model m = GraphFactory.makeDefaultModel() ;
            for (Iterator<String> iter = uriList.iterator() ; iter.hasNext() ; )
            {
                String sourceURI = iter.next() ;
                String absURI = null ;
                if ( baseURI != null )
                    absURI = IRIResolver.resolveString(sourceURI, baseURI) ;
                else
                    absURI = IRIResolver.resolveString(sourceURI) ;
                // FileManager.readGraph?
                RDFDataMgr.read(m, sourceURI, absURI, null) ;
            }
            ds.setDefaultGraph(m.getGraph()) ;
        }
        else
        {
            ds.setDefaultGraph(GraphFactory.createDefaultGraph()) ;
        }
        
        if ( namedSourceList != null )
        {
            for (Iterator<String> iter = namedSourceList.iterator() ; iter.hasNext() ; )
            {
                String sourceURI = iter.next();
                String absURI = null ;
                if ( baseURI != null )
                    absURI = IRIResolver.resolveString(baseURI, sourceURI) ;
                else
                    absURI = IRIResolver.resolveString(sourceURI) ;
                Model m = GraphFactory.makeDefaultModel() ;
                RDFDataMgr.read(m, sourceURI, absURI, null) ;
                Node gn = NodeFactory.createURI(sourceURI) ;
                ds.addGraph(gn, m.getGraph()) ;
            }
        }
        return ds ;
    }
    
//    private static Node nodeOrStr(Object obj)
//    {
//        if ( obj instanceof Node) return (Node)obj ;
//        if ( obj instanceof String) return Node.createURI((String)obj) ;
//        throw new DataException("Not a string nor a Node: ("+Utils.className(obj)+") "+obj) ;
//    }
}
