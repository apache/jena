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

package org.openjena.riot;

import org.openjena.riot.lang.LangTurtleBase ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;

public class RIOT
{
    /** IRI for ARQ */  
    public static final String riotIRI = "http://openjena.org/#riot" ;

    /** The product name */   
    public static final String NAME = "RIOT";
    
    /** The root package name for RIOT */   
    public static final String PATH = "org.openjena.riot";

    public static final String VERSION = "ARQ/"+ARQ.VERSION ;
    public static final String BUILD_DATE = ARQ.BUILD_DATE ;
    
//    static private String metadataLocation = "com/hp/hpl/jena/sparql/arq-properties.xml" ;
//
//    static private Metadata metadata = new Metadata(metadataLocation) ;
//    
//    /** The full name of the current version */   
//    public static final String VERSION = metadata.get(PATH+".version", "unset") ;
//   
//    /** The date and time at which this release was built */   
//    public static final String BUILD_DATE = metadata.get(PATH+".build.datetime", "unset") ;
    
    public static void setStrictMode(boolean state)
    {
        LangTurtleBase.strict = state ;
    }
    
    static boolean initialized = false ;
    public static synchronized void init()
    {
        if ( initialized ) return ;
        initialized = true ;
        
        String NS = RIOT.PATH ;
        SystemInfo sysInfo2 = new SystemInfo(RIOT.riotIRI, RIOT.VERSION, RIOT.BUILD_DATE) ;
        ARQMgt.register(NS+".system:type=SystemInfo", sysInfo2) ;
        SystemARQ.registerSubSystem(sysInfo2) ;
        
        SysRIOT.wireIntoJena() ;
    }
}
