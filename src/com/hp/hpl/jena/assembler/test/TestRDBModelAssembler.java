/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestRDBModelAssembler.java,v 1.7 2006-11-16 14:44:48 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.RDBModelAssembler;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ReificationStyle;

public class TestRDBModelAssembler extends AssemblerTestBase
    {
    public TestRDBModelAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return RDBModelAssembler.class; }

    public void testRDBModelAssemblerType()
        { testDemandsMinimalType( new RDBModelAssembler(), JA.RDBModel );  }

    public void testRDBModelVocabulary()
        {
        Model m = model( "x rdf:type ja:Connectable; x rdf:type ja:NamedModel" );
        Model answer = ModelExpansion.withSchema( m, JA.getSchema() );
        assertTrue( "should infer x rdf:type ja:RDBModel", answer.contains( statement( "x rdf:type ja:RDBModel" ) ) );
        }
    
    public void testInvokesCreateModel()
        {
        Resource root = resourceInModel( "x rdf:type ja:RDBModel; x ja:modelName 'spoo'; x ja:connection C" );
        final ConnectionDescription C = ConnectionDescription.create( "eh:/x", "A", "B", "C", "D" );
        final Model fake = ModelFactory.createDefaultModel();
        final Mode theMode = new Mode( true, true );
        Assembler a = new RDBModelAssembler()
            {
            public Model openModel( Resource root, ConnectionDescription c, String name, ReificationStyle style, Mode mode )
                {
                assertSame( C, c );
                assertSame( theMode, mode );
                return fake;
                }
            };
        Assembler foo = new NamedObjectAssembler( resource( "C" ), C );
        assertSame( fake, a.open( foo, root, theMode ) );
        }
    
    }


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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