package org.apache.jena.arq.querybuilder;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Add;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_Call;
import org.apache.jena.sparql.expr.E_Coalesce;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_Datatype;
import org.apache.jena.sparql.expr.E_DateTimeDay;
import org.apache.jena.sparql.expr.E_DateTimeHours;
import org.apache.jena.sparql.expr.E_DateTimeMinutes;
import org.apache.jena.sparql.expr.E_DateTimeMonth;
import org.apache.jena.sparql.expr.E_DateTimeSeconds;
import org.apache.jena.sparql.expr.E_DateTimeTZ;
import org.apache.jena.sparql.expr.E_DateTimeTimezone;
import org.apache.jena.sparql.expr.E_DateTimeYear;
import org.apache.jena.sparql.expr.E_Divide;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_FunctionDynamic;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.E_IsLiteral;
import org.apache.jena.sparql.expr.E_IsNumeric;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_MD5;
import org.apache.jena.sparql.expr.E_Multiply;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_Now;
import org.apache.jena.sparql.expr.E_NumAbs;
import org.apache.jena.sparql.expr.E_NumCeiling;
import org.apache.jena.sparql.expr.E_NumFloor;
import org.apache.jena.sparql.expr.E_NumRound;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_SHA1;
import org.apache.jena.sparql.expr.E_SHA224;
import org.apache.jena.sparql.expr.E_SHA256;
import org.apache.jena.sparql.expr.E_SHA384;
import org.apache.jena.sparql.expr.E_SHA512;
import org.apache.jena.sparql.expr.E_SameTerm;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrAfter;
import org.apache.jena.sparql.expr.E_StrBefore;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrEncodeForURI;
import org.apache.jena.sparql.expr.E_StrEndsWith;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_StrLength;
import org.apache.jena.sparql.expr.E_StrLowerCase;
import org.apache.jena.sparql.expr.E_StrReplace;
import org.apache.jena.sparql.expr.E_StrStartsWith;
import org.apache.jena.sparql.expr.E_StrSubstring;
import org.apache.jena.sparql.expr.E_StrUUID;
import org.apache.jena.sparql.expr.E_StrUpperCase;
import org.apache.jena.sparql.expr.E_Subtract;
import org.apache.jena.sparql.expr.E_UUID;
import org.apache.jena.sparql.expr.E_UnaryMinus;
import org.apache.jena.sparql.expr.E_UnaryPlus;
import org.apache.jena.sparql.expr.E_Version;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprNone;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Create expressions.
 * 
 * NodeValue contains a number of static functions to make a number of node
 * functions.
 * 
 * @see org.apache.jena.sparql.expr.NodeValue
 * 
 * Function names here map as closely as possible to the tag names for the
 * generated expression.
 *
 */
public class ExprFactory {

	private final PrefixMapping pMap;

	public ExprFactory(PrefixMapping pMap) {
		this.pMap = pMap;
	}

	public ExprFactory() {
		this(PrefixMapping.Extended);
	}

	// expr 0 functions

	/**
	 * implements rand() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-rand
	 * 
	 * @see org.apache.jena.sparql.expr.E_Random
	 * @return E_Random instance
	 */
	public final E_Random rand() {
		return new E_Random();
	}

	/**
	 * implements struuid() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-struuid
	 * 
	 * @see org.apache.jena.sparql.expr.E_StrUUID
	 * @return E_StrUUID instance
	 */
	public final E_StrUUID struuid() {
		return new E_StrUUID();
	}

	/**
	 * implements uuid() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-uuid
	 * 
	 * @see org.apache.jena.sparql.expr.E_UUID
	 * @return E_UUID instance
	 */
	public final E_UUID uuid() {
		return new E_UUID();
	}

	/**
	 * Returns the current ARQ name and version number as a string.
	 * 
	 * @see org.apache.jena.sparql.expr.E_Version
	 * @return E_Version instance
	 */
	public final E_Version version() {
		return new E_Version();
	}

	/**
	 * implements now() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-now
	 * 
	 * @see org.apache.jena.sparql.expr.E_Now
	 * @return E_Now instance
	 */
	public final E_Now now() {
		return new E_Now();
	}

	// expr 1 functions

	/**
	 * implements bound() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-bound
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Bound
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_Bound instance
	 */
	public final E_Bound bound(Object expr) {
		return new E_Bound(asExpr(expr));
	}

	/**
	 * implements datatype() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-datatype
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Datatype
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_Datatype instance
	 */
	public final E_Datatype datatype(Object expr) {
		return new E_Datatype(asExpr(expr));
	}

	/**
	 * implements day() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-day
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_Datatype instance
	 */
	public final E_DateTimeDay day(Object expr) {
		return new E_DateTimeDay(asExpr(expr));
	}

	/**
	 * implements hours() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-hours
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeHours
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTimeHours instance
	 */
	public final E_DateTimeHours hours(Object expr) {
		return new E_DateTimeHours(asExpr(expr));
	}

	/**
	 * implements minutes() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-minutes
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeMinutes
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTimeMinutes instance
	 */
	public final E_DateTimeMinutes minutes(Object expr) {
		return new E_DateTimeMinutes(asExpr(expr));
	}

	/**
	 * implements month() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-month
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeMonth
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTimeMonth instance
	 */
	public final E_DateTimeMonth month(Object expr) {
		return new E_DateTimeMonth(asExpr(expr));
	}

	/**
	 * implements seconds() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-seconds
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeSeconds
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTimeSeconds instance
	 */
	public final E_DateTimeSeconds seconds(Object expr) {
		return new E_DateTimeSeconds(asExpr(expr));
	}

	/**
	 * implements timezone() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-timezone
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeTimezone
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTypeTimezone instance
	 */
	public final E_DateTimeTimezone timezone(Object expr) {
		return new E_DateTimeTimezone(asExpr(expr));
	}

	/**
	 * implements tz() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-tz
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeTZ
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTimeTZ instance
	 */
	public final E_DateTimeTZ tz(Object expr) {
		return new E_DateTimeTZ(asExpr(expr));
	}

	/**
	 * implements year() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-year
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_DateTimeYear
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_DateTimeYear instance
	 */
	public final E_DateTimeYear year(Object expr) {
		return new E_DateTimeYear(asExpr(expr));
	}

	/**
	 * implements iri() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-iri
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_IRI
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_IRI instance
	 */
	public final E_IRI iri(Object expr) {
		return new E_IRI(asExpr(expr));
	}

	/**
	 * implements isBlank() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-isBlank
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_IsBlank
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_isBlank instance
	 */
	public final E_IsBlank isBlank(Object expr) {
		return new E_IsBlank(asExpr(expr));
	}

	/**
	 * implements isIRI() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-isIRI
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_IsIRI
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_IsIRI instance
	 */
	public final E_IsIRI isIRI(Object expr) {
		return new E_IsIRI(asExpr(expr));
	}

	/**
	 * implements isLiteral() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-isLiteral
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_IsLiteral
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_IsLiteral instance
	 */
	public final E_IsLiteral isLiteral(Object expr) {
		return new E_IsLiteral(asExpr(expr));
	}

	/**
	 * implements isNumeric() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-isNumeric
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_IsNumeric
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_IsNumeric instance
	 */
	public final E_IsNumeric isNumeric(Object expr) {
		return new E_IsNumeric(asExpr(expr));
	}

	/**
	 * implements lang() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-lang
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Lang
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_Lang instance
	 */
	public final E_Lang lang(Object expr) {
		return new E_Lang(asExpr(expr));
	}

	/**
	 * implements not() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-not
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_LogicalNot
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_LogicalNot instance
	 */
	public final E_LogicalNot not(Object expr) {
		return new E_LogicalNot(asExpr(expr));
	}

	/**
	 * implements abs() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-abs
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NumAbs
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_NumAbs instance
	 */
	public final E_NumAbs abs(Object expr) {
		return new E_NumAbs(asExpr(expr));
	}

	/**
	 * implements ceil() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-ceil
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NumCeiling
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_NumCeiling instance
	 */
	public final E_NumCeiling ceil(Object expr) {
		return new E_NumCeiling(asExpr(expr));
	}

	/**
	 * implements floor() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-floor
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NumFloor
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_NumFloor instance
	 */
	public final E_NumFloor floor(Object expr) {
		return new E_NumFloor(asExpr(expr));
	}

	/**
	 * implements round() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-round
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NumRound
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_Round instance
	 */
	public final E_NumRound round(Object expr) {
		return new E_NumRound(asExpr(expr));
	}

	/**
	 * implements str() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-str
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Str
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_Str instance
	 */
	public final E_Str str(Object expr) {
		return new E_Str(asExpr(expr));
	}

	/**
	 * implements encode() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-encode
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrEncodeForURI
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_StrEncodedForURI instance
	 */
	public final E_StrEncodeForURI encode(Object expr) {
		return new E_StrEncodeForURI(asExpr(expr));
	}

	/**
	 * implements strlen() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strlen
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrLength
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_StrLength instance
	 */
	public final E_StrLength strlen(Object expr) {
		return new E_StrLength(asExpr(expr));
	}

	/**
	 * implements lcase() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-lcase
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrLowerCase
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_StrLowerCase instance
	 */
	public final E_StrLowerCase lcase(Object expr) {
		return new E_StrLowerCase(asExpr(expr));
	}

	/**
	 * implements ucase() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-ucase
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrUpperCase
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_StrUpperCase instance
	 */
	public final E_StrUpperCase ucase(Object expr) {
		return new E_StrUpperCase(asExpr(expr));
	}

	/**
	 * implements unary minus as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_UnaryMinus
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_UnaryMinus instance
	 */
	public final E_UnaryMinus minus(Object expr) {
		return new E_UnaryMinus(asExpr(expr));
	}

	/**
	 * implements unary plus as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_UnaryPlus
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_UnaryPlus instance
	 */
	public final E_UnaryPlus plus(Object expr) {
		return new E_UnaryPlus(asExpr(expr));
	}

	/**
	 * implements md5() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-md5
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_MD5
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_MD5 instance
	 */
	public final E_MD5 md5(Object expr) {
		return new E_MD5(asExpr(expr));
	}

	/**
	 * implements sha1() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-sha1
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_SHA1
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_SHA1 instance
	 */
	public final E_SHA1 sha1(Object expr) {
		return new E_SHA1(asExpr(expr));
	}

	/**
	 * implements sha224() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-sha224
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_SHA224
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_SHA224 instance
	 */
	public final E_SHA224 sha224(Object expr) {
		return new E_SHA224(asExpr(expr));
	}

	/**
	 * implements sha256() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-sha256
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_SHA256
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_SHA256 instance
	 */
	public final E_SHA256 sha256(Object expr) {
		return new E_SHA256(asExpr(expr));
	}

	/**
	 * implements sha384() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-sha384
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_SHA384
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_SHA384 instance
	 */
	public final E_SHA384 sha384(Object expr) {
		return new E_SHA384(asExpr(expr));
	}

	/**
	 * implements sha512() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-sha512
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_SHA512
	 * 
	 * @param expr
	 *            the expression to check.
	 * @return E_SHA512 instance
	 */
	public final E_SHA512 sha512(Object expr) {
		return new E_SHA512(asExpr(expr));
	}

	// expr2 functions

	/**
	 * implements addition as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Add
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_Add instance
	 */
	public final E_Add add(Object expr1, Object expr2) {
		return new E_Add(asExpr(expr1), asExpr(expr2));
	}

	// E_Cast has a private constructor and seems not to be used.
	// public final E_Cast cast( Expr expr1, Expr expr2 )
	// {
	// return new E_Cast( expr1, expr2 );
	// }
	/**
	 * implements division as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Divide
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_Devide instance
	 */
	public final E_Divide divide(Object expr1, Object expr2) {
		return new E_Divide(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements equality as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Equals
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_Equals instance
	 */
	public final E_Equals eq(Object expr1, Object expr2) {
		return new E_Equals(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements greater than as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_GreaterThan
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_GreaterThan instance
	 */
	public final E_GreaterThan gt(Object expr1, Object expr2) {
		return new E_GreaterThan(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements greater than or equal as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_GreaterThanOrEqual
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_GreaterThanOrEqual instance
	 */
	public final E_GreaterThanOrEqual ge(Object expr1, Object expr2) {
		return new E_GreaterThanOrEqual(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements langMatches() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-langMatches
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_LangMatches
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_LangMatches instance
	 */
	public final E_LangMatches langMatches(Object expr1, Object expr2) {
		return new E_LangMatches(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements less than as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_LessThan
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_LessThan instance
	 */
	public final E_LessThan lt(Object expr1, Object expr2) {
		return new E_LessThan(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements less than or equal as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_LessThanOrEqual
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_LessThanOrEqual instance
	 */
	public final E_LessThanOrEqual le(Object expr1, Object expr2) {
		return new E_LessThanOrEqual(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements logical and as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_LogicalAnd
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_LogicalAnd instance
	 */
	public final E_LogicalAnd and(Object expr1, Object expr2) {
		return new E_LogicalAnd(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements logical or as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_LogicalOr
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_LogicalOr instance
	 */
	public final E_LogicalOr or(Object expr1, Object expr2) {
		return new E_LogicalOr(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements multiplication s per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Multiply
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_Multiply instance
	 */
	public final E_Multiply multiply(Object expr1, Object expr2) {
		return new E_Multiply(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements not equals as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NotEquals
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_NotEquals instance
	 */
	public final E_NotEquals ne(Object expr1, Object expr2) {
		return new E_NotEquals(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements sameTerm() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-sameTerm
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_SameTerm
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_SameTerm instance
	 */
	public final E_SameTerm sameTerm(Object expr1, Object expr2) {
		return new E_SameTerm(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements strafter() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strafter
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrAfter
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrAfter instance
	 */
	public final E_StrAfter strafter(Object expr1, Object expr2) {
		return new E_StrAfter(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements strbefore() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strbefore
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrBefore
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrBefore instance
	 */
	public final E_StrBefore strbefore(Object expr1, Object expr2) {
		return new E_StrBefore(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements contains() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-contains
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrContains
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrContains instance
	 */
	public final E_StrContains contains(Object expr1, Object expr2) {
		return new E_StrContains(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements strdt() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strdt
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrDatatype
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrDatatype instance
	 */
	public final E_StrDatatype strdt(Object expr1, Object expr2) {
		return new E_StrDatatype(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements strends() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strends
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrEndsWith
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrEndsWith instance
	 */
	public final E_StrEndsWith strends(Object expr1, Object expr2) {
		return new E_StrEndsWith(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements strlang() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strlang
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrLang
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrLang instance
	 */
	public final E_StrLang strlang(Object expr1, Object expr2) {
		return new E_StrLang(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements strstarts() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-strstarts
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrStartsWith
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_StrStartsWith instance
	 */
	public final E_StrStartsWith strstarts(Object expr1, Object expr2) {
		return new E_StrStartsWith(asExpr(expr1), asExpr(expr2));
	}

	/**
	 * implements subtraction as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#OperatorMapping
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Subtract
	 * 
	 * @param expr1
	 *            the first expression.
	 * @param expr2
	 *            the second expression.
	 * @return E_Subtract instance
	 */

	public final E_Subtract subtract(Object expr1, Object expr2) {
		return new E_Subtract(asExpr(expr1), asExpr(expr2));
	}

	// expr3 functions

	/**
	 * implements if() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-if
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Conditional
	 * 
	 * @param condition
	 *            the condition to check.
	 * @param thenExpr
	 *            the expression to execute if condition is true.
	 * @param elseExpr
	 *            the expression to execute if condition is false.
	 * @return an E_Conditional instance.
	 */
	public final E_Conditional cond(Expr condition, Expr thenExpr, Expr elseExpr) {
		return new E_Conditional(condition, thenExpr, elseExpr);
	}

	// exprN functions
	/**
	 * implements bnode() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-bnode
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_BNode
	 * 
	 * @param expr1
	 *            the blank node id.
	 * @return an E_BNode instance
	 */
	public final E_BNode bnode(Object expr1) {
		return new E_BNode(asExpr(expr1));
	}

	/**
	 * implements bnode() as per SPARQL 1.1 spec
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-bnode
	 * 
	 * @see org.apache.jena.sparql.expr.E_Random
	 * 
	 * @return an E_BNode instance.
	 */
	public final E_BNode bnode() {
		return new E_BNode();
	}

	/**
	 * Creates a dynamic function call.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rFunctionCall
	 * 
	 * Converts function to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_FunctionDynamic
	 * 
	 * @param function
	 *            The function to execute
	 * @param args
	 *            the arguments to the function.
	 * @return an E_FunctionDynamic instance.
	 */
	public final E_FunctionDynamic call(Object function, ExprList args) {
		return new E_FunctionDynamic(asExpr(function), args);
	}

	/**
	 * Creates a dynamic function call.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rFunctionCall
	 * 
	 * Converts function to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_FunctionDynamic
	 * 
	 * @param function
	 *            The function to execute
	 * @param args
	 *            the arguments to the function.
	 * @return an E_FunctionDynamic instance.
	 */
	public final E_FunctionDynamic call(Object function, Object... args) {
		return call(asExpr(function), asList(args));
	}

	/**
	 * Creates a function call as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rFunctionCall
	 * 
	 * @see org.apache.jena.sparql.expr.E_Call
	 * 
	 *      The first argument is the function to call
	 * 
	 * @param args
	 *            the arguments to the function.
	 * @return an E_Call instance.
	 */
	public final E_Call call(ExprList args) {
		return new E_Call(args);
	}

	/**
	 * implements coalesce() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-coalesce
	 * 
	 * @see org.apache.jena.sparql.expr.E_Coalesce
	 * 
	 * @param args
	 *            the arguments to the function.
	 * @return an E_Coalesce instance.
	 */
	public final E_Coalesce coalesce(ExprList args) {
		return new E_Coalesce(args);
	}

	/**
	 * implements coalesce() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-coalesce
	 * 
	 * @see org.apache.jena.sparql.expr.E_Coalesce
	 * 
	 * @param args
	 *            the arguments to the function.
	 * @return an E_Coalesce instance.
	 */
	public final E_Coalesce coalesce(Object... args) {
		return new E_Coalesce(asList(args));
	}

	/**
	 * Creates a function call as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rFunctionCall
	 * 
	 * @see org.apache.jena.sparql.expr.E_Function
	 *
	 * @param name
	 *            the name of the function.
	 * @param args
	 *            the arguments to the function.
	 * @return an E_Function instance.
	 */
	public final E_Function function(String name, ExprList args) {
		return new E_Function(name, args);
	}

	/**
	 * implements "not in" as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-not-in
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NotOneOf
	 * 
	 * @param expr
	 *            the expression that is not in the list
	 * @param list
	 *            the list of expressions.
	 * @return an E_NotOneOf instance.
	 */
	public final E_NotOneOf notin(Object expr, ExprList list) {
		return new E_NotOneOf(asExpr(expr), list);
	}

	/**
	 * implements "not in" as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-not-in
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_NotOneOf
	 * 
	 * @param expr
	 *            the expression that is not in the list
	 * @param list
	 *            the list of expressions.
	 * @return an E_NotOneOf instance.
	 */
	public final E_NotOneOf notin(Object expr, Object... list) {
		return new E_NotOneOf(asExpr(expr), asList(list));
	}

	/**
	 * implements "in" as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-in
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_OneOf
	 * 
	 * @param expr
	 *            the expression that is not in the list
	 * @param list
	 *            the list of expressions.
	 * @return an E_OneOf instance.
	 */
	public final E_OneOf in(Object expr, ExprList list) {
		return new E_OneOf(asExpr(expr), list);
	}

	/**
	 * implements "in" as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-in
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_OneOf
	 * 
	 * @param expr
	 *            the expression that is not in the list
	 * @param list
	 *            the list of expressions.
	 * @return an E_OneOf instance.
	 */
	public final E_OneOf in(Object expr, Object... list) {
		return new E_OneOf(asExpr(expr), asList(list));
	}

	/**
	 * implements regex() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-regex
	 * 
	 * Converts objects to an Expr objects via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Regex
	 * 
	 * @param expr
	 *            string to match.
	 * @param pattern
	 *            the pattern to match
	 * @param flags
	 *            the regex flags
	 * @return an E_Regex instance.
	 */
	public final E_Regex regex(Object expr, Object pattern, Object flags) {
		return new E_Regex(asExpr(expr), asExpr(pattern), asExpr(flags));
	}

	/**
	 * implements regex() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-regex
	 * 
	 * Converts expr to an Expr object via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_Regex
	 * 
	 * @param expr
	 *            string to match.
	 * @param pattern
	 *            the pattern to match
	 * @param flags
	 *            the regex flags
	 * @return an E_Regex instance.
	 */
	public final E_Regex regex(Object expr, String pattern, String flags) {
		return new E_Regex(asExpr(expr), pattern, flags);
	}

	/**
	 * implements concat() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-concat
	 * 
	 * @see org.apache.jena.sparql.expr.E_StrConcat
	 * 
	 * @param list
	 *            the list of arguments to concatenate
	 * @return an E_StrConcat instance
	 */
	public final E_StrConcat concat(ExprList list) {
		return new E_StrConcat(list);
	}

	/**
	 * implements concat() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-concat
	 * 
	 * @see org.apache.jena.sparql.expr.E_StrConcat
	 * 
	 * @param list
	 *            the list of arguments to concatenate
	 * @return an E_StrConcat instance
	 */
	public final E_StrConcat concat(Object... list) {
		return new E_StrConcat(asList(list));
	}

	/**
	 * implements replace() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-replace
	 * 
	 * Converts Objects to an Expr objects via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrReplace
	 * 
	 * @param arg
	 *            the string literal to replace
	 * @param pattern
	 *            the pattern to replace in the string literal.
	 * @param replacement
	 *            the string literal to replace the pattern with.
	 * @flags flags the flags that control replacement options.
	 * @return an E_StrReplace instance
	 */
	public final E_StrReplace replace(Object arg, Object pattern, Object replacement, Object flags) {
		return new E_StrReplace(asExpr(arg), asExpr(pattern), asExpr(replacement), asExpr(flags));
	}

	/**
	 * implements replace() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-replace
	 * 
	 * Converts Objects to an Expr objects via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrReplace
	 * 
	 * @param arg
	 *            the string literal to replace
	 * @param pattern
	 *            the pattern to replace in the string literal.
	 * @param replacement
	 *            the string literal to replace the pattern with.
	 * @return an E_StrReplace instance
	 */
	public final E_StrReplace replace(Object arg, Object pattern, Object replacement) {
		return new E_StrReplace(asExpr(arg), asExpr(pattern), asExpr(replacement), null);
	}

	/**
	 * implements substr() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-substr
	 *
	 * Converts Objects to an Expr objects via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrSubstring
	 * 
	 * @param src
	 *            the expression to extract the substring from.
	 * @param loc
	 *            the location within the expression string to start
	 * @param len
	 *            the length of the string to extract.
	 * 
	 * @return an E_Substring instance.
	 */
	public final E_StrSubstring substr(Object src, Object loc, Object len) {
		return new E_StrSubstring(asExpr(src), asExpr(loc), asExpr(len));
	}

	/**
	 * implements substr() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-substr
	 *
	 * Converts Objects to an Expr objects via expr()
	 * 
	 * @see #asExpr(Object)
	 * @see org.apache.jena.sparql.expr.E_StrSubstring
	 * 
	 * @param src
	 *            the expression to extract the substring from.
	 * @param loc
	 *            the location within the expression string to start
	 * 
	 * @return an E_Substring instance.
	 */
	public final E_StrSubstring substr(Object src, Object loc) {
		return new E_StrSubstring(asExpr(src), asExpr(loc), null);
	}
	// expr op

	/**
	 * implements exists() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-filter-exists
	 * 
	 * @see org.apache.jena.sparql.expr.E_Exists
	 * 
	 * @param whereClause
	 *            A WhereClause to check existence of.
	 * @return an E_Exists instance,
	 */
	public final E_Exists exists(WhereClause<?> whereClause) {
		return new E_Exists(whereClause.getWhereHandler().getClause());
	}

	/**
	 * implements not exists() as per the SPARQL 11 query definition.
	 * 
	 * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#func-filter-exists
	 * 
	 * @see org.apache.jena.sparql.expr.E_NotExists
	 * 
	 * @param whereClause
	 *            the argument to the function.
	 * @return an E_NotExists instance.
	 */
	public final E_NotExists notexists(WhereClause<?> whereClause) {
		return new E_NotExists(whereClause.getWhereHandler().getClause());
	}

	// exprnone
	/**
	 * Should probably be called "null" but that is a reserved work. An
	 * expression that can be used in place of null.
	 * 
	 * @see org.apache.jena.sparql.expr.ExprNone
	 * 
	 * @return an ExprNone instance
	 */
	public final ExprNone none() {
		return (ExprNone) Expr.NONE;
	}

	/**
	 * Converts the object to a ExprVar.
	 * <ul>
	 * <li>If the object is an ExprVar return it</li>
	 * <li>Will return null if the object is "*" or Node_RuleVariable.WILD</li>
	 * <li>otherwise create an ExprVar from {AbstractQuerybuilder.makeVar}
	 * </ul>
	 * 
	 * @see AbstractQueryBuilder#makeVar(Object)
	 * 
	 * @param o
	 *            the object to convert.
	 * @return an ExprVar
	 */
	public final ExprVar asVar(Object o) {
		if (o instanceof ExprVar) {
			return (ExprVar) o;
		}
		Var v = AbstractQueryBuilder.makeVar(o);
		return v == null ? null : new ExprVar(v);
	}

	/**
	 * Not really an Expr but a container of exprs.
	 * 
	 * @param args
	 *            the list of expressons.
	 * @return the expression list.
	 */
	public final ExprList asList(Object... args) {
		// make sure the list is modifyable
		List<Expr> lst = Arrays.asList(args).stream().map(arg -> asExpr(arg)).collect(Collectors.toList());
		return new ExprList(lst);
	}

	/**
	 * Not really an Expr but a container of exprs. creates an empty list.
	 * 
	 * @return the empty expression list.
	 */
	public final ExprList list() {
		return new ExprList();
	}

	/**
	 * Convert the object into an expression using the query's PrefixMapping
	 * 
	 * @param o
	 *            the object to convert.
	 * @return the Expr.
	 */
	public final Expr asExpr(Object o) {
		return asExpr(o, pMap);
	}

	/**
	 * Convenience method to call AbstractQueryBuilder.quote
	 * 
	 * @see AbstractQueryBuilder#quote(String)
	 * @param s
	 *            the string to quote
	 * @return the quotes string.
	 */
	public final String quote(String s) {
		return AbstractQueryBuilder.quote(s);
	}

	/**
	 * Create an expression from an object.
	 * 
	 * <b>this method does not parse strings to expressions.</b> to parse
	 * strings to expressions see {AbstractQueryBuilder.makeExpr}
	 * 
	 * <ul>
	 * <li>If the object is null returns none()</li>
	 * <li>If the object is an expression return it</li>
	 * <li>If the object fronts a node and is not a var make a NodeVar</li>
	 * <li>otherwise calls var()</li>
	 * </ul>
	 * 
	 * @see #asVar(Object)
	 * @see AbstractQueryBuilder#makeVar(Object)
	 * 
	 * @param o
	 *            the object to create the expression from
	 * @return Expr
	 */
	public static final Expr asExpr(Object o, PrefixMapping pMap) {
		if (o == null) {
			return Expr.NONE;
		}
		if (o instanceof Expr) {
			return (Expr) o;
		}
		Node n = AbstractQueryBuilder.makeNode(o, pMap);

		if (n.isVariable()) {
			return new ExprVar(Var.alloc(n));
		}
		return NodeValue.makeNode(n);

	}

}
