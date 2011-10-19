/*
    (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: SetBunch.java,v 1.1 2009-06-29 08:55:55 castagna Exp $
*/
package com.hp.hpl.jena.mem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class SetBunch implements TripleBunch
    {
    protected Set<Triple> elements = new HashSet<Triple>(20);
    
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
        Iterator<Triple> it = elements.iterator();
        while (it.hasNext())
            if (t.matches( it.next() )) return true;
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
    
    @Override
    public void app( Domain d, StageElement next, MatchOrBind s )
        {
        Iterator<Triple> it = iterator();
        while (it.hasNext())
            if (s.matches( it.next() )) next.run( d );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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