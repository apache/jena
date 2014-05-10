/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.util;

/** A way to write constants */

public class Symbol
{
    protected final static String nilSymbolName = "nil" ;
    protected String symbol ;
    
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
