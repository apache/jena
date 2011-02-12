/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import java.util.List ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.DataSourceImpl ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.assembler.DataSourceAssembler ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;
import com.hp.hpl.jena.util.FileManager ;

/** Make Datasets and DataSources in various ways */

public class DatasetFactory
{
    /** Create a Dataset
     * 
     * @return DataSource
     */
    public static DataSource create()
    { return DataSourceImpl.createMem() ; }

    /** Create a dataset with the given model as the default graph
     * @param model
     * @return DataSource (Updateable Dataset) 
     */ 
    public static DataSource create(Model model)
    { return new DataSourceImpl(model) ; }

    /** Create a dataset
     * @param dataset
     * @return DataSource (Updateable Dataset) 
     */ 
    public static DataSource create(Dataset dataset)
    { return new DataSourceImpl(dataset) ; }

    /** Wrap a datasetgraph to make a mutable dataset
     * @param dataset DatasetGraph
     * @return DataSource (Updateable Dataset) 
     */ 
    public static DataSource create(DatasetGraph dataset)
    { return DataSourceImpl.wrap(dataset) ; }
    
    /** Create a dataset based on a list of URIs : these are merged into the default graph of teh dataset.
     * 
     * @param uriList   URIs merged to form the default dataset 
     * @return Dataset
     */
    
    public static Dataset create(List<String> uriList)
    { return create(uriList, null, null, null) ; }
    
    /** Create a dataset with a default graph and no named graphs
     * 
     * @param uri   URIs merged to form the default dataset 
     * @return Dataset
     */
    
    public static Dataset create(String uri)
    { return create(uri, null, null, null) ; }

    /** Create a dataset based on a list of URIs : these are merged into the default graph of teh dataset.
     * 
     * @param uriList   URIs merged to form the default dataset 
     * @param fileManager
     * @return Dataset
     */
    
    public static Dataset create(List<String> uriList, FileManager fileManager)
    { return create(uriList, null, fileManager, null) ; }
                                              
    /** Create a dataset based on a list of URIs : these are merged into the default graph of teh dataset.
     * 
     * @param uri              graph to be loaded into the unnamed, default graph
     * @param fileManager
     * @return Dataset
     */
    
    public static Dataset create(String uri, FileManager fileManager)
    { return create(uri, null, fileManager, null) ; }

    /** Create a named graph container of graphs based on a list of URIs.
     * 
     * @param namedSourceList
     * @param fileManager
     * @return Dataset
     */
    
    public static Dataset createNamed(List<String> namedSourceList, FileManager fileManager)
    { return create((List<String>)null, namedSourceList, fileManager, null) ; }
    
    /** Create a dataset based on two list of URIs.
     *  The first lists is used to create the background (unnamed graph) by merging, the
     *  second is used to create the collection of named graphs.
     *  
     *  (Jena calls graphs "Models" and triples "Statements")
     * 
     * @param uriList          graphs to be loaded into the unnamed, default graph
     * @param namedSourceList  graphs to be atatched as named graphs
     * @return Dataset
     */
    
    public static Dataset create(List<String> uriList, List<String> namedSourceList)
    {
        return create(uriList, namedSourceList, null, null) ;
    }

    /** Create a dataset container based on two list of URIs.
     *  The first is used to create the background (unnamed graph), the
     *  second is used to create the collection of named graphs.
     *  
     *  (Jena calls graphs "Models" and triples "Statements")
     * 
     * @param uri              graph to be loaded into the unnamed, default graph
     * @param namedSourceList  graphs to be attached as named graphs
     * @return Dataset
     */
    
    public static Dataset create(String uri, List<String> namedSourceList)
    {
        return create(uri, namedSourceList, null, null) ;
    }

    /** Create a named graph container based on two list of URIs.
     *  The first is used to create the background (unnamed graph), the
     *  second is used to create the collection of named graphs.
     *  
     *  (Jena calls graphs "Models" and triples "Statements")
     * 
     * @param uri              graph to be loaded into the unnamed, default graph
     * @param namedSourceList  graphs to be atatched as named graphs
     * @param fileManager
     * @param baseURI          baseURI for relative URI expansion
     * @return Dataset
     */
    
    public static Dataset create(String uri, List<String> namedSourceList,
                                 FileManager fileManager, String baseURI)
    {
        return DatasetUtils.createDataset(uri, namedSourceList, fileManager, baseURI) ;
    }
        
   
    /** Create a named graph container based on two list of URIs.
     *  The first is used to create the background (unnamed graph), the
     *  second is used to create the collection of named graphs.
     *  
     *  (Jena calls graphs "Models" and triples "Statements")
     * 
     * @param uriList          graphs to be loaded into the unnamed, default graph
     * @param namedSourceList  graphs to be atatched as named graphs
     * @param fileManager
     * @param baseURI          baseURI for relative URI expansion
     * @return Dataset
     */
    
    public static Dataset create(List<String> uriList, List<String> namedSourceList,
                                 FileManager fileManager, String baseURI)
    {
        return DatasetUtils.createDataset(uriList, namedSourceList, fileManager, baseURI) ;
    }
    
//    public static Dataset make(Dataset ds)
//    {
//        DataSourceImpl ds2 = new DataSourceImpl(ds) ;
//        return ds2 ; 
//    }

//    public static Dataset make(Dataset ds, Graph defaultGraph)
//    {
//        DataSourceImpl ds2 = new DataSourceImpl(ds) ;
//        ds2.setDefaultGraph(defaultGraph) ;
//        return ds2 ; 
//    }

    public static Dataset make(Dataset ds, Model defaultModel)
    {
        DataSourceImpl ds2 = new DataSourceImpl(ds) ;
        ds2.setDefaultModel(defaultModel) ;
        return ds2 ; 
    }
    
    // Assembler.
    /** Assembler a dataset from the model in a file
     * 
     * @param filename      The filename 
     * @return Dataset
     */
    public static Dataset assemble(String filename)
    {
        Model model = FileManager.get().loadModel(filename) ;
        return assemble(model) ;
    }
    
    /** Assembler a dataset from the model
     * 
     * @param model
     * @return Dataset
     */
    public static Dataset assemble(Model model)
    {
        Resource r = GraphUtils.findRootByType(model, DataSourceAssembler.getType()) ;
        if ( r == null )
            throw new ARQException("No root found for type <"+DataSourceAssembler.getType()+">") ;
        
        return assemble(r) ;
    }
        
    /** Assembler a dataset from a resource
     * 
     * @param resource  The resource for the dataset
     * @return Dataset
     */
    
    public static Dataset assemble(Resource resource)
    {
        Dataset ds = (Dataset)Assembler.general.open(resource) ;
        return ds ;
    }
    


}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
