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

package org.apache.jena.atlas.json;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.json.io.JsonWriter ;

public abstract class JsonValue implements Printable
{
    // Called a "Value" in the JSON spec 
    // Called Element in gson.
    
    public boolean isObject()           { return false ; }
    public JsonObject getAsObject()     { throw new JsonException("Not a JSON object") ; }
    
    public boolean isArray()            { return this instanceof JsonArray ; }
    public JsonArray getAsArray()       { throw new JsonException("Not a JSON array") ; }
    
    public boolean isPrimitive()        { return isString() || isNumber() || isBoolean() || isNull() ; }
    public boolean isNull()             { return false ; }

    public boolean isNumber()           { return false ; }
    public JsonNumber getAsNumber()     { throw new JsonException("Not a JSON number") ; }
    
    public boolean isString()           { return false ; }
    public JsonString getAsString()     { throw new JsonException("Not a JSON string") ; }
    
    public boolean isBoolean()          { return false ; }
    public JsonBoolean getAsBoolean()   { throw new JsonException("Not a JSON boolean") ; }
    
    @Override public abstract int hashCode() ;
    @Override public abstract boolean equals(Object other) ;
    
    public abstract void visit(JsonVisitor visitor) ;

    @Override
    public String toString()
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        output(buff) ;
        return buff.asString() ;
    }
    
    @Override
    public void output(IndentedWriter out)
    {
        JsonWriter w = new JsonWriter(out) ;
        w.startOutput() ;
        this.visit(w) ;
        w.finishOutput() ;
    }
}
