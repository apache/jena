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

package org.apache.jena.fuseki.access;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class AssemblerAccessDataset extends AssemblerBase {
    
    /*
     * <#access_dataset>  rdf:type access:AccessControlledDataset ;
     *    access:registry   <#securityRegistry> ;
     *    access:dataset    <#tdb_dataset_read> ;
     *    .
     */
    @Override
    public Dataset open(Assembler a, Resource root, Mode mode) {
        if ( ! GraphUtils.exactlyOneProperty(root, VocabSecurity.pSecurityRegistry) )
            throw new AssemblerException(root, "Expected exactly one access:registry property"); 
        if ( ! GraphUtils.exactlyOneProperty(root, VocabSecurity.pDataset) )
            throw new AssemblerException(root, "Expected exactly one access:dataset property"); 
        
        RDFNode rnRegistry = root.getProperty(VocabSecurity.pSecurityRegistry).getObject();
        RDFNode rnDataset = root.getProperty(VocabSecurity.pDataset).getObject();
        
        SecurityRegistry sr = (SecurityRegistry)a.open(rnRegistry.asResource()) ;
        Dataset ds = (Dataset)a.open(rnDataset.asResource()) ;
        
        DatasetGraph dsg = new DatasetGraphAccessControl(ds.asDatasetGraph(), sr);
        ds = DatasetFactory.wrap(dsg);
        
//        // Add marker
//        ds.getContext().set(DataAccessCtl.symControlledAccess, true);
//        // Add security registry
//        ds.getContext().set(DataAccessCtl.symSecurityRegistry, sr);
        return ds;
    }
    
}
