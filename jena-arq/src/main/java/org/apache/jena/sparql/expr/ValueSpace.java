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

import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.NodeUtils;

/**
 * Value spaces. This covers the classification in the from the SPARQL specification
 * and, for literals, the value spaces of the primitive datatypes of XSD.
 * <p>
 * The {@link #comparisonOrder()} method it the sort order (comparison space), before
 * considering value and RDF term comparisons. This covers the classification in the
 * from the SPARQL specification and the effective value spaces of the primitive
 * datatypes of XSD (e.g. there comparison across atomic type values spaces such as
 * {@code xsd:dateTime} and {xsd:date}). All numbers are one comparison space.
 * <p>
 * <a href="https://www.w3.org/TR/sparql11-query/#modOrderBy">The SPARQL
 * specification section 15.1</a>
 * <ol>
 * <li>(Lowest) no value assigned to the variable or expression in this solution.
 * <li>Blank nodes
 * <li>IRIs
 * <li>RDF literals See also <a href="https://www.w3.org/TR/xpath-functions/">XPath
 * and XQuery Functions and Operators</a>.
 * <p>
 * Do not use the JDK provided {@link Enum#compareTo} to order value spaces for
 * SPARQL sorting and comparision.
 */

public enum ValueSpace {
    // We could use the enum ordinal but that forces the order in this file.

    // The SPARQL spec
//    (Lowest) no value assigned to the variable or expression in this solution.
//    Blank nodes
//    IRIs
//    RDF literals

    VSPACE_UNDEF(10),
    //VSPACE_NODE(11),
    VSPACE_BLANKNODE(12),
    VSPACE_URI(13),
    VSPACE_VARIABLE(14),

    VSPACE_STRING(50),
    VSPACE_LANG(55),

    // Literals, XSD -- "<" order within values space and other comparison covered by
    VSPACE_NUM(110),

//    VSPACE_INTEGER(110),
//    VSPACE_DECIMAL(110),
//    VSPACE_FLOAT(110),
//    VSPACE_DOUBLE(110),

    VSPACE_BOOLEAN(120),

    // As values, these do not compare ?? check!!!
    VSPACE_DATETIME(130),
    VSPACE_DATE(131),

    VSPACE_TIME(135),

    VSPACE_DURATION(160),

    // Determine the type of duration dynamically (looking at the lexical form).
//    VSPACE_DURATION_YEARMONTH(160),
//    VSPACE_DURATION_DAYTIME(170),

    // XSD primitives
//    VSPACE_DATETIME(130),
//    VSPACE_DATE(140),
//    VSPACE_TIME(150),
//
//    VSPACE_G_YEAR(200),
//    VSPACE_G_YEARMONTH(210),
//    VSPACE_G_MONTHDAY(220),
//    VSPACE_G_MONTH(230),
//    VSPACE_G_DAY(240),

    VSPACE_SORTKEY(900),
    VSPACE_QUOTED_TRIPLE(999),      // RDF-star : Last recognized value space.

    // Unknown RDF term value space.
    VSPACE_UNKNOWN(6000),
    // Known values spaces but different.
    VSPACE_DIFFERENT(9999)
    ;

    private final int comparisonOrder;

    ValueSpace(int orderIndex) {
        this.comparisonOrder = orderIndex;
    }

    public int comparisonOrder() { return comparisonOrder; }

    /** This is not {@link Enum#compareTo}. */
    public static int comparisonOrder(ValueSpace vs1, ValueSpace vs2) {
        if ( vs1 == VSPACE_UNKNOWN && vs2 != VSPACE_UNKNOWN )
            return Expr.CMP_INDETERMINATE;
        return Integer.compare(vs1.comparisonOrder, vs2.comparisonOrder);
    }

//    public static ValueSpace valueSpace(NodeValue nv) {
//        // XXX Check!
//        if ( nv == null ) return VSPACE_UNDEF;
//        if ( nv.isNumber() )        return VSPACE_NUM ;
//        if ( nv.isDateTime() )      return VSPACE_DATETIME ;
//        if ( nv.isString())         return VSPACE_STRING ;
//        if ( nv.isBoolean())        return VSPACE_BOOLEAN ;
//        if ( nv.isTripleTerm())     return VSPACE_TRIPLE_TERM ;
//        if ( ! nv.isLiteral() )     return VSPACE_NODE ;
//
//        if ( ! SystemARQ.ValueExtensions )
//            return VSPACE_UNKNOWN ;
//
//        // Datatypes and their value spaces that are an extension of minimal SPARQL 1.1
//        if ( nv.isDate() )          return VSPACE_DATE ;
//        if ( nv.isTime() )          return VSPACE_TIME ;
//        if ( nv.isDuration() )      return VSPACE_DURATION ;
//
//        if ( nv.isGYear() )         return VSPACE_G_YEAR ;
//        if ( nv.isGYearMonth() )    return VSPACE_G_YEARMONTH ;
//        if ( nv.isGMonth() )        return VSPACE_G_MONTH ;
//        if ( nv.isGMonthDay() )     return VSPACE_G_MONTHDAY ;
//        if ( nv.isGDay() )          return VSPACE_G_DAY ;
//
//        if ( nv.isSortKey() )       return VSPACE_SORTKEY ;
//
//        // Forces to node so put after the possibly value-only cases
//        if ( nv.isBlank() ) return VSPACE_BLANKNODE;
//        if ( nv.isIRI() ) return VSPACE_URI;
//        if ( nv.isVariable() ) return VSPACE_VARIABLE;
//
//        if ( NodeUtils.hasLang(nv.asNode()) )
//            return VSPACE_LANG ;
//        return VSPACE_UNKNOWN ;
//    }

    public static ValueSpace valueSpace(NodeValue nv) {
        // XXX Check!
        // Maybe code getValueSpace into type hierarchy.
        if ( nv == null ) return VSPACE_UNDEF;
        if ( nv.isNumber() )        return VSPACE_NUM ;
        if ( nv.isDateTime() )      return VSPACE_DATETIME ;
        if ( nv.isString())         return VSPACE_STRING ;
        if ( nv.isLangString())     return VSPACE_LANG ;
        if ( nv.isBoolean())        return VSPACE_BOOLEAN ;

        //if ( ! nv.isLiteral() )     return VSPACE_NODE ;

        if ( ! SystemARQ.ValueExtensions )
            return VSPACE_UNKNOWN ;

        // Datatypes and their value spaces that are an extension of minimal SPARQL 1.1
        if ( nv.isDate() )          return VSPACE_DATE ;
        if ( nv.isTime() )          return VSPACE_TIME ;

        // These compare (sort) via their implied dateTime.
        if ( nv.isGYear() )         return VSPACE_DATETIME ;
        if ( nv.isGYearMonth() )    return VSPACE_DATETIME ;
        if ( nv.isGMonth() )        return VSPACE_DATETIME ;
        if ( nv.isGMonthDay() )     return VSPACE_DATETIME ;
        if ( nv.isGDay() )          return VSPACE_DATETIME ;

        // Dynamically classify yearMonth and dateTime,
        if ( nv.isDuration() ) return VSPACE_DURATION;
//        if ( nv.isDayTimeDuration() ) return VSPACE_DURATION_DAYTIME;
//        if ( nv.isYearMonthDuration() ) return VSPACE_DURATION_YEARMONTH;

        if ( nv.isSortKey() )       return VSPACE_SORTKEY ;

        //if ( nv.isLiteral() )       return VSPACE_UNKNOWN ;

        if ( nv.isBlank() )         return VSPACE_BLANKNODE;
        if ( nv.isIRI() )           return VSPACE_URI;
        if ( nv.isVariable() )      return VSPACE_VARIABLE;
        if ( nv.isTripleTerm())     return VSPACE_QUOTED_TRIPLE ;

        if ( NodeUtils.hasLang(nv.asNode()) )
            return VSPACE_LANG ;
        //Includes unrecognized datatypes.
        return VSPACE_UNKNOWN ;
    }

}
