/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdf.model;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.Parameter;

import org.apache.jena.rdf.model.helpers.ModelCreator;
import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.shared.PrefixMapping;

/**
 * Base for test cases about Models. All derived classes will use the model field for the
 * model created in the setUp method. createModel will create a model using the
 * {@link ModelCreator} the class is parameterized with.
 * <p>
 * Derived classes are annotated {@code @ParameterizedClass} with
 * {@code @MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")},
 * which supplies the modelFactory field.
 */
public abstract class AbstractModelTestBase {

    @Parameter
    protected ModelCreator modelFactory;

    protected Model model;

    /**
     * Create a new model.
     *
     * @return A new model from the modelFactory.
     */
    public final Model createModel() {
        return modelFactory.create();
    }

    /**
     * A new model, with the extended prefixes, containing the given facts.
     * Replaces {@code ModelHelper.modelWithStatements(this, facts)}, which needed
     * a {@code AbstractModelTestBase} to call back into.
     */
    protected Model modelWithStatements(final String facts) {
        return ModelHelper.modelAdd(createExtendedModel(), facts);
    }

    /** A new model, with the extended prefixes. Replaces {@code ModelHelper.createModel(this)}. */
    protected Model createExtendedModel() {
        Model result = createModel();
        result.setNsPrefixes(PrefixMapping.Extended);
        return result;
    }

    /**
     * sets the model instance variable
     */
    @BeforeEach
    public void setUp() {
        model = createModel();
    }

    /**
     * Closes the model instance variable and shuts it down.
     */
    @AfterEach
    public void tearDown() {
        model.close();
        model = null;
    }

    protected static String getFileName(final String fn) {
        URL u = AbstractModelTestBase.class.getClassLoader().getResource(fn);
        if ( u == null ) {
            throw new RuntimeException(new FileNotFoundException(fn));
        }
        try {
            return u.toURI().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected InputStream getInputStream(final String fn) {
        ClassLoader loader = AbstractModelTestBase.class.getClassLoader();
        if ( loader == null )
            throw new SecurityException("Cannot access class loader");
        final InputStream in = loader.getResourceAsStream(fn);
        if ( in == null )
            throw new IllegalArgumentException("Resource: " + fn + " not found on class path.");
        return in;
    }

    public static class LitTestObj {
        protected long content;

        public LitTestObj(final long l) {
            content = l;
        }

        public LitTestObj(final String s) {
            content = Long.parseLong(s.substring(1, s.length() - 1));
        }

        @Override
        public boolean equals(final Object o) {
            return (o instanceof LitTestObj) && (content == ((LitTestObj)o).content);
        }

        @Override
        public int hashCode() {
            return (int)(content ^ (content >> 32));
        }

        @Override
        public String toString() {
            return "[" + Long.toString(content) + "]";
        }
    }

    protected static final boolean tvBoolean = true;
    protected static final byte tvByte = 1;
    protected static final short tvShort = 2;
    protected static final int tvInt = -1;
    protected static final long tvLong = -2;
    protected static final char tvChar = '!';
    protected static final float tvFloat = (float)123.456;
    protected static final double tvDouble = -123.456;
    protected static final String tvString = "test 12 string";
    protected static final Object tvLitObj = new LitTestObj(1234);
    protected static final LitTestObj tvObject = new LitTestObj(12345);
    protected static final double dDelta = 0.000000005;

    protected static final float fDelta = 0.000005f;
}
