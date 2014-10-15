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

package com.hp.hpl.jena.sdb.compiler;
/* H2 contribution from Martin HEIN (m#)/March 2008 */
/* SAP contribution from Fergal Monaghan (m#)/May 2012 */

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
        
        if ( StoreUtils.isSAP(store) )
        {
            request.LeftJoinTranslation = true ;
            request.LimitOffsetTranslation = true ;
            request.DistinctTranslation = true ;
        }
        
        QueryCompiler queryCompiler = store.getQueryCompilerFactory().createQueryCompiler(request) ;
        Op op2 = queryCompiler.compile(op) ;
        return op2 ;
    }
    

}
