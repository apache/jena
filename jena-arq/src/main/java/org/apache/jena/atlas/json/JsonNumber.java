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

import java.math.BigDecimal ;

public class JsonNumber extends JsonPrimitive
{
    public static JsonNumber valueDecimal(String image)
    { return new JsonNumber(image) ; }

    public static JsonNumber valueDouble(String image)
    { return new JsonNumber(image) ; }

    public static JsonNumber valueInteger(String image)
    { return new JsonNumber(image) ; }
 
    public static JsonNumber value(long number)
    { return new JsonNumber(number) ; }
    
    public static JsonNumber value(double number)
    { return new JsonNumber(number) ; }
    
    public static JsonNumber value(BigDecimal number)
    { return new JsonNumber(number) ; }

    private final BigDecimal number ;

    private JsonNumber(String string)        { this.number = new BigDecimal(string) ; }
    
    private JsonNumber(long number)          { this.number = BigDecimal.valueOf(number) ; }
    private JsonNumber(double number)        { this.number = BigDecimal.valueOf(number) ; }
    private JsonNumber(BigDecimal number)    { this.number = number ; }
    
    //public JsonNumber(Number number)    { this.number = number ; }
    
    @Override
    public boolean isNumber()           { return true ; }
    @Override
    public JsonNumber getAsNumber()     { return this ; }
    
    public Number value()               { return this.number ; }

    @Override
    public void visit(JsonVisitor visitor)
    { visitor.visit(this) ; }
    
    @Override
    public int hashCode()
    {
        return number.hashCode() ;
    }

    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof JsonNumber ) ) return false ;
//        Number n1 = number ;
//        Number n2 = ((JsonNumber)other).number ;
//        
//        if ( ! n1.getClass().equals(n2.getClass()))
//            return false ;
        return number.equals(((JsonNumber)other).number) ; 
    }
    
}
