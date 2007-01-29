/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: AcceptanceTesting.java,v 1.1 2007-01-29 13:25:06 chris-dollin Exp $
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

