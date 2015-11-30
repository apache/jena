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

package org.apache.jena.sparql.core.assembler;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.modify.GraphStoreNull ;
import org.apache.jena.sparql.modify.GraphStoreNullTransactional ;

public class DatasetNullAssembler extends AssemblerBase implements Assembler
{
    public static Resource getType() { return DatasetAssemblerVocab.tDatasetNull ; }
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        boolean transactional = true;
        
        if (root.hasProperty(DatasetAssemblerVocab.pTransactional))
        {
            Node b = root.getProperty(DatasetAssemblerVocab.pTransactional).getObject().asNode();
            NodeValue nv = NodeValue.makeNode(b);
            if (nv.isBoolean())
            {
                transactional = nv.getBoolean();
            }
            else
            {
                Log.warn(DatasetNullAssembler.class,
                         "Failed to recognize value for transactional setting (ignored): " + b);
            }
        }
        
        DatasetGraph dsg = transactional ? new GraphStoreNullTransactional() : new GraphStoreNull();
        
        return DatasetFactory.wrap(dsg) ;
    }
}
