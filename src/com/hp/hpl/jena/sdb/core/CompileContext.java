/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreHolder;
import com.hp.hpl.jena.shared.PrefixMapping;


/** A collection of things to track during query compilation
 * from SPARQL to SQL.
 * 
 * @author Andy Seaborne
 * @version $Id: CompileContext.java,v 1.14 2006/04/17 11:55:35 andy_seaborne Exp $
 */

public class CompileContext extends StoreHolder
{
    PrefixMapping prefixMapping ;
    
    public CompileContext(Store store, PrefixMapping prefixMapping)
    { super(store) ; this.prefixMapping = prefixMapping ; }
    
    public Store getStore()            { return store() ; }

    public PrefixMapping getPrefixMapping()
    { 
        if ( prefixMapping == null )
            return SDB.getGlobalPrefixMapping() ; 
        return prefixMapping ;
    }

    private static final String triplesTableAliasBase = "T"+SDBConstants.SQLmark ;
    private Generator genTableAlias = new Gensym(triplesTableAliasBase) ;
    public Generator getGenTableAlias() { return genTableAlias ; }
    
    private static String joinAliasBase = "J"+SDBConstants.SQLmark ;
    private Generator genJoinAlias = new Gensym(joinAliasBase) ;
    public Generator getGenJoinAlias() { return genJoinAlias ; }
}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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