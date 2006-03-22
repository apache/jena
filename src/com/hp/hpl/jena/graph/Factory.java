/*
  (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Factory.java,v 1.23 2006-03-22 13:52:54 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.shared.*;

/**
    A factory class for creating Graphs.
    
    @author kers
*/

public class Factory
    {

    private Factory()
        { super(); }

    public static final boolean faster =
        System.getProperty( "jena.faster", "yes" ).equals( "yes" );

    public static final boolean newHashing = 
        System.getProperty( "jena.hashing", "yes" ).equals( "yes" );
    /**
        Answer a memory-based Graph with the Standard reification style.
    */
    public static Graph createDefaultGraph()
        { return createDefaultGraph( ReificationStyle.Standard ); }
        
    /**
        Answer a memory-based Graph with the given reification style.
    */
    public static Graph createDefaultGraph( ReificationStyle style )
        { return Factory.createGraphMem( style ); }
              
    public static Graph createGraphMem()
        { return faster ? (Graph) new GraphMemFaster() : new GraphMem(); }

    public static Graph createGraphMem( ReificationStyle style )
        { return faster ? (Graph) new GraphMemFaster( style ) : new GraphMem( style ); }
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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