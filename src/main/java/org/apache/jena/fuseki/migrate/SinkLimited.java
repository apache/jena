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

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkWrapper ;
import org.openjena.riot.RiotException ;

public class SinkLimited<T> extends SinkWrapper<T>
{
   private long count = 0 ;
   private final long limit ;
    
    public SinkLimited(Sink<T> output, long limit)
    {
        super(output) ;
        this.limit = limit ;
    }

    @Override
    public void send(T thing)
    {
        count++ ;
        if ( count > limit )
            throw new RiotException("Limit "+limit+" exceeded") ; 
        super.send(thing) ;
    }
    
    public long getCount() { return count ; } 
    public long getLimit() { return limit ; }
}

