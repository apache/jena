/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.ReificationStyle;

public class ReifierTDB implements Reifier
{

    @Override
    public ExtendedIterator allNodes()
    {
        // SELECT ?n { ?n rdf:type rdf:Statement }
        return null ;
    }

    @Override
    public ExtendedIterator allNodes(Triple t)
    {
        // SELECT ?n { ?n rdf:type rdf:Statement ; rdf:subject S ; rdf:predicate P ; rdf:object O } 
        return null ;
    }

    @Override
    public void close()
    {}

    @Override
    public ExtendedIterator find(TripleMatch m)
    {
        return null ;
    }

    @Override
    public ExtendedIterator findEither(TripleMatch m, boolean showHidden)
    {
        return null ;
    }

    @Override
    public ExtendedIterator findExposed(TripleMatch m)
    {
        return null ;
    }

    private ExtendedIterator find()
    {
        return null ;
    }
    
    @Override
    public Graph getParentGraph()
    {
        return null ;
    }

    @Override
    public ReificationStyle getStyle()
    {
        return null ;
    }

    @Override
    public boolean handledAdd(Triple t)
    {
        return false ;
    }

    @Override
    public boolean handledRemove(Triple t)
    {
        return false ;
    }

    @Override
    public boolean hasTriple(Node n)
    {
        return false ;
    }

    @Override
    public boolean hasTriple(Triple t)
    {
        return false ;
    }

    @Override
    public Node reifyAs(Node n, Triple t)
    {
        return null ;
    }

    @Override
    public void remove(Triple t)
    {}

    @Override
    public void remove(Node n, Triple t)
    {}

    @Override
    public int size()
    {
        return 0 ;
    }

    @Override
    public Triple getTriple(Node n)
    {
        return null ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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