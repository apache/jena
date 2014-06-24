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

package org.apache.jena.query.text;

import java.util.HashMap ;
import java.util.Map ;

public class Entity
{
    private final String id ;
    private final String graph ;
    private final Map<String, Object> map = new HashMap<>() ;

    public Entity(String entityId, String entityGraph) {
        this.id = entityId ;
        this.graph = entityGraph;
    }

    /** @deprecated Use {@linkplain #Entity(String, String)} */
    @Deprecated
    public Entity(String entityId)          { this(entityId, null) ; }
    
    public String getId()                   { return id ; }

    public String getGraph()                { return graph ; }

    public void put(String key, Object value)
    { map.put(key, value) ; }
    
    public Object get(String key)
    { return map.get(key) ; }

    public Map<String, Object> getMap()     { return map ; }
    
    @Override
    public String toString() {
        return id+" : "+map ;
    }
}

