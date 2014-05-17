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

package com.hp.hpl.jena.sparql.util;

import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;

/** Assist with naming symbols by URI.
 *  This class keeps a prefix mapping so that subsystems can register their
 *  prefix and keep code shorter.
 */
public class MappingRegistry
{
    private static PrefixMap mapping = PrefixMapFactory.create() ;
    
    public static void addPrefixMapping(String prefix, String uri)
    {
        mapping.add(prefix, uri) ;
    }
    
    public static String mapPrefixName(String prefixName)
    {
        String x = mapping.expand(prefixName) ;
        if ( x == null ) return prefixName ;
        return x ;
    }
}
