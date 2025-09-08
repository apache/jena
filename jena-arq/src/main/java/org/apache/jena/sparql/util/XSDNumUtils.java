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

package org.apache.jena.sparql.util;

import java.math.BigDecimal;

public class XSDNumUtils {

    /**
     * Parse an XSD double lexical form.
     * Adds in the cases not covered by {@link Double#parseDouble}.
     * {@code INF} is strictly upper case, but we accept lower case.
     * {@code -NaN} and {@code +NaN} are not accepted.
     */
    public static double xsdParseDouble(String lexicalForm) {
        // Generalized the lexical space. Java's Double.parseDouble does not cover everything.
        return switch(lexicalForm ) {
            case "INF",  "inf"  -> Double.POSITIVE_INFINITY;
            case "+INF", "+inf" -> Double.POSITIVE_INFINITY;
            case "-INF", "-inf" -> Double.NEGATIVE_INFINITY;
            case "NaN"          -> Double.NaN ;
            // Acceptable as Java doubles (value is "NaN" but not as xsd:double
            case "-NaN"-> throw new NumberFormatException("-NaN is not valid as an xsd:double");
            case "+NaN"-> throw new NumberFormatException("+NaN is not valid as an xsd:double");
            // Includes +0 and -0.
            default-> Double.parseDouble(lexicalForm);
        };
    }

    /**
     * Parse an XSD float lexical form.
     * Adds in the cases not covered by {@link Float#parseFloat}.
     * {@code INF} is strictly upper case, but we accept lower case.
     * {@code -NaN} and {@code +NaN} are not accepted.
     */
    public static float xsdParseFloat(String lexicalForm) {
        // Generalized the lexical space. Java's Float.parseFloat does not cover everything.
        return switch(lexicalForm ) {
            case "INF",  "inf"  -> Float.POSITIVE_INFINITY;
            case "+INF", "+inf" -> Float.POSITIVE_INFINITY;
            case "-INF", "-inf" -> Float.NEGATIVE_INFINITY;
            case "NaN"          -> Float.NaN ;
            // Acceptable as Java floats (value is "NaN" but not as xsd:float
            case "-NaN"-> throw new NumberFormatException("-NaN is not valid as an xsd:float");
            case "+NaN"-> throw new NumberFormatException("+NaN is not valid as an xsd:float");
            // Includes +0 and -0.
            default-> Float.parseFloat(lexicalForm);
        };
    }

    /** Parse an XSD decimal. */
    public static BigDecimal xsdParseDecimal(String lexicalForm) {
        return new BigDecimal(lexicalForm);
    }

    /**
     * Produce a lexical form for {@link BigDecimal} that is compatible with Turtle
     * syntax (i.e it has a decimal point). This is also the function used by TDB2 to
     * turn decimal NodeId values into the lexical form of an xsd:decimal.
     */
    public static String stringForm(BigDecimal decimal) {
        return XSDNumUtils.canonicalDecimalStrWithDot(decimal);
    }

    public static String stringForm(double d) {
        if ( Double.isInfinite(d) ) {
            if ( d < 0 )
                return "-INF" ;
            return "INF" ;
        }

        if ( Double.isNaN(d) )
            return "NaN" ;

        // Otherwise, SPARQL form always has exponent.
        String x = Double.toString(d) ;
        if ( (x.indexOf('e') != -1) || (x.indexOf('E') != -1) )
            return x ;
        // Must be 'e' to agree with TDB2 previous behaviour.
        return x + "e0" ;
    }

    public static String stringForm(float f) {
        if ( Float.isInfinite(f) ) {
            if ( f < 0 )
                return "-INF" ;
            return "INF" ;
        }

        if ( Float.isNaN(f) )
            return "NaN" ;

        // No SPARQL short form
        String x = Float.toString(f) ;
        return x;
    }

    /**
     * The format of {@code xsd:decimal} used in ARQ expression evaluation. This is
     * XSD 1.0 for long-term consistency (integer values for {@code xsd:decimal} have
     * ".0").
     */
    public static String stringFormatARQ(BigDecimal bd) {
        return canonicalDecimalStrWithDot(bd);
    }


    /**
     * Strict XSD 1.0 format for {@code xsd:decimal}.
     * <p>
     * Decimal canonical form where integer values has a ".0" (as in XSD 1.0).
     * <p>
     * In XSD 1.0, canonical integer-valued decimal has a trailing ".0".
     * In XSD 1.1 and F&amp;O v 3.1, xs:string cast of a decimal which is integer valued, does
     * not have the trailing ".0".
     */
    public static String stringFormatXSD10(BigDecimal bd) {
        return canonicalDecimalStrWithDot(bd);
    }

    /**
     * Strict XSD 1.1 format for {@code xsd:decimal}.
     * <p>
     * Decimal canonical form where integer values has no ".0" (as in XSD 1.1).
     * <p>
     * In XSD 1.0, canonical integer-valued decimal has a trailing ".0".
     * In XSD 1.1 and F&amp;O v 3.1, xs:string cast of a decimal which is integer valued, does
     * not have the trailing ".0".
     */
    public static String stringFormatXSD11(BigDecimal bd) {
        return canonicalDecimalStrNoIntegerDot(bd);
    }

    /**
     * Decimal format, cast-to-string.
     * <p>
     * Decimal canonical form where integer values have no ".0" (as in XSD 1.1).
     * <p>
     * In XSD 1.1, canonical integer-valued decimal has a trailing ".0".
     * In F&amp;O v 3.1, xs:string cast of a decimal which is integer valued, does
     * not have the trailing ".0".
     */
    public static String canonicalDecimalStrNoIntegerDot(BigDecimal bd) {
        if ( bd.signum() == 0 )
            return "0";
        if ( bd.scale() <= 0 )
            // No decimal part.
            return bd.toPlainString();
        return bd.stripTrailingZeros().toPlainString();
    }

    /**
     * Integer-valued decimals have a trailing ".0".
     * (In XML Schema Datatype 1.1 they did not have a ".0".)
     * <p>
     */
    public static String canonicalDecimalStrWithDot(BigDecimal decimal) {
        if ( decimal.signum() == 0 )        // -1,0,1
            return "0.0";
        if ( decimal.scale() <= 0 )
            // No decimal part.
            return decimal.toPlainString()+".0";
        String str = decimal.stripTrailingZeros().toPlainString();
        // Maybe the decimal part was only zero.
        int dotIdx = str.indexOf('.') ;
        if ( dotIdx < 0 )
            // No DOT.
            str = str + ".0";
        return str;
    }

    /**
     * Return a canonical decimal with a trailing ".0".
     * This is canonicalizing the value/scale.
     * <p>
     * This is the {@link BigDecimal} form used to encode into NodeIds in TDB2.
     * <p>
     * It has a trailing ".0" for integer values so it is Turtle compatible, but
     * otherwise has no trailing zeros.
     * <p>
     * For TDB2, we require a consistent, fixed value/scale form for any value to be
     * encoded in a TDB2 NodeId and when reconstructed to get the same lexical form.
     *
     * @see BigDecimal
     */
    public static BigDecimal canonicalDecimal(BigDecimal decimal) {
        BigDecimal result = decimal;
        if (result.scale() > 1) {
            result = decimal.stripTrailingZeros();
        }
        if (result.scale() <= 0) {
            result = result.setScale(1);
        }
        return result;
    }
}
