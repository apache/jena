/*
 	(c) Copyright 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestRestrictionsDontNeedTyping.java,v 1.1 2007-10-31 16:05:15 chris-dollin Exp $
*/

package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.*;

/**
    Test that restriction inference works even when the restriction isn't given an 
    explicit type (ie we're not caught in a forward rule -> backward rule
    layering problem).
    
 	@author kers
*/
public class TestRestrictionsDontNeedTyping extends ModelTestBase
    {
    public TestRestrictionsDontNeedTyping( String name )
        { super( name ); }

    public void testSomeValuesFrom()
        {
        testSomeValuesFrom( OntModelSpec.OWL_MEM_RULE_INF );
        }

    private void testSomeValuesFrom( OntModelSpec owlSpec )
        {
        Model m = model( "V owl:equivalentClass _R; _R owl:onProperty P; _R owl:someValuesFrom T; X P t; t rdf:type T" );
        OntModel ont = ModelFactory.createOntologyModel( owlSpec, m );
        assertTrue( ont.contains( resource( "X" ), RDF.type, resource( "V" ) ) );
        }
    
    Model model( String statements )
        { 
        Model result = ModelFactory.createDefaultModel();
        result.setNsPrefixes( PrefixMapping.Extended );
        return modelAdd( result, statements );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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