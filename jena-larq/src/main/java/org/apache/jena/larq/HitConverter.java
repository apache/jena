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

package org.apache.jena.larq;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.util.iterator.Map1;

/** Convert Lucene search hits to LARQ form (node and score)
 *  Hides the Lucene classes from the rest of ARQ. */ 

class HitConverter implements Map1<HitLARQ,Binding>
{
    private Binding binding ;
    private Var subject ;
    private Var score ;
    
    HitConverter(Binding binding, Var subject, Var score)
    {
        this.binding = binding ;
        this.subject = subject ;
        this.score = score ;
    }
    
    @Override
    public Binding map1(HitLARQ hit)
    {
        BindingMap b = BindingFactory.create(binding) ;
        b.add(Var.alloc(subject), hit.getNode()) ;
        if ( score != null )
            b.add(Var.alloc(score), NodeFactory.floatToNode(hit.getScore())) ;
        return b ;
    }
    
}
