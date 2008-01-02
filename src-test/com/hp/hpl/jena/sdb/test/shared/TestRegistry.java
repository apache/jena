/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test.shared;

import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestRegistry
{
    String[] databaseNames = {  "derby", "HSQLDB",
                                "MySQL", "PostgreSQL", 
                                "SQLServer", "Oracle", "DB2" } ;
    
    String[] layoutNames = {    "layout2/hash" , "layout2", 
                                "layout2/index", 
                                "layout1",
                                "layoutRDB" } ;
    
    @Test public void reg_database_1()
    {
        // Tests default configuration.
        for ( String s : databaseNames )
            assertNotNull(DatabaseType.fetch(s)) ;
    }

    @Test public void reg_database_2()
    {
        for ( String s : DatabaseType.allNames() )
            assertNotNull(DatabaseType.fetch(s)) ;
    }
    
    @Test public void reg_database_3()
    {
        for ( DatabaseType t : DatabaseType.allTypes() )
            assertNotNull(DatabaseType.fetch(t.getName())) ;
    }

    @Test public void reg_layout_1()
    {
        // Tests default configuration.
        for ( String s : layoutNames )
            assertNotNull(LayoutType.fetch(s)) ;
    }

    @Test public void reg_layout_2()
    {
        for ( String s : LayoutType.allNames() )
            assertNotNull(LayoutType.fetch(s)) ;
    }
    
    @Test public void reg_layout_3()
    {
        for ( LayoutType t : LayoutType.allTypes() )
            assertNotNull(LayoutType.fetch(t.getName())) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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