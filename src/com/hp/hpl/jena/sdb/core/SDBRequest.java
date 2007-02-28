/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreHolder;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarAlloc;
import com.hp.hpl.jena.sparql.util.Context;


/** A collection of things to track during query compilation
 * and execution from SPARQL to SQL.
 * 
 * @author Andy Seaborne
 * @version $Id: SDBRequest.java,v 1.14 2006/04/17 11:55:35 andy_seaborne Exp $
 */

public class SDBRequest extends StoreHolder
{
    private PrefixMapping prefixMapping ;
    private Query query ;
    
    // Per request unique variables.
    private VarAlloc varAlloc = new VarAlloc(AliasesSparql.VarBase) ;
    
    // See TransformSDB
    public boolean LeftJoinTranslation = true ;
    private Context context ;

    public SDBRequest(Store store, Query query, Context context)
    { 
        super(store) ;
        this.query = query ;
        this.prefixMapping = query.getPrefixMapping() ;
        if ( context == null )
            context = ARQ.getContext() ;
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
    public Var genvar()                         { return varAlloc.allocVar() ; }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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