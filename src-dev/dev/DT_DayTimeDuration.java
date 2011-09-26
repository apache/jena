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

package dev;

import com.hp.hpl.jena.datatypes.BaseDatatype ;
import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration ;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDDurationType ;

public class DT_DayTimeDuration extends BaseDatatype
{
    // Avoid excessive reparsing.
    
    // Singleton
    private static DT_DayTimeDuration datatype = new DT_DayTimeDuration() ; 
    public static DT_DayTimeDuration get() { return datatype ; }
    
    private static XSDDurationType xsdDurationType= new XSDDurationType() ;
    
    private static final String URI = XSDDatatype.XSD+"#dayTimeDuration" ;

    private DT_DayTimeDuration()
    {
        super(URI) ;
    }
    
    @Override
    public Class<?> getJavaClass() { return DT_DayTimeDuration.class ; }
    
    @Override
    public String unparse(Object value)
    {
        return value.toString() ; 
    }
    
    // The value space of an  xsd:dayTimeDuration is fractional seconds.
    // But it is also a derived type of Duration, whch as a value space of 
    //  a 6-dimensional dateTime vector.
    
    // -?P(nD)(T(nH)(nM)(nS))
    
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException
    {
        if ( ! XSDDatatype.XSDduration.isValid(lexicalForm) )
            throw new DatatypeFormatException(lexicalForm, this, "Invalid xsd:dayTimeDuration") ;
        
        try {
            Object value = xsdDurationType.parse(lexicalForm) ;
            XSDDuration duration = (XSDDuration)value ;
            if ( duration.getYears() != 0 || duration.getMonths() != 0 )
                throw new DatatypeFormatException(lexicalForm, this, "Invalid xsd:dayTimeDuration") ;
            return duration ;
        } catch (Exception ex)
        {
            throw new DatatypeFormatException(lexicalForm, this, ex.getMessage()) ;
        }
    }

//    public Object cannonicalise( Object value )
//    {
//        return value;
//    }
    
    @Override
    public String toString() { return "xsd:dayTimeDuration" ; }

}
