/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl ;
import com.hp.hpl.jena.sparql.core.DataSourceImpl ;
import com.hp.hpl.jena.sparql.core.DatasetDesc ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
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
        DataSource ds = new DataSourceImpl() ;
        addInGraphs(ds, uriList, namedSourceList, fileManager, baseURI) ;
        return ds ;
    }

    public static Dataset createDataset(DatasetDesc datasetDesc)
    {
        return createDataset(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), null, null) ;
    }

    public static Dataset createDataset(DatasetDesc datasetDesc,  
                                        FileManager fileManager, String baseURI)
    {
        return createDataset(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), fileManager, baseURI) ;
    }
    
    
    /** add graphs into an exiting DataSource */
    public static Dataset addInGraphs(DataSource ds, List<String> uriList, List<String> namedSourceList)
    {
        return addInGraphs(ds, uriList, namedSourceList, null, null) ;
    }
    
    /** add graphs into an exiting DataSource */
    public static Dataset addInGraphs(DataSource ds, List<String> uriList, List<String> namedSourceList,
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
    
    public static DatasetGraph createDatasetGraph(DatasetDesc datasetDesc)
    {
        return createDatasetGraph(datasetDesc.getDefaultGraphURIs(), datasetDesc.getNamedGraphURIs(), null, null) ;
    }

    public static DatasetGraph createDatasetGraph(DatasetDesc datasetDesc,  
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
        DataSourceGraphImpl ds = new DataSourceGraphImpl() ;
        
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
                    absURI = IRIResolver.resolve(baseURI, sourceURI) ;
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
 
/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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