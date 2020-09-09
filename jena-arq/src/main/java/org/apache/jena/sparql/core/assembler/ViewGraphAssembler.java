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

package org.apache.jena.sparql.core.assembler;

import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pDataset;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pGraphName;
import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.pNamedGraph;
import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/** Assembler to project a model out of a dataset.
 *
 * <pre>
 * &lt;#graph&gt; rdf:type ja:ViewGraph ;
 *     ja:graphName "..." ;            # Optional: else default graph
 *     js:dataset <#dataset> ;
 *     .
 * <#dataset> rdf:type ja:RDFDataset ; # Any subtype
 *    ...
 * </pre>
 *
 */
public class ViewGraphAssembler extends AssemblerBase implements Assembler {

    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        Resource dataset = getResourceValue(root, pDataset) ;
        if ( dataset == null )
            throw new AssemblerException(root, "Must give a dataset with ja:dataset") ;

        String graphName = null ;
        if ( root.hasProperty(pNamedGraph) )
            graphName = getAsStringValue(root, pNamedGraph) ;
        if ( root.hasProperty(pGraphName) )
            graphName = getAsStringValue(root, pGraphName) ;

        Dataset ds = DatasetFactory.assemble(dataset);
        Model model = (graphName == null) ? ds.getDefaultModel() : ds.getNamedModel(graphName) ;
        return model;
    }
}

