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

package org.apache.jena.riot.system;

import java.util.Map ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;

import com.hp.hpl.jena.shared.PrefixMapping ;

//private static PrefixMap empty = new PrefixMapNull() ; 
    /**
     * Creates an always-empty prefix map, which can not be changed.
     */
//    public static PrefixMap nullPrefixMap() {
//    }
    
    public class PrefixMapWrapper implements PrefixMap
    {
        protected final PrefixMap other ;

        public PrefixMapWrapper(PrefixMap other) { this.other = other ; }
        
        @Override
        public Map<String, IRI> getMapping()
        { return other.getMapping() ; }

        @Override
        public Map<String, IRI> getMappingCopy()
        { return other.getMappingCopy() ; }

        @Override
        public Map<String, String> getMappingCopyStr()
        { return other.getMappingCopyStr() ; } 

        @Override
        public void add(String prefix, String iriString)
        { other.add(prefix, iriString) ; }

        @Override
        public void add(String prefix, IRI iri)
        { other.add(prefix, iri) ; }

        @Override
        public void putAll(PrefixMap pmap)
        { other.putAll(pmap) ; }

        @Override
        public void putAll(PrefixMapping pmap)
        { other.putAll(pmap) ; }

        @Override
        public void putAll(Map<String, String> mapping)
        { other.putAll(mapping) ; }

        @Override
        public void delete(String prefix)
        { other.delete(prefix) ; }

        @Override
        public boolean contains(String prefix)
        { return other.contains(prefix) ; }

        @Override
        public String abbreviate(String uriStr)
        { return other.abbreviate(uriStr) ; }

        @Override
        public Pair<String, String> abbrev(String uriStr)
        { return other.abbrev(uriStr) ; }

        @Override
        public String expand(String prefixedName)
        { return other.expand(prefixedName) ; }

        @Override
        public String expand(String prefix, String localName)
        { return other.expand(prefix, localName) ; }

        @Override
        public boolean isEmpty()
        { return other.isEmpty() ; }

        @Override
        public int size()
        { return other.size() ; }
    }
