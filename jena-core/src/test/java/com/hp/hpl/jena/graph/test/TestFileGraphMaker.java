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

package com.hp.hpl.jena.graph.test;

import java.io.File ;
import java.util.HashSet ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphMaker ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.impl.FileGraph ;
import com.hp.hpl.jena.graph.impl.FileGraphMaker ;
import com.hp.hpl.jena.util.FileUtils ;

/**
    Test a FileGraphMaker; use the abstract tests, plus specialised ones for the name
    conversion routines.
*/
public class TestFileGraphMaker extends AbstractTestGraphMaker
    {
    public TestFileGraphMaker( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestFileGraphMaker.class ); }

    @Override
    public GraphMaker getGraphMaker()
        { String scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker" ).getPath();
        return new FileGraphMaker( scratch, true ); }

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
        FileGraphMaker A = new FileGraphMaker( scratch.getPath(), true );
        FileGraphMaker B = new FileGraphMaker( scratch.getPath(), true );
        FileGraph gA = (FileGraph) A.createGraph( "already", true );
        GraphUtil.addInto( gA, content );
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
        FileGraphMaker A = new FileGraphMaker( scratch.getPath(), true );
        A.createGraph( "empty" ).close();
        assertTrue( "file 'empty' should exist in '" + scratch + "'", new File( scratch, "empty" ) .exists() );
        A.close();
        assertFalse( "file 'empty' should no longer exist in '" + scratch + "'", new File( scratch, "empty" ) .exists() );
        }
    
    public void testForgetsClosedGraphs()
        {
        File scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker-forgets" );
        FileGraphMaker m = new FileGraphMaker( scratch.getPath(), true );
        m.createGraph( "example" ).close();
        assertEquals( new HashSet<String>(), m.listGraphs().toSet() );
        m.close();
        }
    
    public void testDoesntReusedClosedGraphs()
        {
        File scratch = FileUtils.getScratchDirectory( "jena-test-FileGraphMaker-noReuse" );
        FileGraphMaker m = new FileGraphMaker( scratch.getPath(), true );
        Graph m1 = m.createGraph( "hello" );
        m1.close();
        Graph m2 = m.createGraph( "hello" );
        assertNotSame( m1, m2 );
        m2.add( triple( "this graph isOpen" ) );
        m.close();
        }
    }
