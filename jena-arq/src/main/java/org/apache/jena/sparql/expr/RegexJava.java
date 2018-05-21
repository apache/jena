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

package org.apache.jena.sparql.expr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexJava implements RegexEngine
{
    private Pattern regexPattern;

    public RegexJava(String pattern, String flags) {
        regexPattern = makePattern("Regex", pattern, flags);
    }

    @Override
    public boolean match(String s) {
        Matcher m = regexPattern.matcher(s);
        return m.find();
    }

    public static Pattern makePattern(String label, String patternStr, String flags) {
        try {
            int mask = 0;
            if ( flags != null ) {
                mask = makeMask(flags);
                if ( flags.contains("q") )
                    patternStr = Pattern.quote(patternStr);
            }
            return Pattern.compile(patternStr, mask);
        }
        catch (PatternSyntaxException pEx) {
            throw new ExprEvalException(label+" pattern exception: " + pEx);
        }
    }

    public static int makeMask(String modifiers) {
        if ( modifiers == null )
            return 0;

        int newMask = 0;
        for ( int i = 0; i < modifiers.length(); i++ ) {
            switch (modifiers.charAt(i)) {
                case 'i': 
                    newMask |= Pattern.UNICODE_CASE;
                    newMask |= Pattern.CASE_INSENSITIVE;
                    break;
                case 'm': newMask |= Pattern.MULTILINE;           break;
                case 's': newMask |= Pattern.DOTALL;              break;
                // Not suported by Java regex
                //case 'x': newMask |= Pattern.; break;
                case 'q':; break;
                
                default: 
                    throw new ExprEvalException("Unsupported flag in regex modifiers: "+modifiers.charAt(i));
            }
        }
        return newMask;
    }
}
