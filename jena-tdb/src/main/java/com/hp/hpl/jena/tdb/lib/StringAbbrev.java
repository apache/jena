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

package com.hp.hpl.jena.tdb.lib;

import java.util.HashMap;
import java.util.Map;

public class StringAbbrev
{
    Map<String, String> prefix2string = new HashMap<>() ;

    public StringAbbrev()
    { 
        prefix2string.put("_",":") ;
    }
    
    public void add(String prefix, String string)
    {
        if ( prefix.contains(":") )
            throw new IllegalArgumentException("Prefix contains ':' -- "+prefix) ;
        if ( prefix.equals("") )
            throw new IllegalArgumentException("Prefix is the empty string") ;
        prefix2string.put(prefix, string) ;
    }

    public String abbreviate(String s)
    {
        for (Map.Entry<String, String> e : prefix2string.entrySet() )
        {
            String prefix = e.getKey() ;
            String string = e.getValue() ;
            if ( s.startsWith(string) )
            {
                String s2 = ":"+prefix+":"+s.substring(string.length()) ;
                return s2 ; 
            }
        }
        
        // Should have been caught by the "_" rule.
        if ( s.startsWith(":") ) 
            s = ":"+s ;
        return s ;
    }
    
    public String expand(String s)
    {
        if ( ! s.startsWith(":") )
            return s ;
        int i = s.indexOf(":", 1) ;
        if ( i < 0 )
            return s ;
        // It's "::" which is for strings starting :  
        if ( i == 1 )
            return s.substring(1) ;
        
        String prefix = s.substring(1, i) ;
        if ( prefix == null )
        {
            return s.substring(i+1) ;
        }
        
        String start = prefix2string.get(prefix) ;
        return start+s.substring(i+1) ;
    }
}
