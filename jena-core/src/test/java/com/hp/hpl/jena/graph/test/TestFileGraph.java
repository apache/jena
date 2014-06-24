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
import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.impl.FileGraph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.util.FileUtils ;

/**
    Test FileGraph by seeing if we can make some file graphs and then read
    them back.
*/

public class TestFileGraph extends GraphTestBase
    {
    public TestFileGraph( String name )
        { super( name ); }

    // TODO want a wider variety of cases, now we've discovered how to abstract.
    public static TestSuite suite()
        {
        TestSuite result = new TestSuite( TestFileGraph.class );
        result.addTest( new Case( "x /R y", "xxxA", ".rdf" ) );
        result.addTest( new Case( "x /R y", "xxxB", ".n3" ) );
        result.addTest( new Case( "x /R y", "xxxC", ".nt" ) );
        result.addTest( new Case( "x /R y; p /R q", "xxxD", ".rdf" ) );
        result.addTest( new Case( "x /R y; p /R q", "xxxE", ".n3" ) );
        result.addTest( new Case( "x /R y; p /R q", "xxxF", ".nt" ) );
        result.addTest( new Case( "http://domain/S ftp:ftp/P O", "xxxG", ".rdf" ) );
        result.addTest( new Case( "http://domain/S ftp:ftp/P O", "xxxH", ".nt" ) );
        result.addTest( new Case( "http://domain/S ftp:ftp/P O", "xxxI", ".n3" ) );
        return result;
        }
        
    public void testPlausibleGraphname()
        {
        assertTrue( FileGraph.isPlausibleGraphName( "agnessi.rdf" ) ); 
        assertTrue( FileGraph.isPlausibleGraphName( "parabola.nt" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "hyperbola.n3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "chris.dollin.n3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "hedgehog.spine.end.rdf" ) );    
        }
        
    public void testisPlausibleUppercaseGraphname()
        {
        assertTrue( FileGraph.isPlausibleGraphName( "LOUDER.RDF" ) ); 
        assertTrue( FileGraph.isPlausibleGraphName( "BRIDGE.NT" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "NOTN2.N3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "chris.dollin.N3" ) );    
        assertTrue( FileGraph.isPlausibleGraphName( "hedgehog.spine.end.RDF" ) );        
        }
        
    public void testImPlausibleGraphName()
        {
        assertFalse( FileGraph.isPlausibleGraphName( "undecorated" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "danger.exe" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "pretty.jpg" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "FileGraph.java" ) );    
        assertFalse( FileGraph.isPlausibleGraphName( "infix.rdf.c" ) );                
        }
    
    public void testTransactionCommit()
        {
        Graph initial = graphWith( "initial hasValue 42; also hasURI hello" );
        Graph extra = graphWith( "extra hasValue 17; also hasURI world" );
        File foo = FileUtils.tempFileName( "fileGraph", ".nt" );
        
        Graph g = new FileGraph( foo, true, true );
        GraphUtil.addInto( g, initial );
        g.getTransactionHandler().begin();
        GraphUtil.addInto( g, extra );
        g.getTransactionHandler().commit();
        Graph union = graphWith( "" );
        GraphUtil.addInto(union, initial );
        GraphUtil.addInto(union, extra );
        assertIsomorphic( union, g );
        Model inFile = ModelFactory.createDefaultModel();
        inFile.read( "file:///" + foo, "N-TRIPLES" );
        assertIsomorphic( union, inFile.getGraph() );
        }    
    
    public void testTransactionAbort()
        {
        Graph initial = graphWith( "initial hasValue 42; also hasURI hello" );
        Graph extra = graphWith( "extra hasValue 17; also hasURI world" );
        File foo = FileUtils.tempFileName( "fileGraph", ".n3" );
        Graph g = new FileGraph( foo, true, true );
        GraphUtil.addInto( g, initial );
        g.getTransactionHandler().begin();
        GraphUtil.addInto( g, extra );
        g.getTransactionHandler().abort();
        assertIsomorphic( initial, g );
        }
    
    public void testTransactionCommitThenAbort()
        {
        Graph initial = graphWith( "A pings B; B pings C" );
        Graph extra = graphWith( "C pingedBy B; fileGraph rdf:type Graph" );
        File foo = FileUtils.tempFileName( "fileGraph", ".nt" );
        Graph g = new FileGraph( foo, true, true );
        g.getTransactionHandler().begin();
        GraphUtil.addInto( g, initial );
        g.getTransactionHandler().commit();
        g.getTransactionHandler().begin();
        GraphUtil.addInto( g, extra );
        g.getTransactionHandler().abort();
        assertIsomorphic( initial, g );
        Model inFile = ModelFactory.createDefaultModel();
        inFile.read( "file:///" + foo, "N-TRIPLES" );
        assertIsomorphic( initial, inFile.getGraph() );
        }

    public void testClosingNotifys()
        {
        final List<File> history = new ArrayList<>();
        FileGraph.NotifyOnClose n = new FileGraph.NotifyOnClose() 
            {
            @Override
            public void notifyClosed( File f )
                { history.add( f ); }
            };
        File file = FileUtils.tempFileName( "fileGraph", ".nt" );
        Graph g = new FileGraph( n, file, true, true );
        assertEquals( new ArrayList<File>(), history );
        g.close();
        assertEquals( oneElementList( file ), history );
        }
    
    protected List<Object> oneElementList( Object x )
        {
        List<Object> result = new ArrayList<>();
        result.add( x );
        return result;
        }
    
    /**
        Test that the graph encoded as the test-string content can be
        written out to a temporary file generated from the prefix and suffix,
        and then read back correctly. The temporary files are marked as
        delete-on-exit to try and avoid cluttering the user's filespace ...
    */
    private static class Case extends TestFileGraph
        {
        String content;
        String prefix;
        String suffix;

        Case( String content, String prefix, String suffix )
            {
            super( "Case: " + content + " in " + prefix + "*" + suffix );
            this.content = content;
            this.prefix = prefix;
            this.suffix = suffix;
            }
            
        @Override
        public void runTest()
            {
            File foo = FileUtils.tempFileName( prefix, suffix );
            Graph original = graphWith( content );
            Graph g = new FileGraph( foo, true, true );
            GraphUtil.addInto( g, original );
            g.close();
            Graph g2 = new FileGraph( foo, false, true );
            assertIsomorphic( original, g2 );
            g2.close();
            }
        }
        
    }
