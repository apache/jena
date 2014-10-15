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

package com.hp.hpl.jena.tdb.sys;

import java.util.Properties ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

public class EnvTDB
{
    public static void processGlobalSystemProperties()
    {
        Context context = processProperties(System.getProperties()) ;
        TDB.getContext().putAll(context) ;
    }
    
    static final String prefix = SystemTDB.tdbSymbolPrefix+":" ;
    public static Context processProperties(Properties properties)
    {
        Context context = new Context() ;
        Set<Object> keys = properties.keySet() ;
        for ( Object key : keys )
        {
            if ( key instanceof String )
            {
                String keyStr = (String)key ;
                if ( keyStr.startsWith(prefix) )
                    keyStr = SystemTDB.symbolNamespace+keyStr.substring(prefix.length()) ;
                
                
                if ( ! keyStr.startsWith(SystemTDB.symbolNamespace) )
                    continue ;
                
                Object value = properties.get(key) ;
                
                Symbol symbol = Symbol.create(keyStr) ;
                
                context.set(symbol, value) ;
            }
        }
        return context ;
    }
}
