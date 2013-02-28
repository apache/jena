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

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.iri.IRI ;

import com.hp.hpl.jena.shared.PrefixMapping ;

/** Always empty prefix map */
public class PrefixMapNull implements PrefixMap
{
    public static PrefixMap empty = new PrefixMapNull() ;

    private PrefixMapNull()
    {}

    @Override
    public Map<String, IRI> getMapping()
    {
        return Collections.emptyMap() ;
    }

    @Override
    public Map<String, IRI> getMappingCopy()
    {
        return Collections.emptyMap() ;
    }

    @Override
    public Map<String, String> getMappingCopyStr()
    {
        return Collections.emptyMap() ;
    }

    @Override
    public void add(String prefix, String iriString)
    {
        throw new UnsupportedOperationException("Unmodifiable PrefixMap") ;
    }

    @Override
    public void add(String prefix, IRI iri)
    {
        throw new UnsupportedOperationException("Unmodifiable PrefixMap") ;
    }

    @Override
    public void putAll(PrefixMap pmap)
    {
        throw new UnsupportedOperationException("Unmodifiable PrefixMap") ;
    }

    @Override
    public void putAll(PrefixMapping pmap)
    {
        throw new UnsupportedOperationException("Unmodifiable PrefixMap") ;
    }

    @Override
    public void putAll(Map<String, String> mapping)
    {
        throw new UnsupportedOperationException("Unmodifiable PrefixMap") ;
    }

    @Override
    public void delete(String prefix)
    {
        throw new UnsupportedOperationException("Unmodifiable PrefixMap") ;
    }

    @Override
    public boolean contains(String prefix)
    {
        return false ;
    }

    @Override
    public String abbreviate(String uriStr)
    {
        return null ;
    }

    @Override
    public Pair<String, String> abbrev(String uriStr)
    {
        return null ;
    }

    @Override
    public String expand(String prefixedName)
    {
        return null ;
    }

    @Override
    public String expand(String prefix, String localName)
    {
        return null ;
    }

    @Override
    public boolean isEmpty()
    {
        return true ;
    }

    @Override
    public int size()
    {
        return 0 ;
    }
}
