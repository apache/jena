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

package org.apache.jena.shex.expressions;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.atlas.lib.InternalErrorException;

public class Cardinality {
    public static final int UNSET = -3;
    public static final int UNBOUNDED = -2;

    public final String image;
    public final int min;
    public final int max;

    public Cardinality(String image, int min, int max) {
        this.image = image;
        this.min = min;
        this.max = max;
    }

    private static Pattern repeatRange = Pattern.compile(".(\\d+)(,(\\d+|\\*)?)?.");

    public static Cardinality create(String image) {
        int min = -1;
        int max = -1;
        switch(image) {
            case "*": min = 0 ; max = UNBOUNDED ; break;
            case "?": min = 0 ; max = 1 ;  break;
            case "+": min = 1 ; max = UNBOUNDED ; break;
            default: {
                Matcher matcher = repeatRange.matcher(image);
                if ( !matcher.matches() )
                    throw new InternalErrorException("ShExC: Unexpected cardinality: '"+image+"'");
                min = integerRange(matcher.group(1), UNSET);
                if ( matcher.groupCount() != 3 )
                    throw new InternalErrorException("ShExC: Unexpected cardinality: '"+image+"'");
                String comma = matcher.group(2);
                if ( comma == null ) {
                    max = min;
                    break;
                }
                // Has a comma, may have something after it.
                max = integerRange(matcher.group(3), UNBOUNDED);
            }
        }
        return new Cardinality(image, min, max);

    }

    private static int integerRange(String str, int dftValue) {
        if ( str == null )
            return dftValue;
        if ( str.equals("*") )
            return UNBOUNDED;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new InternalErrorException("Number format exception");
        }
    }


    static String cardStr(int min, int max) {
        // min is never UNBOUNDED
        // "{,number}" is not legal syntax

        if ( min == UNSET && max == UNSET )
            return "";

        // Special syntax
        if ( min == 0 && max == UNBOUNDED )
            return "*";
        if ( min == 1 && max == UNBOUNDED )
            return "+";
        if ( min == 0 && max == 1 )
            return "?";
        // max == min => no comma.
        if ( max == min )
            return "{"+min+"}";
        // Max UNBOUNDED
        if ( max == UNBOUNDED )
            return "{"+min+",}";
        // General
        return "{"+min+","+max+"}";
    }

    @Override
    public String toString() {
        return cardStr(min, max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, max, min);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        Cardinality other = (Cardinality)obj;
        return Objects.equals(image, other.image) && max == other.max && min == other.min;
    }
}
