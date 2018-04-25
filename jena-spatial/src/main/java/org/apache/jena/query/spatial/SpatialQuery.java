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

package org.apache.jena.query.spatial;

import org.locationtech.spatial4j.context.SpatialContext ;

import org.apache.jena.query.spatial.assembler.SpatialAssembler ;
import org.apache.jena.query.spatial.pfunction.library.* ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.lib.Metadata ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.pfunction.PropertyFunction ;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.sys.JenaSystem ;

public class SpatialQuery
{
    private static volatile boolean initialized = false ;
    private static Object lock = new Object() ;

    public static String NS = "http://jena.apache.org/spatial#" ;
    public static String IRI = "http://jena.apache.org/#spatial" ;
    public static final Symbol spatialIndex = Symbol.create(NS+"index") ;
    public static final String PATH         = "org.apache.jena.query.spatial";
    
    static private String metadataLocation  = "org/apache/jena/query/spatial/properties.xml" ;
    static private Metadata metadata        = new Metadata(metadataLocation) ;
    public static final String NAME         = "ARQ Spatial Query";
   
    public static final String VERSION      = metadata.get(PATH+".version", "unknown") ;
    public static final String BUILD_DATE   = metadata.get(PATH+".build.datetime", "unset") ;
    
    public static SpatialContext ctx = SpatialContext.GEO;
    
    // an optional feature for WKT literals, loaded when necessary, but not required
    public static final String JTS_SPATIAL_CONTEXT_FACTORY_CLASS = "org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory"; 
    
    static { JenaSystem.init(); }
    
    public static void init() 
    {
        if ( initialized ) 
            return ;
        synchronized(lock)
        {
            if ( initialized ) {
                JenaSystem.logLifecycle("SpatialQuery.init - skip") ;
                return ; 
            }
            initialized = true ;
            JenaSystem.logLifecycle("SpatialQuery.init - start") ;

            SpatialAssembler.init() ;
            
            SystemInfo sysInfo = new SystemInfo(IRI, PATH, VERSION, BUILD_DATE) ;
            SystemARQ.registerSubSystem(sysInfo) ;
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#withinCircle", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new IsWithinCirclePF() ;
                }
            });
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#nearby", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new IsNearByPF() ;
                }
            });
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#withinBox", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new IsWithinBoxPF() ;
                }
            });
            
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#intersectBox", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new IntersectsBoxPF() ;
                }
            });
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#north", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new NorthPF() ;
                }
            });
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#south", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new SouthPF() ;
                }
            });
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#east", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new EastPF() ;
                }
            });
            
            PropertyFunctionRegistry.get().put("http://jena.apache.org/spatial#west", new PropertyFunctionFactory() {
                @Override
                public PropertyFunction create(String uri) {
                    return new WestPF() ;
                }
            });
            
            JenaSystem.logLifecycle("SpatialQuery.init - finish") ;

        }
    }
}

