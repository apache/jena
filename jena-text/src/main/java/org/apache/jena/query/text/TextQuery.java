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

package org.apache.jena.query.text;

import org.apache.jena.query.text.assembler.TextAssembler ;

import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

public class TextQuery
{
    private static boolean initialized = false ;
    private static Object lock = new Object() ;
    public static String NS = "http://jena.apache.org/text#" ;
    public static String IRI = "http://jena.apache.org/#text" ;
    public static final Symbol textIndex = Symbol.create(NS+"index") ;
    public static final String PATH         = "org.apache.jena.query.text";
    
    static private String metadataLocation  = "org/apache/jena/query/text/properties.xml" ;
    static private Metadata metadata        = new Metadata(metadataLocation) ;
    public static final String NAME         = "ARQ Text Query";
   
    public static final String VERSION      = metadata.get(PATH+".version", "unknown") ;
    public static final String BUILD_DATE   = metadata.get(PATH+".build.datetime", "unset") ;
    
    static { init() ; }
    
    public static void init() 
    {
        if ( initialized ) return ;
        synchronized(lock)
        {
            if ( initialized ) return ;
            initialized = true ;
            TDB.init() ;
            TextAssembler.init() ;
            
            SystemInfo sysInfo = new SystemInfo(IRI, PATH, VERSION, BUILD_DATE) ;
            SystemARQ.registerSubSystem(sysInfo) ;
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/text#query", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new TextQueryPF() ;
                }
            });
        }
    }
}


