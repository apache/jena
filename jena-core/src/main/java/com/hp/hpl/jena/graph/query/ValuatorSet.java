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

package com.hp.hpl.jena.graph.query;

import java.util.*;

import com.hp.hpl.jena.util.CollectionFactory;

/**
	ValuatorSet - a set of Valuators, which can be added to and evaluated [only].

	@author kers
*/
public class ValuatorSet 
    {
    private Set<Valuator> valuators = CollectionFactory.createHashedSet();
    
    public ValuatorSet() 
        {}
    
    /**
        Answer true iff evaluating this ValuatorSet runs some Valuators. 
    */
    public boolean isNonTrivial()
        { return valuators.size() > 0; }
    
    /**
         Answer this ValuatorSet after adding the Valuator <code>e</code> to it.
    */
    public ValuatorSet add( Valuator e )
        {
        valuators.add( e );
        return this;    
        }
        
    /**
         Answer true iff no Valuator in this set evaluates to <code>false</code>. The
         Valuators are evaluated in an unspecified order, and evaluation ceases as
         soon as any Valuator has returned false.
    */
    public boolean evalBool( IndexValues vv )
        { 
        Iterator<Valuator> it = valuators.iterator();
        while (it.hasNext()) 
            if (it.next().evalBool( vv ) == false) return false;
        return true;
        }
                    
    }
