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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public abstract class SimpleReifierFragmentHandler implements ReifierFragmentHandler 
    { 
    Fragments.GetSlot which; 
    SimpleReifierFragmentsMap map;
    
    public SimpleReifierFragmentHandler( SimpleReifierFragmentsMap map, Fragments.GetSlot n ) 
        { which = n; this.map = map; }
    
    public abstract boolean clashesWith( ReifierFragmentsMap map, Node fragmentObject, Triple reified );
    
    @Override
    public boolean clashedWith( Node tag, Node fragmentObject, Triple reified )
        {
        if (clashesWith( map, fragmentObject, reified ))
            {
            map.putAugmentedTriple( this, tag, fragmentObject, reified );
            return true;
            }
        else
            return false;
        }
    
    @Override
    public Triple reifyIfCompleteQuad( Triple fragment, Node tag, Node object )
        {
        return map.reifyCompleteQuad( this, fragment, tag, object );
        }
    
    /**
     * @param tag
     * @param already
     * @param fragment
     * @return
     */
    @Override
    public Triple removeFragment( Node tag, Triple already, Triple fragment )
        { 
        return map.removeFragment( this, tag, already, fragment );
        }
    }
