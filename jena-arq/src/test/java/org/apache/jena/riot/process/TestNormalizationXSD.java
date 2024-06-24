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

package org.apache.jena.riot.process;

import org.apache.jena.graph.Node ;
import org.apache.jena.riot.process.normalize.NormalizeRDFTerms;
import org.junit.Test;

public class TestNormalizationXSD extends AbstractTestNormalization {
    @Override
    protected Node normalize(Node n1) {
        Node n2 = NormalizeRDFTerms.getXSD().normalize(n1);
        return n2;
    }

    // xsd:decimal
    // XSD 1.1 - no ".0"
    // XSD 1.0 - does have ".0"

    @Test @Override public void normalize_decimal_01() { normalize("'0.0'^^xsd:decimal", "'0'^^xsd:decimal") ; }
    @Test @Override public void normalize_decimal_02() { normalize("'0'^^xsd:decimal", "'0'^^xsd:decimal") ; }
    @Test @Override public void normalize_decimal_03() { normalize("'1.0'^^xsd:decimal", "'1'^^xsd:decimal") ; }

    @Test @Override public void normalize_decimal_06() { normalize("'-0.0'^^xsd:decimal", "'0'^^xsd:decimal") ; }
    @Test @Override public void normalize_decimal_07() { normalize("'+0.0'^^xsd:decimal", "'0'^^xsd:decimal") ; }
    @Test @Override public void normalize_decimal_08() { normalize("'+00560.0'^^xsd:decimal", "'560'^^xsd:decimal") ; }

    @Test @Override public void normalize_decimal_10() { normalize("'-1.0'^^xsd:decimal", "'-1'^^xsd:decimal") ; }
    @Test @Override public void normalize_decimal_11() { normalize("'+1.0'^^xsd:decimal", "'1'^^xsd:decimal") ; }

    @Test @Override public void normalize_decimal_14() { normalize("'-1'^^xsd:decimal", "'-1'^^xsd:decimal") ; }
    @Test @Override public void normalize_decimal_15() { normalize("'0'^^xsd:decimal",  "'0'^^xsd:decimal") ; }

    @Test @Override public void normalize_double_01() { normalize("'1e0'^^xsd:double", "'1.0E0'^^xsd:double") ; }
    @Test @Override public void normalize_double_02() { normalize("'0e0'^^xsd:double", "'0.0E0'^^xsd:double") ; }
    @Test @Override public void normalize_double_03() { normalize("'00e0'^^xsd:double", "'0.0E0'^^xsd:double") ; }
    @Test @Override public void normalize_double_04() { normalize("'0e00'^^xsd:double", "'0.0E0'^^xsd:double") ; }
    @Test @Override public void normalize_double_05() { normalize("'10e0'^^xsd:double", "'1.0E1'^^xsd:double") ; }
    @Test @Override public void normalize_double_06() { normalize("'1e1'^^xsd:double", "'1.0E1'^^xsd:double") ; }

    @Test @Override public void normalize_double_10() { normalize("'-1e+0'^^xsd:double", "'-1.0E0'^^xsd:double") ; }
    @Test @Override public void normalize_double_11() { normalize("'+0e01'^^xsd:double", "'0.0E0'^^xsd:double") ; }
    @Test @Override public void normalize_double_12() { normalize("'1000'^^xsd:double", "'1.0E3'^^xsd:double") ; }
    @Test @Override public void normalize_double_13() { normalize("'+1.e4'^^xsd:double", "'1.0E4'^^xsd:double"); }

    @Test @Override public void normalize_double_20() { normalize("'1e-3'^^xsd:double", "1.0E-3") ; }

    @Test @Override public void normalize_double_25() { normalize("'-1.23456789012345678901234'^^xsd:double", "-1.2345678901234567E0") ; }

    @Test @Override public void normalize_double_34() { normalize("'-0'^^xsd:double",  "'-0.0E0'^^xsd:double"); }
    @Test @Override public void normalize_double_35() { normalize("'+0'^^xsd:double",  "'0.0E0'^^xsd:double"); }

    @Test @Override public void normalize_float_01() { normalize("'1e0'^^xsd:float",   "'1.0E0'^^xsd:float"); }
    @Test @Override public void normalize_float_02() { normalize("'0e0'^^xsd:float",   "'0.0E0'^^xsd:float"); }
    @Test @Override public void normalize_float_03() { normalize("'00e0'^^xsd:float",  "'0.0E0'^^xsd:float"); }
    @Test @Override public void normalize_float_04() { normalize("'0e00'^^xsd:float",  "'0.0E0'^^xsd:float"); }
    @Test @Override public void normalize_float_05() { normalize("'10e0'^^xsd:float",  "'1.0E1'^^xsd:float"); }
    @Test @Override public void normalize_float_06() { normalize("'1e01'^^xsd:float",  "'1.0E1'^^xsd:float"); }

    @Test @Override public void normalize_float_20() { normalize("'1e-3'^^xsd:float",  "'1.0E-3'^^xsd:float") ; }

    // Excessive precision
    @Test @Override public void normalize_float_25() { normalize("'1.234567890'^^xsd:float", "'1.234568E0'^^xsd:float"); }

    @Test @Override public void normalize_float_34() { normalize("'-0'^^xsd:float",     "'-0.0E0'^^xsd:float"); }
    @Test @Override public void normalize_float_35() { normalize("'+0'^^xsd:float",     "'0.0E0'^^xsd:float"); }


}
