/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: MapMany.java,v 1.1.1.1 2002-12-19 19:21:18 bwm Exp $
*/

package com.hp.hpl.jena.util.iterator;

/**
	@author kers
<br>
    MapMany(it, filler) is a new iterator based on _it_ which maps the elements
    of _it_ using _filler_. It is a generalisation of a Map1Iterator when the
    number of outputs from the map is not restricted to be 1.
*/

import java.util.*;

public class MapMany extends NiceIterator implements ClosableIterator
    {
    ClosableIterator base;
    MapFiller sm;
    ArrayList pending;
    
    public MapMany( ClosableIterator base, MapFiller sm )
        {
        this.sm = sm;
        this.base = base;
        this.pending = new ArrayList();
        }
        
    public void close()
        { base.close(); }
        
    public boolean hasNext()
        { return pending.size() > 0 || refill(); }
        
    public Object next()
        {
        hasNext();
        return pending.remove(pending.size()-1);
        }
        
    private boolean refill() 
        {
        return base.hasNext() && sm.refill( base.next(), pending ) && hasNext();
        }
    }
        
/*
    (c) Copyright Hewlett-Packard Company 2002
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
