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

package org.apache.jena.graph.compose.test;

import org.apache.jena.graph.GraphMemFactory ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.compose.MultiUnion ;
import org.apache.jena.graph.test.NodeCreateUtils ;
import org.apache.jena.rdf.model.impl.ReifierStd ;
import org.apache.jena.rdf.model.test.ModelTestBase ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
    Test the reifier for multi-unions.
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
        Graph result = GraphMemFactory.createDefaultGraph( );
        String [] factArray = facts.split( ";" );
            for ( String aFactArray : factArray )
            {
                String fact = aFactArray.trim();
                if ( fact.equals( "" ) )
                {
                }
                else if ( fact.charAt( 0 ) == '!' )
                {
                    Triple t = NodeCreateUtils.createTriple( fact.substring( 1 ) );
                    result.add( t );
                    ReifierStd.reifyAs( result, NodeCreateUtils.create( "_r" + ++count ), t );
                }
                else if ( fact.charAt( 0 ) == '~' )
                {
                    Triple t = NodeCreateUtils.createTriple( fact.substring( 1 ) );
                    ReifierStd.reifyAs( result, NodeCreateUtils.create( "_r" + ++count ), t );
                }
                else
                {
                    result.add( NodeCreateUtils.createTriple( fact ) );
                }
            }
        return result;
        }
    }
