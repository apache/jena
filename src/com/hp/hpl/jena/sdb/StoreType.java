/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import com.hp.hpl.jena.query.util.Symbol;
import com.hp.hpl.jena.query.util.TranslationTable;


public class StoreType extends Symbol
{
    // Need :: layout + database type + options
    // Maybe connection+database+options => instance ;; instant+layout => store 
    
    private static final String BASE = "http://jena.hpl.hp.com/2006/04/store/" ; 
    
    public static final StoreType layoutSimple   = new StoreType(BASE+"simple") ;
    public static final StoreType layout2        = new StoreType(BASE+"layout2") ;
    public static final StoreType layoutRDB      = new StoreType(BASE+"layoutRDB") ;
//    public static final StoreType layout2pg      = new StoreType(BASE+"layout2pg") ;
//    public static final StoreType layout2hsql    = new StoreType(BASE+"layout2hsql") ;
    
    public static TranslationTable layoutNames = new TranslationTable(true) ;
    private static void put(String name, StoreType storeName)
    {
        layoutNames.put(canonical(name), storeName) ; 
    }
    
    static {
        put("layout1",       StoreType.layoutSimple) ;
        put("simple",        StoreType.layoutSimple) ;
        put("layout2",       StoreType.layout2) ;
        put("layoutRDB",     StoreType.layoutRDB) ;
//        put("layout2pg",     StoreType.layout2pg) ;
//        put("layout2hsql",   StoreType.layout2hsql) ;
    }
    
    //protected StoreType(DatabaseType dbType, LayoutType layoutType) {}
    
    protected StoreType(String s)     { super(s) ; }
    protected StoreType(StoreType s)  { super(s) ; }
    
    private static String canonical(String s) { return s.toLowerCase() ; }
    
    public static StoreType lookup(String s)
    {
        return (StoreType)layoutNames.lookup(s.toLowerCase()) ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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