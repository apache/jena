/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: GraphMemBase.java,v 1.9 2006-01-25 14:00:57 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.ReificationStyle;

/**
     GraphMemBase - a common base class for GraphMem and SmallGraphMem.
     Any GraphMemBase maintains a reference count, set to one when it is created,
     and incremented by the method <code>openAgain()</code>. When the graph 
     is closed, the count is decrememented, and when it reaches 0, the tables are 
     trashed and GraphBase.close() called. Thus in normal use one close is enough,
     but GraphMakers using GraphMems can arrange to re-use the same named 
     graph.
    
     @author kers
*/
public abstract class GraphMemBase extends GraphBase
    {
    /**
         The number-of-times-opened count.
    */
    protected int count;
    
    /**
        This Graph's TripleStore. Visible for <i>read-only</i> purposes only.
    */
    public final TripleStore store;
    
    /**
         initialise a GraphMemBase withn its count set to 1.
    */
    public GraphMemBase( ReificationStyle style )
        { 
        super( style );
        store = createTripleStore();
        count = 1; 
        }
    
    protected abstract TripleStore createTripleStore();
    
    /**
         Note a re-opening of this graph by incrementing the count. Answer
         this Graph.
    */
    public GraphMemBase openAgain()
        { 
        count += 1; 
        return this;
        }

    /**
         Sub-classes over-ride this method to release any resources they no
         longer need once fully closed.
    */
    protected abstract void destroy();

    /**
         Close this graph; if it is now fully closed, destroy its resources and run
         the GraphBase close.
    */
    public void close()
        {
        if (--count == 0)
            {
            destroy();
            super.close();
            }
        }
    
    /**
         Remove all triples from this graph; used to implement removeAll.
    */
    public abstract void clear();

    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemBulkUpdateHandler( this );
        return bulkHandler;
        }

    /**
        Answer true iff this triple can be compared for sameValueAs by .equals(),
        ie, it is a concrete triple with a non-literal object.
    */
    protected final boolean isSafeForEquality( Triple t )
        { return t.isConcrete() && !t.getObject().isLiteral(); }
    }


/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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