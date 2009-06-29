/*
 	(c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestReificationWrapper.java,v 1.1 2009-06-29 08:55:40 castagna Exp $
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
    protected final Class<? extends Graph> graphClass;
    protected final ReificationStyle style;
    
    public TestReificationWrapper( Class<? extends Graph> graphClass, String name, ReificationStyle style ) 
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
    
        @Override
        public Graph getGraph()
            {
            Graph base = Factory.createDefaultGraph();            
            return new ReificationWrapperGraph( base, ReificationStyle.Standard ); 
            }
        }

    @Override
    public Graph getGraph()
        { return getGraph( style );  }

    @Override
    public Graph getGraph( ReificationStyle style )
        { return new ReificationWrapperGraph( new GraphMem( Standard ), style );  }
    }

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
