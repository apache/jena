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

package com.hp.hpl.jena.sparql.function;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.library.* ;

/** Standard function library. */

public class StandardFunctions
{
    public static void loadStdDefs(FunctionRegistry registry)
    {
        String xfn = ARQConstants.fnPrefix ;
        
        String sparqlfn = "http://www.w3.org/sparqfn/" ;    // Not fixed yet.
        
        // See http://www.w3.org/TR/xpath-datamodel/#types-hierarchy
        // No durations here
        
        addCastNumeric(registry, XSDDatatype.XSDdecimal) ;
        addCastNumeric(registry, XSDDatatype.XSDinteger) ;

        addCastNumeric(registry, XSDDatatype.XSDlong) ;
        addCastNumeric(registry, XSDDatatype.XSDint) ;
        addCastNumeric(registry, XSDDatatype.XSDshort) ;
        addCastNumeric(registry, XSDDatatype.XSDbyte) ;
        
        addCastNumeric(registry, XSDDatatype.XSDnonPositiveInteger) ;
        addCastNumeric(registry, XSDDatatype.XSDnegativeInteger) ;

        addCastNumeric(registry, XSDDatatype.XSDnonNegativeInteger) ;
        addCastNumeric(registry, XSDDatatype.XSDpositiveInteger) ;
        addCastNumeric(registry, XSDDatatype.XSDunsignedLong) ;
        addCastNumeric(registry, XSDDatatype.XSDunsignedInt) ;
        addCastNumeric(registry, XSDDatatype.XSDunsignedShort) ;

        addCastNumeric(registry, XSDDatatype.XSDdouble) ;
        addCastNumeric(registry, XSDDatatype.XSDfloat) ;
        
        addCastBoolean(registry, XSDDatatype.XSDboolean) ;

        addCast(registry, XSDDatatype.XSDduration) ;
        addCast(registry, XSDDatatype.XSDstring) ;
        addCast(registry, XSDDatatype.XSDanyURI) ;
        
        // Specialized casting rules
        addCastDT(registry, XSDDatatype.XSDdateTime) ;
        addCastDT(registry, XSDDatatype.XSDdate) ;
        addCastDT(registry, XSDDatatype.XSDtime) ;
        addCastDT(registry, XSDDatatype.XSDgYear) ;
        addCastDT(registry, XSDDatatype.XSDgYearMonth) ;
        addCastDT(registry, XSDDatatype.XSDgMonth) ;
        addCastDT(registry, XSDDatatype.XSDgMonthDay) ;
        addCastDT(registry, XSDDatatype.XSDgDay) ;

        //TODO op:numeric-greater-than etc.
        
        add(registry, xfn+"boolean",        FN_BEV.class) ;
        add(registry, xfn+"not",            FN_Not.class) ;

        add(registry, xfn+"matches",        FN_Matches.class) ;
        add(registry, xfn+"string-length",  FN_StrLength.class) ;
        //add(registry, xfn+"string-join",   FN_StrJoin.class) ;    // Works fn:string-join works on a sequence.
        add(registry, xfn+"concat",         FN_StrConcat.class) ;
        add(registry, xfn+"substring",      FN_StrSubstring.class) ;
        add(registry, xfn+"starts-with",    FN_StrStartsWith.class) ;
        
        add(registry, xfn+"lower-case",     FN_StrLowerCase.class) ;
        add(registry, xfn+"upper-case",     FN_StrUpperCase.class) ;
        
        add(registry, xfn+"contains",       FN_StrContains.class) ;
        add(registry, xfn+"ends-with",      FN_StrEndsWith.class) ;
        
        add(registry, xfn+"abs",            FN_Abs.class) ;
        add(registry, xfn+"ceiling",        FN_Ceiling.class) ;
        add(registry, xfn+"floor",          FN_floor.class) ;
        add(registry, xfn+"round",          FN_Round.class) ;
        
        // SPARQL functions.
        // Check the exact URI.

//        add(registry, sparqlfn+"boolean",        FN_BEV.class) ;
//        add(registry, sparqlfn+"not",            FN_Not.class) ;
//
//        add(registry, sparqlfn+"matches",        FN_Matches.class) ;
//        add(registry, sparqlfn+"string-length",  FN_StrLength.class) ;
//        add(registry, sparqlfn+"concat",         FN_StrConcat.class) ;
//        add(registry, sparqlfn+"substring",      FN_StrSubstring.class) ;
//        add(registry, sparqlfn+"starts-with",    FN_StrStartsWith.class) ;
//        
//        add(registry, sparqlfn+"lower-case",     FN_StrLowerCase.class) ;
//        add(registry, sparqlfn+"upper-case",     FN_StrUpperCase.class) ;
//        
//        add(registry, sparqlfn+"contains",       FN_StrContains.class) ;
//        add(registry, sparqlfn+"ends-with",      FN_StrEndsWith.class) ;
//        
//        add(registry, sparqlfn+"abs",            FN_Abs.class) ;
//        add(registry, sparqlfn+"ceiling",        FN_Ceiling.class) ;
//        add(registry, sparqlfn+"floor",          FN_floor.class) ;
//        add(registry, sparqlfn+"round",          FN_Round.class) ;

//        add(registry, sparqlfn+"concat",         FN_Concat.class) ;
//        add(registry, sparqlfn+"upper-case",     FN_UpperCase.class) ;
//        add(registry, sparqlfn+"lower-case",     FN_LowerCase.class) ;
//        add(registry, sparqlfn+"encode-for-uri", FN_EncodeForURI.class) ;
//        add(registry, sparqlfn+"contains",       FN_Contains.class) ;
//
//        add(registry, sparqlfn+"starts-with",    FN_StartsWith.class) ;
//        add(registry, sparqlfn+"ends-with",      FN_EndsWith.class) ;

//        add(registry, xfn+"year-from-dateTime",     FN_YearFromDateTime.class) ;
//        add(registry, xfn+"month-from-dateTime",    FN_MonthFromDateTime.class) ;
//        add(registry, xfn+"day-from-dateTime",      FN_DayFromDateTime.class) ;
//
//        
//        add(registry, xfn+"hours-from-dateTime",    FN_HoursFromDateTime.class) ;
//        add(registry, xfn+"minutes-from-dateTime",  FN_MinutesFromDateTime.class) ;
//        add(registry, xfn+"seconds-from-dateTime",  FN_SecondsFromDateTime.class) ;
        
//        add(registry, xfn+"timezone-from-dateTime",  FN_TimezoneFromDateTime.class) ;
        
        
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
    
    private static void addCastNumeric(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD_Numeric(dt) ) ;
    }

    private static void addCastBoolean(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD_Boolean(dt) ) ;
    }

    private static void addCastDT(FunctionRegistry registry, XSDDatatype dt)
    {
        registry.put(dt.getURI(), new CastXSD_DateTime(dt) ) ;
    }

    private static void add(FunctionRegistry registry, String uri, Class<?> funcClass)
    {
        registry.put(uri, funcClass) ;
    }

}
