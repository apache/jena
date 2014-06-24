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

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

/** Maps string to string for use with convenience names.
 */

public class TranslationTable<X>
{
    Map<String, X> map = new HashMap<>() ;
    boolean ignoreCase = false ;
    
    /** Create a translation table which respects case */
    
    public TranslationTable() { this(false) ; }
    
    /** Create a translation table - say whether to ignore case or not */ 
    public TranslationTable(boolean ignoreCase) { this.ignoreCase = ignoreCase ; } 
    
    public X lookup(String name)
    {
        if ( name == null )
            return null ;

        for ( Map.Entry<String, X> entry : map.entrySet() )
        {
            String k = entry.getKey();
            if ( ignoreCase )
            {
                if ( k.equalsIgnoreCase( name ) )
                {
                    return entry.getValue();
                }
            }
            else
            {
                if ( k.equals( name ) )
                {
                    return entry.getValue();
                }
            }
        }
        return null ;
    }
    
    public void put(String k, X v)
    {
        map.put(k, v) ;
    }
    
    public Iterator<String> keys() { return map.keySet().iterator() ; }
    public Iterator<X> values() { return map.values().iterator() ; }
}
