/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

/** A way to write constants */

public class Symbol
{
    // Need a intern table for symbols??
    private final static String nilSymbolName = "nil" ;
    String symbol ;
    
    static public Symbol create(String symbolStr) { return new Symbol(symbolStr) ; }
    static public Symbol create(Symbol other) { return new Symbol(other) ; }
    
    protected Symbol(String symbol)
    { 
        if ( symbol == null )
            symbol = nilSymbolName ;
        else
            symbol = symbol.intern();
        this.symbol = symbol ;
    }

    protected Symbol(Symbol other)  { this.symbol = other.symbol ; }
    
    @Override
    public int hashCode() { return symbol.hashCode() ; } 
    
    //public boolean isCompatibleWith(Symbol other) { return equals(other) ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof Symbol ) )
            return false ;

        Symbol otherSymbol = (Symbol)other ;
        return this.symbol == otherSymbol.symbol ; // String interning.
        //return this.symbol.equals(otherSymbol.symbol) ;
    }

    public String getSymbol() { return symbol ; }
    @Override
    public String toString()  { return "symbol:"+symbol ; }
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