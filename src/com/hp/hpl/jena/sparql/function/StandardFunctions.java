/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.function.library.*;

/** Standard function library.
 * 
 * @author Andy Seaborne
 */

public class StandardFunctions
{
    public static void loadStdDefs(FunctionRegistry registry)
    {
        String xfn = ARQConstants.fnPrefix ;
        
        // See http://www.w3.org/TR/xpath-datamodel/#types-hierarchy
        // No durations here
        
        addCast(registry, XSDDatatype.XSDdecimal) ;
        addCast(registry, XSDDatatype.XSDinteger) ;

        addCast(registry, XSDDatatype.XSDlong) ;
        addCast(registry, XSDDatatype.XSDint) ;
        addCast(registry, XSDDatatype.XSDshort) ;
        addCast(registry, XSDDatatype.XSDbyte) ;
        
        addCast(registry, XSDDatatype.XSDnonPositiveInteger) ;
        addCast(registry, XSDDatatype.XSDnegativeInteger) ;

        addCast(registry, XSDDatatype.XSDnonNegativeInteger) ;
        addCast(registry, XSDDatatype.XSDpositiveInteger) ;
        addCast(registry, XSDDatatype.XSDunsignedLong) ;
        addCast(registry, XSDDatatype.XSDunsignedInt) ;
        addCast(registry, XSDDatatype.XSDunsignedShort) ;

        addCast(registry, XSDDatatype.XSDdouble) ;
        addCast(registry, XSDDatatype.XSDfloat) ;
        
        addCast(registry, XSDDatatype.XSDduration) ;
        
        addCast(registry, XSDDatatype.XSDboolean) ;
        addCast(registry, XSDDatatype.XSDstring) ;

        addCast(registry, XSDDatatype.XSDanyURI) ;
        
        // Specialzed casting rules
        addCastDT(registry, XSDDatatype.XSDdateTime) ;
        addCastDT(registry, XSDDatatype.XSDdate) ;
        addCastDT(registry, XSDDatatype.XSDgYear) ;
        addCastDT(registry, XSDDatatype.XSDgYearMonth) ;
        addCastDT(registry, XSDDatatype.XSDgMonth) ;
        addCastDT(registry, XSDDatatype.XSDgMonthDay) ;
        addCastDT(registry, XSDDatatype.XSDgDay) ;

        //TODO op:numeric-greater-than etc.
        
        add(registry, xfn+"boolean", BEV.class) ;
        add(registry, xfn+"not",     not.class) ;

        add(registry, xfn+"matches",       matches.class) ;
        add(registry, xfn+"string-length", strLength.class) ;
        add(registry, xfn+"string-join",   strConcat.class) ;   // Misnamed.
        add(registry, xfn+"concat",        strConcat.class) ;
        add(registry, xfn+"substring",     strSubstring.class) ;
        add(registry, xfn+"starts-with",   strStartsWith.class) ;
        
        add(registry, xfn+"lower-case",   strLowerCase.class) ;
        add(registry, xfn+"upper-case",   strUpperCase.class) ;
        
        add(registry, xfn+"contains",      strContains.class) ;
        add(registry, xfn+"ends-with",     strEndsWith.class) ;
        
        add(registry, xfn+"abs", abs.class) ;
        add(registry, xfn+"ceiling", ceiling.class) ;
        add(registry, xfn+"floor", floor.class) ;
        add(registry, xfn+"round", round.class) ;
        
        // fn:compare/2 and /3 and provide collation argument
        //    Locale locale = new Locale(String language, String country)
        //      language is two letter lower case, county is uppercase.
        //        http://www.loc.gov/standards/iso639-2/englangn.html
        //      Check in Locale.getISOCountries()
        //        http://www.iso.ch/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/list-en1.html
        //      Check in Locale.getISOLanguages
        //    Collator.getInstance(Locale)
        // fn:current-date() as xs:date = xs:date(fn:current-dateTime()).
        // fn:current-dateTime as xs:dateTime
        // fn:current-time() as xs:time
        
        // WRONG: fn:max/fn:min are aggregate functions that take a sequence
        //add(registry, xfn+"max", max.class) ;
        //add(registry, xfn+"min", min.class) ;
    }
    
    private static void addCast(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD(dt) ) ;
    }

    private static void addCastDT(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD_DT(dt) ) ;
    }

    private static void add(FunctionRegistry registry, String uri, Class<?> funcClass)
    {
        registry.put(uri, funcClass) ;
    }

}


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
