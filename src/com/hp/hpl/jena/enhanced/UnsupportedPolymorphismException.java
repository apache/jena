/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: UnsupportedPolymorphismException.java,v 1.8 2007-01-02 11:53:27 andy_seaborne Exp $
*/

package com.hp.hpl.jena.enhanced;

import com.hp.hpl.jena.shared.JenaException;

/**
    Exception to throw if an enhanced graph does not support polymorphism
    to a specific class. The exception records the "bad" class and node for
    later reporting.
*/
public class UnsupportedPolymorphismException extends JenaException
    {
    private final Class type;
    private final EnhNode node;
        
    /**
        Initialise this exception with the node that couldn't be polymorphed and
        the class it couldn't be polymorphed to.
    */
    public UnsupportedPolymorphismException( EnhNode node, Class type )
        {
        super( constructMessage( node, type ) );
        this.node = node;
        this.type = type;
        }

    private static String constructMessage( EnhNode node, Class type )
        {
        String mainMessage = "cannot convert " + node + " to " + type;
        return node.getGraph() == null ? mainMessage : mainMessage + " -- it has no model";
        }

    /** 
        Answer the (enhanced) Graph of the node that couldn't be polymorphed;
        may be null if that node had no attached model.   
    */
    public EnhGraph getBadGraph() 
        { return node.getGraph(); }
    
    /** 
        Answer the class that the node couldn't be polymorphed to
    */
    public Class getBadClass() 
        { return type; }

    /**
        Answer the node that couldn't be polymorphed.
    */
    public Object getBadNode()
        { return node; }
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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