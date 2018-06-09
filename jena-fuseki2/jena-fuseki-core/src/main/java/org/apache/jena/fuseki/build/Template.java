/**
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

package org.apache.jena.fuseki.build;

import java.nio.file.Path ;

import org.apache.jena.fuseki.server.FusekiEnv ;

public class Template
{
    public static Path getPath(String templateName) {
        return FusekiEnv.FUSEKI_BASE.resolve(templateName) ;
    }
    
    public static final String templateDir          = "templates" ;
    
    // These are used by the command line start up.
    public static final String templateServiceFN    = templateDir+"/config-service" ;       // Dummy used by dataset-less service.

    // TDB1 - for backwards compatibility, the files are called "tdb"
    public static final String templateTDB1_FN        = templateDir+"/config-tdb" ;
    public static final String templateTDB1_MemFN     = templateDir+"/config-tdb-mem" ; 
    public static final String templateTDB1_DirFN     = templateDir+"/config-tdb-dir" ;
    public static final String templateTDB1_DirReadFN = templateDir+"/config-tdb-dir-read-only" ;
    
    public static final String templateTDB2_FN        = templateDir+"/config-tdb2" ;
    public static final String templateTDB2_MemFN     = templateDir+"/config-tdb2-mem" ; 
    public static final String templateTDB2_DirFN     = templateDir+"/config-tdb2-dir" ;
    public static final String templateTDB2_DirReadFN = templateDir+"/config-tdb2-dir-read-only" ;
    
    
    public static final String templateTIM_MemFN      = templateDir+"/config-mem" ;

    // Template may be in a resources area of a jar file so you can't do a directory listing.
    public static final String[] templateNames = {
        templateTIM_MemFN,
        templateServiceFN,
        
        templateTDB1_FN ,
        templateTDB1_MemFN ,
        templateTDB1_DirFN ,
        //templateTDB1_DirReadFN,
        
        templateTDB2_FN ,
        templateTDB2_MemFN ,
        templateTDB2_DirFN ,
        //templateTDB2_DirReadFN
    } ;
    
    public static final String NAME = "NAME" ;
    public static final String DATA = "DATA" ;
    public static final String DIR =  "DIR" ;
}

