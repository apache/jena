/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Expression.java,v 1.7 2003-10-10 09:06:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

/**
	Expression - the interface for expressions that is expected by Query for constraints.

	@author kers
*/
public interface Expression 
    { 
    public boolean evalBool( VariableValues vv );
    
    public Expression prepare( VariableIndexes vi );
    
    public boolean evalBool( IndexValues iv );
    
    public static abstract class EE implements Expression
        {
        public Expression prepare( VariableIndexes vi ) { return this; }   
        public abstract boolean evalBool( VariableValues vv );
        public abstract boolean evalBool( IndexValues vv );
        public abstract Object eval( IndexValues vv );
        }
        
    public static Expression TRUE = new EE() 
        { 
        public boolean evalBool( VariableValues vv ) { return true; }
        public boolean evalBool( IndexValues vv ) { return true; }
        public Object eval( IndexValues vv ) { return Boolean.TRUE; }
        };
    
    public static Expression FALSE = new EE() 
        { 
        public boolean evalBool( VariableValues vv ) { return false; }
        public boolean evalBool( IndexValues vv ) { return false; }
        public Object eval( IndexValues vv ) { return Boolean.FALSE; }
        };
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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
