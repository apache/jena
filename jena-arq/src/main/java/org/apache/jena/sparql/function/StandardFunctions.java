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

package org.apache.jena.sparql.function;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.function.library.* ;
import org.apache.jena.sparql.function.library.sqrt ;
import org.apache.jena.sparql.function.library.leviathan.* ;

/** Standard function library. */

public class StandardFunctions
{
    /* JENA-508
     * Missing: (July 2016)
     * 
     *   fn:format-dateTime
     *   fn:format-date
     *   fn:format-time
     * 
     * and adapters to SPARQL operations that have keywords:
     *   fn:replace
     *   fn:matches
     * and sparql:* for all the SPARQL builtins. 
     */
    
    /* Implementation notes
     *   fn:format-dateTime / fn:format-time / fn:format-date
     *   This is not Java's SimpleDateFormat.
     *   It has its own picture syntax.
     *     Like adjust-* we may need only one function. 
     */
    
    public static void loadStdDefs(FunctionRegistry registry) {
        String xfn = ARQConstants.fnPrefix ;
        String math = ARQConstants.mathPrefix ;
        String sparqlfn = ARQConstants.fnSparql ;
        
        // Update documentation in xsd-support.md
        
        // See also:
        // http://www.w3.org/TR/xpath-datamodel/#types-hierarchy
        // https://www.w3.org/TR/xpath-datamodel-3/
        
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
        
        addCastXSD(registry, XSDDatatype.XSDboolean) ;
        addCastXSD(registry, XSDDatatype.XSDduration) ;
        addCastXSD(registry, XSDDatatype.XSDdayTimeDuration) ;
        addCastXSD(registry, XSDDatatype.XSDyearMonthDuration) ;
        addCastXSD(registry, XSDDatatype.XSDstring) ;
        addCastXSD(registry, XSDDatatype.XSDanyURI) ;
        
        addCastTemporal(registry, XSDDatatype.XSDdateTime) ;
        addCastTemporal(registry, XSDDatatype.XSDdate) ;
        addCastTemporal(registry, XSDDatatype.XSDtime) ;
        addCastTemporal(registry, XSDDatatype.XSDgYear) ;
        addCastTemporal(registry, XSDDatatype.XSDgYearMonth) ;
        addCastTemporal(registry, XSDDatatype.XSDgMonth) ;
        addCastTemporal(registry, XSDDatatype.XSDgMonthDay) ;
        addCastTemporal(registry, XSDDatatype.XSDgDay) ;

        // Using ARQ prefix http://jena.apache.org/ARQ/function#
        add(registry, ARQConstants.ARQFunctionLibraryURI+"collation",        collation.class) ;

        //TODO op:numeric-greater-than etc.
        //TODO sparql:* for all the SPARQL builtins.
        
        // Sections refer to XQ/XP Expression 3.1
        // https://www.w3.org/TR/xpath-functions-3/

        // 3.1.1 fn:error
        add(registry, xfn+"error",         FN_Error.class) ;

        
//      5.4.1 fn:concat
//      5.4.3 fn:substring
//      5.4.4 fn:string-length
//      5.4.5 fn:normalize-space
//      5.4.6 fn:normalize-unicode
//      5.4.7 fn:upper-case
//      5.4.8 fn:lower-case
//      5.5.1 fn:contains
//      5.5.2 fn:starts-with
//      5.5.3 fn:ends-with
//      5.5.4 fn:substring-before
//      5.5.5 fn:substring-after
        
        //add(registry, xfn+"string-join",   FN_StrJoin.class) ;    // Works fn:string-join works on a sequence.
        add(registry, xfn+"concat",         FN_StrConcat.class) ;
        add(registry, xfn+"substring",      FN_StrSubstring.class) ;
        add(registry, xfn+"string-length",  FN_StrLength.class) ;
        // fn:normalize-space
        add(registry,xfn+"normalize-space", FN_StrNormalizeSpace.class);
        // fn:normalize-unicode
        add(registry,xfn+"normalize-unicode", FN_StrNormalizeUnicode.class);

        add(registry, xfn+"upper-case",     FN_StrUpperCase.class) ;
        add(registry, xfn+"lower-case",     FN_StrLowerCase.class) ;
        add(registry, xfn+"contains",       FN_StrContains.class) ;
        add(registry, xfn+"starts-with",    FN_StrStartsWith.class) ;
        add(registry, xfn+"ends-with",      FN_StrEndsWith.class) ;

        add(registry, xfn+"substring-before",   FN_StrBefore.class) ;
        add(registry, xfn+"substring-after",    FN_StrAfter.class) ;

//      5.6.2 fn:matches
//      5.6.3 fn:replace c.f. SPARQL REPLACE.
        add(registry, xfn+"matches",        FN_Matches.class) ;
        add(registry, xfn+"replace",        FN_StrReplace.class) ;

//      Not 5.6.4 fn:tokenize - returns a sequence.
        
//        4.7.2 fn:format-number
        add(registry, xfn+"format-number",  FN_FormatNumber.class) ;

//        4.4.1 fn:abs
//        4.4.2 fn:ceiling
//        4.4.3 fn:floor
//        4.4.4 fn:round
//        4.4.5 fn:round-half-to-even
        add(registry, xfn+"abs",            FN_Abs.class) ;
        add(registry, xfn+"ceiling",        FN_Ceiling.class) ;
        add(registry, xfn+"floor",          FN_Floor.class) ;
        add(registry, xfn+"round",          FN_Round.class) ;
        add(registry, xfn+"round-half-to-even",          FN_Round_Half_Even.class) ;
//        6.1 fn:resolve-uri        -- Two argument form makes sense.
//        6.2 fn:encode-for-uri
//        6.3 fn:iri-to-uri         -- meaningless in SPARQL.
//        6.4 fn:escape-html-uri
        
        add(registry, xfn+"encode-for-uri", FN_StrEncodeForURI.class) ;

        add(registry, xfn+"dateTime",               FN_DateTime.class) ;
        add(registry, xfn+"year-from-dateTime",     FN_YearFromDateTime.class) ;
        add(registry, xfn+"month-from-dateTime",    FN_MonthFromDateTime.class) ;
        add(registry, xfn+"day-from-dateTime",      FN_DayFromDateTime.class) ;
        add(registry, xfn+"hours-from-dateTime",    FN_HoursFromDateTime.class) ;
        add(registry, xfn+"minutes-from-dateTime",  FN_MinutesFromDateTime.class) ;
        add(registry, xfn+"seconds-from-dateTime",  FN_SecondsFromDateTime.class) ;
        add(registry, xfn+"timezone-from-dateTime", FN_TimezoneFromDateTime.class) ;

        add(registry, xfn+"years-from-duration",    FN_YearsFromDuration.class) ;
        add(registry, xfn+"months-from-duration",   FN_MonthsFromDuration.class) ;
        add(registry, xfn+"days-from-duration",     FN_DaysFromDuration.class) ;
        add(registry, xfn+"hours-from-duration",    FN_HoursFromDuration.class) ;
        add(registry, xfn+"minutes-from-duration",  FN_MinutesFromDuration.class) ;
        add(registry, xfn+"seconds-from-duration",  FN_SecondsFromDuration.class) ;
        
//      7.3.1 fn:boolean
//      7.3.2 fn:not
      add(registry, xfn+"boolean",        FN_BEV.class) ;
      add(registry, xfn+"not",            FN_Not.class) ;
        
        // XQ/XP 3.
//        9.6.1 fn:adjust-dateTime-to-timezone
        add(registry, xfn+"adjust-dateTime-to-timezone",  FN_AdjustDatetimeToTimezone.class) ;
//        9.6.2 fn:adjust-date-to-timezone
        add(registry, xfn+"adjust-date-to-timezone",  FN_AdjustDatetimeToTimezone.class) ;
//        9.6.3 fn:adjust-time-to-timezone
        add(registry, xfn+"adjust-time-to-timezone",  FN_AdjustDatetimeToTimezone.class) ;
//        9.8.1 fn:format-dateTime
//        9.8.2 fn:format-date
//        9.8.3 fn:format-time
        
        // math:
//        4.8 Trigonometric and exponential functions
//        4.8.1 math:pi
//        4.8.2 math:exp
//        4.8.3 math:exp10
//        4.8.4 math:log
//        4.8.5 math:log10
//        4.8.6 math:pow
//        4.8.7 math:sqrt
//        4.8.8 math:sin
//        4.8.9 math:cos
//        4.8.10 math:tan
//        4.8.11 math:asin
//        4.8.12 math:acos
//        4.8.13 math:atan
//        4.8.14 math:atan2
        
        // check.
        add(registry, math+"pi",        pi.class) ;        
        add(registry, math+"exp",       Math_exp.class) ;      // -> XSDFuncOp
        add(registry, math+"exp10",     Math_exp10.class) ;    // -> XSDFuncOp
        // Levianthan "log" is a function which takes one or two arguments.  
        add(registry, math+"log",       Math_log.class) ;      // -> XSDFuncOp   ln.class?
        add(registry, math+"log10",     Math_log10.class) ;    // -> XSDFuncOp                 // - rename

        // math_pow : integer preserving, otherwise doubles. 
        add(registry, math+"pow",       Math_pow.class) ;      // -> XSDFuncOp
        add(registry, math+"sqrt",      sqrt.class) ;
    
        // From leviathan, with math: naming.
        add(registry, math+"sin",       sin.class) ;
        add(registry, math+"cos",       cos.class) ;
        add(registry, math+"tan",       tan.class) ;
        add(registry, math+"asin",      sin1.class) ;
        add(registry, math+"acos",      cos1.class) ;
        add(registry, math+"atan",      tan1.class) ;
        
        add(registry, math+"atan2",     Math_atan2.class) ;
        
        // F&O 3.1
        add(registry, xfn+"apply",           FN_Apply.class);
        add(registry, xfn+"collation-key",   FN_CollationKey.class);

        
        // And add op:'s
//        4.2.1 op:numeric-add
//        4.2.2 op:numeric-subtract
//        4.2.3 op:numeric-multiply
//        4.2.4 op:numeric-divide
//        4.2.5 op:numeric-integer-divide
//        4.2.6 op:numeric-mod
//        4.2.7 op:numeric-unary-plus
//        4.2.8 op:numeric-unary-minus
//        4.3.1 op:numeric-equal
//        4.3.2 op:numeric-less-than
//        4.3.3 op:numeric-greater-than
//        7.2.1 op:boolean-equal
//        7.2.2 op:boolean-less-than
//        7.2.3 op:boolean-greater-than
//        8.2.1 op:yearMonthDuration-less-than
//        8.2.2 op:yearMonthDuration-greater-than
//        8.2.3 op:dayTimeDuration-less-than
//        8.2.4 op:dayTimeDuration-greater-than
//        8.2.5 op:duration-equal
//        8.4.1 op:add-yearMonthDurations
//        8.4.2 op:subtract-yearMonthDurations
//        8.4.3 op:multiply-yearMonthDuration
//        8.4.4 op:divide-yearMonthDuration
//        8.4.5 op:divide-yearMonthDuration-by-yearMonthDuration
//        8.4.6 op:add-dayTimeDurations
//        8.4.7 op:subtract-dayTimeDurations
//        8.4.8 op:multiply-dayTimeDuration
//        8.4.9 op:divide-dayTimeDuration
//        8.4.10 op:divide-dayTimeDuration-by-dayTimeDuration
//        9.4.1 op:dateTime-equal
//        9.4.2 op:dateTime-less-than
//        9.4.3 op:dateTime-greater-than
//        9.4.4 op:date-equal
//        9.4.5 op:date-less-than
//        9.4.6 op:date-greater-than
//        9.4.7 op:time-equal
//        9.4.8 op:time-less-than
//        9.4.9 op:time-greater-than
//        9.4.10 op:gYearMonth-equal
//        9.4.11 op:gYear-equal
//        9.4.12 op:gMonthDay-equal
//        9.4.13 op:gMonth-equal
//        9.4.14 op:gDay-equal
//        9.7.2 op:subtract-dateTimes
//        9.7.3 op:subtract-dates
//        9.7.4 op:subtract-times
//        9.7.5 op:add-yearMonthDuration-to-dateTime
//        9.7.6 op:add-dayTimeDuration-to-dateTime
//        9.7.7 op:subtract-yearMonthDuration-from-dateTime
//        9.7.8 op:subtract-dayTimeDuration-from-dateTime
//        9.7.9 op:add-yearMonthDuration-to-date
//        9.7.10 op:add-dayTimeDuration-to-date
//        9.7.11 op:subtract-yearMonthDuration-from-date
//        9.7.12 op:subtract-dayTimeDuration-from-date
//        9.7.13 op:add-dayTimeDuration-to-time
//        9.7.14 op:subtract-dayTimeDuration-from-time
    }
    
    private static void addCastXSD(FunctionRegistry registry, XSDDatatype dt) {
        registry.put(dt.getURI(), new CastXSD(dt)) ;
    }

    private static void addCastNumeric(FunctionRegistry registry, XSDDatatype dt) {
        registry.put(dt.getURI(), new CastXSD(dt)) ;
    }

    private static void addCastTemporal(FunctionRegistry registry, XSDDatatype dt) {
        registry.put(dt.getURI(), new CastXSD(dt)) ;
    }

    private static void add(FunctionRegistry registry, String uri, Class<? > funcClass) {
        registry.put(uri, funcClass) ;
    }
}
