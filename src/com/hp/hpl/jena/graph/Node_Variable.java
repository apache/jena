/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Node_Variable.java,v 1.10 2004-08-06 13:17:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

/**
    "variable" nodes; these are outside the RDF2003 specification, but are
    used internally for "placeholder" nodes where blank nodes would be
    wrong, most specifically in Query.
    @author kers
*/

public class Node_Variable extends Node_Fluid
    {
    protected Node_Variable( Object name )
        { super( name ); }

    public String getName()
        { return ((VariableName) label).name; }
    
    public Object visitWith( NodeVisitor v )
        { return v.visitVariable( this, getName() ); }
        
    public boolean isVariable()
        { return true; }
        
    public String toString()
        { return "?" + ((VariableName) label).name; }
    
    public boolean equals( Object other )
        { return other instanceof Node_Variable && label.equals( ((Node_Variable) other).label ); }
    
    public static Object variable( String name )
        { return new VariableName( name ); }
    
    public static class VariableName
        {
        String name;
        
        VariableName( String name ) 
            { this.name = name; }
        
        public int hashCode()
            { return name.hashCode(); }
        
        public boolean equals( Object other )
            { return other instanceof VariableName && name.equals( ((VariableName) other).name );  }
        
        public String toString()
            { return "?" + name; }
        }
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
