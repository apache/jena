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

package com.hp.hpl.jena.sdb.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.sparql.util.Symbol;

public class SymbolRegistry<T extends Symbol>
{
    protected Set<T> registeredSymbols = new HashSet<T>() ;
    protected Map<String, T> registeredNames = new HashMap<String, T>() ;
    
    public void register(T symbol)
    {
        register(null, symbol) ;
    }
    
    public void register(String name, T symbol)
    {
        if ( name == null )
            name = symbol.getSymbol() ;
        registeredSymbols.add(symbol) ;
        registeredNames.put(name, symbol) ;
    }
    
    public T lookup(String symName)
    {
        for ( String name: registeredNames.keySet() )
        {
            if ( symName.equalsIgnoreCase(name) )
                return registeredNames.get(name) ;
        }
        return null ;
    }
    
    public List<String> allNames()
    { return new ArrayList<String>(registeredNames.keySet()) ; }

    public List<T> allSymbols() { return new ArrayList<T>(registeredSymbols) ; }
}
