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

package org.apache.jena.riot.adapters;

import java.util.Iterator ;

import org.apache.jena.riot.system.stream.LocationMapper ;

import com.hp.hpl.jena.rdf.model.Model ;

/** Adapter from Jena2 LocationMapper to RIOT LocationMapper */ 
public class AdapterLocationMapper extends com.hp.hpl.jena.util.LocationMapper 
{
    private final LocationMapper mapper ;

    public AdapterLocationMapper(LocationMapper mapper) {  this.mapper = mapper ; }
    
    @Override
    public String altMapping(String uri, String otherwise)  { return mapper.altMapping(uri, otherwise) ; }

    @Override
    public void addAltEntry(String uri, String alt)         { mapper.addAltEntry(uri, alt) ; }

    @Override
    public void addAltPrefix(String uriPrefix, String altPrefix)    { mapper.addAltPrefix(uriPrefix, altPrefix) ; } 

    /** Iterate over all the entries registered */ 
    @Override
    public Iterator<String> listAltEntries()  { return mapper.listAltEntries() ; } 
    /** Iterate over all the prefixes registered */ 
    @Override
    public Iterator<String> listAltPrefixes() { return mapper.listAltEntries() ; } 
    
    @Override
    public void removeAltEntry(String uri)  { mapper.removeAltEntry(uri) ; }

    @Override
    public void removeAltPrefix(String uriPrefix)   { mapper.removeAltPrefix(uriPrefix) ; }

    @Override
    public String getAltEntry(String uri)           { return mapper.getAltEntry(uri) ; } 

    @Override
    public String getAltPrefix(String uriPrefix)    { return mapper.getAltPrefix(uriPrefix) ; }
    
    @Override
    public int hashCode()               { return mapper.hashCode() ; }
    
    @Override
    public boolean equals(Object obj)   { return mapper.equals(obj) ; }
    
    @Override
    public String toString()            { return mapper.toString() ; } 
    
    @Override
    public void toModel(Model model)    { mapper.toModel(model) ; }
}

