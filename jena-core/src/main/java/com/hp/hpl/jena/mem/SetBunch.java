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

package com.hp.hpl.jena.mem;

import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

public class SetBunch implements TripleBunch
    {
    protected Set<Triple> elements = new HashSet<>(20);
    
    public SetBunch( TripleBunch b )
        { 
        for (Iterator<Triple> it = b.iterator(); it.hasNext();) 
            elements.add( it.next() );
        }

    protected static boolean equalsObjectOK( Triple t )
        { 
        Node o = t.getObject();
        return o.isLiteral() ? o.getLiteralDatatype() == null : true;
        }

    @Override
    public boolean contains( Triple t )
        { return elements.contains( t ); }
    
    @Override
    public boolean containsBySameValueAs( Triple t )
        { return equalsObjectOK( t ) ? elements.contains( t ) : slowContains( t ); }
    
    protected boolean slowContains( Triple t )
        {
            for ( Triple element : elements )
            {
                if ( t.matches( element ) )
                {
                    return true;
                }
            }
        return false;
        }

    @Override
    public int size()
        { return elements.size(); }
    
    @Override
    public void add( Triple t )
        { elements.add( t ); }
    
    @Override
    public void remove( Triple t )
        { elements.remove( t ); }
    
    @Override
    public ExtendedIterator<Triple> iterator( HashCommon.NotifyEmpty container )
        {
        return iterator();
        }
    
    @Override
    public ExtendedIterator<Triple> iterator()
        { return WrappedIterator.create( elements.iterator() ); }        
    
    }
