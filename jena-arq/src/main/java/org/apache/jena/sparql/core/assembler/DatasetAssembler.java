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

package org.apache.jena.sparql.core.assembler ;

import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;

import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.DatasetGraph;

public abstract class DatasetAssembler extends AssemblerBase implements Assembler {

    /** @deprecated Use {@link #getGeneralType} */
    @Deprecated
    public static Resource getType() { return getGeneralType(); }

    /** This is the superclass of all datasets assemblers */
       public static Resource getGeneralType() {
        return DatasetAssemblerVocab.tDataset ;
    }

    @Override
    public Dataset open(Assembler a, Resource root, Mode mode) {
        DatasetGraph dsg = createNamedDataset(a, root) ;
        return DatasetFactory.wrap(dsg);
    }

    /**
     * Indirection to allow subclasses to have a pool of created datasets
     * (e.g. {@link NamedDatasetAssembler}).
     * <p>
     * Not used by TDB with a location because databases required
     * to be shared system-wide by location. This includes in-memory
     * named locations.
     */
    protected DatasetGraph createNamedDataset(Assembler a, Resource root) {
        return createDataset(a, root);
    }

    /**
     * Create a fresh dataset from the description.
     */
    protected abstract DatasetGraph createDataset(Assembler a, Resource root);

    /**
     * Helper for datasets that layer on top of other datasets.
     * The property is usually {@code ja:dataset}.
     * Assemble a DatasetGraph from description referred to by resource-property.
     */
    protected DatasetGraph createBaseDataset(Resource dbAssem, Property pDataset) {
        Resource dataset = getResourceValue(dbAssem, pDataset) ;
        if ( dataset == null )
            throw new AssemblerException(dbAssem, "Required base dataset missing: "+dbAssem) ;
        Dataset base = (Dataset)Assembler.general.open(dataset);
        return base.asDatasetGraph();
    }
}
