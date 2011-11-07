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

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.query.QueryBuildException ;

/** Extension factory that instantiates a class each time. */ 

class FunctionFactoryAuto implements FunctionFactory
{
    Class<?> extClass ;
    
    FunctionFactoryAuto(Class<?> xClass)
    {
        extClass = xClass ;
    }
    
    @Override
    public Function create(String uri)
    {
        try
        {
            return (Function)extClass.newInstance() ;
        } catch (Exception e)
        {
            throw new QueryBuildException("Can't instantiate function for "+uri, e) ;
        } 
    }
}
