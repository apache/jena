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

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

public class AcceptanceTesting extends AssemblerTestBase
    {
    public AcceptanceTesting( String name )
        { super( name ); }

    /**
        Acceptance test inherited from ontology ModelSpec tests when ModelSpec
        went obsolete. Ensure that an OntModel constructed with a reasoner
        does the (well, some) reasoning. Probably unnecessary given the way
        the assembler unit test suite works but belt-and-braces for now at least.
    */
    public void test_ijd_01()
        {
        Model m = modelWithStatements
            ( "x ja:ontModelSpec _o"
            + "; _o ja:reasonerFactory _f; _o ja:ontLanguage http://www.w3.org/2002/07/owl#"
            + "; _f ja:reasonerURL http://jena.hpl.hp.com/2003/OWLFBRuleReasoner" );
        OntModel om = (OntModel) ModelFactory.assembleModelFrom( m );
        proxyForReasoning( om );
        }

    /**
         A proxy for the notion "reasoning works on this OntModel".
    */
    private void proxyForReasoning( OntModel om )
        {
        OntClass A = om.createClass( "A" );
        OntClass B = om.createClass( "B" );
        OntClass C = om.createClass( "C" );
        C.addSuperClass( B );
        B.addSuperClass( A );
        assertTrue( C.hasSuperClass( A ) );
        }
    }
