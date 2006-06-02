/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import com.hp.hpl.jena.query.util.Symbol;
import com.hp.hpl.jena.query.util.TranslationTable;


public class StoreName extends Symbol
{
    //TODO Design AND finish
    // Need :: layout + database type + options
    // Maybe connection+database+options => instance ;; instant+layout => store 
    
    private static final String BASE = "http://jena.hpl.hp.com/2006/04/store/" ; 
    
    public static final StoreName layoutSimple   = new StoreName(BASE+"simple") ;
    public static final StoreName layout2        = new StoreName(BASE+"layout2") ;
    public static final StoreName layoutRDB      = new StoreName(BASE+"layoutRDB") ;
    public static final StoreName layout2bulk    = new StoreName(BASE+"layout2bulk") ;
    public static final StoreName layout2pg      = new StoreName(BASE+"layout2pg") ;
    public static final StoreName layout2hsql    = new StoreName(BASE+"layout2hsql") ;
    
    public static TranslationTable layoutNames = new TranslationTable(true) ;
    static {
        layoutNames.put("layout1",       StoreName.layoutSimple) ;
        layoutNames.put("simple",        StoreName.layoutSimple) ;
        layoutNames.put("layout2",       StoreName.layout2) ;
        layoutNames.put("layoutRDB",     StoreName.layoutRDB) ;
        layoutNames.put("layout2bulk",   StoreName.layout2bulk) ;
        layoutNames.put("layout2pg",     StoreName.layout2pg) ;
        layoutNames.put("layout2hsql",   StoreName.layout2hsql) ;
    }
    
    protected StoreName(String s)     { super(s) ; }
    protected StoreName(StoreName s)  { super(s) ; }
    
    public static StoreName lookup(String s)
    {
        return (StoreName)layoutNames.lookup(s) ;
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