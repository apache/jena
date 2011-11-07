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

import java.util.Comparator ;

import com.hp.hpl.jena.graph.Triple ;

public class TripleComparator implements Comparator<Triple>
{
    private static final NodeComparator nc = new NodeComparator();
    
	@Override
    public int compare(Triple o1, Triple o2)
    {
        int toReturn = nc.compare(o1.getSubject(), o2.getSubject());
        if (toReturn == 0)
        {
            toReturn = nc.compare(o1.getPredicate(), o2.getPredicate());
            if (toReturn == 0)
            {
                toReturn = nc.compare(o1.getObject(), o2.getObject());
            }
        }
        
        return toReturn;
    }
}
