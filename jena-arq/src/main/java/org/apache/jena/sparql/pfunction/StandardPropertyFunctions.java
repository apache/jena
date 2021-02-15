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

package org.apache.jena.sparql.pfunction;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.pfunction.library.triple.TripleTermFind;
import org.apache.jena.sparql.vocabulary.ListPFunction;
import org.apache.jena.vocabulary.RDFS;

public class StandardPropertyFunctions {
    @SuppressWarnings("deprecation")
    public static void loadStdDefs(PropertyFunctionRegistry registry) {
        add(registry, ListPFunction.member.getURI(),    org.apache.jena.sparql.pfunction.library.listMember.class);
        add(registry, ListPFunction.index.getURI(),     org.apache.jena.sparql.pfunction.library.listIndex.class);
        add(registry, ListPFunction.length.getURI(),    org.apache.jena.sparql.pfunction.library.listLength.class);

        add(registry, ListPFunction.memberJ2.getURI(),    org.apache.jena.sparql.pfunction.library.listMember.class);
        add(registry, ListPFunction.indexJ2.getURI(),     org.apache.jena.sparql.pfunction.library.listIndex.class);
        add(registry, ListPFunction.lengthJ2.getURI(),    org.apache.jena.sparql.pfunction.library.listLength.class);

        // This is called during Jena-wide initialization.
        // Use function for constant (JENA-1294)
        add(registry, RDFS.Init.member().getURI(), org.apache.jena.sparql.pfunction.library.container.class);

        // Property function - RDF-star
//        PropertyFunctionFactory factory = (uri)->new TripleTermFind();
//        registry.put(ARQConstants.ARQPropertyFunctionLibraryURI+"find", factory);
        add(registry, ARQConstants.ARQPropertyFunctionLibraryURI+"find", TripleTermFind.class);
    }

    private static void add(PropertyFunctionRegistry registry, String uri, Class<? > funcClass) {
        registry.put(uri, funcClass);
    }
}
