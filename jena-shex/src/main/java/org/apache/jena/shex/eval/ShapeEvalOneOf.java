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

package org.apache.jena.shex.eval;

import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.expressions.TripleExpression;
import org.apache.jena.shex.expressions.TripleExprOneOf;
import org.apache.jena.shex.sys.ValidationContext;

/*package*/ class ShapeEvalOneOf {

    static boolean matchesOneOf(ValidationContext vCxt, Set<Triple> matchables, Node node, TripleExprOneOf oneOf, Set<Node> extras) {
        //XOR semantics
        //        int matchCount = 0;
        //        for ( TripleExpression tripleExpr : oneOf.expressions() ) {
        //            if ( matches(vCxt, matchables, node, tripleExpr, extras) ) {
        //                matchCount++;
        //                if ( matchCount > 1 )
        //                    break;
        //            }
        //        }
        //        return matchCount == 1;

        //        // Any semantics.
        //        return oneOf.expressions().stream().anyMatch(ex->matches(vCxt, matchables, node, ex, extras));

        // OR semantics
        for ( TripleExpression tripleExpr : oneOf.expressions() ) {
            if ( ShapeEval.matches(vCxt, matchables, node, tripleExpr, extras) ) {
                return true;
            }
        }
        return false;
    }

}
