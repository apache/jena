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


import java.util.List;
import java.util.regex.Pattern;

import org.apache.jena.atlas.iterator.Iter ;


public class Lib2
{
    private static Pattern p = Pattern.compile("http:[^ \n]*[#/]([^/ \n]*)") ;
    /** Abbreviate, crudely, URI in strings, leaving only their last component. */ 
    public static String printAbbrev(Object obj)
    {
        if ( obj==null )
            return "<null>" ;
        String x = obj.toString() ;
        return p.matcher(x).replaceAll("::$1") ;
    }
    
    /** Abbreviate, crudely, URI in strings, leaving only their last component. */ 
    public static <T> String printAbbrevList(List<T> objs)
    {
        String x = Iter.asString(objs, "\n") ;
        return printAbbrev(x) ;
    }
}
