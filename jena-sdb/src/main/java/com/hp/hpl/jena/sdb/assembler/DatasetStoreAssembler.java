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

package com.hp.hpl.jena.sdb.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.StoreDesc;

public class DatasetStoreAssembler extends AssemblerBase implements Assembler
{
    static StoreDescAssembler storeAssem = new StoreDescAssembler() ;
    
    @Override
    public Dataset open(Assembler a, Resource root, Mode mode)
    {
        StoreDesc desc = openStore(a, root, mode) ;
        Dataset ds = SDBFactory.connectDataset(desc) ;
        AssemblerUtils.setContext(root, ds.getContext());
        return ds ;
    }
    
    /** Get the StoreDesc for this dataset */ 
    public StoreDesc openStore(Assembler a, Resource root, Mode mode)
    {
        Resource s = storeResource(root) ;
        StoreDesc desc = storeAssem.open(a, s, mode) ;
        return desc ;
    }
    
    static Resource storeResource(Resource dsAssem)
    {
        return GraphUtils.getResourceValue(dsAssem, AssemblerVocab.pStore) ;
        
    }
}
