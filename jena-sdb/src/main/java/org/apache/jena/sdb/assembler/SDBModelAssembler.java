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

package org.apache.jena.sdb.assembler;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sdb.SDBFactory ;
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sparql.util.graph.GraphUtils ;

public class SDBModelAssembler extends AssemblerBase implements Assembler
{
    DatasetStoreAssembler datasetAssem = new DatasetStoreAssembler() ;
    
    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        // Make a model.
        // [] rdf:type sdb:Model ;
        //    sdb:dataset <dataset> ;
        //    sdb:graphName <someURI> .
        
        // A model (graph) is a (dataset, name) pair where the name can be absent
        // meaning the default graph of the dataset.
        
        Resource dataset = GraphUtils.getResourceValue(root, AssemblerVocab.pDataset) ;
        if ( dataset == null )
            throw new MissingException(root, "No dataset for model or graph") ;
        StoreDesc storeDesc = datasetAssem.openStore(a, dataset, mode) ;

        // Attempt to find a graph name - may be absent.
        // Two names : "namedGraph" and "graphName"
        String x = GraphUtils.getAsStringValue(root, AssemblerVocab.pNamedGraph1) ;
        if ( x == null )
            x = GraphUtils.getAsStringValue(root, AssemblerVocab.pNamedGraph2) ;
        
        // No name - default model.
        Graph g = null ;
        if ( x == null )
            return SDBFactory.connectDefaultModel(storeDesc) ;
        else
            return SDBFactory.connectNamedModel(storeDesc, x) ;
    }
}
