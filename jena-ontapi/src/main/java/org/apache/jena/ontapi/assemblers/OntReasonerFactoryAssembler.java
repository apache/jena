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

package org.apache.jena.ontapi.assemblers;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.assembler.exceptions.CannotHaveRulesException;
import org.apache.jena.assembler.exceptions.NotExpectedTypeException;
import org.apache.jena.assembler.exceptions.UnknownReasonerException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;

import java.util.ArrayList;

/**
 * Copy-paste of ReasonerFactoryAssembler due to conflict.
 *
 * @see org.apache.jena.assembler.assemblers.ReasonerFactoryAssembler
 */
public class OntReasonerFactoryAssembler extends AssemblerBase {

    @Override
    public Object open(Assembler a, Resource root, Mode irrelevant) {
        checkType(root, OA.ReasonerFactory);
        return addSchema(root, a, addRules(root, a, getReasonerFactory(root)));
    }

    private ReasonerFactory addSchema(Resource root, Assembler a, final ReasonerFactory rf) {
        if (!root.hasProperty(OA.schema)) {
            return rf;
        }
        var schema = loadSchema(root, a);
        return new ReasonerFactory() {
            @Override
            public Reasoner create(Resource configuration) {
                return rf.create(configuration).bindSchema(schema);
            }

            @Override
            public Model getCapabilities() {
                return rf.getCapabilities();
            }

            @Override
            public String getURI() {
                return rf.getURI();
            }
        };
    }

    private Graph loadSchema(Resource root, Assembler a) {
        var result = GraphMemFactory.createDefaultGraph();
        root.listProperties(OA.schema).forEach(statement -> loadSchema(result, a, getResource(statement)));
        return result;
    }

    private void loadSchema(Graph result, Assembler a, Resource root) {
        Model m = a.openModel(root);
        Graph g = m.getGraph();
        g.getTransactionHandler().executeAlways(() -> GraphUtil.addInto(result, m.getGraph()));
    }

    private ReasonerFactory addRules(Resource root, Assembler a, final ReasonerFactory r) {
        var rules = OntRuleSetAssembler.addRules(new ArrayList<>(), a, root);
        if (rules.isEmpty()) {
            return r;
        }
        if (!(r instanceof GenericRuleReasonerFactory)) {
            throw new CannotHaveRulesException(root);
        }
        return new ReasonerFactory() {
            @Override
            public Reasoner create(Resource configuration) {
                GenericRuleReasoner result = (GenericRuleReasoner) r.create(configuration);
                result.addRules(rules);
                return result;
            }

            @Override
            public Model getCapabilities() {
                return r.getCapabilities();
            }

            @Override
            public String getURI() {
                return r.getURI();
            }
        };
    }

    protected Reasoner getReasoner(Resource root) {
        return getReasonerFactory(root).create(root);
    }

    protected static ReasonerFactory getReasonerFactory(Resource root) {
        Resource reasonerURL = getUniqueResource(root, OA.reasonerURL);
        String className = getOptionalClassName(root);
        return className != null ? getReasonerFactoryByClassName(root, className)
                : reasonerURL == null ? GenericRuleReasonerFactory.theInstance()
                : getReasonerFactoryByURL(root, reasonerURL);
    }

    private static ReasonerFactory getReasonerFactoryByClassName(Resource root, String className) {
        Class<?> c = loadClass(root, className);
        mustBeReasonerFactory(root, c);
        ReasonerFactory theInstance = resultFromStatic(c, "theInstance");
        return theInstance == null ? createInstance(root, c) : theInstance;
    }

    private static ReasonerFactory createInstance(Resource root, Class<?> c) {
        try {
            return (ReasonerFactory) c.getConstructor().newInstance();
        } catch (Exception e) {
            throw new AssemblerException(root, "could not create instance of " + c.getName(), e);
        }
    }

    private static ReasonerFactory resultFromStatic(Class<?> c, @SuppressWarnings("SameParameterValue") String methodName) {
        try {
            return (ReasonerFactory) c.getMethod(methodName, (Class<?>[]) null).invoke(null, (Object[]) null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Throws a <code>NotExpectedTypeException</code> if <code>c</code>
     * isn't a subclass of <code>ReasonerFactory</code>.
     */
    private static void mustBeReasonerFactory(Resource root, Class<?> c) {
        if (!ReasonerFactory.class.isAssignableFrom(c))
            throw new NotExpectedTypeException(root, ReasonerFactory.class, c);
    }

    /**
     * Answers the string described by the value of the unique optional
     * <code>OA.reasonerClass</code> property of <code>root</code>,
     * or null if there's no such property. The value may be a URI, in which case
     * it must be a <b>java:</b> URI with content the class name; or it may
     * be a literal, in which case its lexical form is its class name; otherwise, BOOM.
     */
    private static String getOptionalClassName(Resource root) {
        return getOptionalClassName(root, OA.reasonerClass);
    }

    /**
     * Answers a ReasonerFactory which delivers reasoners with the given
     * URL <code>reasonerURL</code>. If there is no such reasoner, throw
     * an <code>UnknownReasonerException</code>.
     */
    public static ReasonerFactory getReasonerFactoryByURL(Resource root, Resource reasonerURL) {
        String url = reasonerURL.getURI();
        ReasonerFactory factory = ReasonerRegistry.theRegistry().getFactory(url);
        if (factory == null) throw new UnknownReasonerException(root, reasonerURL);
        return factory;
    }

}
