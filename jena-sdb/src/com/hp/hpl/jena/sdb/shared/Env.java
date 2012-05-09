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

package com.hp.hpl.jena.sdb.shared;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.FileManager;

public class Env
{
    private static Logger log = LoggerFactory.getLogger(Env.class) ;
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
