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

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;

import java.math.BigDecimal ;
import java.math.BigInteger ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.XSD ;

/** Break expression testing suite into parts
* @see TestExpressions
* @see TestExprLib
* @see TestNodeValue
*/
public class TestExpressions
{
    public final static int NO_FAILURE    = 100 ;
    public final static int PARSE_FAIL    = 250 ;   // Parser should catch it.
    public final static int EVAL_FAIL     = 200 ;   // Parser should pass it but eval should fail it
    
    static boolean flagVerboseWarning ;
    @BeforeClass static public void beforeClass() {  
        flagVerboseWarning = NodeValue.VerboseWarnings ;
        NodeValue.VerboseWarnings = false ;
    }
    
    @AfterClass static public void afterClass() { NodeValue.VerboseWarnings = flagVerboseWarning ; }
    
    @Test public void testVar_1() { testVar("?x", "x") ; }
    @Test public void testVar_2() { testVar("$x", "x") ; }
    @Test public void testVar_3() { testVar("?name", "name") ; }
    @Test public void testVar_4() { testVar("$name", "name") ; }
    @Test public void testSyntax_1() { testSyntax("?x11") ; }


    @Test public void testVar_5() { testVar("?x_", "x_") ; }
    @Test public void testVar_6() { testVar("?x.", "x") ; }
    @Test public void testVar_7() { testVar("?x.x", "x") ; }
    @Test public void testVar_8() { testVar("?0", "0") ; }
    @Test public void testVar_9() { testVar("?0x", "0x") ; }
    @Test public void testVar_10() { testVar("?x0", "x0") ; }
    @Test public void testVar_11() { testVar("?_", "_") ; }
    
    
    @Test(expected=QueryParseException.class) public void testVar_12() { testVar("?", "") ; }
    @Test(expected=QueryParseException.class) public void testSyntax_2() { testSyntax("??") ; }
    @Test(expected=QueryParseException.class) public void testSyntax_3() { testSyntax("?.") ; }
    @Test(expected=QueryParseException.class) public void testSyntax_4() { testSyntax("?#") ; }
    @Test public void testNumeric_1() { testNumeric("7", 7) ; }
    @Test public void testNumeric_2() { testNumeric("-3", -3) ; }
    @Test public void testNumeric_3() { testNumeric("+2", 2) ; }
    @Test(expected=QueryParseException.class) public void testNumeric_4() { testNumeric("0xF", 0xF) ; }
    @Test(expected=QueryParseException.class) public void testNumeric_5() { testNumeric("0x12", 0x12) ; }
    @Test public void testNumeric_6() { testNumeric("3--4", 3-(-4)) ; }
    @Test public void testNumeric_7() { testNumeric("3++4", 3+(+4)) ; }
    @Test public void testNumeric_8() { testNumeric("3-+4", 3-+4) ; }
    @Test public void testNumeric_9() { testNumeric("3+-4", 3+-4) ; }
    @Test public void testNumeric_10() { testNumeric("3-(-4)", 3-(-4)) ; }
    @Test public void testNumeric_11() { testNumeric("3+4+5", 3+4+5) ; }
    @Test public void testNumeric_12() { testNumeric("(3+4)+5", 3+4+5) ; }
    @Test public void testNumeric_13() { testNumeric("3+(4+5)", 3+4+5) ; }
    @Test public void testNumeric_14() { testNumeric("3*4+5", 3*4+5) ; }
    @Test public void testNumeric_15() { testNumeric("3*(4+5)", 3*(4+5)) ; }
    @Test public void testNumeric_16() { testNumeric("10-3-5", 10-3-5) ; }
    @Test public void testNumeric_17() { testNumeric("(10-3)-5", (10-3)-5) ; }
    @Test public void testNumeric_18() { testNumeric("10-(3-5)", 10-(3-5)) ; }
    @Test public void testNumeric_19() { testNumeric("10-3+5", 10-3+5) ; }
    @Test public void testNumeric_20() { testNumeric("10-(3+5)", 10-(3+5)) ; }
    @Test(expected=QueryParseException.class) public void testNumeric_21() { testNumeric("1<<2", 1<<2) ; }
    @Test(expected=QueryParseException.class) public void testNumeric_22() { testNumeric("1<<2<<2", 1<<2<<2) ; }
    @Test(expected=QueryParseException.class) public void testNumeric_23() { testNumeric("10000>>2", 10000>>2) ; }
    @Test public void testNumeric_24() { testNumeric("1.5 + 2.5", 1.5+2.5) ; }
    @Test public void testNumeric_25() { testNumeric("1.5 + 2", 1.5+2) ; }
    @Test public void testNumeric_26() { testNumeric("4111222333444", 4111222333444L) ; }
    @Test public void testNumeric_27() { testNumeric("1234 + 4111222333444", 1234 + 4111222333444L) ; }
    
    @Test public void testNumeric_28() { testNumeric("+2.5", new BigDecimal("+2.5")) ; }
    @Test public void testNumeric_29() { testNumeric("-2.5", new BigDecimal("-2.5")) ; }
    @Test public void testNumeric_30() { testNumeric("10000000000000000000000000000+1", new BigInteger("10000000000000000000000000001")) ; }
    @Test public void testNumeric_31() { testNumeric("-10000000000000000000000000000+1", new BigInteger("-9999999999999999999999999999")) ; }
    
    @Test public void testBoolean_1() { testBoolean("4111222333444 > 1234", 4111222333444L > 1234) ; }
    @Test public void testBoolean_2() { testBoolean("4111222333444 < 1234", 4111222333444L < 1234L) ; }
    @Test public void testBoolean_3() { testBoolean("1.5 < 2", 1.5 < 2 ) ; }
    @Test public void testBoolean_4() { testBoolean("1.5 > 2", 1.5 > 2 ) ; }
    @Test public void testBoolean_5() { testBoolean("1.5 < 2.3", 1.5 < 2.3 ) ; }
    @Test public void testBoolean_6() { testBoolean("1.5 > 2.3", 1.5 > 2.3 ) ; }
    @Test public void testBoolean_7() { testBoolean("'true'^^<"+XSDDatatype.XSDboolean.getURI()+">", true) ; }
    @Test public void testBoolean_8() { testBoolean("'1'^^<"+XSDDatatype.XSDboolean.getURI()+">", true) ; }
    @Test public void testBoolean_9() { testBoolean("'false'^^<"+XSDDatatype.XSDboolean.getURI()+">", false) ; }
    @Test public void testBoolean_10() { testBoolean("'0'^^<"+XSDDatatype.XSDboolean.getURI()+">", false) ; }
    @Test public void testBoolean_11() { testBoolean("1 || false", true) ; }
    @Test public void testBoolean_12() { testBoolean("'foo'  || false", true) ; }
    @Test public void testBoolean_13() { testBoolean("0 || false", false) ; }
    @Test public void testBoolean_14() { testBoolean("'' || false", false) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_15() { testEval("!'junk'^^<urn:unknown>") ; }
    @Test public void testBoolean_16() { testBoolean("2 < 3", 2 < 3) ; }
    @Test public void testBoolean_17() { testBoolean("2 > 3", 2 > 3) ; }
    @Test public void testBoolean_18() { testBoolean("(2 < 3) && (3<4)", (2 < 3) && (3<4)) ; }
    @Test public void testBoolean_19() { testBoolean("(2 < 3) && (3>=4)", (2 < 3) && (3>=4)) ; }
    @Test public void testBoolean_20() { testBoolean("(2 < 3) || (3>=4)", (2 < 3) || (3>=4)) ; }
    
    // ?x is unbound in the next few tests
    @Test public void testBoolean_21() { testBoolean("(2 < 3) || ?x > 2", true) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_22() { testEval("(2 > 3) || ?x > 2") ; }
    @Test public void testBoolean_23() { testBoolean("(2 > 3) && ?x > 2", false) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_24() { testEval("(2 < 3) && ?x > 2") ; }
    @Test public void testBoolean_25() { testBoolean("?x > 2 || (2 < 3)", true) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_26() { testEval("?x > 2 || (2 > 3)") ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_27() { testEval("?x > 2 && (2 < 3)") ; }
    @Test public void testBoolean_28() { testBoolean("?x > 2 && (2 > 3)", false) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_29() { testEval("! ?x ") ; }

    @Test public void testBoolean_30() { testBoolean("! true ", false) ; }
    @Test public void testBoolean_31() { testBoolean("! false ", true) ; }
    @Test public void testBoolean_32() { testBoolean("2 = 3", 2 == 3) ; }
    @Test public void testBoolean_33() { testBoolean("!(2 = 3)", !(2 == 3)) ; }
    @Test public void testBoolean_34() { testBoolean("'2' = 2", false) ; }
    @Test public void testBoolean_35() { testBoolean("2 = '2'", false) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_36() { testEval("2 < '3'") ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_37() { testEval("'2' < 3") ; }
    @Test public void testBoolean_38() { testBoolean("\"fred\" != \"joe\"", true ) ; }
    @Test public void testBoolean_39() { testBoolean("\"fred\" = \"joe\"", false ) ; }
    @Test public void testBoolean_40() { testBoolean("\"fred\" = \"fred\"", true ) ; }
    @Test public void testBoolean_41() { testBoolean("\"fred\" = 'fred'", true ) ; }
    @Test public void testBoolean_42() { testBoolean("true = true", true) ; }
    @Test public void testBoolean_43() { testBoolean("false = false", true) ; }
    @Test public void testBoolean_44() { testBoolean("true = false", false) ; }
    @Test public void testBoolean_45() { testBoolean("true > true", false) ; }
    @Test public void testBoolean_46() { testBoolean("true >= false", true) ; }
    @Test public void testBoolean_47() { testBoolean("false > false", false) ; }
    @Test public void testBoolean_48() { testBoolean("false >= false", true) ; }
    @Test public void testBoolean_49() { testBoolean("true > false", true) ; }
    @Test public void testBoolean_50() { testBoolean("1 = true", false) ; }
    @Test public void testBoolean_51() { testBoolean("1 != true", true) ; }
    @Test public void testBoolean_52() { testBoolean("'a' != false", true) ; }
    @Test public void testBoolean_53() { testBoolean("0 != false", true) ; }
    @Test public void testBoolean_54() { testBoolean(dateTime1+" = "+dateTime2, true) ; }
    @Test public void testBoolean_55() { testBoolean(dateTime1+" <= "+dateTime2, true) ; }
    @Test public void testBoolean_56() { testBoolean(dateTime1+" >= "+dateTime2, true) ; }
    @Test public void testBoolean_57() { testBoolean(dateTime3+" < "+dateTime1, true) ; }
    @Test public void testBoolean_58() { testBoolean(dateTime3+" > "+dateTime1, false) ; }
    @Test public void testBoolean_59() { testBoolean(dateTime4+" < "+dateTime1, false) ; }
    @Test public void testBoolean_60() { testBoolean(dateTime4+" > "+dateTime1, true) ; }
    @Test public void testBoolean_61() { testBoolean(time1+" = "+time2, true) ; }
    @Test public void testBoolean_62() { testBoolean(time1+" <= "+time2, true) ; }
    @Test public void testBoolean_63() { testBoolean(time1+" >= "+time2, true) ; }
    @Test public void testBoolean_64() { testBoolean(time3+" < "+time2, false) ; }
    @Test public void testBoolean_65() { testBoolean(time3+" > "+time2, true) ; }
    @Test public void testBoolean_66() { testBoolean(time4+" < "+time2, true) ; }
    @Test public void testBoolean_67() { testBoolean(time4+" > "+time2, false) ; }
    
    @Test public void testBoolean_68() { testBoolean("isNumeric(12)", true) ; }
    @Test public void testBoolean_69() { testBoolean("isNumeric('12')", false) ; }
    @Test public void testBoolean_70() { testBoolean("isNumeric('12'^^<"+XSDDatatype.XSDbyte.getURI()+">)", true) ; }
    @Test public void testBoolean_71() { testBoolean("isNumeric('1200'^^<"+XSDDatatype.XSDbyte.getURI()+">)", false) ; }
    
    @Test(expected=ExprEvalException.class)
    public void testBoolean_72()       { testBoolean("isNumeric(?x)", true) ; } 
    
    @Test public void testDuration_01() { testBoolean(duration1+" = "+duration1, true) ; }
    @Test public void testDuration_02() { testBoolean(duration1+" < "+duration2, true) ; }
    @Test public void testDuration_03() { testBoolean(duration1+" > "+duration2, false) ; }
    @Test public void testDuration_04() { testBoolean(duration1+" < "+duration2, true) ; }
    @Test public void testDuration_05() { testBoolean(duration1+" = "+duration3, true) ; }
    @Test public void testDuration_06() { testBoolean(duration1+" <= "+duration3, true) ; }
    @Test public void testDuration_07() { testBoolean(duration1+" >= "+duration3, true) ; }
    
    // Jena bug (<=2.6.2) for durations with fractional seconds.
    // @Test public void testDuration_08() { testBoolean(duration5+" > "+duration4, true) ; }
    
    @Test public void testDuration_09() { testBoolean(duration7+" < "+duration8, true) ; }
    
    @Test public void testURI_1()       { testURI("<a>",     baseNS+"a" ) ; }
    @Test public void testURI_2()       { testURI("<a\\u00E9>",     baseNS+"a\u00E9" ) ; }
    @Test public void testURI_3()       { testURI("ex:b",     exNS+"b" ) ; }
    @Test public void testURI_4()       { testURI("ex:b_",    exNS+"b_" ) ; }
    @Test public void testURI_5()       { testURI("ex:a_b",   exNS+"a_b" ) ; }
    @Test public void testURI_6()       { testURI("ex:", exNS ) ; }
    
    @Test(expected=QueryParseException.class)
    public void testURI_7()             { testURI("x.:", xNS) ; }
    
    @Test public void testURI_8()       { testURI("rdf:_2",   rdfNS+"_2" ) ; }
    @Test public void testURI_9()       { testURI("rdf:__2",  rdfNS+"__2" ) ; }
    @Test public void testURI_10()      { testURI(":b",       dftNS+"b" ) ; }
    @Test public void testURI_11()      { testURI(":", dftNS ) ; }
    @Test public void testURI_12()      { testURI(":\\u00E9", dftNS+"\u00E9" ) ; }
    @Test public void testURI_13()      { testURI("\\u0065\\u0078:", exNS ) ; }
    @Test public void testURI_14()      { testURI("select:a", selNS+"a" ) ; }
    
    @Test(expected=QueryParseException.class)
    public void testSyntax_5()          { testSyntax("_:") ; }
    
    @Test public void testURI_15()      { testURI("ex:a.",   exNS+"a") ; }
    @Test public void testURI_16()      { testURI("ex:a.a",  exNS+"a.a") ; }
    
    @Test(expected=QueryParseException.class)
    public void testURI_17()      { testURI("x.:a.a",  xNS+"a.a") ; }
    
    @Test public void testNumeric_50()  { testNumeric("1:b", 1) ; }
    @Test public void testURI_18()      { testURI("ex:2",    exNS+"2" ) ; }
    @Test public void testURI_19()      { testURI("ex:2ab_c",    exNS+"2ab_c" ) ; }
    @Test public void testBoolean_76()  { testBoolean("'fred'@en = 'fred'", false ) ; }
    @Test public void testBoolean_77()  { testBoolean("'fred'@en = 'bert'", false ) ; }
    @Test public void testBoolean_78()  { testBoolean("'fred'@en != 'fred'", true ) ; }
    @Test public void testBoolean_79()  { testBoolean("'fred'@en != 'bert'", true ) ; }
    @Test public void testBoolean_80()  { testBoolean("'chat'@en = 'chat'@fr", false ) ; }
    @Test public void testBoolean_81()  { testBoolean("'chat'@en = 'maison'@fr", false ) ; }
    @Test public void testBoolean_82()  { testBoolean("'chat'@en != 'chat'@fr", true ) ; }
    @Test public void testBoolean_83()  { testBoolean("'chat'@en != 'maison'@fr", true ) ; }
    @Test public void testBoolean_84()  { testBoolean("'chat'@en = 'chat'@EN", true ) ; }
    @Test public void testBoolean_85()  { testBoolean("'chat'@en = 'chat'@en-uk", false ) ; }
    @Test public void testBoolean_86()  { testBoolean("'chat'@en != 'chat'@EN", false ) ; }
    @Test public void testBoolean_87()  { testBoolean("'chat'@en != 'chat'@en-uk", true ) ; }
    @Test public void testBoolean_88()  { testBoolean("'chat'@en = <http://example/>", false ) ; }
    
    @Test(expected=QueryParseException.class) 
    public void testURI_20()      { testURI("()", RDF.nil.getURI()) ; }
    
    @Test(expected=QueryParseException.class) public void testSyntax_6() { testSyntax("[]") ; }
    
    @Test public void testBoolean_89() { testBoolean("'fred'^^<type1> = 'fred'^^<type1>", true ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_90() { testEval("'fred'^^<type1> != 'joe'^^<type1>" ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_91() { testEval("'fred'^^<type1> = 'fred'^^<type2>" ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_92() { testEval("'fred'^^<type1> != 'joe'^^<type2>" ) ; }
    @Test public void testBoolean_93() { testBoolean("'fred'^^<"+XSDDatatype.XSDstring.getURI()+"> = 'fred'", true ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_94() { testEval("'fred'^^<type1> = 'fred'" ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_95() { testEval("'fred'^^<type1> != 'fred'" ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_96() { testBoolean("'21'^^<int> = '21'", true ) ; }
    @Test public void testNumeric_51() { testNumeric("'21'^^<"+XSDDatatype.XSDinteger.getURI()+">", 21) ; }
    @Test public void testBoolean_97() { testBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> = 21", true) ; }
    @Test public void testBoolean_98() { testBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> = 22", false) ; }
    @Test public void testBoolean_99() { testBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> != 21", false) ; }
    @Test public void testBoolean_100() { testBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> != 22", true) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_101() { testEval("'x'^^<type1>  = 21") ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_102() { testEval("'x'^^<type1> != 21") ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_103() { testEval("'x'^^<http://example/unknown> = true") ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_104() { testEval("'x'^^<http://example/unknown> != true") ; }
    @Test public void testBoolean_105() { testBoolean("'x'^^<http://example/unknown> = 'x'^^<http://example/unknown>", true) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_106() { testEval("'x'^^<http://example/unknown> = 'y'^^<http://example/unknown>") ; }
    @Test public void testBoolean_107() { testBoolean("'x'^^<http://example/unknown> != 'x'^^<http://example/unknown>", false) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_108() { testEval("'x'^^<http://example/unknown> != 'y'^^<http://example/unknown>") ; }
    @Test public void testString_1() { testString("'a\\nb'", "a\nb") ; }
    @Test public void testString_2() { testString("'a\\n'", "a\n") ; }
    @Test public void testString_3() { testString("'\\nb'", "\nb") ; }
    @Test public void testString_4() { testString("'a\\tb'", "a\tb") ; }
    @Test public void testString_5() { testString("'a\\bb'", "a\bb") ; }
    @Test public void testString_6() { testString("'a\\rb'", "a\rb") ; }
    @Test public void testString_7() { testString("'a\\fb'", "a\fb") ; }
    @Test public void testString_8() { testString("'a\\\\b'", "a\\b") ; }
    @Test public void testString_9() { testString("'a\\u0020a'", "a a") ; }
    @Test public void testString_10() { testString("'a\\uF021'", "a\uF021") ; }
    
    @Test(expected=QueryParseException.class) 
    public void testString_11() { testString("'a\\X'") ; }

    @Test(expected=QueryParseException.class) 
    public void testString_12() { testString("'aaa\\'") ; }
    
    @Test(expected=QueryParseException.class) 
    public void testString_13() { testString("'\\u'") ; }
    
    @Test(expected=QueryParseException.class) 
    public void testString_14() { testString("'\\u111'") ; }
    
//    @Test public void testBoolean_109() { testBoolean("\"fred\\1\" = 'fred1'", false ) ; }
//    @Test public void testBoolean_110() { testBoolean("\"fred2\" = 'fred\\2'", true ) ; }
    @Test public void testBoolean_111() { testBoolean("'fred\\\\3' != \"fred3\"", true ) ; }
    @Test public void testBoolean_112() { testBoolean("'urn:fred' = <urn:fred>" , false) ; }
    @Test public void testBoolean_113() { testBoolean("'urn:fred' != <urn:fred>" , true) ; }
    @Test public void testBoolean_114() { testBoolean("'urn:fred' = <urn:fred>", false ) ; }
    @Test public void testBoolean_115() { testBoolean("'urn:fred' != <urn:fred>", true ) ; }
    @Test public void testBoolean_116() { testBoolean("REGEX('aabbcc', 'abbc')", true ) ; }
    @Test public void testBoolean_117() { testBoolean("REGEX('aabbcc' , 'a..c')", true ) ; }
    @Test public void testBoolean_118() { testBoolean("REGEX('aabbcc' , '^aabb')", true ) ; }
    @Test public void testBoolean_119() { testBoolean("REGEX('aabbcc' , 'cc$')", true ) ; }
    @Test public void testBoolean_120() { testBoolean("! REGEX('aabbcc' , 'abbc')", false ) ; }
    @Test public void testBoolean_121() { testBoolean("REGEX('aa\\\\cc', '\\\\\\\\')", true ) ; }
    @Test public void testBoolean_122() { testBoolean("REGEX('aab*bcc', 'ab\\\\*bc')", true ) ; }
    @Test public void testBoolean_123() { testBoolean("REGEX('aabbcc', 'ab\\\\\\\\*bc')", true ) ; }
    @Test public void testBoolean_124() { testBoolean("REGEX('aabbcc', 'B.*B', 'i')", true ) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_125() { testEval("2 < 'fred'") ; }
    @Test public void testBoolean_126() { testBoolean("datatype('fred') = <"+XSD.xstring.getURI()+">", true) ; }
    @Test public void testBoolean_127() { testBoolean("datatype('fred'^^<urn:foo>) = <urn:foo>", true) ; }
    @Test public void testBoolean_128() { testBoolean("datatype('fred'^^<foo>) = <Foo>", false) ; }
    @Test public void testString_15() { testString("lang('fred'@en)", "en") ; }
    @Test public void testString_16() { testString("lang('fred'@en-uk)", "en-uk") ; }
    @Test public void testString_17() { testString("lang('fred')", "") ; }
    @Test public void testBoolean_129() { testBoolean("isURI(?x)", true, env) ; }
    @Test public void testBoolean_130() { testBoolean("isURI(?a)", false, env) ; }
    @Test public void testBoolean_131() { testBoolean("isURI(?b)", false, env) ; }
    
    // ?y is unbound
    @Test(expected=ExprEvalException.class) public void testBoolean_132() { testBoolean("isURI(?y)", false, env) ; }
    @Test public void testBoolean_133() { testBoolean("isURI(<urn:foo>)", true, env) ; }
    @Test public void testBoolean_134() { testBoolean("isURI('bar')", false, env) ; }
    @Test public void testBoolean_135() { testBoolean("isLiteral(?x)", false, env) ; }
    @Test public void testBoolean_136() { testBoolean("isLiteral(?a)", true, env) ; }
    @Test public void testBoolean_137() { testBoolean("isLiteral(?b)", false, env) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_138() { testBoolean("isLiteral(?y)", false, env) ; }
    @Test public void testBoolean_139() { testBoolean("isBlank(?x)", false, env) ; }
    @Test public void testBoolean_140() { testBoolean("isBlank(?a)", false, env) ; }
    @Test public void testBoolean_141() { testBoolean("isBlank(?b)", true, env) ; }
    @Test(expected=ExprEvalException.class) public void testBoolean_142() { testBoolean("isBlank(?y)", false, env) ; }
    @Test public void testBoolean_143() { testBoolean("bound(?a)", true, env) ; }
    @Test public void testBoolean_144() { testBoolean("bound(?b)", true, env) ; }
    @Test public void testBoolean_145() { testBoolean("bound(?x)", true, env) ; }
    @Test public void testBoolean_146() { testBoolean("bound(?y)", false, env) ; }
    @Test public void testString_18()   { testString("str(<urn:x>)", "urn:x") ; }
    @Test public void testString_19()   { testString("str('')", "") ; }
    @Test public void testString_20()   { testString("str(15)", "15") ; }
    @Test public void testString_21()   { testString("str('15.20'^^<"+XSDDatatype.XSDdouble.getURI()+">)", "15.20") ; }
    @Test public void testString_22()   { testString("str('lex'^^<x:unknown>)", "lex") ; }
    @Test public void testBoolean_147() { testBoolean("sameTerm(1, 1)", true, env) ; }
    @Test public void testBoolean_148() { testBoolean("sameTerm(1, 1.0)", false, env) ; }
    @Test public void testNumeric_52()  { testNumeric("<"+xsd+"integer>('3')", 3) ; }
    @Test public void testNumeric_53()  { testNumeric("<"+xsd+"byte>('3')", 3) ; }
    @Test public void testNumeric_54()  { testNumeric("<"+xsd+"int>('3')", 3) ; }
    @Test public void testBoolean_149() { testBoolean("<"+xsd+"double>('3') = 3", true) ; }
    @Test public void testBoolean_150() { testBoolean("<"+xsd+"float>('3') = 3", true) ; }
    @Test public void testBoolean_151() { testBoolean("<"+xsd+"double>('3') = <"+xsd+"float>('3')", true) ; }
    @Test public void testBoolean_152() { testBoolean("<"+xsd+"double>(str('3')) = 3", true) ; }
    
    @Test public void testString_23()   { testString("'a'+'b'", "ab") ; }
    @Test(expected=ExprEvalException.class)
    public void testString_24()         { testString("'a'+12") ; }
    public void testString_25()         { testString("12+'a'") ; }
    public void testString_26()         { testString("<uri>+'a'") ; }



    static String duration1 = "'P1Y1M1DT1H1M1S"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String duration2 = "'P2Y1M1DT1H1M1S"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String duration3 = "'P1Y1M1DT1H1M1S"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String duration4 = "'PT1H1M1S"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String duration5 = "'PT1H1M1.1S"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String duration7 = "'-PT1H"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String duration8 = "'PT0H0M0S"+"'^^<"+XSDDatatype.XSDduration.getURI()+">";
    static String dateTime1 = "'2005-02-25T12:03:34Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
    static String dateTime2 = "'2005-02-25T12:03:34Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
    // Earlier
    static String dateTime3 = "'2005-01-01T12:03:34Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
    // Later
    static String dateTime4 = "'2005-02-25T13:00:00Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
    static String time1 = "'12:03:34Z'^^<" + XSDDatatype.XSDtime.getURI() + ">";
    static String time2 = "'12:03:34Z'^^<" + XSDDatatype.XSDtime.getURI() + ">";
    static String time3 = "'13:00:00Z'^^<" + XSDDatatype.XSDtime.getURI() + ">";
    static String time4 = "'11:03:34Z'^^<" + XSDDatatype.XSDtime.getURI() + ">";
    static String exNS = "http://example.org/" ;
    static String xNS  = "http://example.org/dot#" ;
    static String selNS = "http://select/" ;
    static String dftNS = "http://default/" ;
    static  String baseNS = "http://base/" ;
    static String rdfNS = RDF.getURI() ;
    static Query query = QueryFactory.make() ;
    static {
        query.setBaseURI(baseNS) ;
        
        query.setPrefix("ex",      exNS) ;
        query.setPrefix("rdf",     RDF.getURI()) ;
        query.setPrefix("x.",      xNS) ;
        query.setPrefix("",        dftNS) ;
        query.setPrefix("select",  selNS) ;
    }
    static String xsd = XSDDatatype.XSD+"#" ;
    static Binding env ; 
    static {
        BindingMap b = BindingFactory.create() ;
        b.add(Var.alloc("a"), NodeFactory.createLiteral("A")) ;
        b.add(Var.alloc("b"), NodeFactory.createAnon()) ;
        b.add(Var.alloc("x"), NodeFactory.createURI("urn:x")) ;
        env = b ;
    }

    private static Expr parse(String exprString)
    {
        return ExprUtils.parse(query, exprString, false) ;
    }

    private static void testVar(String string, String rightVarName)
    {
        Expr expr = parse(string) ;
        assertTrue("Not a NodeVar: "+expr, expr.isVariable()) ;
        ExprVar v = (ExprVar)expr ;
        assertEquals("Different variable names", rightVarName, v.getVarName()) ;
    }

    private static void testSyntax(String exprString)
    {
        ExprUtils.parse(query, exprString, false) ;
    }

    private static void testNumeric(String string, int i)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( BindingFactory.binding() , new FunctionEnvBase()) ;
        assertTrue(v.isInteger()) ;
        assertEquals(i, v.getInteger().intValue()) ;
    }
    
    private static void testNumeric(String string, BigDecimal decimal)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( BindingFactory.binding() , new FunctionEnvBase()) ;
        assertTrue(v.isDecimal()) ;
        assertEquals(decimal, v.getDecimal()) ;
    }

    private static void testNumeric(String string, BigInteger integer)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( BindingFactory.binding() , new FunctionEnvBase()) ;
        assertTrue(v.isInteger()) ;
        assertEquals(integer, v.getInteger()) ;
    }
    private static void testNumeric(String string, double d)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( BindingFactory.binding(), new FunctionEnvBase()) ;
        assertTrue(v.isDouble()) ;
        assertEquals(d, v.getDouble(),0) ;
    }

    private static void testEval(String string)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( BindingFactory.binding(), new FunctionEnvBase()) ;
    }

    private static void testBoolean(String string, boolean b)
    {
        testBoolean(string, b, BindingFactory.binding()) ;
    }

    private static void testBoolean(String string, boolean b, Binding env)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( env, new FunctionEnvBase()) ;
        assertTrue(v.isBoolean()) ;
        assertEquals(b, v.getBoolean()) ;
    }

    private static void testURI(String string, String uri)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( env, new FunctionEnvBase()) ;
        assertTrue(v.isIRI()) ;
        assertEquals(uri, v.getNode().getURI()) ;
    }

    private static void testString(String string, String string2)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( env, new FunctionEnvBase()) ;
        assertTrue(v.isString()) ;
        assertEquals(string2, v.getString()) ;
    }

    private static void testString(String string)
    {
        Expr expr = parse(string) ;
        NodeValue v = expr.eval( env, new FunctionEnvBase()) ;
        assertTrue(v.isString()) ;
    }

}
