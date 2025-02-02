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

package org.apache.jena.sparql.resultset;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.exec.RowSet;

/**
 * Comparison of ResultSets.
 * Note that reading ResultSets is destructive so consider using {@link ResultSetRewindable}
 * from {@link ResultSetFactory#makeRewindable}.
 *
 *  @deprecated Use {@link ResultsCompare}.
 */
@Deprecated(forRemoval = true)
public class ResultSetCompare
{
    /** Compare two result sets for equivalence.  Equivalence means:
     * A row rs1 has one matching row in rs2, and vice versa.
     * A row is only matched once.
     * Rows match if they have the same variables with the same values.
     * bNodes must map to a consistent other bNodes.  Value comparisons of nodes.
     *
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */

    public static boolean equalsByValue(ResultSet rs1, ResultSet rs2) {
        return equalsByValue(RowSet.adapt(rs1), RowSet.adapt(rs2));
    }

    /** See {@link #equalsByValue(ResultSet, ResultSet)} */
    public static boolean equalsByValue(RowSet rs1, RowSet rs2) {
        return ResultsCompare.equalsByValue(rs1, rs2);
    }

    /**
     * Compare two result sets for equivalence.  Equivalence means:
     * A row rs1 has one matching row in rs2, and vice versa.
     * A row is only matched once.
     * Rows match if they have the same variables with the same values,
     * bNodes must map to a consistent other bNodes.
     * Term comparisons of nodes.
     *
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */

    public static boolean equalsByTerm(ResultSet rs1, ResultSet rs2) {
        return ResultsCompare.equalsByTerm(rs1, rs2);
    }

    /**
     * Compare two result sets for equivalence.  Equivalence means:
     * A row rs1 has one matching row in rs2, and vice versa.
     * A row is only matched once.
     * Rows match if they have the same variables with the same values,
     * bNodes must map to a consistent other bNodes.
     * Term comparisons of nodes.
     *
     * Destructive - rs1 and rs2 are both read, possibly to exhaustion.
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByTerm(RowSet rs1, RowSet rs2) {
        return ResultsCompare.equalsByTerm(rs1, rs2);
    }

    /** Compare two result sets for equivalence.  Equivalence means:
     * Each row in rs1 matches the same index row in rs2.
     * Rows match if they have the same variables with the same values,
     * bNodes must map to a consistent other bNodes.
     * Value comparisons of nodes.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByValueAndOrder(ResultSet rs1, ResultSet rs2) {
        return ResultsCompare.equalsByValueAndOrder(rs1, rs2);
    }

    /** See {@link #equalsByValueAndOrder(ResultSet, ResultSet)} */
    public static boolean equalsByValueAndOrder(RowSet rs1, RowSet rs2) {
        return ResultsCompare.equalsByValueAndOrder(rs1, rs2);
    }

    /** compare two result sets for equivalence.  Equivalence means:
     * Each row in rs1 matches the same index row in rs2.
     * Rows match if they have the same variables with the same values,
     * bNodes must map to a consistent other bNodes.
     * RDF term comparisons of nodes.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsByTermAndOrder(ResultSet rs1, ResultSet rs2) {
        return ResultsCompare.equalsByTermAndOrder(rs1, rs2);
    }

    /** See {@link #equalsByTermAndOrder(ResultSet, ResultSet)} */
    public static boolean equalsByTermAndOrder(RowSet rs1, RowSet rs2) {
        return ResultsCompare.equalsByTermAndOrder(rs1, rs2);
    }

    /**
     * Compare two result sets for exact equality equivalence and order.
     * Blank nodes must have same labels.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsExact(ResultSet rs1, ResultSet rs2) {
        return ResultsCompare.equalsExact(rs1, rs2);
    }

    /**
     * Compare two result sets for exact equality equivalence and order.
     * Blank nodes must have same labels.
     *
     * @param rs1
     * @param rs2
     * @return true if they are equivalent
     */
    public static boolean equalsExact(RowSet rs1, RowSet rs2) {
        return ResultsCompare.equalsByTermAndOrder(rs1, rs2);
    }

    private static boolean compareHeader(RowSet rs1, RowSet rs2) {
        if ( rs1 == null && rs2 == null )
            return true;
        if ( rs1 == null )
            return false;
        if ( rs2 == null )
            return false;
        return ListUtils.equalsUnordered(rs1.getResultVars(), rs2.getResultVars());
    }

    /*
     * Compare two result sets for blank node isomorphism equivalence. Only does RDF
     * term comparison.
     *
     * This method does not handle triple terms in results.
     *
     * @deprecated Use {@link #equalsByTerm(ResultSet, ResultSet)} */
    @Deprecated(forRemoval = true)
    public static boolean isomorphic(ResultSet rs1, ResultSet rs2) {
        Model m1 = RDFOutput.encodeAsModel(rs1);
        Model m2 = RDFOutput.encodeAsModel(rs2);
        return m1.isIsomorphicWith(m2);
    }

    /**
     * Compare two row sets for blank node isomorphism equivalence. Only does RDF
     * term comparison.
     *
     * This method does not handle triple terms in results.
     *
     * @deprecated Use {@link #equalsByTerm(RowSet, RowSet)}
     */
    @Deprecated(forRemoval = true)
    public static boolean isomorphic(RowSet rs1, RowSet rs2) {
        return isomorphic(ResultSet.adapt(rs1), ResultSet.adapt(rs2));
    }

}
