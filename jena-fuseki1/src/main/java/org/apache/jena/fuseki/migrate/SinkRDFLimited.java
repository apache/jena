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

package org.apache.jena.fuseki.migrate;

import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFWrapper ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class SinkRDFLimited extends StreamRDFWrapper
{
   private long count = 0 ;
   private final long limit ;
    
    public SinkRDFLimited(StreamRDF output, long limit)
    {
        super(output) ;
        this.limit = limit ;
    }

    @Override
    public void triple(Triple triple)
    { 
        count++ ;
        super.triple(triple) ;
    }

    @Override
    public void quad(Quad quad)
    { 
        count++ ;
        super.quad(quad) ;
    }

    public long getCount() { return count ; } 
    public long getLimit() { return limit ; }
}

