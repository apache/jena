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

package org.apache.jena.rdf12;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;


class LibTestSPARQL12 {

    static void testSPARQLSyntax(Outcome testType, String label, String queryString, boolean verbose) {
        try {
            QueryFactory.create(queryString, Syntax.syntaxSPARQL_12);
            if ( testType == Outcome.BAD )
                fail("Expected failure");
            if ( verbose )
                printString(label, queryString);
            return;
        } catch( QueryParseException ex) {
            if ( testType == Outcome.BAD )
                throw ex;
            if ( verbose )
                printString(label, queryString);
//            String exMsg = parserErrorMessage(ex);
//            System.out.println("**** "+exMsg);
            throw ex;
        } catch( Throwable ex) {
            if ( verbose )
                printString(label, queryString);
            ex.printStackTrace();
            throw ex;
        }
    }

    static String parserErrorMessage(QueryParseException ex) {
        String exMsg = ex.getMessage();
        int idx = exMsg.indexOf("\n");
        if ( idx >= 0 )
            exMsg = exMsg.substring(0, idx);
        return exMsg;
    }

    static void printString(String label, String text) {
        if ( label != null )
            System.out.println("====  "+label);
        System.out.printf("====  12345789 123456789\n");
        String[] x = text.split("\n");
        for ( int i = 0 ; i < x.length ; i++ ) {
            System.out.printf("%2d :: %s\n", i+1, x[i]);
        }
    }
}
