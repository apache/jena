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

package org.apache.jena.sparql.util;

import java.util.Objects;

/** A way to write constants */

public class Symbol {
    protected final static String nilSymbolName = "nil";
    
    protected String symbol;

    static public Symbol create(String symbolStr) {
        return new Symbol(symbolStr);
    }

    static public Symbol create(Symbol other) {
        return new Symbol(other);
    }

    protected Symbol(String symbol) {
        if ( symbol == null )
            symbol = nilSymbolName;
        this.symbol = symbol;
    }

    protected Symbol(Symbol other) {
        this.symbol = other.symbol;
    }


    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "symbol:" + symbol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( !(obj instanceof Symbol) )
            return false;
        Symbol other = (Symbol)obj;
        return Objects.equals(symbol, other.symbol);
    }
}
