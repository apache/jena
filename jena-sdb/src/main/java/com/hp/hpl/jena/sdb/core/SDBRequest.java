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

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.StoreHolder;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.util.Context;


/** A collection of things to track during query compilation
 * and execution from SPARQL to SQL.
 */

public class SDBRequest extends StoreHolder
{
    private PrefixMapping prefixMapping ;
    private Query query ;
    
    // Per request unique variables.
    private VarAlloc varAlloc = new VarAlloc(AliasesSparql.VarBase) ;
    
    // Set in SDBCompile.compile
    public boolean LeftJoinTranslation = true ;     // Does the DB support general join expressions? 
    public boolean LimitOffsetTranslation = false ; // Does the DB grok the Limit/Offset SQL?
    public boolean DistinctTranslation = true ;     // Some DBs can't do DISTINCt on CLOBS.
    
    private Context context ;

    public SDBRequest(Store store, Query query, Context context)
    { 
        super(store) ;
        this.query = query ;
        
        this.prefixMapping = null ;
        if ( query != null )
            prefixMapping = query.getPrefixMapping() ;
        if ( context == null )
            context = SDB.getContext() ;
        this.context = new Context(context) ;
    }

    public SDBRequest(Store store, Query query)
    { 
        this(store, query, null) ;
    }
    
    public Context getContext()                 { return context ; }
    public PrefixMapping getPrefixMapping()     { return prefixMapping ; }
    public Query getQuery()                     { return query ; }
    public Store getStore()                     { return store() ; }
    
    // Per request allocations
    private Map<String, Generator> generators = new HashMap<String, Generator>() ;
    public Generator generator(String base)
    {
        Generator g = generators.get(base) ;
        if ( g == null )
        {
            g = Gensym.create(base) ;
            generators.put(base, g) ;
        }
        return g ;
    }

    public String genId(String base)
    {
        Generator gen = generator(base) ;
        return gen.next() ;
    }

    
    public Var genVar()                         { return varAlloc.allocVar() ; }
}
