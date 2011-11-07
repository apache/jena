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

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.sdb.sql.SQLUtils;

public class Gensym implements Generator
{

    private String base ;
    private int count = 1 ;
    private String lastAlloc = null ;

    public static Gensym create(String base) 
    { return new Gensym(SQLUtils.gen(base)) ; }
    
    public static Gensym create(String base, int startCount) 
    { return new Gensym(SQLUtils.gen(base), startCount) ; }

    private Gensym(String base) { this(base, 1) ; }
    private Gensym(String base, int startCount) { this.base = base ; this.count = startCount ; }
    
    @Override
    public String next()
    {
        String x = base+(count++) ;
        lastAlloc = x ;
        return x ;
    }
    
    @Override
    public String current()
    {
        if ( lastAlloc == null )
            next() ;
        return lastAlloc ;
    }
    
    private String read()
    {
        if ( lastAlloc == null )
            return "<>" ; 
        return lastAlloc ;
    }
    
    @Override
    public String toString() { return "gensym/"+read() ; }
}
