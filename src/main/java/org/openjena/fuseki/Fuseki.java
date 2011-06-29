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

package org.openjena.fuseki;

import org.openjena.riot.RIOT ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;

public class Fuseki
{
    // External log : operations, etc.
    static public String PATH = "org.openjena.fuseki" ;
    static public String FusekiIRI = "http://openjena.org/Fuseki" ;
    
    static public String PagesPublish = "pages-publish" ;
    static public String PagesAll =     "pages-update" ;
    
    //static private String metadataDevLocation = "org/openjena/fuseki/fuseki-properties-dev.xml" ;
    static private String metadataLocation = "org/openjena/fuseki/fuseki-properties.xml" ;
    static private Metadata metadata = initMetadata() ;
    private static Metadata initMetadata()
    {
        Metadata m = new Metadata() ;
        //m.addMetadata(metadataDevLocation) ;
        m.addMetadata(metadataLocation) ;
        return m ;
    }
    
    static public String NAME = "Fuseki" ;
    static public String VERSION = metadata.get(PATH+".version", "development") ;
    static public String BUILD_DATE = metadata.get(PATH+".build.datetime", "unknown") ; // call Date if unavailable.
    public static String serverHttpName     = NAME+" ("+VERSION+")" ;    
    
    // Log for operations
    public static String requestLogName = PATH+".Fuseki" ;
    public static Logger requestLog = LoggerFactory.getLogger(PATH+".Fuseki") ;
    public static String serverLogName = PATH+".Server" ;
    public static Logger serverLog = LoggerFactory.getLogger(PATH+".Server") ;
    // Log for general server messages.
    
    private static boolean initialized = false ;
    public static void init()
    {
        if ( initialized )
            return ;
        initialized = true ;
        ARQ.init() ;
        SystemInfo sysInfo = new SystemInfo(FusekiIRI, VERSION, BUILD_DATE) ;
        ARQMgt.register(PATH+".system:type=SystemInfo", sysInfo) ;
        SystemARQ.registerSubSystem(sysInfo) ;
        RIOT.init() ;
    }
  
    // Force a call to init.
    static { init() ; }
}
