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

package com.hp.hpl.jena.ontology.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
    Ensure that an OntGraph passes the graph tests. Clunky because it has to go
    via OntModel - there doesn't appear to be an OntGraph class.
*/

public class TestOntGraph extends AbstractTestGraph
    {
    public TestOntGraph( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestOntGraph.class ); }

    @Override
    public Graph getGraph()
        { return ModelFactory.createOntologyModel().getGraph(); }
    }
