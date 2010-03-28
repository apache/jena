/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    public boolean containsKey(Object key)
    {
        return map.containsKey(key) ;
    }
    //@Override
    public boolean containsValue(Object value)
    {
        return map.containsValue(value) ;
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */