/*
  (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestFileGraphMaker.java,v 1.15 2006-03-22 13:52:22 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.io.File;
import java.util.HashSet;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileUtils;

import junit.framework.*;

/**
    Test a FileGraphMaker; use the abstract tests, plus specialised ones for the name
    conversion routines.

 	@author hedgehog
*/
public class TestFileGraphMaker extends AbstractTestGraphMaker
    {
    public TestFileGraphMaker( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestFileGraphMaker.class ); }

    public GraphMaker getGraphMaker()
        { String scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker" ).getPath();
        return new FileGraphMaker( scratch, ReificationStyle.Minimal, true ); }

    public void testToFilename()
        { assertEquals( "plain", FileGraphMaker.toFilename( "plain" ) );
        assertEquals( "with_Sslash", FileGraphMaker.toFilename( "with/slash" ) );
        assertEquals( "with_Ccolon", FileGraphMaker.toFilename( "with:colon" ) );
        assertEquals( "with_Uunderbar", FileGraphMaker.toFilename( "with_underbar" ) );
        assertEquals( "with_Stwo_Sslashes", FileGraphMaker.toFilename( "with/two/slashes" ) );
        assertEquals( "with_Sa_Cmixture_U...", FileGraphMaker.toFilename( "with/a:mixture_..." ) ); }

    public void testToGraphname()
        { assertEquals( "plain", FileGraphMaker.toGraphname( "plain" ) );
        assertEquals( "with/slash", FileGraphMaker.toGraphname( "with_Sslash" ) );
        assertEquals( "with:colon", FileGraphMaker.toGraphname( "with_Ccolon" ) );
        assertEquals( "with_underbar", FileGraphMaker.toGraphname( "with_Uunderbar" ) );
        assertEquals( "a/mixture_of:things", FileGraphMaker.toGraphname( "a_Smixture_Uof_Cthings" ) );
        assertEquals( "with/two/slashes", FileGraphMaker.toGraphname( "with_Stwo_Sslashes" ) ); }

    public void testDetectsExistingFiles()
        {
        File scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker-already" );
        Graph content = graphWith( "something hasProperty someValue" );
        FileGraphMaker A = new FileGraphMaker( scratch.getPath(), ReificationStyle.Minimal, true );
        FileGraphMaker B = new FileGraphMaker( scratch.getPath(), ReificationStyle.Minimal, true );
        FileGraph gA = (FileGraph) A.createGraph( "already", true );
        gA.getBulkUpdateHandler().add( content );
        gA.close();
        FileGraph gB = (FileGraph) B.openGraph( "already", false );
        assertIsomorphic( content, gB );
        gB.close();
        gB.delete();
        gA.delete();
        }
    
    public void testDeletesFilesOfClosedMaker()
        {
        File scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker-forgets" );
        FileGraphMaker A = new FileGraphMaker( scratch.getPath(), ReificationStyle.Minimal, true );
        A.createGraph( "empty" ).close();
        assertTrue( "file 'empty' should exist in '" + scratch + "'", new File( scratch, "empty" ) .exists() );
        A.close();
        assertFalse( "file 'empty' should no longer exist in '" + scratch + "'", new File( scratch, "empty" ) .exists() );
        }
    
    public void testForgetsClosedGraphs()
        {
        File scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker-forgets" );
        FileGraphMaker m = new FileGraphMaker( scratch.getPath(), ReificationStyle.Minimal, true );
        m.createGraph( "example" ).close();
        assertEquals( new HashSet(), iteratorToSet( m.listGraphs() ) );
        m.close();
        }
    
    public void testDoesntReusedClosedGraphs()
        {
        File scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker-noReuse" );
        FileGraphMaker m = new FileGraphMaker( scratch.getPath(), ReificationStyle.Minimal, true );
        Graph m1 = m.createGraph( "hello" );
        m1.close();
        Graph m2 = m.createGraph( "hello" );
        assertNotSame( m1, m2 );
        m2.add( triple( "this graph isOpen" ) );
        m.close();
        }
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
