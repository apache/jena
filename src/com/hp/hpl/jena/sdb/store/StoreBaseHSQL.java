/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.compiler.QueryCompilerFactory;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.layout2.StoreBase;
import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.util.HSQLUtils;
import com.hp.hpl.jena.sdb.util.StoreUtils;



public abstract class StoreBaseHSQL extends StoreBase
{
    protected boolean currentlyOpen = true ;
    
    public StoreBaseHSQL(SDBConnection connection, StoreDesc desc,
                         StoreFormatter formatter,
                         StoreLoader loader,
                         QueryCompilerFactory compilerF,
                         SQLBridgeFactory sqlBridgeF,
                         TableDescTriples tripleTableDesc,
                         TableDescQuads quadTableDesc,
                         TableDescNodes nodeTableDesc)
    {
        super(connection, desc, formatter, loader, compilerF, sqlBridgeF, 
              new GenerateSQL(), tripleTableDesc, quadTableDesc, nodeTableDesc) ;
    }

    @Override 
    public void close()
    {
        if ( currentlyOpen ) {
        	super.close() ;
            // This interacts with JDBC connection management 
            HSQLUtils.shutdown(getConnection()) ;
        }
        
        currentlyOpen = false ; 
        super.close();
    }

    public static void close(Store store)
    {
        if ( StoreUtils.isHSQL(store) )
            ((StoreBaseHSQL)store).close() ;
    }
    
    public static void checkpoint(Store store)
    {
        if ( StoreUtils.isHSQL(store) )
            ((StoreBaseHSQL)store).checkpoint() ;
    }
    
    public void checkpoint()
    { 
        if ( currentlyOpen ) 
            HSQLUtils.checkpoint(getConnection()) ;
    }
    
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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