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

package com.hp.hpl.jena.sdb.core;

import java.util.HashMap;
import java.util.Map;

public class Map2<S,T>
{
    Map<S, T> mapForward  = new HashMap<S, T>() ;
    Map<T, S> mapBackward = new HashMap<T, S>() ;
    
    // Two way map
    public Map2() {}
    
    public void putMapping(S s, T t)
    {
        mapForward.put(s, t) ;
        mapBackward.put(t, s) ;
    }
    
    // Protected - need to give better names for instantiated form.
    
    protected T getByLeft(S s) { return mapForward.get(s) ; }
    protected S getByRight(T t) { return mapBackward.get(t) ; }
    
    protected boolean containsLeft(S s) { return  mapForward.containsKey(s) ; }
    protected boolean containsRight(T t) { return  mapBackward.containsKey(t) ; }
}
