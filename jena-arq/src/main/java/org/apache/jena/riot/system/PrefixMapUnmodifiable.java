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

import java.util.Collections ;
import java.util.Map ;

import org.apache.jena.iri.IRI ;

import com.hp.hpl.jena.shared.PrefixMapping ;

/** A PrefixMap that traps update operations on a wrapped prefix map */ 
public class PrefixMapUnmodifiable extends PrefixMapWrapper
{
    public PrefixMapUnmodifiable(PrefixMap other) { super(other) ; }

    @Override
    public Map<String, IRI> getMapping()
    { return Collections.unmodifiableMap(other.getMapping()) ; }

    @Override
    public void add(String prefix, String iriString)
    { throw new UnsupportedOperationException("Unmodifiable PrefixMap") ; }  

    @Override
    public void add(String prefix, IRI iri)
    { throw new UnsupportedOperationException("Unmodifiable PrefixMap") ; }  

    @Override
    public void putAll(PrefixMap pmap)
    { throw new UnsupportedOperationException("Unmodifiable PrefixMap") ; }  

    @Override
    public void putAll(PrefixMapping pmap)
    { throw new UnsupportedOperationException("Unmodifiable PrefixMap") ; }  

    @Override
    public void putAll(Map<String, String> mapping)
    { throw new UnsupportedOperationException("Unmodifiable PrefixMap") ; }  

    @Override
    public void delete(String prefix)
    { throw new UnsupportedOperationException("Unmodifiable PrefixMap") ; }  
}
