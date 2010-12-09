/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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