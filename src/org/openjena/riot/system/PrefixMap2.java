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

package org.openjena.riot.system;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.iri.IRI ;

// UNUSED
/** Extend a PrefixMap - never alters the partent PrefixMap */
public class PrefixMap2 extends PrefixMap
{
    PrefixMap parent ;
    PrefixMap local ;
    
    public PrefixMap2(PrefixMap parent)
    {
        this.parent = parent ;
        this.local = new PrefixMap() ; 
    }
    
    /** Add a prefix, overwites any existing association */
    @Override
    public void add(String prefix, IRI iri)
    { 
        prefix = canonicalPrefix(prefix) ;
        // Add to local always.
        local.add(prefix, iri) ;
    }
    
    /** Add a prefix, overwites any existing association */
    @Override
    public void delete(String prefix)
    { 
        prefix = canonicalPrefix(prefix) ;
        local.delete(prefix) ;
        if ( parent._contains(prefix) )
            Log.warn(this, "Attempt to delete a prefix in the parent" ) ;
    }
    
    /** Expand a prefix, return null if it can't be expanded */
    @Override
    public String expand(String prefix, String localName) 
    { 
        prefix = canonicalPrefix(prefix) ;
        String x = local.expand(prefix, localName) ;
        if ( x != null )
            return x ;
        return parent.expand(prefix, localName) ;
    }
}
