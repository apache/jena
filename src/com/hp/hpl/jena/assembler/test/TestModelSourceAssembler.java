/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestModelSourceAssembler.java,v 1.5 2006-05-01 10:54:25 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.ModelSourceAssembler;
import com.hp.hpl.jena.assembler.exceptions.PropertyRequiredException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.MemoryModelGetter;

public class TestModelSourceAssembler extends AssemblerTestBase
    {
    public TestModelSourceAssembler( String name )
        { super( name ); }

    protected Class getAssemblerClass()
        { return ModelSourceAssembler.class; }

    public void testModelSourceAssemblerType()
        { testDemandsMinimalType( new ModelSourceAssembler(), JA.ModelSource );  }
   
    public void testModelSourceVocabulary()
        {
        assertDomain( JA.Connectable, JA.connection );
        assertRange( JA.Connection, JA.connection );
        assertSubclassOf( JA.Connectable, JA.Object );
        assertSubclassOf( JA.RDBModelSource, JA.Connectable );
        assertSubclassOf( JA.RDBModelSource, JA.ModelSource );
        }
    
    public void testDBSourceDemandsConnection()
        {
        Resource root = resourceInModel( "x rdf:type ja:ModelSource; x rdf:type ja:RDBModelSource" );
        Assembler a = new ModelSourceAssembler();
        try 
            { a.open( root ); fail( "should catch missing connection" ); }
        catch (PropertyRequiredException e) 
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( JA.connection, e.getProperty() );
            }
        }
    
    public void testMemModelMakerSource()
        {
        Assembler a = new ModelSourceAssembler();
        ModelGetter g = (ModelGetter) a.open( resourceInModel( "mg rdf:type ja:ModelSource" ) );
        assertInstanceOf( MemoryModelGetter.class, g );
        }
    
    public void testRDBModelMakerSource()
        {
        final ConnectionDescription c = new ConnectionDescription( "url", "user", "password", "type" );
        final List history = new ArrayList();
        Assembler a = new ModelSourceAssembler() 
            {
            protected ModelGetter createRDBGetter( ConnectionDescription cGiven )
                {
                assertSame( c, cGiven );
                history.add( "created" );
                return ModelFactory.createMemModelMaker();
                }
            };
        Assembler mock = new NamedObjectAssembler( resource( "C" ), c );
        Resource root = resourceInModel( "mg rdf:type ja:RDBModelSource; mg rdf:type ja:ModelSource; mg ja:connection C" );
        assertInstanceOf( ModelGetter.class, a.open( mock, root ) );
        assertEquals( listOfOne( "created" ), history );
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