/*
 (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id: TestDatabaseModes.java,v 1.5 2008-01-02 12:10:32 andy_seaborne Exp $
 */

package com.hp.hpl.jena.assembler.acceptance;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.*;

public class TestDatabaseModes extends AssemblerTestBase
    {
    public TestDatabaseModes( String name )
        { super( name ); }

    public void testRDBModelOpenedWhenExists()
        {
        openWith( "square", false, true );
        openWith( "circle", true, true );
        }

    public void testRDBModelCreatedWhenMissing()
        {
        openWith( "line", true, true );
        openWith( "edge", true, false );
        }

    public void testRDBModelFailsIfExists()
        {
        try
            {
            openWith( "triangle", true, false );
            AllAccept.fail( "should trap existing model" );
            }
        catch (AlreadyExistsException e)
            {
            AllAccept.assertEquals( "triangle", e.getMessage() );
            }
        try
            {
            openWith( "hex", false, false );
            AllAccept.fail( "should trap existing model" );
            }
        catch (AlreadyExistsException e)
            {
            AllAccept.assertEquals( "hex", e.getMessage() );
            }
        }

    public void testRDBModelFailsIfMissing()
        {
        try
            {
            openWith( "parabola", false, true );
            AllAccept.fail( "should trap missing model" );
            }
        catch (NotFoundException e)
            {
            AllAccept.assertEquals( "parabola", e.getMessage() );
            }
        try
            {
            openWith( "curve", false, false );
            AllAccept.fail( "should trap missing model" );
            }
        catch (NotFoundException e)
            {
            AllAccept.assertEquals( "curve", e.getMessage() );
            }
        }

    private void openWith( String name, boolean mayCreate, boolean mayReuse )
        {
        Assembler.general.openModel
            ( getRoot( name ), new Mode( mayCreate, mayReuse ) ).close();
        }

    private Resource getRoot( String name )
        {
        return resourceInModel( getDescription( name ) );
        }

    private String getDescription( String modelName )
        {
        return ("x rdf:type ja:RDBModel; x ja:modelName 'spoo'; x ja:connection C"
                + "; C ja:dbURLProperty 'jena.db.url'"
                + "; C ja:dbUserProperty 'jena.db.user'"
                + "; C ja:dbPasswordProperty 'jena.db.password'"
                + "; C ja:dbTypeProperty 'jena.db.type'"
                + "; C ja:dbClassProperty 'jena.db.driver'").replaceAll(
                "spoo", modelName );
        }
    }

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */