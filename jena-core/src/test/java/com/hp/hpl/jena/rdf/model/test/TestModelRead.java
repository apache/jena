/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test ;

import org.apache.jena.iri.IRIException ;
import org.junit.Assert ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper ;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory ;
import com.hp.hpl.jena.shared.ConfigException ;

/**
 * TestModelRead - test that the model.read operation(s) exist.
 */
public class TestModelRead extends AbstractModelTestBase
{
    protected static Logger logger = LoggerFactory.getLogger(TestModelRead.class) ;

    public TestModelRead(final TestingModelFactory modelFactory, final String name) {
        super(modelFactory, name) ;
    }

    public TestModelRead() {
        this(new TestPackage.PlainModelFactory(), "TestModelRead") ;
    }

    public void testDefaultLangXML() {
        final Model model = ModelFactory.createDefaultModel() ;
        model.read(getFileName("modelReading/plain.rdf"), null, null) ;
    }

    public void testGRDDLConfigMessage() {
        try {
            model.read("http://www.w3.org/", "GRDDL") ;
            // ok.
        } catch (final ConfigException e) {
            // expected.
        }
    }

    public void testLoadsSimpleModel() {
        final Model expected = createModel() ;
        expected.read(getFileName("modelReading/simple.n3"), "N3") ;
        Assert.assertSame(model, model.read(getFileName("modelReading/simple.n3"), "base", "N3")) ;
        ModelHelper.assertIsoModels(expected, model) ;
    }

    public void testReturnsSelf() {

        Assert.assertSame(model, model.read(getFileName("modelReading/empty.n3"), "base", "N3")) ;
        Assert.assertTrue(model.isEmpty()) ;
    }

    public void testSimpleLoadExplicitBase() {
        final Model mBasedExplicit = createModel() ;
        mBasedExplicit.read(getFileName("modelReading/based.n3"), "http://example/", "N3") ;
        ModelHelper.assertIsoModels(ModelHelper.modelWithStatements(this, "http://example/ ja:predicate ja:object"),
                                    mBasedExplicit) ;
    }
    @SuppressWarnings("deprecation")
    public void testSimpleLoadImplictBase() throws IRIException {
        final Model mBasedImplicit = createModel() ;
        final String fn = com.hp.hpl.jena.n3.IRIResolver.resolveFileURL(getFileName("modelReading/based.n3")) ;
        final Model wanted = createModel().add(ModelHelper.resource(fn), ModelHelper.property("ja:predicate"),
                                               ModelHelper.resource("ja:object")) ;
        mBasedImplicit.read(fn, "N3") ;
        ModelHelper.assertIsoModels(wanted, mBasedImplicit) ;
    }

}
