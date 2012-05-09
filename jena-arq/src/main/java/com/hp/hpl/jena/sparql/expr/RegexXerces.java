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

package com.hp.hpl.jena.sparql.expr;

import org.apache.xerces.impl.xpath.regex.ParseException ;
import org.apache.xerces.impl.xpath.regex.RegularExpression ;

public class RegexXerces implements RegexEngine
{
    RegularExpression regexPattern ;

    public RegexXerces(String pattern, String flags)
    {
        regexPattern = makePattern(pattern, flags) ;
    }
    
    @Override
    public boolean match(String s)
    {
        return regexPattern.matches(s) ;
    }
    
    private RegularExpression makePattern(String patternStr, String flags)
    {
        // Input : only  m s i x
        // Check/modify flags.
        // Always "u", never patternStr
        //x: Remove whitespace characters (#x9, #xA, #xD and #x20) unless in [] classes
        try { return new RegularExpression(patternStr, flags) ; }
        catch (ParseException pEx)
        { throw new ExprException("Regex: Pattern exception: "+pEx) ; }
    }
}
