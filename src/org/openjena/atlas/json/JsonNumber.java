/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import java.math.BigDecimal;

public class JsonNumber extends JsonPrimitive
{
    public static JsonNumber valueDecimal(String image)
    {
        return new JsonNumber(image) ;
    }

    public static JsonNumber valueDouble(String image)
    {
        return new JsonNumber(image) ;
    }

    public static JsonNumber valueInteger(String image)
    {
        return new JsonNumber(image) ;
    }
 
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
    public JsonNumber getNumber()       { return this ; }
    
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

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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