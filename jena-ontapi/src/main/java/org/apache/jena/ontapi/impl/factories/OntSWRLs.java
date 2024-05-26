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

package org.apache.jena.ontapi.impl.factories;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.EnhNodeProducer;
import org.apache.jena.ontapi.common.OntEnhGraph;
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.ontapi.impl.objects.OntSWRLImpl;
import org.apache.jena.ontapi.model.OntSWRL;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SWRL;

final class OntSWRLs {
    public static final EnhNodeFilter VARIABLE_FILTER = EnhNodeFilter.URI.and(new EnhNodeFilter.HasType(SWRL.Variable));
    public static final EnhNodeFilter BUILTIN_FILTER = (n, g) -> {
        if (!n.isURI())
            return false;
        OntPersonality p = OntEnhGraph.asPersonalityModel(g).getOntPersonality();
        if (p.getBuiltins().get(OntSWRL.Builtin.class).contains(n)) {
            return true;
        }
        return Iterators.findFirst(g.asGraph().find(n, RDF.Nodes.type, SWRL.Builtin.asNode())).isPresent();
    };

    public static EnhNodeFactory makeAtomFactory(Class<? extends OntSWRLImpl.AtomImpl<?>> view, Resource type) {
        return OntEnhNodeFactories.createCommon(new EnhNodeProducer.Default(view),
                new EnhNodeFinder.ByType(type), EnhNodeFilter.ANON.and(new EnhNodeFilter.HasType(type)));
    }

    public static class SWRLImplFactory extends BaseEnhNodeFactoryImpl {
        private static final Node IMP = SWRL.Imp.asNode();
        private static final Node BODY = SWRL.body.asNode();
        private static final Node HEAD = SWRL.head.asNode();
        private static final Node LIST = SWRL.AtomList.asNode();

        private static final Implementation LIST_FACTORY = STDObjectFactories.RDF_LIST;

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return eg.asGraph().find(Node.ANY, RDF.Nodes.type, IMP)
                    .filterKeep(t -> hasAtomList(HEAD, t.getSubject(), eg) && hasAtomList(BODY, t.getSubject(), eg))
                    .mapWith(t -> createInstance(t.getSubject(), eg));
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return eg.asGraph().contains(node, RDF.Nodes.type, IMP)
                    && hasAtomList(HEAD, node, eg)
                    && hasAtomList(BODY, node, eg);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            return new OntSWRLImpl.ImpImpl(node, eg);
        }

        private boolean hasAtomList(Node p, Node node, EnhGraph eg) {
            return Iterators.anyMatch(eg.asGraph().find(node, p, Node.ANY), t -> isAtomList(t.getObject(), eg));
        }

        private boolean isAtomList(Node n, EnhGraph eg) {
            if (RDF.Nodes.nil.equals(n)) return true;
            return eg.asGraph().contains(n, RDF.Nodes.type, LIST) && LIST_FACTORY.canWrap(n, eg);
        }
    }
}
