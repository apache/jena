/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestModelExpansion.java,v 1.2 2006-01-11 09:54:38 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.ModelExpansion;
import com.hp.hpl.jena.rdf.model.Model;

public class TestModelExpansion extends AssemblerTestBase
    {
    public TestModelExpansion( String name )
        { super( name ); }

    public void testAddsSubclasses() 
        {
        Model base = model( "a R b" );
        Model schema = model( "x rdfs:subClassOf y; y P z" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b; x rdfs:subClassOf y" ), answer );
        }
    
    public void testOmitsAnonynousSubclasses()
        {
        Model base = model( "a R b" );
        Model schema = model( "x rdfs:subClassOf _y; z rdfs:subClassOf _a" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b" ), answer );
        }
    
    public void testAddsDomainTypes()
        {
        Model base = model( "a R b" );
        Model schema = model( "R rdfs:domain T" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b; a rdf:type T" ), answer );
        }
    
    public void testAddsRangeTypes()
        {
        Model base = model( "a R b" );
        Model schema = model( "R rdfs:range T" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a R b; b rdf:type T" ), answer );
        }
    
    public void testAddsSupertypes()
        {
        Model base = model( "a rdf:type T; T rdfs:subClassOf U" );
        Model schema = model( "T rdfs:subClassOf V" );
        Model answer = ModelExpansion.withSchema( base, schema );
        assertIsoModels( model( "a rdf:type T; a rdf:type U; a rdf:type V; T rdfs:subClassOf U; T rdfs:subClassOf V" ), answer );
        }
    }


/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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