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
    public static final String templateMemFN        = templateDir+"/config-mem" ;
    public static final String templateTDBFN        = templateDir+"/config-tdb" ;
    public static final String templateTDBMemFN     = templateDir+"/config-tdb-mem" ; 
    public static final String templateTDBDirFN     = templateDir+"/config-tdb-dir" ;
    public static final String templateTDBDirReadFN = templateDir+"/config-tdb-dir-read-only" ;
    public static final String templateServiceFN    = templateDir+"/config-service" ;       // Dummy used by dataset-less service.
    
    public static final String templateMemFN_1      = templateDir+"/config-mem-txn" ;

    // Template may be in a resources area of a jar file so you can't do a directory listing.
    public static final String[] templateNames = {
        templateMemFN ,
        templateTDBFN ,
        templateTDBMemFN ,
        templateTDBDirFN ,
        templateServiceFN
    } ;
    
    public static final String NAME = "NAME" ;
    public static final String DATA = "DATA" ;
    public static final String DIR =  "DIR" ;
}

