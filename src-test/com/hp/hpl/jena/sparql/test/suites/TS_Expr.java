/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;

import junit.framework.TestSuite;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.junit.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class TS_Expr extends TestSuite
{
    // Ideally, split this and us testXXX form.
    
    // Mid level testing
    // Low level : see TestFunctions,. TestNodeValue
    // High level: see TestExpressions
    
    // Better: Turn into testXXX() for ease of navigation?
    // Have a query with name spaces
    // helpers:
    //    execTest(String, NodeValue result) 
    //    execTest(String, Exception) ExprEval/ExprType or ParseException
    // Remove TestExpr{Boolean,Numeric,RDFTerm,Syntax,URI}
    
	static final String testSetName = "ARQ - Expressions" ;

	public static TestSuite suite()
    {
        TS_Expr ts = new TS_Expr(testSetName) ;
        ts.addStdTests(ts) ;
    	return ts ;
    }
    
    
    public TS_Expr(String name)
    {
    	super(name) ;
    }
    
    public void addStdTests(TestSuite ts)
    {
        ts.addTest(variables()) ;
        ts.addTest(numericValues()) ;
        ts.addTest(numericExpr()) ;
        ts.addTest(booleanValues()) ;
        ts.addTest(booleanExpr()) ;
        ts.addTest(DateTime()) ;
        ts.addTest(rdfTermsURIs()) ;
        ts.addTest(rdfTerms()) ;
        ts.addTest(typedLiterals()) ;
        ts.addTest(stringExpr()) ;
        ts.addTest(builtinOps()) ;
        ts.addTest(castExpr()) ;

    }
        
    static public TestSuite variables()
    {
        TestSuite ts = new TestSuite("Variables") ;
        ts.addTest(new TestVar("?x", "x")) ;
        ts.addTest(new TestVar("$x", "x")) ;
        ts.addTest(new TestVar("?name", "name")) ;
        ts.addTest(new TestVar("$name", "name")) ;
        ts.addTest(new TestExprSyntax("?x11")) ;
        
        ts.addTest(new TestVar("?x_", "x_")) ;
        ts.addTest(new TestVar("?x.", "x")) ;    // Does not include the DOT
        ts.addTest(new TestVar("?x.x", "x")) ;   // Does not include the DOT
        ts.addTest(new TestVar("?0", "0")) ;
        ts.addTest(new TestVar("?0x", "0x")) ;
        ts.addTest(new TestVar("?x0", "x0")) ;
        ts.addTest(new TestVar("?_", "_")) ;
        
        ts.addTest(new TestVar("?", TestExpr.PARSE_FAIL)) ;
        
        // Illegal variable names
        ts.addTest(new TestExprSyntax("??",  TestExpr.PARSE_FAIL)) ;
        ts.addTest(new TestExprSyntax("?.",  TestExpr.PARSE_FAIL)) ;
        ts.addTest(new TestExprSyntax("?#",  TestExpr.PARSE_FAIL)) ;

        return ts ; 
    }
    
    static public TestSuite numericValues()
    {
        TestSuite ts = new TestSuite("Numeric Values") ; 
        ts.addTest(new TestExprNumeric("7", 7)) ;
        ts.addTest(new TestExprNumeric("-3", -3)) ;
        ts.addTest(new TestExprNumeric("+2", 2)) ;
        //ts.addTest(new TestExprNumeric("0xF", 0xF)) ;
        //ts.addTest(new TestExprNumeric("0x12", 0x12)) ;
        return ts ;
    }
     
    
    static public TestSuite numericExpr()
    {
        TestSuite ts = new TestSuite("Numeric Expressions") ; 
        // Unary operators
        ts.addTest(new TestExprNumeric("3+-4", 3+-4)) ;
        // Not sure what's right here!
        //ts.addTest(new TestExprNumeric("3--4", 3-(-4))) ;
        //ts.addTest(new TestExprNumeric("3++4", 3+(+4))) ;
        
        ts.addTest(new TestExprNumeric("3-+4", 3-+4)) ; 
        ts.addTest(new TestExprNumeric("3+-4", 3+-4)) ; 

        ts.addTest(new TestExprNumeric("3-(-4)", 3-(-4))) ;
        
        ts.addTest(new TestExprNumeric("3+4+5", 3+4+5)) ;
        ts.addTest(new TestExprNumeric("(3+4)+5", 3+4+5)) ;
        ts.addTest(new TestExprNumeric("3+(4+5)", 3+4+5)) ;

        // Precedence
        ts.addTest(new TestExprNumeric("3*4+5", 3*4+5)) ;
        ts.addTest(new TestExprNumeric("3*(4+5)", 3*(4+5))) ;

        ts.addTest(new TestExprNumeric("10-3-5", 10-3-5)) ;
        ts.addTest(new TestExprNumeric("(10-3)-5", (10-3)-5)) ;
        ts.addTest(new TestExprNumeric("10-(3-5)", 10-(3-5))) ;
        ts.addTest(new TestExprNumeric("10-3+5", 10-3+5)) ;
        ts.addTest(new TestExprNumeric("10-(3+5)", 10-(3+5))) ;

//        ts.addTest(new TestNumeric("1<<2", 1<<2)) ;
//        ts.addTest(new TestNumeric("1<<2<<2", 1<<2<<2)) ;

//        ts.addTest(new TestNumeric("10000>>2", 10000>>2)) ;

        ts.addTest(new TestExprNumeric("1.5 + 2.5", 1.5+2.5)) ;
        ts.addTest(new TestExprNumeric("1.5 + 2", 1.5+2)) ;
        
        // Test longs
        // A long is over 32bites signed = +2Gig
        ts.addTest(new TestExprNumeric("4111222333444", 4111222333444L)) ;
        ts.addTest(new TestExprNumeric("1234 + 4111222333444", 1234 + 4111222333444L)) ;
        
        ts.addTest(new TestExprBoolean("4111222333444 > 1234", 4111222333444L > 1234)) ;
        ts.addTest(new TestExprBoolean("4111222333444 < 1234", 4111222333444L < 1234L)) ;
        
        ts.addTest(new TestExprBoolean("1.5 < 2", 1.5 < 2 )) ;
        ts.addTest(new TestExprBoolean("1.5 > 2", 1.5 > 2 )) ;
        ts.addTest(new TestExprBoolean("1.5 < 2.3", 1.5 < 2.3 )) ;
        ts.addTest(new TestExprBoolean("1.5 > 2.3", 1.5 > 2.3 )) ;
        
        return ts ;
    }
    
    static public TestSuite booleanValues()
    {
        TestSuite ts = new TestSuite("Boolean Values") ; 
        // Booleans
        ts.addTest(new TestExprBoolean("'true'^^<"+XSDDatatype.XSDboolean.getURI()+">", true)) ;
        ts.addTest(new TestExprBoolean("'1'^^<"+XSDDatatype.XSDboolean.getURI()+">", true)) ;
        ts.addTest(new TestExprBoolean("'false'^^<"+XSDDatatype.XSDboolean.getURI()+">", false)) ;
        ts.addTest(new TestExprBoolean("'0'^^<"+XSDDatatype.XSDboolean.getURI()+">", false)) ;

        // Booleans : effective boolean value rule apply - use || to cause this.
        ts.addTest(new TestExprBoolean("1 || false", true)) ;
        ts.addTest(new TestExprBoolean("'foo'  || false", true)) ;       // String, length > 0
        ts.addTest(new TestExprBoolean("0 || false", false)) ;
        ts.addTest(new TestExprBoolean("'' || false", false)) ;         // String, length = 0

        // BEV applies to overall result only. 
        ts.addTest(new TestExprBoolean("!'junk'^^<urn:unknown>", TestExpr.EVAL_FAIL)) ;
        
        return ts ; 
    }
    
    static public TestSuite booleanExpr()
    {
        TestSuite ts = new TestSuite("Boolean Expressions") ; 
        ts.addTest(new TestExprBoolean("2 < 3", true)) ;
        // Boolean expressions
        ts.addTest(new TestExprBoolean("2 < 3", 2 < 3)) ;
        ts.addTest(new TestExprBoolean("2 > 3", 2 > 3)) ;
        ts.addTest(new TestExprBoolean("(2 < 3) && (3<4)", (2 < 3) && (3<4))) ;
        ts.addTest(new TestExprBoolean("(2 < 3) && (3>=4)", (2 < 3) && (3>=4))) ;
        ts.addTest(new TestExprBoolean("(2 < 3) || (3>=4)", (2 < 3) || (3>=4))) ;
        
        // RHS is undef
        ts.addTest(new TestExprBoolean("(2 < 3) || ?x > 2", true) ) ;
        ts.addTest(new TestExprBoolean("(2 > 3) || ?x > 2", TestExpr.EVAL_FAIL) ) ;
        ts.addTest(new TestExprBoolean("(2 > 3) && ?x > 2", false) ) ;
        ts.addTest(new TestExprBoolean("(2 < 3) && ?x > 2", TestExpr.EVAL_FAIL) ) ;
        
        // LHS is undef
        // Error OR true => true
        // Error OR false => error
        ts.addTest(new TestExprBoolean("?x > 2 || (2 < 3)", true) ) ;
        ts.addTest(new TestExprBoolean("?x > 2 || (2 > 3)", TestExpr.EVAL_FAIL) ) ;
       
        ts.addTest(new TestExprBoolean("?x > 2 && (2 < 3)", TestExpr.EVAL_FAIL) ) ;
        ts.addTest(new TestExprBoolean("?x > 2 && (2 > 3)", false) ) ;
        
        // ! error => error
        ts.addTest(new TestExprBoolean("! ?x ", TestExpr.EVAL_FAIL) ) ;
        ts.addTest(new TestExprBoolean("! true ", false) ) ;
        ts.addTest(new TestExprBoolean("! false ", true) ) ;
        
        ts.addTest(new TestExprBoolean("2 = 3", 2 == 3)) ;
        ts.addTest(new TestExprBoolean("!(2 = 3)", !(2 == 3))) ;
        
        // SPARQL - no coercion
        // No coercion of strings to numbers
        ts.addTest(new TestExprBoolean("'2' = 2", false)) ;
        ts.addTest(new TestExprBoolean("2 = '2'", false)) ;
        ts.addTest(new TestExprBoolean("2 < '3'", 2 < 3, TestExpr.EVAL_FAIL)) ;
        ts.addTest(new TestExprBoolean("'2' < 3", 2 < 3, TestExpr.EVAL_FAIL)) ;

        ts.addTest(new TestExprBoolean("\"fred\" != \"joe\"", true )) ;
        ts.addTest(new TestExprBoolean("\"fred\" = \"joe\"", false )) ;
        ts.addTest(new TestExprBoolean("\"fred\" = \"fred\"", true )) ;
        ts.addTest(new TestExprBoolean("\"fred\" = 'fred'", true )) ;
        
        // Comparing booleans
        ts.addTest(new TestExprBoolean("true = true", true)) ;
        ts.addTest(new TestExprBoolean("false = false", true)) ;
        ts.addTest(new TestExprBoolean("true = false", false)) ;
        
        // false < true.
        ts.addTest(new TestExprBoolean("true > true", false)) ;
        ts.addTest(new TestExprBoolean("true >= false", true)) ;
        ts.addTest(new TestExprBoolean("false > false", false)) ;
        ts.addTest(new TestExprBoolean("false >= false", true)) ;
        ts.addTest(new TestExprBoolean("true > false", true)) ;

        ts.addTest(new TestExprBoolean("1 = true", false)) ;
        
        // Known value types
        ts.addTest(new TestExprBoolean("1 != true", true)) ;
        ts.addTest(new TestExprBoolean("'a' != false", true)) ;
        ts.addTest(new TestExprBoolean("0 != false", true)) ;
        
        return ts ;
        
    }
    
    static public TestSuite DateTime()
    {
        TestSuite ts = new TestSuite("DateTime Expressions") ;

        String dateTime1 = "'2005-02-25T12:03:34Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
        String dateTime2 = "'2005-02-25T12:03:34Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
        // Earlier
        String dateTime3 = "'2005-01-01T12:03:34Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
        // Later
        String dateTime4 = "'2005-02-25T13:00:00Z'^^<"+XSDDatatype.XSDdateTime.getURI()+">" ;
        
        ts.addTest(new TestExprBoolean(dateTime1+" = "+dateTime2, true)) ;
        ts.addTest(new TestExprBoolean(dateTime1+" <= "+dateTime2, true)) ;
        ts.addTest(new TestExprBoolean(dateTime1+" >= "+dateTime2, true)) ;

        ts.addTest(new TestExprBoolean(dateTime3+" < "+dateTime1, true)) ;
        ts.addTest(new TestExprBoolean(dateTime3+" > "+dateTime1, false)) ;

        ts.addTest(new TestExprBoolean(dateTime4+" < "+dateTime1, false)) ;
        ts.addTest(new TestExprBoolean(dateTime4+" > "+dateTime1, true)) ;

        return ts ;
    }
    
    static public TestSuite rdfTermsURIs()
    {
        TestSuite ts = new TestSuite("RDF terms: QNames, URIs") ; 
        
        Query query = QueryFactory.make() ;
        
        String exNS = "http://example.org/" ;
        String xNS  = "http://example.org/dot#" ;

        String selNS = "http://select/" ;
        
        String dftNS = "http://default/" ;
        String baseNS = "http://base/" ;
        String rdfNS = RDF.getURI() ;

        query.setBaseURI(baseNS) ;
        ts.addTest(new TestExprURI("<a>",     baseNS+"a",     query, null, TestExpr.NO_FAILURE )) ; 
        ts.addTest(new TestExprURI("<a\\u00E9>",     baseNS+"a\u00E9",     query, null, TestExpr.NO_FAILURE )) ; 

        
        query.setPrefix("ex",      exNS) ;
        query.setPrefix("rdf",     RDF.getURI()) ;
        query.setPrefix("x.",      xNS) ;
        query.setPrefix("",        dftNS) ;
        query.setPrefix("select",  selNS) ;

        ts.addTest(new TestExprURI("ex:b",     exNS+"b",     query, null, TestExpr.NO_FAILURE )) ; 
        ts.addTest(new TestExprURI("ex:b_",    exNS+"b_",    query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI("ex:a_b",   exNS+"a_b",   query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI("ex:",      exNS,         query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI("x.:",      xNS,          query, null, TestExpr.PARSE_FAIL)) ;
        ts.addTest(new TestExprURI("rdf:_2",   rdfNS+"_2",   query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI("rdf:__2",  rdfNS+"__2",  query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI(":b",       dftNS+"b",    query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI(":",        dftNS,        query, null, TestExpr.NO_FAILURE )) ;

        ts.addTest(new TestExprURI(":\\u00E9", dftNS+"\u00E9", query, null, TestExpr.NO_FAILURE )) ;
        // 65 => e 78 => x  It's ex:
        ts.addTest(new TestExprURI("\\u0065\\u0078:", exNS,  query, null, TestExpr.NO_FAILURE )) ;

        // Keywords and qnames
        
        ts.addTest(new TestExprURI("select:a", selNS+"a",  query, null, TestExpr.NO_FAILURE )) ;
        
        // Illegal
        ts.addTest(new TestExprRDFTerm("_:",    TestExpr.PARSE_FAIL)) ;
        
        // Must parse without the DOT
        ts.addTest(new TestExprURI("ex:a.",   exNS+"a",   query, null, TestExpr.NO_FAILURE)) ;
        
        // Does include ".a"
        ts.addTest(new TestExprURI("ex:a.a",  exNS+"a.a", query, null, TestExpr.NO_FAILURE)) ;
        
        // Namespace prefix ending with dot.
        ts.addTest(new TestExprURI("x.:a.a",  xNS+"a.a", query, null, TestExpr.PARSE_FAIL)) ;
        
        // Parses as a integer 1, not a qname
        ts.addTest(new TestExprNumeric("1:b",  1)) ;
        
        // This is a negative test - it checks that the "2" is not part of the qname
        // SPARQL not allows this.
        ts.addTest(new TestExprURI("ex:2",    exNS+"2",       query, null, TestExpr.NO_FAILURE )) ;
        ts.addTest(new TestExprURI("ex:2ab_c",    exNS+"2ab_c",       query, null, TestExpr.NO_FAILURE )) ;
        
        return ts ;
    }    
    
    static public TestSuite rdfTerms()
    {
        TestSuite ts = new TestSuite("RDF terms: general") ;
        
        ts.addTest(new TestExprBoolean("'fred'@en = 'fred'", false )) ;
        ts.addTest(new TestExprBoolean("'fred'@en = 'bert'", false )) ;
        
        ts.addTest(new TestExprBoolean("'fred'@en != 'fred'", true )) ;
        ts.addTest(new TestExprBoolean("'fred'@en != 'bert'", true )) ;
        
        ts.addTest(new TestExprBoolean("'chat'@en = 'chat'@fr", false )) ;
        ts.addTest(new TestExprBoolean("'chat'@en = 'maison'@fr", false )) ;

        ts.addTest(new TestExprBoolean("'chat'@en != 'chat'@fr", true )) ;
        ts.addTest(new TestExprBoolean("'chat'@en != 'maison'@fr", true )) ;
        
        ts.addTest(new TestExprBoolean("'chat'@en = 'chat'@EN", true )) ;
        ts.addTest(new TestExprBoolean("'chat'@en = 'chat'@en-uk", false )) ;
        ts.addTest(new TestExprBoolean("'chat'@en != 'chat'@EN", false )) ;
        ts.addTest(new TestExprBoolean("'chat'@en != 'chat'@en-uk", true )) ;
        
        ts.addTest(new TestExprBoolean("'chat'@en = <http://example/>", false )) ;
        // These have been moved out of the expressions syntax
//        ts.addTest(new TestExprURI("()", RDF.nil.getURI())) ;
//        ts.addTest(new TestExprRDFTerm("[]", TestExpr.NO_FAILURE)) ;
        
        return ts ;
    }

    
    static public TestSuite typedLiterals()
    {
        TestSuite ts = new TestSuite("Type Literals") ; 

        // Typed literals
        // Same types.
        ts.addTest(new TestExprBoolean("'fred'^^<type1> = 'fred'^^<type1>", true )) ;
        
        ts.addTest(new TestExprBoolean("'fred'^^<type1> != 'joe'^^<type1>",  TestExpr.EVAL_FAIL )) ;
        // Different types.
        ts.addTest(new TestExprBoolean("'fred'^^<type1> = 'fred'^^<type2>", TestExpr.EVAL_FAIL )) ;
        
        // NB It is not true that these ar eknown to be different values.
        ts.addTest(new TestExprBoolean("'fred'^^<type1> != 'joe'^^<type2>", TestExpr.EVAL_FAIL )) ;
        
        // true: xsd:string is sameValueAs plain (classic) RDF literal
        ts.addTest(new TestExprBoolean("'fred'^^<"+XSDDatatype.XSDstring.getURI()+"> = 'fred'", true )) ;
        // false: parsing created two RDF literals and these are different 
        ts.addTest(new TestExprBoolean("'fred'^^<type1> = 'fred'", TestExpr.EVAL_FAIL )) ;
        ts.addTest(new TestExprBoolean("'fred'^^<type1> != 'fred'", TestExpr.EVAL_FAIL )) ;

        // Numeric expessions: ignore typing (compatibility with RDF-99) 
        //ts.addTest(new TestExprBoolean("'21'^^<int> = '21'", true )) ;

        ts.addTest(new TestExprNumeric("'21'^^<"+XSDDatatype.XSDinteger.getURI()+">", 21)) ;
        ts.addTest(new TestExprBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> = 21", true)) ;
        ts.addTest(new TestExprBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> = 22", false)) ;

        ts.addTest(new TestExprBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> != 21", false)) ;
        ts.addTest(new TestExprBoolean("'21'^^<"+XSDDatatype.XSDinteger.getURI()+"> != 22", true)) ;
        
        // Unknown types - one unknown
        ts.addTest(new TestExprBoolean("'x'^^<type1>  = 21", TestExpr.EVAL_FAIL)) ;
        ts.addTest(new TestExprBoolean("'x'^^<type1> != 21", TestExpr.EVAL_FAIL)) ;
        
        // One unknown type
        ts.addTest(new TestExprBoolean("'x'^^<http://example/unknown> = true", TestExpr.EVAL_FAIL)) ;
        ts.addTest(new TestExprBoolean("'x'^^<http://example/unknown> != true", TestExpr.EVAL_FAIL)) ;

        // Two unknown types
        ts.addTest(new TestExprBoolean("'x'^^<http://example/unknown> = 'x'^^<http://example/unknown>", true)) ;
        ts.addTest(new TestExprBoolean("'x'^^<http://example/unknown> = 'y'^^<http://example/unknown>", TestExpr.EVAL_FAIL)) ;
        
        ts.addTest(new TestExprBoolean("'x'^^<http://example/unknown> != 'x'^^<http://example/unknown>", false)) ;
        ts.addTest(new TestExprBoolean("'x'^^<http://example/unknown> != 'y'^^<http://example/unknown>", TestExpr.EVAL_FAIL)) ;
        

        
        return ts ;
    }

    static public TestSuite stringExpr()
    {
        TestSuite ts = new TestSuite("String Expressions") ; 
        
        // Escape sequences
        ts.addTest(new TestExprString("'a\\nb'", "a\nb")) ;
        ts.addTest(new TestExprString("'a\\n'", "a\n")) ;
        ts.addTest(new TestExprString("'\\nb'", "\nb")) ;
        
        ts.addTest(new TestExprString("'a\\tb'", "a\tb")) ;
        ts.addTest(new TestExprString("'a\\bb'", "a\bb")) ;
        ts.addTest(new TestExprString("'a\\rb'", "a\rb")) ;
        ts.addTest(new TestExprString("'a\\fb'", "a\fb")) ;
        ts.addTest(new TestExprString("'a\\\\b'", "a\\b")) ;

        ts.addTest(new TestExprString("'a\\u0020a'", "a a")) ;
        ts.addTest(new TestExprString("'a\\uF021'", "a\uF021")) ;

        // Illegal
        ts.addTest(new TestExprString("'a\\X'", "",    TestExpr.PARSE_FAIL)) ;
        ts.addTest(new TestExprString("'aaa\\'", "",   TestExpr.PARSE_FAIL)) ;
        ts.addTest(new TestExprString("'\\u'", "",     TestExpr.PARSE_FAIL)) ;
        ts.addTest(new TestExprString("'\\u111'", "",  TestExpr.PARSE_FAIL)) ;
        
        // Escapes in strings
        //ts.addTest(new TestExprBoolean("\"fred\\1\" = 'fred1'", true )) ;
        //ts.addTest(new TestExprBoolean("\"fred2\" = 'fred\\2'", true )) ;
        ts.addTest(new TestExprBoolean("'fred\\\\3' != \"fred3\"", true )) ;
        

        // Strings are not URIs : issue over whether it is an eval failure or not.
        if ( false )
        {
            // Failure case 
            ts.addTest(new TestExprBoolean("'urn:fred' = <urn:fred>", TestExpr.EVAL_FAIL )) ;
            ts.addTest(new TestExprBoolean("'urn:fred' != <urn:fred>", TestExpr.EVAL_FAIL )) ;
        }
        else
        {
            // testable case 
            ts.addTest(new TestExprBoolean("'urn:fred' = <urn:fred>", false )) ;
            ts.addTest(new TestExprBoolean("'urn:fred' != <urn:fred>", true )) ;
        }
        ts.addTest(new TestExprBoolean("REGEX('aabbcc', 'abbc')", true )) ;
        ts.addTest(new TestExprBoolean("REGEX('aabbcc' , 'a..c')", true )) ;
        ts.addTest(new TestExprBoolean("REGEX('aabbcc' , '^aabb')", true )) ;
        ts.addTest(new TestExprBoolean("REGEX('aabbcc' , 'cc$')", true )) ;
        ts.addTest(new TestExprBoolean("! REGEX('aabbcc' , 'abbc')", false )) ;
        
        // For each \ in the regexp need - x2 because of SPARQL escapes, x2 because of Java escapes
        ts.addTest(new TestExprBoolean("REGEX('aa\\\\cc', '\\\\\\\\')", true )) ;
        // Four slahes - \* in the regexp
        ts.addTest(new TestExprBoolean("REGEX('aab*bcc', 'ab\\\\*bc')", true )) ;
        // Eight slashes - so 2 in SPARQL regexp, ie. \\* - any number of \ 
        ts.addTest(new TestExprBoolean("REGEX('aabbcc',  'ab\\\\\\\\*bc')", true )) ;
        ts.addTest(new TestExprBoolean("REGEX('aabbcc', 'B.*B', 'i')", true )) ;

        // These are false because a failure should occur

        ts.addTest(new TestExprBoolean("2 < 'fred'", TestExpr.EVAL_FAIL)) ;

        // No "true" token
        // ts.addTest(new TestBoolean("2 || true", FAILURE_OK, false)) ;
        
        return ts ;
    }
        
    static public TestSuite builtinOps()
    {
        TestSuite ts = new TestSuite("Builtin Operators") ; 
        Binding env = new BindingMap() ;
        env.add(Var.alloc("a"), Node.createLiteral("A")) ;
        env.add(Var.alloc("b"), Node.createAnon()) ;
        env.add(Var.alloc("x"), Node.createURI("urn:x")) ;
        // No ?y
        
        ts.addTest(new TestExprBoolean("datatype('fred') = <"+XSD.xstring.getURI()+">", true)) ;
        
        // Test specials: bound, datatype and lang
        
        ts.addTest(new TestExprBoolean("datatype('fred'^^<urn:foo>) = <urn:foo>", true)) ;
        ts.addTest(new TestExprBoolean("datatype('fred'^^<foo>) = <Foo>", false)) ;

        ts.addTest(new TestExprString("lang('fred'@en)", "en")) ;
        ts.addTest(new TestExprString("lang('fred'@en-uk)", "en-uk")) ;
        ts.addTest(new TestExprString("lang('fred')", "")) ;

        ts.addTest(new TestExprBoolean("isURI(?x)", true, env)) ;
        ts.addTest(new TestExprBoolean("isURI(?a)", false, env)) ;
        ts.addTest(new TestExprBoolean("isURI(?b)", false, env)) ;
        ts.addTest(new TestExprBoolean("isURI(?y)", false, env, TestExpr.EVAL_FAIL)) ;
        
        // These are parse errors
        ts.addTest(new TestExprBoolean("isURI(<urn:foo>)", true, env)) ;
        ts.addTest(new TestExprBoolean("isURI('bar')", false, env)) ;

        
        ts.addTest(new TestExprBoolean("isLiteral(?x)", false, env)) ;
        ts.addTest(new TestExprBoolean("isLiteral(?a)", true, env)) ;
        ts.addTest(new TestExprBoolean("isLiteral(?b)", false, env)) ;
        ts.addTest(new TestExprBoolean("isLiteral(?y)", false, env, TestExpr.EVAL_FAIL)) ;

        ts.addTest(new TestExprBoolean("isBlank(?x)", false, env)) ;
        ts.addTest(new TestExprBoolean("isBlank(?a)", false, env)) ;
        ts.addTest(new TestExprBoolean("isBlank(?b)", true, env)) ;
        ts.addTest(new TestExprBoolean("isBlank(?y)", false, env, TestExpr.EVAL_FAIL)) ;
        
        // Bound
        ts.addTest(new TestExprBoolean("bound(?a)",true, env)) ;
        ts.addTest(new TestExprBoolean("bound(?b)",true, env)) ;
        ts.addTest(new TestExprBoolean("bound(?x)",true, env)) ;
        ts.addTest(new TestExprBoolean("bound(?y)",false, env)) ;

        // Str
        ts.addTest(new TestExprString("str(<urn:x>)", "urn:x")) ;
        ts.addTest(new TestExprString("str('')", "")) ;
        ts.addTest(new TestExprString("str(15)", "15")) ;
        // Chkec that exact lexcial form is preserved
        ts.addTest(new TestExprString("str('15.20'^^<"+XSDDatatype.XSDdouble.getURI()+">)", "15.20")) ;
        ts.addTest(new TestExprString("str('lex'^^<x:unknown>)", "lex")) ;
        
        // sameTerm
        ts.addTest(new TestExprBoolean("sameTerm(1,1)", true, env)) ;
        ts.addTest(new TestExprBoolean("sameTerm(1,1.0)", false, env)) ;
        
        return ts ;
    }
    
    static public TestSuite castExpr()
    {
        String xsd = XSDDatatype.XSD+"#" ;
        TestSuite ts = new TestSuite("XSD Casts") ; 
        
        // Note: TestExprNumeric is assymetric: if the right result is an integer, 
        // the evaluated result can't be a double. 
        ts.addTest(new TestExprNumeric("<"+xsd+"integer>('3')", 3)) ;
        ts.addTest(new TestExprNumeric("<"+xsd+"byte>('3')", 3)) ;
        ts.addTest(new TestExprNumeric("<"+xsd+"int>('3')", 3)) ;
        
        ts.addTest(new TestExprBoolean("<"+xsd+"double>('3') = 3", true)) ;
        ts.addTest(new TestExprBoolean("<"+xsd+"float>('3') = 3", true)) ;
        ts.addTest(new TestExprBoolean("<"+xsd+"double>('3') = <"+xsd+"float>('3')", true)) ;

        ts.addTest(new TestExprBoolean("<"+xsd+"double>(str('3')) = 3", true)) ;
        
        return ts ;
    }



}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
