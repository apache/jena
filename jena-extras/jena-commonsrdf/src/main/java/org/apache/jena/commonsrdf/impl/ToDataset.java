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

package org.apache.jena.commonsrdf.impl;

import org.apache.commons.rdf.api.Dataset;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.riot.system.StreamRDF;

/** Adapter to go from Jena StreamRDF (e.g. a parser output stream) to a CommonsRDF Dataset */
public class ToDataset implements StreamRDF {

    private Dataset dataset;
    private RDF factory;

    public ToDataset(RDF factory) {
        this(factory, factory.createDataset());
    }

    public ToDataset(RDF factory, Dataset dataset) {
        this.factory = factory;
        this.dataset = dataset;
    }

    @Override
    public void start() {}

    @Override
    public void triple(org.apache.jena.graph.Triple triple) {
        dataset.getGraph().add(JenaCommonsRDF.fromJena(factory, triple));
    }

    @Override
    public void quad(org.apache.jena.sparql.core.Quad quad) {
        dataset.add(JenaCommonsRDF.fromJena(factory, quad));
    }

    @Override
    public void base(String base) {}

    @Override
    public void prefix(String prefix, String iri) {}

    @Override
    public void finish() {}

    public Dataset getDataset() {
        return dataset;
    }

}

