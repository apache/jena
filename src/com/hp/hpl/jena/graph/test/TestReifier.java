/*
  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestReifier.java,v 1.21 2004-11-01 16:38:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.lang.reflect.Constructor;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.impl.ReifierFragmentsMap;
import com.hp.hpl.jena.graph.impl.ReifierTripleMap;
import com.hp.hpl.jena.graph.impl.SimpleReifier;
import com.hp.hpl.jena.graph.impl.SimpleReifierFragmentsMap;
import com.hp.hpl.jena.graph.impl.SimpleReifierTripleMap;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import junit.framework.*;

/**
    This class tests the reifiers of ordinary GraphMem graphs.
	@author kers
*/

public class TestReifier extends AbstractTestReifier
    {
    public TestReifier( String name )
        { super( name ); graphClass = null; style = null; }
        
    protected final Class graphClass;
    protected final ReificationStyle style;
    
    public TestReifier( Class graphClass, String name, ReificationStyle style ) 
        {
        super( name );
        this.graphClass = graphClass;
        this.style = style;
        }
        
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTest( MetaTestGraph.suite( TestReifier.class, GraphMem.class ) );
        return result; 
        }   
        
    public Graph getGraph( ReificationStyle style ) 
        {
        try
            {
            Constructor cons = getConstructor( graphClass, new Class[] {ReificationStyle.class} );
            if (cons != null) return (Graph) cons.newInstance( new Object[] { style } );
            Constructor cons2 = getConstructor( graphClass, new Class [] {this.getClass(), ReificationStyle.class} );
            if (cons2 != null) return (Graph) cons2.newInstance( new Object[] { this, style } );
            throw new JenaException( "no suitable graph constructor found for " + graphClass );
            }
        catch (RuntimeException e)
            { throw e; }
        catch (Exception e)
            { throw new JenaException( e ); }
        }        
    
    public void testExtendedConstructorExists()
        {
        GraphBase parent = new GraphBase() {

            public ExtendedIterator graphBaseFind( TripleMatch m )
                {
                // TODO Auto-generated method stub
                return null;
                }};
        ReifierTripleMap tm = new SimpleReifierTripleMap();
        ReifierFragmentsMap fm = new SimpleReifierFragmentsMap();
        SimpleReifier sr = new SimpleReifier( parent, tm, fm, ReificationStyle.Minimal );
        }
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
