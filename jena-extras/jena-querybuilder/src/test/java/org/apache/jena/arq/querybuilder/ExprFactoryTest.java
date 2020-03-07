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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.expr.nodevalue.NodeValueNode;
import org.apache.jena.sparql.expr.nodevalue.NodeValueString;
import org.junit.Test;

public class ExprFactoryTest {

	private ExprFactory factory = new ExprFactory();

	@Test
	public void randTest() {
		Expr e = factory.rand();
		assertTrue(e instanceof E_Random);
	}

	@Test
	public void struuidTest() {
		Expr e = factory.struuid();
		assertTrue(e instanceof E_StrUUID);
	}

	@Test
	public void uuidTest() {
		Expr e = factory.uuid();
		assertTrue(e instanceof E_UUID);
	}

	@Test
	public void versionTest() {
		Expr e = factory.version();
		assertTrue(e instanceof E_Version);
	}

	@Test
	public void nowTest() {
		Expr e = factory.now();
		assertTrue(e instanceof E_Now);
	}

	// expr 1 functions
	@Test
	public void boundTest_Var() {
		Expr e = factory.bound(Var.alloc("hello"));
		assertTrue(e instanceof E_Bound);
		assertEquals("?hello", ((E_Bound) e).getArg().toString());
	}

	public void boundTest_Object() {
		Expr e = factory.bound("hello");
		assertTrue(e instanceof E_Bound);
		assertEquals("?hello", e.getVarName());
	}

	public void boundTest_ExprVar() {
		ExprVar ev = new ExprVar("hello");
		Expr e = factory.bound(ev);
		assertTrue(e instanceof E_Bound);
		assertEquals("?hello", e.getVarName());
	}

	@Test
	public void datatypeTest() {
		Expr e = factory.datatype(factory.none());
		assertTrue(e instanceof E_Datatype);
	}

	@Test
	public void dayTest() {
		Expr e = factory.day(factory.none());
		assertTrue(e instanceof E_DateTimeDay);
	}

	@Test
	public void hoursTest() {
		Expr e = factory.hours(factory.none());
		assertTrue(e instanceof E_DateTimeHours);
	}

	@Test
	public void minutesTest() {
		Expr e = factory.minutes(factory.none());
		assertTrue(e instanceof E_DateTimeMinutes);
	}

	@Test
	public void monthTest() {
		Expr e = factory.month(factory.none());
		assertTrue(e instanceof E_DateTimeMonth);
	}

	@Test
	public void secondsTest() {
		Expr e = factory.seconds(factory.none());
		assertTrue(e instanceof E_DateTimeSeconds);
	}

	@Test
	public void timezoneTest() {
		Expr e = factory.timezone(factory.none());
		assertTrue(e instanceof E_DateTimeTimezone);
	}

	@Test
	public void tzTest() {
		Expr e = factory.tz(factory.none());
		assertTrue(e instanceof E_DateTimeTZ);
	}

	@Test
	public void yearTest() {
		Expr e = factory.year(factory.none());
		assertTrue(e instanceof E_DateTimeYear);
	}

	@Test
	public void iriTest() {
		Expr e = factory.iri(factory.none());
		assertTrue(e instanceof E_IRI);
	}

	@Test
	public void isBlankTest() {
		Expr e = factory.isBlank(factory.none());
		assertTrue(e instanceof E_IsBlank);
	}

	@Test
	public void isIRITest() {
		Expr e = factory.isIRI(factory.none());
		assertTrue(e instanceof E_IsIRI);
	}

	@Test
	public void isLiteralTest() {
		Expr e = factory.isLiteral(factory.none());
		assertTrue(e instanceof E_IsLiteral);
	}

	@Test
	public void isNumericTest() {
		Expr e = factory.isNumeric(factory.none());
		assertTrue(e instanceof E_IsNumeric);
	}

	@Test
	public void langTest() {
		Expr e = factory.lang(factory.none());
		assertTrue(e instanceof E_Lang);
	}

	@Test
	public void notTest() {
		Expr e = factory.not(factory.none());
		assertTrue(e instanceof E_LogicalNot);
	}

	@Test
	public void absTest() {
		Expr e = factory.abs(factory.none());
		assertTrue(e instanceof E_NumAbs);
	}

	@Test
	public void ceilTest() {
		Expr e = factory.ceil(factory.none());

		assertTrue(e instanceof E_NumCeiling);
	}

	@Test
	public void floorTest() {
		Expr e = factory.floor(factory.none());
		assertTrue(e instanceof E_NumFloor);
	}

	@Test
	public void roundTest() {
		Expr e = factory.round(factory.none());
		assertTrue(e instanceof E_NumRound);
	}

	@Test
	public void strTest() {
		Expr e = factory.str(factory.none());
		assertTrue(e instanceof E_Str);
	}

	@Test
	public void encode_for_uriTest() {
		Expr e = factory.encode(factory.none());
		assertTrue(e instanceof E_StrEncodeForURI);
	}

	@Test
	public void strlenTest() {
		Expr e = factory.strlen(factory.none());
		assertTrue(e instanceof E_StrLength);
	}

	@Test
	public void lcaseTest() {
		Expr e = factory.lcase(factory.none());
		assertTrue(e instanceof E_StrLowerCase);
	}

	@Test
	public void ucaseTest() {
		Expr e = factory.ucase(factory.none());
		assertTrue(e instanceof E_StrUpperCase);
	}

	@Test
	public void minusTest() {
		Expr e = factory.minus(factory.none());
		assertTrue(e instanceof E_UnaryMinus);
	}

	@Test
	public void plusTest() {
		Expr e = factory.plus(factory.none());
		assertTrue(e instanceof E_UnaryPlus);
	}

	@Test
	public void md5Test() {
		Expr e = factory.md5(factory.none());
		assertTrue(e instanceof E_MD5);
	}

	@Test
	public void sha1Test() {
		Expr e = factory.sha1(factory.none());
		assertTrue(e instanceof E_SHA1);
	}

	@Test
	public void sha224Test() {
		Expr e = factory.sha224(factory.none());
		assertTrue(e instanceof E_SHA224);
	}

	@Test
	public void sha256Test() {
		Expr e = factory.sha256(factory.none());
		assertTrue(e instanceof E_SHA256);
	}

	@Test
	public void sha384Test() {
		Expr e = factory.sha384(factory.none());
		assertTrue(e instanceof E_SHA384);
	}

	@Test
	public void sha512Test() {
		Expr e = factory.sha512(factory.none());
		assertTrue(e instanceof E_SHA512);
	}

	// expr2 functions
	@Test
	public void addTest() {
		Expr e = factory.add(factory.none(), factory.none());
		assertTrue(e instanceof E_Add);
	}
	// E_Cast has a private constructor and seems not to be used.
	// public void castTest( Expr expr1, Expr expr2 )
	// {
	// assertTrue( e instanceof E_Cast);
	// }
	
	@Test
	public void divideTest() {
		Expr e = factory.divide(factory.none(), factory.none());
		assertTrue(e instanceof E_Divide);
	}

	@Test
	public void eqTest() {
		Expr e = factory.eq(factory.none(), factory.none());
		assertTrue(e instanceof E_Equals);
	}

	@Test
	public void gtTest() {
		Expr e = factory.gt(factory.none(), factory.none());
		assertTrue(e instanceof E_GreaterThan);
	}

	@Test
	public void geTest() {
		Expr e = factory.ge(factory.none(), factory.none());
		assertTrue(e instanceof E_GreaterThanOrEqual);
	}

	@Test
	public void langMatchesTest() {
		Expr e = factory.langMatches(factory.none(), factory.none());
		assertTrue(e instanceof E_LangMatches);
	}

	@Test
	public void ltTest() {
		Expr e = factory.lt(factory.none(), factory.none());
		assertTrue(e instanceof E_LessThan);
	}

	@Test
	public void leTest() {
		Expr e = factory.le(factory.none(), factory.none());
		assertTrue(e instanceof E_LessThanOrEqual);
	}

	@Test
	public void andTest() {
		Expr e = factory.and(factory.none(), factory.none());
		assertTrue(e instanceof E_LogicalAnd);
	}

	@Test
	public void orTest() {
		Expr e = factory.or(factory.none(), factory.none());
		assertTrue(e instanceof E_LogicalOr);
	}

	@Test
	public void multiplyTest() {
		Expr e = factory.multiply(factory.none(), factory.none());
		assertTrue(e instanceof E_Multiply);
	}

	@Test
	public void neTest() {
		Expr e = factory.ne(factory.none(), factory.none());
		assertTrue(e instanceof E_NotEquals);
	}

	@Test
	public void sameTermTest() {
		Expr e = factory.sameTerm(factory.none(), factory.none());
		assertTrue(e instanceof E_SameTerm);
	}

	@Test
	public void strafterTest() {
		Expr e = factory.strafter(factory.none(), factory.none());
		assertTrue(e instanceof E_StrAfter);
	}

	@Test
	public void strbeforeTest() {
		Expr e = factory.strbefore(factory.none(), factory.none());
		assertTrue(e instanceof E_StrBefore);
	}

	@Test
	public void containsTest() {
		Expr e = factory.contains(factory.none(), factory.none());
		assertTrue(e instanceof E_StrContains);
	}

	@Test
	public void strdtTest() {
		Expr e = factory.strdt(factory.none(), factory.none());
		assertTrue(e instanceof E_StrDatatype);
	}

	@Test
	public void strendsTest() {
		Expr e = factory.strends(factory.none(), factory.none());
		assertTrue(e instanceof E_StrEndsWith);
	}

	@Test
	public void strlangTest() {
		Expr e = factory.strlang(factory.none(), factory.none());
		assertTrue(e instanceof E_StrLang);
	}

	@Test
	public void strtartsTest() {
		Expr e = factory.strstarts(factory.none(), factory.none());
		assertTrue(e instanceof E_StrStartsWith);
	}

	@Test
	public void subtractTest() {
		Expr e = factory.subtract(factory.none(), factory.none());
		assertTrue(e instanceof E_Subtract);
	}

	// expr3 functions
	
	@Test
	public void condTest() {
		Expr e = factory.cond(factory.none(), factory.none(), factory.none());
		assertTrue(e instanceof E_Conditional);
	}

	// exprN functions
	
	@Test
	public void bnodeTest_expr() {
		Expr e = factory.bnode(factory.none());
		assertTrue(e instanceof E_BNode);
	}

	@Test
	public void bnodeTest() {
		Expr e = factory.bnode();
		assertTrue(e instanceof E_BNode);
	}

	@Test
	public void exprListTest() {
		ExprList e = factory.list();
		assertNotNull(e);
	}

	@Test
	public void exprListTest_OneArg() {
		ExprList e = factory.asList(factory.none());
        assertNotNull(e);
	}

	@Test
	public void exprListTest_MultipleArg() {
		ExprList e = factory.asList(factory.none(), factory.none());
        assertNotNull(e);
	}

	@Test
	public void callTest_dynamic() {
		Expr e = factory.call(factory.none(), factory.list());
		assertTrue(e instanceof E_FunctionDynamic);
	}

	@Test
	public void callTest() {
		Expr e = factory.call(factory.list());
		assertTrue(e instanceof E_Call);
	}

	@Test
	public void coalesceTest() {
		Expr e = factory.coalesce(factory.list());
		assertTrue(e instanceof E_Coalesce);
	}

	@Test
	public void functionTest() {
		Expr e = factory.function("name", factory.list());
		assertTrue(e instanceof E_Function);
	}

	@Test
	public void notinTest() {
		Expr e = factory.notin(factory.none(), factory.list());
		assertTrue(e instanceof E_NotOneOf);
	}

	@Test
	public void inTest() {
		Expr e = factory.in(factory.none(), factory.list());
		assertTrue(e instanceof E_OneOf);
	}

	@Test
	public void regexTest_expr() {
		Expr e = factory.regex(factory.none(), factory.none(), factory.none());
		assertTrue(e instanceof E_Regex);
	}

	@Test
	public void regexTest_strings() {
		/*
		 * case 'i' : // Need both (Java 1.4) newMask |= Pattern.UNICODE_CASE ;
		 * newMask |= Pattern.CASE_INSENSITIVE; break ; case 'm' : newMask |=
		 * Pattern.MULTILINE ; break ; case 's' : newMask |= Pattern.DOTALL ;
		 * break ;
		 */
		Expr e = factory.regex(factory.none(), "pattern", "ims");
		assertTrue(e instanceof E_Regex);
	}

	@Test
	public void concatTest() {
		Expr e = factory.concat(factory.list());
		assertTrue(e instanceof E_StrConcat);
	}

	@Test
	public void replaceTest() {
		Expr e = factory.replace(factory.none(), factory.none(), factory.none(), factory.none());
		assertTrue(e instanceof E_StrReplace);
	}

	@Test
	public void substrTest() {
		Expr e = factory.substr(factory.none(), factory.none(), factory.none());
		assertTrue(e instanceof E_StrSubstring);
	}

	// expr op
	
	@Test
	public void existsTest() {
		WhereHandler handler = new WhereHandler(new Query());
		WhereClause<?> whereClause = mock(WhereClause.class);
		when(whereClause.getWhereHandler()).thenReturn(handler);
		Expr e = factory.exists(whereClause);
		assertTrue(e instanceof E_Exists);
	}

	@Test
	public void notexistsTest() {
		WhereHandler handler = new WhereHandler(new Query());
		WhereClause<?> whereClause = mock(WhereClause.class);
		when(whereClause.getWhereHandler()).thenReturn(handler);
		Expr e = factory.notexists(whereClause);
		assertTrue(e instanceof E_NotExists);
	}

	// exprnone
	@Test
	public void noneTest() {
		Expr e = factory.none();
		assertTrue(e instanceof ExprNone);
	}

	@Test
	public void asVarTest() {
		Expr e = factory.asVar("hello");
		assertTrue(e instanceof ExprVar);
	}

	@Test
	public void asVarTest_null() {
		Expr e = factory.asVar(null);
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("?_", v.asVar().getName());
	}

	@Test
	public void asVarTest_var() {
		Expr e = factory.asVar(Var.alloc("hello"));
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("hello", v.asVar().getName());
	}

	@Test
	public void asVarTest_node() {
		Expr e = factory.asVar(NodeFactory.createVariable("hello"));
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("hello", v.asVar().getName());
	}

	@Test
	public void asVarTest_asterisk() {
		assertNull(factory.asVar("*"));
	}

	@Test
	public void asVarTest_string() {
		Expr e = factory.asVar("foo");
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("foo", v.asVar().getName());
	}

	@Test
	public void asVarTest_varString() {
		Expr e = factory.asVar("?foo");
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("foo", v.asVar().getName());
	}

	@Test
	public void asExprTest() {
		assertNotNull( factory.asExpr("hello") );
	}

	@Test
	public void asExprTest_number() {
		Expr e = factory.asExpr( 3 );
		assertTrue(e instanceof NodeValueInteger);
		NodeValueInteger i = (NodeValueInteger)e;
		assertEquals( 3, i.asNode().getLiteralValue());
	}
	
	@Test
	public void asExprTest_null() {
		Expr e = factory.asExpr(null);
		assertTrue(e instanceof ExprNone);
	}

	@Test
	public void asExprTest_var() {
		Expr e = factory.asExpr(Var.alloc("hello"));
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("hello", v.asVar().getName());
	}

	@Test
	public void asExprTest_URInode() {
		Expr e = factory.asExpr(NodeFactory.createURI("http://example.com/foo"));
		assertTrue(e instanceof NodeValueNode);
		NodeValueNode n = (NodeValueNode) e;
		assertEquals("http://example.com/foo", n.asNode().getURI());
	}

	@Test
	public void asExprTest_URIstring() {
		Expr e = factory.asExpr("http://example.com/foo");
		assertTrue(e instanceof NodeValueString);
		NodeValueString n = (NodeValueString) e;
		assertEquals("\"http://example.com/foo\"", n.asNode().toString());
	}
	
	public void asExprTest_Varnode() {
		Expr e = factory.asExpr(NodeFactory.createVariable("hello"));
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("hello", v.asVar().getName());
	}
	@Test
	public void asExprTest_asterisk() {
		Expr e = factory.asExpr("*");
		assertTrue( e instanceof NodeValueString);
		NodeValueString n = (NodeValueString) e;
		assertEquals("*", n.asString());
	}

	@Test
	public void asExprTest_string() {
		Expr e = factory.asExpr("foo");
		assertTrue(e instanceof NodeValueString);
		NodeValueString n = (NodeValueString) e;
		assertEquals("foo", n.asString());
	}

	@Test
	public void asExprTest_varString() {
		Expr e = factory.asExpr("?foo");
		assertTrue(e instanceof ExprVar);
		ExprVar v = (ExprVar) e;
		assertEquals("foo", v.asVar().getName());
	}
	
	@Test
	public void asListTest() {
		ExprList lst = factory.asList("?foo", "http://example.com", Converters.quoted("hello"), 1, 5L, 3.14f, 6.28d, Var.alloc( "bar" ), null, factory.rand() );
		assertEquals( 10, lst.size() );
		assertEquals( new ExprVar( "foo" ), lst.get(0));
		//assertEquals( new ExprVar( "foo" ), lst.get(0));
		assertEquals( new NodeValueString( "hello" ), lst.get(2));
		assertEquals(  1 , lst.get(3).getConstant().asNode().getLiteralValue());
		assertEquals( 5 , lst.get(4).getConstant().asNode().getLiteralValue());
		assertEquals( 3.14f, lst.get(5).getConstant().asNode().getLiteralValue());
		assertEquals( 6.28, lst.get(6).getConstant().asNode().getLiteralValue());
		assertEquals( new ExprVar( "bar" ), lst.get(7));
		assertEquals( Expr.NONE, lst.get(8));
		assertEquals( new E_Random(), lst.get(9));
	}
}
