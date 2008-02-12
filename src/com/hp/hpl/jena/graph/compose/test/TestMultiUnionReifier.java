/*
 	(c) Copyright 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestMultiUnionReifier.java,v 1.1 2008-02-12 09:32:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    Test the reifier for multi-unions.
    
 	@author kers
*/
public class TestMultiUnionReifier extends ModelTestBase
    {
    public TestMultiUnionReifier( String name )
        { super( name ); }

    public void testX()
        {
        MultiUnion mu = multi( "a P b; !b Q c; ~c R d", "" );
        for (ExtendedIterator it = GraphUtil.findAll( mu ); it.hasNext();)
            {
            System.err.println( "]]  " + it.next() );
            }
        }

    private MultiUnion multi( String a, String b )
        {
        Graph A = graph( a ), B = graph( b );
        return new MultiUnion( new Graph[] {A, B} );
        }

    static int count = 0;
    
    private Graph graph( String facts )
        {
        Graph result = Factory.createDefaultGraph( ReificationStyle.Standard );
        String [] factArray = facts.split( ";" );
        for (int i = 0; i < factArray.length; i += 1)
            {
            String fact = factArray[i].trim();
            if (fact.equals(  ""  ))
                {}
            else if (fact.charAt( 0 ) == '!')
                {
                Triple t = Triple.create( fact.substring( 1 ) );
                result.add( t );
                result.getReifier().reifyAs( NodeCreateUtils.create( "_r" + ++count ), t );
                }
            else if (fact.charAt( 0 ) == '~')
                {
                Triple t = Triple.create( fact.substring( 1 ) );
                result.getReifier().reifyAs( NodeCreateUtils.create( "_r" + ++count ), t );
                }
            else
                result.add( Triple.create( fact ) );
            }
        return result;
        }
    }

