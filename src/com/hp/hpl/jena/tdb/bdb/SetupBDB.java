/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bdb;

import java.io.File;

import com.sleepycat.je.*;

import com.hp.hpl.jena.tdb.Const;
import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.base.file.Location;

public class SetupBDB
{
    public Environment dbEnv = null;
    public DatabaseConfig dbConfig = null ;
    public LockMode lockMode = LockMode.DEFAULT ;
    public CursorConfig cursorConfig = CursorConfig.DEFAULT ;
    
    public SetupBDB(String dirname)
    {
        Location.ensureDirectory(dirname) ;
        try { 
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            
            // Aggressively high.
            envConfig.setCachePercent(Const.BDB_cacheSizePercent) ;
            
            //envConfig.setTransactional(true) ;
            dbEnv = new Environment(new File(dirname), envConfig);
            dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
        }
        catch (DatabaseException ex)
        {
            throw new TDBException("SetupBDB",ex) ;
        }     
    }

    public void close()
    {
        try { 
            dbEnv.cleanLog();
            dbEnv.close();
        }
        catch (DatabaseException ex)
        {
            throw new TDBException("SetupBDB.close",ex) ;
        }     
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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