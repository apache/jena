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

package org.apache.jena.rdfxml.xmloutput.impl;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.CollectionFactory;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Find all the namespaces needed to print RDF/XML, including inside triple terms.
 * Namespaces are needed for predicates.
 */
public class FindNamespacesRDFXML {

    public static Set<String> namespacesForRDFXML(Model model) {
        return namespacesForRDFXML(model.getGraph());
    }

    public static Set<String> namespacesForRDFXML(Graph graph) {
        Set<String> namespaces = CollectionFactory.createHashedSet();
        ExtendedIterator<Triple> iter = graph.find();
        try {
            iter.forEachRemaining(triple->{
                processTriple(namespaces, triple);
            });
        } finally { iter.close(); }
        return namespaces;
    }

    private static void processTriple(Set<String> namespaces, Triple triple) {
        String predicateURI = triple.getPredicate().getURI();
        accNamespace(namespaces, predicateURI);

        // Nor necessary for RDF/XML but Jena used to include them.
//        if ( triple.getObject().isLiteral() ) {
//            String dtURI = triple.getObject().getLiteralDatatypeURI();
//            accNamepsace(namespaces, dtURI);
//            return;
//        }

        if ( triple.getObject().isTripleTerm() ) {
            processTriple(namespaces, triple.getObject().getTriple());
        }
    }

    private static void accNamespace(Set<String> namespaces, String uri) {
        String ns = uri.substring(0,  SplitIRI.splitXML(uri));
        namespaces.add(ns);
    }

}
