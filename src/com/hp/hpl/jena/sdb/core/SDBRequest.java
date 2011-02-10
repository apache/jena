/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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
 * 
 * @author Andy Seaborne
 */

public class SDBRequest extends StoreHolder
{
    private PrefixMapping prefixMapping ;
    private Query query ;
    
    // Per request unique variables.
    private VarAlloc varAlloc = new VarAlloc(AliasesSparql.VarBase) ;
    
    // Set in SDBCompile.compile
    public boolean LeftJoinTranslation = true ;     // Does the DB support general join expressions? 
    public boolean LimitOffsetTranslation = true ;  // Does the DB grok the Limit/Offset SQL?
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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */