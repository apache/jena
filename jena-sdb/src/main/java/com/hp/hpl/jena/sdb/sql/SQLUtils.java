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

package com.hp.hpl.jena.sdb.sql;

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL ;

public class SQLUtils
{
    static SQLUtilsStd op = new SQLUtilsStd() ;
    
    static public String sqlStr(String ... str)
    { return strjoinNL(str) ; }
    
    static public String escapeStr(String s)
    { return op.escapeStr(s) ; }
    
    static public String unescapeStr(String s)
    { return op.unescapeStr(s) ; }
    
    static public String quoteStr(String s)
    { return op.quoteStr(s) ; }

    static public String unquoteStr(String s)
    { return op.unquoteStr(s) ; }
    
    static public String quoteIdentifier(String name)
    { return op.quoteIdentifier(name) ; }

    public static String getSQLmark()
    { return op.getSQLmark() ; }
    
    public static String gen(String first, String last)
    { return op.gen(first, last) ; }
    
    public static String gen(String first)
    { return op.gen(first) ; }
}
