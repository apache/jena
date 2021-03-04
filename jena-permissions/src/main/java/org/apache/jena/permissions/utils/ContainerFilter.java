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
package org.apache.jena.permissions.utils;

import java.util.function.Predicate;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

/**
 * A Predicate that filters out all predicates that do not start with '_'
 * followed by an integer.
 *
 */
public class ContainerFilter implements Predicate<Statement> {

    @Override
    public boolean test(final Statement o) {
        final Property p = o.getPredicate();
        if (p.getNameSpace().equals(RDF.getURI()) && p.getLocalName().startsWith("_")) {
            try {
                Integer.parseInt(p.getLocalName().substring(1));
                return true;
            } catch (final NumberFormatException e) {
                // acceptable;
            }
        }
        return false;
    }

}
