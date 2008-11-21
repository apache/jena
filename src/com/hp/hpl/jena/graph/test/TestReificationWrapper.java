/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestReificationWrapper.java,v 1.1 2008-11-21 15:27:25 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.shared.*;

/**
    Tests for ReificationWrapper and hence ReificationWrapperGraph.
    
 	@author kers
*/
public class TestReificationWrapper extends AbstractTestReifier
    {
    protected final Class graphClass;
    protected final ReificationStyle style;
    
    public TestReificationWrapper( Class graphClass, String name, ReificationStyle style ) 
        {
        super( name );
        this.graphClass = graphClass;
        this.style = style;
        }
        
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTest( MetaTestGraph.suite( TestReificationWrapper.class, ReificationWrapperGraph.class, ReificationStyle.Standard ) );
        result.addTestSuite( TestReificationWrapperGraph.class );
        return result; 
        }       
    
    public static class TestReificationWrapperGraph extends AbstractTestGraph
        {
        public TestReificationWrapperGraph( String name )
            { super( name ); }
    
        public Graph getGraph()
            {
            Graph base = Factory.createDefaultGraph();            
            return new ReificationWrapperGraph( base, ReificationStyle.Standard ); 
            }
        }

    public Graph getGraph()
        { return getGraph( style );  }

    public Graph getGraph( ReificationStyle style )
        { return new ReificationWrapperGraph( new GraphMem( Standard ), style );  }
    }

