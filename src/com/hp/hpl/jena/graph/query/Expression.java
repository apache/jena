/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Expression.java,v 1.12 2003-10-15 09:22:36 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import java.util.*;

/**
	Expression - the interface for expressions that is expected by Query for constraints.

	@author kers
*/
public interface Expression 
    { 
    public boolean evalBool( VariableValues vv );
    
    public Valuator prepare( VariableIndexes vi );
    
    public boolean isVariable();
    public String getName();
    public boolean isApply();
    public int argCount();
    public Expression getArg( int i );

    public static Expression TRUE = new BoolConstant( true );
    
    public static Expression FALSE = new BoolConstant( false );
    
    public static abstract class Base
        {        
        public boolean isVariable() { return false; }
        public boolean isApply() { return false; }
        public String getName() { return null; }
        public int argCount() { return 0; }
        public Expression getArg( int i ) { return null; }
        }
    
    public static class Util
        {
        public static Set variablesOf( Expression e )
            { 
            Set result = new HashSet();
            if (e.isVariable()) result.add( e.getName() );
            else if (e.isApply())
                for (int i = 0; i < e.argCount(); i += 1)
                    result.addAll( variablesOf( (Expression) e.getArg( i ) ) );
            return result;
            }           
        }
    
    public static class BoolConstant extends Base implements Expression, Valuator
        {
        private boolean value;
        public BoolConstant( boolean value ) { this.value = value; }
        public Valuator prepare( VariableIndexes vi ) { return this; }   
        public boolean evalBool( VariableValues vv ) { return value; }
        public boolean evalBool( IndexValues vv ) { return value; }
        }    
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
