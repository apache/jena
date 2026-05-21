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

package org.apache.jena.assembler;

import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.CannotConstructException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.rdf.model.impl.RDFWriterFImpl;
import org.apache.jena.shared.BrokenException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.test.X_RDFReaderF;
import org.apache.jena.test.X_RDFWriterF;
import org.apache.jena.vocabulary.LocationMappingVocab;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import static org.apache.jena.rdf.model.ModelTestLib.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AssemblerTestLib {

    static { JenaSystem.init();
        JenaSystem.init();
        // Include parsers and writers needed for the tests.
        // These are not up-to-date but enough to work with the test suite.
        RDFReaderFImpl.alternative(new X_RDFReaderF());
        RDFWriterFImpl.alternative(new X_RDFWriterF());
    }

    /*package*/ static  Class<? extends Assembler> getAssemblerClass() {
        throw new BrokenException("this class must define getAssemblerClass");
    }

    /**
     * An assembler that always returns the same fixed object.
     */
    /*package*/ static  final class FixedObjectAssembler extends AssemblerBase {
        private final Object x;

        protected FixedObjectAssembler(Object x) {
            this.x = x;
        }

        @Override
        public Object open(Assembler a, Resource root, Mode irrelevant) {
            return x;
        }
    }

    /**
     * An assembler that insists on being called on a given name, and always returns
     * the same fixed object.
     */
    /*package*/ static  class NamedObjectAssembler extends AssemblerBase {
        final Resource name;
        final Object result;

        public NamedObjectAssembler(Resource name, Object result) {
            this.name = name;
            this.result = result;
        }

        @Override
        public Model openModel(Resource root, Mode mode) {
            return (Model)open(this, root, mode);
        }

        @Override
        public Object open(Assembler a, Resource root, Mode irrelevant) {
            assertEquals(name, root);
            return result;
        }
    }

    /*package*/ static  final Model schema = JA.getSchema();

    /*package*/ static  Model model(String string) {
        Model result = createModel();
        setRequiredPrefixes(result);
        return modelAddFacts(result, string);
    }

    /*package*/ static  Model model() {
        return model("");
    }

    /**
     * Subclasses may override to use their choice of string-to-model parsers.
     */
    /*package*/ static  Model modelAddFacts(Model result, String string) {
        return modelAdd(result, string);
    }

    /**
     * Subclasses may extend to get their choice of defined prefixes.
     */
    /*package*/ static  Model setRequiredPrefixes(Model m) {
        m.setNsPrefix("ja", JA.getURI());
        m.setNsPrefix("lm", LocationMappingVocab.getURI());
        return m;
    }

    /*package*/ static  Resource resourceInModel(String string) {
        Model m = model(string);
        Resource r = resource(m, string.substring(0, string.indexOf(' ')));
        return r.inModel(m);
    }

    /*package*/ static  void testDemandsMinimalType(Assembler a, Resource type) {
        try {
            a.open(resourceInModel("x rdf:type rdf:Resource"));
            fail("should trap insufficient type");
        } catch (CannotConstructException e) {
            assertEquals(getAssemblerClass(), e.getAssemblerClass());
            assertEquals(type, e.getType());
            assertEquals(resource("x"), e.getRoot());
        }
    }

    /*package*/ static  void assertSamePrefixMapping(PrefixMapping wanted, PrefixMapping got) {
        if ( !wanted.samePrefixMappingAs(got) )
            fail("wanted: " + wanted + " but got: " + got);
    }

    /**
     * assert that the property <code>p</code> has <code>domain</code> as its
     * rdfs:domain.
     */
    /*package*/ static  void assertDomain(Resource domain, Property p) {
        if ( !schema.contains(p, RDFS.domain, domain) )
            fail(p + " was expected to have domain " + domain);
    }

    /**
     * assert that the property <code>p</code> has <code>range</code> as its
     * rdfs:range.
     */
    /*package*/ static  void assertRange(Resource range, Property p) {
        if ( !schema.contains(p, RDFS.range, range) )
            fail(p + " was expected to have range " + range);
    }

    /**
     * assert that <code>expectedSub</code> is an rdfs:subClassOf
     * <code>expectedSuper</code>.
     */
    /*package*/ static  void assertSubclassOf(Resource expectedSub, Resource expectedSuper) {
        if ( !schema.contains(expectedSub, RDFS.subClassOf, expectedSuper) )
            fail(expectedSub + " should be a subclass of " + expectedSuper);
    }

    /**
     * assert that <code>instance</code> has rdf:type <code>type</code>.
     */
    /*package*/ static  void assertType(Resource type, Resource instance) {
        if ( !schema.contains(instance, RDF.type, type) )
            fail(instance + " should have rdf:type " + type);
    }

}
