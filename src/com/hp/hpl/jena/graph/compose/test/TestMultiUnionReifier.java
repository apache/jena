/*
 	(c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestMultiUnionReifier.java,v 1.4 2009-06-16 10:50:16 castagna Exp $
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
        for (ExtendedIterator<Triple> it = GraphUtil.findAll( mu ); it.hasNext();)
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
                Triple t = NodeCreateUtils.createTriple( fact.substring( 1 ) );
                result.add( t );
                result.getReifier().reifyAs( NodeCreateUtils.create( "_r" + ++count ), t );
                }
            else if (fact.charAt( 0 ) == '~')
                {
                Triple t = NodeCreateUtils.createTriple( fact.substring( 1 ) );
                result.getReifier().reifyAs( NodeCreateUtils.create( "_r" + ++count ), t );
                }
            else
                result.add( NodeCreateUtils.createTriple( fact ) );
            }
        return result;
        }
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
