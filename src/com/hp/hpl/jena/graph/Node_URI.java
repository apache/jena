/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Node_URI.java,v 1.8 2004-04-23 14:32:07 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.*;

/**
    RDF nodes with a global identity given by a URI.
	@author kers
*/
public class Node_URI extends Node_Concrete
    {
    /* package */ Node_URI( Object uri )
        { super( uri ); }

    public String getURI()
        { return (String) label; }
        
    public Object visitWith( NodeVisitor v )
        { return v.visitURI( this, (String) label ); }
        
    public boolean isURI()
        { return true; }
        
    /**
        Answer a String representing the node, taking into account the PrefixMapping.
        The horrible test against null is a stopgap to avoid a circularity issue.
        TODO fix the circularity issue
    */
    public String toString( PrefixMapping pm, boolean quoting )
        { return pm == null ? (String) label : pm.shortForm( (String) label ); }
        
    public boolean equals( Object other )
        { return other instanceof Node_URI && label.equals( ((Node_URI) other).label ); }
    
    public String getNameSpace()
        { 
        String s = (String) label;
        return s.substring( 0, Util.splitNamespace( s ) );
        }
    
    public String getLocalName()
        {  
        String s = (String) label;
        return s.substring( Util.splitNamespace( s ) );
        }
    
    public boolean hasURI( String uri )
        { return label.equals( uri ); }
    }

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
