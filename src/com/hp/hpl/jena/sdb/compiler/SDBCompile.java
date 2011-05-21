/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;
/* H2 contribution from Martin HEIN (m#)/March 2008 */

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.Context;

public class SDBCompile
{
    // ----- Compilation : Op -> SQL
    public static Op compile(Store store, Op op)
    {
        return compile(store, op, null) ;
    }
    
    public static Op compile(Store store, Op op, Context context)
    {
        if ( context == null )
            context = SDB.getContext() ;
        
        SDBRequest request = new SDBRequest(store, null, context) ;
        return compile(store, op, null, context, request) ;
    }
    
    // And the main compilation algorithm.
    // QueryCompilerMain does the bridge generation.
    public static Op compile(Store store, Op op, Binding binding, Context context, SDBRequest request)
    {
        if ( binding != null && ! binding.isEmpty() )
            op = Substitute.substitute(op, binding) ;
        
        // Defaults are set in SDBCompile.compile
        // LeftJoinTranslation = true ;         -- Does the DB support general join expressions? 
        // LimitOffsetTranslation = false ;     -- Does the DB grok the Limit/Offset SQL?
        // DistinctTranslation = true ;         -- Some DBs can't do DISTINCT on CLOBS.
        
        
        if ( StoreUtils.isHSQL(store) )
        {
            request.LeftJoinTranslation = false ;   // Does not deal with non-linear join trees.
            request.DistinctTranslation = true ; 
            request.LimitOffsetTranslation = false ;    // Does not cope with the nested SQL
        }
        
        if ( StoreUtils.isH2(store) )
        {
            request.LeftJoinTranslation = false ;   // Does not deal with non-linear join trees.
            request.DistinctTranslation = true ; 
            request.LimitOffsetTranslation = false ;    // Does not cope with the nested SQL
        }
        
        // Any of these need fixing and testing ...
        
        if ( StoreUtils.isDerby(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = false ;
            request.DistinctTranslation = false ;
        }
        
        if ( StoreUtils.isPostgreSQL(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = true ;
            request.DistinctTranslation = true ;
        }
        
        if ( StoreUtils.isMySQL(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = true ;
            request.DistinctTranslation = true ;
        }
        
        if ( StoreUtils.isSQLServer(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = false ;
            request.DistinctTranslation = false ;
        }
        
        if ( StoreUtils.isOracle(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = false ;
            request.DistinctTranslation = false ;
        }
        
        if ( StoreUtils.isDB2(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = false ;
            request.DistinctTranslation = false ;
        }
        
        QueryCompiler queryCompiler = store.getQueryCompilerFactory().createQueryCompiler(request) ;
        Op op2 = queryCompiler.compile(op) ;
        return op2 ;
    }
    

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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