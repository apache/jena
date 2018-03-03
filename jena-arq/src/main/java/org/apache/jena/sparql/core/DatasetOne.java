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

package org.apache.jena.sparql.core;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.core.DatasetImpl;

/**
 * A dataset that just hold a single model as the default graph. 
 * It is particularly appropriate for use with inference models.
 * 
 * @apiNote
 * This class makes the use of DatasetImpl with one fixed model clearer. It may
 * become useful to have a separate implementation altogether at some time.
 */
public class DatasetOne extends DatasetImpl {
    public static Dataset create(Model model) {
        return new DatasetOne(model);
    }

    private final Model defaultModel;

    public DatasetOne(Model model) {
        super(DatasetGraphOne.create(model.getGraph()));
        this.defaultModel = model;
    }


    @Override
    public Model getDefaultModel() {
        return defaultModel;
    }

    @Override
    public Dataset setDefaultModel(Model model) {
        throw new UnsupportedOperationException("Can not set the default model after a DatasetOne has been created");
    }

    @Override
    public Model getNamedModel(String uri) {
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        return graph2model(dsg.getGraph(n)) ;
    }

    @Override
    public Dataset addNamedModel(String uri, Model model) {
        throw new UnsupportedOperationException("Can not add a named mode to DatasetOne");
    }

    @Override
    public Dataset removeNamedModel(String uri) {
        return this;
    }

    @Override
    public Dataset replaceNamedModel(String uri, Model model) {
        throw new UnsupportedOperationException("Can not replace a named model in DatasetOne");
    }
    @Override
    public boolean containsNamedModel(String uri) {
        return false;
    }
}
