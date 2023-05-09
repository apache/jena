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

package org.apache.jena.ontapi.vocabulary;

import org.apache.jena.rdf.model.Resource;

/**
 * Vocabulary definition for the <a href="https://www.w3.org/TR/owl2-syntax/">OWL 2 Web Ontology Language</a>
 * See <a href="http://www.w3.org/2002/07/owl#">schema(ttl)</a>
 * Note: {@code owl:real} and {@code owl:rational} are absent in the schema and standard jena vocabulary (don't know why).
 */
public class OWL extends org.apache.jena.vocabulary.OWL2 {

    /**
     * The {@code owl:real} datatype does not directly provide any lexical forms.
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Real_Numbers.2C_Decimal_Numbers.2C_and_Integers">4.1 Real Numbers, Decimal Numbers, and Integers</a>
     */
    public final static Resource real = resource("real");

    /**
     * The {@code owl:rational} datatype supports lexical forms defined by the following grammar
     * (whitespace within the grammar MUST be ignored and MUST NOT be included in the lexical forms of owl:rational,
     * and single quotes are used to introduce terminal symbols):
     * numerator '/' denominator
     *
     * @see <a href="https://www.w3.org/TR/owl2-syntax/#Real_Numbers.2C_Decimal_Numbers.2C_and_Integers">4.1 Real Numbers, Decimal Numbers, and Integers</a>
     */
    public final static Resource rational = resource("rational");

}
