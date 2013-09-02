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

package org.apache.jena.query.spatial.assembler;

import static org.apache.jena.query.spatial.assembler.SpatialVocab.pDataset;
import static org.apache.jena.query.spatial.assembler.SpatialVocab.pIndex;

import org.apache.jena.query.spatial.SpatialDatasetFactory;
import org.apache.jena.query.spatial.SpatialIndex;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

public class SpatialDatasetAssembler extends AssemblerBase implements Assembler
{
//    private DatasetAssembler datasetAssembler = new DatasetAssembler() ;
//    
//    public static Resource getType() { return textDataset ; }
        
    /*
<#spatial_dataset> rdf:type     spatial:SpatialDataset ;
    spatial:dataset <#dataset> ;
    spatial:index   <#index> ;
    .

    */
    
    @Override
    public Dataset open(Assembler a, Resource root, Mode mode)
    {
        Resource dataset = GraphUtils.getResourceValue(root, pDataset) ;
        Resource index   = GraphUtils.getResourceValue(root, pIndex) ;
        
        Dataset ds = (Dataset)a.open(dataset) ;
        SpatialIndex textIndex = (SpatialIndex)a.open(index) ;
        
        Dataset dst = SpatialDatasetFactory.create(ds, textIndex) ;
        return dst ;
        
    }
}

