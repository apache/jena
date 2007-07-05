/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.shared;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.util.FileManager;

public class Env
{
    private static Log log = LogFactory.getLog(Env.class) ;
    private static final String SDBROOT = "SDBROOT" ; 
    public static String sysBase = null ;
    private static FileManager fileManager = null ; 
    
    static { initFM() ; }
    
    private static void initFM()
    {
        fileManager = new FileManager() ;
        fileManager.addLocatorFile() ;

        // Enabling this causes SDB to look in its installation directory for sdb.ttl files.
        // While convenient, that can be dangerous with the data manipulation commands. 
        // sysBase = System.getenv(SDBROOT) ;
        if ( sysBase == null )
            return ;
        
        File baseDir = new File(sysBase) ;
        if ( ! baseDir.exists() )
        {
            log.warn("Directory does not exist: "+baseDir) ;
            return ;
        }
        
        if ( ! baseDir.isDirectory() )
        {
            log.warn("Not a directory (but does exist): "+baseDir) ;
            return ;
        }
        fileManager.addLocatorFile(sysBase) ;
    }
    
    public static
    FileManager fileManager() { return fileManager ; }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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