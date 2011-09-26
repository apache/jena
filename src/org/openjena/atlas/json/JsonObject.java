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

package org.openjena.atlas.json;

import java.util.Collection ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Set ;
import java.util.Map.Entry ;

public class JsonObject extends JsonValue //implements Map<String, JsonValue>
{
    private final Map<String, JsonValue> map = new HashMap<String, JsonValue>() ;
    
    @Override
    public boolean isObject()       { return true ; }
    @Override
    public JsonObject getAsObject() { return this ; }
    
    @Override
    public void visit(JsonVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public int hashCode()
    {
        return map.hashCode() ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof JsonObject) ) return false ;
        return map.equals(((JsonObject)other).map) ;
    }
    
    //@Override
    public void clear()
    { map.clear() ; }
    
    //@Override
    public boolean hasKey(Object key)
    {
        return map.containsKey(key) ;
    }
    
//    //@Override
//    public boolean containsValue(Object value)
//    {
//        return map.containsValue(value) ;
//    }
    
    //@Override
    public Set<String> keys()
    {
        return map.keySet() ;
    }

    
    //@Override
    public Set<Entry<String, JsonValue>> entrySet()
    {
        return map.entrySet() ;
    }
    
    //@Override
    public JsonValue get(String key)
    {
        return map.get(key) ;
    }
    //@Override
    public boolean isEmpty()
    {
        return map.isEmpty() ;
    }
    //@Override
    public Set<String> keySet()
    {
        return map.keySet() ;
    }
    //@Override
    public JsonValue put(String key, JsonValue value)
    {
        return map.put(key, value) ;
    }
    
    //@Override
    public JsonValue put(String key, String value)
    {
        return map.put(key, new JsonString(value)) ;
    }

    //@Override
    public JsonValue put(String key, long value)
    {
        return map.put(key, JsonNumber.value(value)) ;
    }

    //@Override
    public JsonValue put(String key, boolean b)
    {
        return map.put(key, new JsonBoolean(b)) ;
    }

    
    
    //@Override
    public void putAll(Map< ? extends String, ? extends JsonValue> m)
    {
        map.putAll(m) ;
    }
    
    //@Override
    public JsonValue remove(Object key)
    {
        return map.remove(key) ;
    }
    //@Override
    public int size()
    {
        return map.size() ;
    }
    //@Override
    public Collection<JsonValue> values()
    {
        return map.values() ;
    }
}
