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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.junit.Test;

/** Basic testing of the Dataset API */
public abstract class AbstractTestDataset
{
    protected abstract Dataset createDataset();

    static Model model1 = ModelFactory.createDefaultModel();
    static Model model2 = ModelFactory.createDefaultModel();

    static Resource s1 = model1.createResource("s1");
    static Resource s2 = model1.createResource("s2");

    static Property p1 = model1.createProperty("p1");
    static Property p2 = model1.createProperty("p2");

    static Resource o1 = model1.createResource("o1");
    static Resource o2 = model1.createResource("o2");

    static {
        model1.add(s1, p1, o1);
        model2.add(s2, p2, o2);
    }

    @Test public void dataset_01() {
        Dataset ds = createDataset();
        assertNotNull(ds.getDefaultModel());
        assertNotNull(ds.asDatasetGraph());
    }

    @Test public void dataset_02() {
        Dataset ds = createDataset();
        ds.getDefaultModel().add(s1,p1,o1);
        assertTrue(model1.isIsomorphicWith(ds.getDefaultModel()));
    }

    @Test public void dataset_03() {
        Dataset ds = createDataset();
        ds.setDefaultModel(model2);
        assertTrue(model2.isIsomorphicWith(ds.getDefaultModel()));
    }

    @Test public void dataset_04() {
        String graphName = "http://example/";
        Dataset ds = createDataset();
        ds.addNamedModel(graphName, model1);
        assertTrue(ds.containsNamedModel(graphName));

        List<String> x = Iter.toList(ds.listNames());
        assertEquals(1, x.size());
        assertEquals(graphName, x.get(0));

        assertFalse(model1.isIsomorphicWith(ds.getDefaultModel()));
        Model m = ds.getNamedModel(graphName);

        assertNotNull(m);
        assertTrue(model1.isIsomorphicWith(m));

        ds.removeNamedModel(graphName);
        // Not getNamedModel and test for null as some datasets are "auto graph creating"
        assertFalse(ds.containsNamedModel(graphName));
    }

    @Test public void dataset_05() {
        String graphName = "http://example/";
        Dataset ds = createDataset();
        ds.addNamedModel(graphName, model1);
        ds.replaceNamedModel(graphName, model2);
        assertTrue(ds.containsNamedModel(graphName));

        List<String> x = Iter.toList(ds.listNames());
        assertEquals(1, x.size());
        assertEquals(graphName, x.get(0));

        assertFalse(model1.isIsomorphicWith(ds.getNamedModel(graphName)));
        assertTrue(model2.isIsomorphicWith(ds.getNamedModel(graphName)));
    }

    @Test public void dataset_06() {
        String graphName = "http://example/";
        Dataset ds = createDataset();
        ds.addNamedModel(graphName, model1);
        assertFalse("Dataset should not be empty after a named graph has been added!", ds.isEmpty());
    }

    @Test public void dataset_07() {
        String graphName = "http://example/";
        Dataset ds = createDataset();
        ds.addNamedModel(graphName, model1);
        assertTrue("Named graph not found", ds.containsNamedModel(graphName));
    }

    // Even if empty, union and named default graph exist (sort of).

    @Test public void dataset_08() {
        Dataset ds = createDataset();
        assertTrue("Union named graph not found", ds.containsNamedModel(Quad.unionGraph.getURI()));
    }

    @Test public void dataset_09() {
        Dataset ds = createDataset();
        assertTrue("Default graph not found using '<"+Quad.defaultGraphIRI.getURI()+">'", ds.containsNamedModel(Quad.defaultGraphIRI.getURI()));
    }

    @Test public void dataset_10() {
        Dataset ds = createDataset();
        Resource name = ResourceFactory.createResource("http://ex/name1");

        ds.addNamedModel(name, model1);
        assertTrue(ds.containsNamedModel(name));
        assertTrue(Iter.anyMatch(ds.listNames(), x->"http://ex/name1".equals(x)));

        ds.removeNamedModel(name);
        assertFalse(ds.containsNamedModel(name));
    }

    @Test public void dataset_11() {
        Dataset ds = createDataset();
        Resource name = ResourceFactory.createResource();

        ds.addNamedModel(name, model1);
        assertTrue(ds.containsNamedModel(name));
        assertTrue(Iter.anyMatch(ds.listModelNames(), x->x.equals(name)));

        ds.removeNamedModel(name);
        assertFalse(ds.containsNamedModel(name));
    }
}
