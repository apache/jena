/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestModelSpecRDB.java,v 1.3 2007-01-02 11:49:24 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model.spec.test;

import com.hp.hpl.jena.db.impl.DriverMap;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.JenaModelSpec;

import junit.framework.TestSuite;

public class TestModelSpecRDB extends ModelTestBase
    {
    public TestModelSpecRDB( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelSpecRDB.class ); }

    public void testSpecExists()
        {
        Model d = modelWithStatements( "" );
        Resource r = d.createResource( "" );
        ModelSpecImpl ms = new RDBModelSpec( r, d );
        }
    
    public void testCreatorExists()
        {
        Model d = modelWithStatements( "" );
        Resource r = resource( d, "_x" );
        ModelSpecCreator c = ModelSpecCreatorRegistry.instance.getCreator
            ( JenaModelSpec.RDBModelSpec );
        ModelSpec x = c.create( r, d );
        assertTrue( x instanceof RDBModelSpec );
        }
    
    public void testExplicitClassName()
        {
        Model d = modelWithStatements( "_x rdf:type jms:RDBModelSpec" );
        Resource r = resource( d, "_x" );
        Model m = modelWithStatements( "_x jms:dbClass 'some.fake.class'" );
        String name = RDBMakerCreator.getClassName( m, resource( "_x" ) );
        assertEquals( "some.fake.class", name );
        }
    
    public void testImpliedClassName()
        {
        Model m = modelWithStatements( "_x jms:dbType 'mysql'" );
        String name = RDBMakerCreator.getClassName( m, resource( "_x" ) );
        assertEquals( DriverMap.get( "mysql" ), name );
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