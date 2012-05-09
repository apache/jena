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
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileManager ;

/** Internal Dataset/DataSource factory + graph equivalents. */

public class DatasetUtils
{
    
    public static Dataset createDataset(String uri, List<String> namedSourceList)
    {
        return createDataset(uri, namedSourceList, null, null) ;
    }

    public static Dataset createDataset(String uri, List<String> namedSourceList,
                                        FileManager fileManager, String baseURI)
    {
        List<String> uriList = new ArrayList<String>() ;
        uriList.add(uri) ;
        return createDataset(uriList, namedSourceList, fileManager, baseURI) ;
    }
    
    public static Dataset createDataset(List<String> uriList, List<String> namedSourceList)
    {
        return createDataset(uriList, namedSourceList, null, null) ;
    }

    public static Dataset createDataset(List<String> uriList, List<String> namedSourceList,
                                        FileManager fileManager, String baseURI)
    {
        // Fixed dataset - any GRAPH <notThere> in a query must return no match.
        Dataset ds = DatasetFactory.createMemFixed() ;
        addInGraphs(ds, uriList, namedSourceList, fileManager, baseURI) ;
        return ds ;
    }

    public static Dataset createDataset(DatasetDescription datasetDesc)
    {
        return createDataset(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), null, null) ;
    }

    public static Dataset createDataset(DatasetDescription datasetDesc,  
                                        FileManager fileManager, String baseURI)
    {
        return createDataset(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), fileManager, baseURI) ;
    }
    
    
    /** add graphs into an exiting DataSource */
    public static Dataset addInGraphs(Dataset ds, List<String> uriList, List<String> namedSourceList)
    {
        return addInGraphs(ds, uriList, namedSourceList, null, null) ;
    }
    
    /** add graphs into an existing DataSource */
    public static Dataset addInGraphs(Dataset ds, List<String> uriList, List<String> namedSourceList,
                                      FileManager fileManager, String baseURI)
    {
        if ( fileManager == null )
            fileManager = FileManager.get() ;
        
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
                    absURI = IRIResolver.resolve(sourceURI, baseURI) ;
                else
                    absURI = IRIResolver.resolveGlobal(sourceURI) ;
                fileManager.readModel(ds.getDefaultModel(), sourceURI, absURI, null) ;
            }
        }
        
        if ( namedSourceList != null )
        {
            for (Iterator<String> iter = namedSourceList.iterator() ; iter.hasNext() ; )
            {
                String sourceURI = iter.next() ;
                String absURI = null ;
                if ( baseURI != null )
                    absURI = IRIResolver.resolve(sourceURI, baseURI) ;
                else
                    absURI = IRIResolver.resolveGlobal(sourceURI) ;
                Model m = GraphFactory.makeDefaultModel() ;
                fileManager.readModel(m, sourceURI, absURI, null) ;
                ds.addNamedModel(absURI, m) ;
            }
        }
        return ds ;
    }
    
    // ---- DatasetGraph level.
    
    public static DatasetGraph createDatasetGraph(DatasetDescription datasetDesc)
    {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), null, null) ;
    }

    public static DatasetGraph createDatasetGraph(DatasetDescription datasetDesc,  
                                                  FileManager fileManager, String baseURI)
    {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), fileManager, baseURI) ;
    }
    
    
        
    public static DatasetGraph createDatasetGraph(String uri, List<String> namedSourceList,
                                                  FileManager fileManager, String baseURI)
   {
       List<String> uriList = new ArrayList<String>() ;
       uriList.add(uri) ;
       return createDatasetGraph(uriList, namedSourceList, fileManager, baseURI) ;
   }

    public static DatasetGraph createDatasetGraph(List<String> uriList, List<String> namedSourceList,
                                                  FileManager fileManager, String baseURI)
    {
        DatasetGraph ds = DatasetGraphFactory.createMem() ;
        
        if ( fileManager == null )
            fileManager = FileManager.get() ;

        // Merge into background graph
        if ( uriList != null )
        {
            Model m = GraphFactory.makeDefaultModel() ;
            for (Iterator<String> iter = uriList.iterator() ; iter.hasNext() ; )
            {
                String sourceURI = iter.next() ;
                String absURI = null ;
                if ( baseURI != null )
                    absURI = IRIResolver.resolve(sourceURI, baseURI) ;
                else
                    absURI = IRIResolver.resolveGlobal(sourceURI) ;
                // FileManager.readGraph?
                fileManager.readModel(m, sourceURI, absURI, null) ;
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
                    absURI = IRIResolver.resolve(baseURI, sourceURI) ;
                else
                    absURI = IRIResolver.resolveGlobal(sourceURI) ;
                Model m = fileManager.loadModel(sourceURI, absURI, null) ;
                Node gn = Node.createURI(sourceURI) ;
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
