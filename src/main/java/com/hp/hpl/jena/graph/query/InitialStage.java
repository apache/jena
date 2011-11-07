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

/**
    The initial stage of a query, responsible for dropping the no-variables-bound seed
    binding domain into the remaining stages of the query pipeline.
    
	@author kers
*/
public class InitialStage extends Stage
    {
    /**
        The value passed in is the computed width of the result array(s); this
        is used to allocate the seeding node array.
        
     	@param count the width of the result binding array
     */
    public InitialStage( int count )
        { this.count = count; }
        
    final int count;
    
    @Override
    public void close()
        { markClosed(); }

    /**
        To deliver value into the Pipe result, we drop in a binding array of the correct
        width in which all the elements are null, then we close the pipe. Everything else
        is spawned by the following stages.
    */
    @Override
    public Pipe deliver( Pipe result )
        {
        result.put( new Domain( count ) );
        result.close();
        return result;
        }
    }
