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

package com.hp.hpl.jena.sdb.util;

import java.util.List;
import java.util.Map;

import static org.apache.jena.atlas.lib.StrUtils.* ;


public class StrUtils
{
    public static String sqlList(List<String> args)
    { return strjoin(", ", args) ; }
    
    public static String sqlList(String[] args)
    { return strjoin(", ", args) ; }
    
    public static String substitute(String str, Map<String, String>subs)
    {
        for ( Map.Entry<String, String> e : subs.entrySet() )
        {
            String param = e.getKey() ;
            if ( str.contains(param) ) 
                str = str.replace(param, e.getValue()) ;
        }
        return str ;
    }
    
    // A common combination
    public static String strform(Map<String, String>subs, String... args)
    {
        return substitute(strjoinNL(args),subs) ;
    }
}
