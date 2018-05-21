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

import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.graph.GraphUtils;

/**
 * An assembler that creates a dataset around a single graph. The dataset created is
 * fixed; graphs can not be added or removed. The wrapped graph is the default graph of
 * the dataset.
 * <p>
 * General datasets and SPARQL Update can create graphs by inserting a quad.
 * The dataset returned by this assembler does not support that.
 * 
 * @see DatasetAssembler {@code DatasetAssembler}, for a general dataset.
 * @see InMemDatasetAssembler {@code InMemDatasetAssembler}, for a fully transactional, in-memory dataset.
 */
public class DatasetOneAssembler extends AssemblerBase {
    public static Resource getType() {
        return DatasetAssemblerVocab.tDatasetOne;
    }

    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        Dataset ds = createDataset(a, root, mode);
        return ds;
    }

    public Dataset createDataset(Assembler a, Resource root, Mode mode) {
        // Can use ja:graph or ja:defaultGraph but not both.
        Resource dftGraphDesc1 = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pDefaultGraph);
        Resource dftGraphDesc2 = GraphUtils.getResourceValue(root, DatasetAssemblerVocab.pGraph);
        
        if ( dftGraphDesc1 != null && dftGraphDesc2 != null )
            throw new AssemblerException(root, "Found both ja:graph and ja:defaultGraph"); 
        
        Resource graphDesc = ( dftGraphDesc1 != null) ? dftGraphDesc1 : dftGraphDesc2 ;
        Model model;
        if ( graphDesc != null )
            model = a.openModel(graphDesc);
        else
            // Assembler description did not define one.
            model = GraphFactory.makeDefaultModel();
        Dataset ds = DatasetFactory.wrap(model);
        
        List<RDFNode> nodes = GraphUtils.multiValue(root, DatasetAssemblerVocab.pNamedGraph);
        if ( ! nodes.isEmpty() ) {
            String x = DatasetAssemblerVocab.tDatasetOne.getLocalName();
            throw new AssemblerException(root, "A "+x+" dataset can only hold a default graph, and no named graphs");
        }
        AssemblerUtils.setContext(root, ds.getContext());
        return ds;
    }
}
