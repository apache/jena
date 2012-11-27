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

public class JsonString extends JsonPrimitive
{
    private final String string ;

    public JsonString(String string)    { this.string = string ; }
    
    @Override
    public boolean isString()           { return true ; }
    @Override
    public JsonString getAsString()       { return this ; }
    
    public String value()               { return this.string ; }
    
    @Override
    public void visit(JsonVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public int hashCode()
    {
        return string.hashCode() ;
    }

    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof JsonString ) ) return false ;
        return string.equals(((JsonString)other).string) ; 
    }
}
