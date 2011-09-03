/**
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

package org.openjena.fuseki.config;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.assembler.Assembler;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.query.Dataset;

public class DatasetDesc
{
    static Logger log = LoggerFactory.getLogger(DatasetDesc.class) ;
    Resource datasetRoot ; 
    Dataset dataset = null ;    // Unpooled slot.
    int sizeOfPool = -1 ;       // No pool.
    BlockingDeque<Dataset> pool = null ;
    
    Property poolSize = null ;
    
    public DatasetDesc(Resource datasetRoot)
    { 
        this.datasetRoot = datasetRoot ;
    }

    /** Called to create early (e.g. for checking) */
    public void initialize()
    {
        if ( datasetRoot.hasProperty(poolSize) )
        {
            if ( ! GraphUtils.exactlyOneProperty(datasetRoot, poolSize) )
                log.error("Multiple pool size property ("+Utils.nodeLabel(datasetRoot)+")") ;
            
            String x = GraphUtils.getStringValue(datasetRoot, poolSize) ;
            try {
                sizeOfPool = Integer.parseInt(x) ;
            } catch (NumberFormatException ex)
            {
                log.error("Not a number: "+x) ;
                throw ex ; 
            }
            pool = new LinkedBlockingDeque<Dataset>(sizeOfPool) ;
            for ( int i = 0 ; i < sizeOfPool ; i++ )
                pool.addLast(newDataset()) ;
            log.info(String.format("Pool size %d for dataset %s", sizeOfPool, Utils.nodeLabel(datasetRoot))) ;
        }
        else
            dataset = newDataset() ;
    }
    
    private Dataset newDataset()
    {
        return (Dataset)Assembler.general.open(getResource())  ;
    }

    public Resource getResource() { return datasetRoot ; }
    
//    public Dataset acquireDataset(Request request, Response response)
//    {
//        if ( dataset != null )
//            return dataset ;
//        // From pool.
//        try
//        { 
//            log.debug("Take from pool") ; 
//            return pool.takeFirst() ;
//        } catch (InterruptedException ex)
//        {
//            throw new JosekiServerException("Failed to get a dataset from the pool (InterruptedException): "+ex.getMessage()) ;
//        }
//    }
    
    public void returnDataset(Dataset ds)
    {
        
        if ( pool != null )
        {
            log.debug("Return to pool") ;
            pool.addLast(ds) ;
        }
    }
    
    @Override
    public String toString()
    {
        if ( dataset != null )
            return dataset.toString() ;
        
        return "Dataset not set : "+Utils.nodeLabel(datasetRoot) ;
    }
}
