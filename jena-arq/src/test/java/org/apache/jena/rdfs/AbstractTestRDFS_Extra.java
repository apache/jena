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

package org.apache.jena.rdfs;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public abstract class AbstractTestRDFS_Extra
    extends AbstractTestRDFS_Find
{
    public AbstractTestRDFS_Extra(String testLabel) {
        super(testLabel);
    }

    @TestFactory
    @Disabled("Needs investigation!")
    public List<DynamicTest> testSubPropertyOfRdfType01() {
        String schemaStr = """
            PREFIX :     <http://ex.org/>
            PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            :directType        rdfs:subPropertyOf rdf:type .
        """;

        String dataStr = """
            PREFIX :     <http://ex.org/>

            :fido :directType :Dog .
        """;

        return prepareRdfsFindTests(schemaStr, dataStr).build();
    }

    @TestFactory
    public List<DynamicTest> testSubClassOf01() {
        String schemaStr = """
            PREFIX :     <http://ex.org/>
            PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            :Dog        rdfs:subClassOf rdf:Mammal .
        """;

        String dataStr = """
            PREFIX :     <http://ex.org/>

            :fido   a           :Dog .
        """;

        return prepareRdfsFindTests(schemaStr, dataStr).build();
    }

    @TestFactory
    public List<DynamicTest> testRange01() {
        String schemaStr = """
            PREFIX :     <http://ex.org/>
            PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            :owner rdfs:range :Person .
        """;

        String dataStr = """
            PREFIX :     <http://ex.org/>

            :fido :owner :alice .
        """;

        return prepareRdfsFindTests(schemaStr, dataStr).build();
    }

    @TestFactory
    public List<DynamicTest> testRangeWithLiteral01() {
        String schemaStr = """
            PREFIX :     <http://ex.org/>
            PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

            :name rdfs:range :Literal .
        """;

        String dataStr = """
            PREFIX :     <http://ex.org/>

            :fido :name "Fido" .
        """;

        return prepareRdfsFindTests(schemaStr, dataStr).build();
    }

    @TestFactory
    public List<DynamicTest> testDomain01() {
       String schemaStr = """
           PREFIX :     <http://ex.org/>
           PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
           PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

           :owner rdfs:domain :Pet .
       """;

       String dataStr = """
           PREFIX :     <http://ex.org/>

           :fido :owner :alice .
       """;

       return prepareRdfsFindTests(schemaStr, dataStr).build();
    }
}
