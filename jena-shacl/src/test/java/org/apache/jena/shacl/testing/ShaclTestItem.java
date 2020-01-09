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

package org.apache.jena.shacl.testing;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.test_vocab.MF;
import org.apache.jena.shacl.test_vocab.SHT;

public class ShaclTestItem {
    private final String name;
    private final String origin;
    private final Resource item;

    private final Resource shapesGraph;
    private final Resource dataGraph;
    private final Resource result;

    public ShaclTestItem(String name, String origin, Resource r) {
        this.name = name;
        this.origin = origin;
        this.item = r;
        Resource action = item.getRequiredProperty(MF.action).getResource();
        shapesGraph = action.getRequiredProperty(SHT.shapesGraph).getResource();
        dataGraph = action.getRequiredProperty(SHT.dataGraph).getResource();

        // Validation entry:
        result = item.getRequiredProperty(MF.result).getResource();
    }

    public String name() {
        return name;
    }

    public String origin() {
        return origin;
    }

    public Resource getItem() {
        return item;
    }

    public Resource getShapesGraph() {
        return shapesGraph;
    }

    public Resource getDataGraph() {
        return dataGraph;
    }

    public Resource getResult() {
        return result;
    }

    public boolean isGeneralFailure() {
        return result.equals(SHT.Failure) ;
    }
}
