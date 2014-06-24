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

package com.hp.hpl.jena.sparql.util;

import com.hp.hpl.jena.datatypes.BaseDatatype ;
import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.TypeMapper ;

public class RomanNumeralDatatype extends BaseDatatype //implements RDFDatatype
{
    private static boolean firstClassDatatype = false ;
    public static void enableAsFirstClassDatatype()
    { 
        if ( ! firstClassDatatype )
            TypeMapper.getInstance().registerDatatype(get()) ;
    }
    
    // Singleton
    private static RomanNumeralDatatype datatype = new RomanNumeralDatatype() ; 
    public static RomanNumeralDatatype get() { return datatype ; }
    
    private static final String URI = "http://rome.example.org/Numeral" ;

    private RomanNumeralDatatype() { super(URI) ; }

    @Override
    public Class<?> getJavaClass() { return RomanNumeral.class ; }
    
    @Override
    public String unparse(Object value)
    {
        return value.toString() ; 
    }
    
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException
    {
        try {
            //return new RomanNumeral(lexicalForm) ;
            
            // This means it will be the same as an integer elsewhere
            return RomanNumeral.r2i( lexicalForm );
        } catch (NumberFormatException ex)
        {
            throw new DatatypeFormatException(lexicalForm, this, ex.getMessage()) ;
        }
    }

//    public Object cannonicalise( Object value )
//    {
//        return value;
//    }
    
    @Override
    public String toString() { return "Roman Numeral" ; }
}
