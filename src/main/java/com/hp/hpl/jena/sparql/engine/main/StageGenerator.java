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

package com.hp.hpl.jena.sparql.engine.main;

import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;

/** Interface for execution of a basic graph pattern.
 *  A StageGenerator is registred in the context of an query
 *  execution to be found and called by the StageBuilder.
 *  
 *  The StageGenerator is called repeated for a basic graph pattern
 *  with each possible bindings unsed to instantiate variables
 *  where possible.  
 *  
 *  Each call of a stage generator returns a QueryIterator
 *  of solutions to the pattern for each of the possibilities
 *  (bindings) from the input query iterator.
 *  
 *  Result bindings to a particular input binding should use that
 *  as their parent, to pick up the variable bounds for that
 *  particular input. 
 * 
 * @see StageBuilder
 */

public interface StageGenerator
{
    public QueryIterator execute(BasicPattern pattern, 
                                 QueryIterator input,
                                 ExecutionContext execCxt) ;
}
