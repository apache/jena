/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: AcceptanceTesting.java,v 1.2 2008-01-02 12:05:57 andy_seaborne Exp $
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

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
