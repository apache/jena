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

package org.apache.jena.graph.test;

import static org.junit.Assert.* ;
import junit.framework.JUnit4TestAdapter ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.impl.LiteralLabel ;
import org.apache.jena.graph.impl.LiteralLabelFactory ;
import org.junit.Test ;

// See also TestTypedLiterals
/** Tests for LiteralLabel.sameValueAs
 *  These tests should work for RDF 1.0 and RDF 1.1
 */
public class TestLiteralLabelSameValueAs
{
    public TestLiteralLabelSameValueAs() {}

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestLiteralLabelSameValueAs.class) ;
    }

    private static void testSameValueAs(LiteralLabel lit1, LiteralLabel lit2, boolean sameValue) {
        assertEquals("lit1 sameValueAs lit2", sameValue, lit1.sameValueAs(lit2)) ;
        assertEquals("lit2 sameValueAs lit1", sameValue, lit2.sameValueAs(lit1)) ;
        if ( ! sameValue ) {
            // ! SameValue => ! equals
            assertFalse(lit1.equals(lit2)) ;
            assertFalse(lit2.equals(lit1)) ;
        }
    }

    private static LiteralLabel gen(String lex, RDFDatatype dt) {
        return LiteralLabelFactory.create(lex, dt) ;
    }

    private static LiteralLabel gen(String lex, String lang) {
        return LiteralLabelFactory.createLang(lex, lang) ;
    }

    private static LiteralLabel gen(String lex) {
        return LiteralLabelFactory.create(lex, XSDDatatype.XSDstring) ;
    }

    static RDFDatatype dtUnknown = NodeFactory.getType("http://example/unknown") ;

    // Strings.
    @Test public void literalLabel_string_01()  { testSameValueAs(gen("abc"), gen("abc"), true) ; }
    @Test public void literalLabel_string_02()  { testSameValueAs(gen("abc"), gen("abcd"), false) ; }
    @Test public void literalLabel_string_03()  { testSameValueAs(gen("abc"), gen("abc", XSDDatatype.XSDstring), true) ; }

    // Lang
    @Test public void literalLabel_lang_01()    { testSameValueAs(gen("abc", "en"), gen("abc", "en-uk"), false) ; }
    @Test public void literalLabel_lang_02()    { testSameValueAs(gen("abc", "en"), gen("abc", "EN"), true) ; }
    @Test public void literalLabel_lang_03()    { testSameValueAs(gen("abc", "en"), gen("abc", "en-uk"), false) ; }

    // Decimal derived types.
    @Test public void literalLabel_numeric_01() { testSameValueAs(gen("01", XSDDatatype.XSDinteger), gen("+1", XSDDatatype.XSDinteger), true) ; }
    @Test public void literalLabel_numeric_02() { testSameValueAs(gen("01", XSDDatatype.XSDinteger), gen("+1", XSDDatatype.XSDint), true) ; }
    @Test public void literalLabel_numeric_03() { testSameValueAs(gen("-01", XSDDatatype.XSDinteger), gen("-1", XSDDatatype.XSDdecimal), true) ; }
    @Test public void literalLabel_numeric_04() { testSameValueAs(gen("-01", XSDDatatype.XSDinteger), gen("-1.0", XSDDatatype.XSDdecimal), true) ; }

    @Test public void literalLabel_numeric_05() { testSameValueAs(gen("+1", XSDDatatype.XSDdouble), gen("1e0", XSDDatatype.XSDdouble), true) ; }
    @Test public void literalLabel_numeric_06() { testSameValueAs(gen("-10e-1", XSDDatatype.XSDfloat), gen("-0.1e1", XSDDatatype.XSDfloat), true) ; }

    // Not across double/integer
    @Test public void literalLabel_numeric_10() { testSameValueAs(gen("1", XSDDatatype.XSDinteger), gen("1e0", XSDDatatype.XSDdouble), false) ; }

    // Unknown
    @Test public void literalLabel_unknown_01() { testSameValueAs(gen("abc", dtUnknown), gen("abc", dtUnknown), true) ; }
    @Test public void literalLabel_unknown_02() { testSameValueAs(gen("abc", dtUnknown), gen("xyz", dtUnknown), false) ; }
    @Test public void literalLabel_unknown_03() { testSameValueAs(gen("1", XSDDatatype.XSDinteger), gen("1", dtUnknown), false) ; }

    // Bad lexical forms.
    @Test public void literalLabel_bad_01()     { testSameValueAs(gen("abc",XSDDatatype.XSDinteger), gen("abc"), false) ; }
    @Test public void literalLabel_bad_03()     { testSameValueAs(gen("abc", XSDDatatype.XSDinteger), gen("abc", dtUnknown), false) ; }


}
