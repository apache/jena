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

package org.apache.jena.sparql.expr;

/*package*/ class NodeValueCompare {}
//// Old (upto 4.6.1 comprare code.)

// XXX Delete!

///*package*/ class NodeValueCompare {
//    private NodeValueCompare() {}
//
//    // ----------------------------------------------------------------
//    // ---- sameValueAs
//
//    // Disjoint value spaces : dateTime and dates are not comparable
//    // Every langtag implies another value space as well.
//
//    /**
//     * Return true if the two NodeValues are known to be the same value return false
//     * if known to be different values, throw ExprEvalException otherwise
//     */
//    /*public*/private static boolean sameValueAs(NodeValue nv1, NodeValue nv2) {
//        if ( nv1 == null || nv2 == null )
//            throw new ARQInternalErrorException("Attempt to sameValueAs on a null");
//
//        ValueSpaceClassification compType = NodeValue.classifyValueOp(nv1, nv2);
//
//        // Special case - date/dateTime comparison is affected by timezones and may
//        // be
//        // indeterminate based on the value of the dateTime/date.
//
//        switch (compType) {
//            case VSPACE_NUM :
//                return XSDFuncOp.compareNumeric(nv1, nv2) == CMP_EQUAL;
//            case VSPACE_DATETIME :
//            case VSPACE_DATE :
//            case VSPACE_TIME :
//            case VSPACE_G_YEAR :
//            case VSPACE_G_YEARMONTH :
//            case VSPACE_G_MONTH :
//            case VSPACE_G_MONTHDAY :
//            case VSPACE_G_DAY : {
//                int x = XSDFuncOp.compareDateTime(nv1, nv2);
//                if ( x == Expr.CMP_INDETERMINATE )
//                    throw new ExprNotComparableException("Indeterminate dateTime comparison");
//                return x == CMP_EQUAL;
//            }
//            case VSPACE_DURATION : {
//                int x = XSDFuncOp.compareDuration(nv1, nv2);
//                if ( x == Expr.CMP_INDETERMINATE )
//                    throw new ExprNotComparableException("Indeterminate duration comparison");
//                return x == CMP_EQUAL;
//            }
//
//            case VSPACE_STRING :
//                return XSDFuncOp.compareString(nv1, nv2) == CMP_EQUAL;
//            case VSPACE_BOOLEAN :
//                return XSDFuncOp.compareBoolean(nv1, nv2) == CMP_EQUAL;
//
//            case VSPACE_TRIPLE_TERM : {
//                Triple t1 = nv1.getNode().getTriple();
//                Triple t2 = nv2.getNode().getTriple();
//                return nSameValueAs(t1.getSubject(),   t2.getSubject()) &&
//                       nSameValueAs(t1.getPredicate(), t2.getPredicate()) &&
//                       nSameValueAs(t1.getObject(),    t2.getObject());
//            }
//
//            case VSPACE_LANG :
//            case VSPACE_NODE :
//                // Two non-literals
//                return NodeFunctions.sameTerm(nv1.getNode(), nv2.getNode());
//
//            case VSPACE_UNKNOWN : {
//                // One or two unknown value spaces, or one has a lang tag (but not
//                // both).
//                Node node1 = nv1.asNode();
//                Node node2 = nv2.asNode();
//
//                if ( !SystemARQ.ValueExtensions )
//                    // No value extensions => raw rdfTermEquals
//                    return NodeFunctions.rdfTermEquals(node1, node2);
//
//                // Some "value spaces" are know to be not equal (no overlap).
//                // Like one literal with a language tag, and one without can't be
//                // sameAs.
//
//                if ( !node1.isLiteral() || !node2.isLiteral() )
//                    // Can't both be non-literals - that's VSPACE_NODE
//                    // One or other not a literal => not sameAs
//                    return false;
//
//                // Two literals at this point.
//
//                if ( NodeFunctions.sameTerm(node1, node2) )
//                    return true;
//
//                if ( !node1.getLiteralLanguage().equals("") || !node2.getLiteralLanguage().equals("") )
//                    // One had lang tag but weren't sameNode => not equals
//                    return false;
//
//                raise(new ExprEvalException("Unknown equality test: " + nv1 + " and " + nv2));
//                throw new ARQInternalErrorException("raise returned (sameValueAs)");
//            }
//            case VSPACE_SORTKEY :
//                return nv1.getSortKey().compareTo(nv2.getSortKey()) == 0;
//
//            case VSPACE_DIFFERENT :
//                // Known to be incompatible.
//                if ( !SystemARQ.ValueExtensions && (nv1.isLiteral() && nv2.isLiteral()) )
//                    raise(new ExprEvalException("Incompatible: " + nv1 + " and " + nv2));
//                return false;
//        }
//
//        throw new ARQInternalErrorException("sameValueAs failure " + nv1 + " and " + nv2);
//    }
//
//    /** Worker for sameAs. */
//    private static boolean nSameValueAs(Node n1, Node n2) {
//        NodeValue nv1 = NodeValue.makeNode(n1);
//        NodeValue nv2 = NodeValue.makeNode(n2);
//        return sameValueAs(nv1, nv2);
//    }
//
//    /**
//     * Return true if the two Nodes are known to be different, return false if the
//     * two Nodes are known to be the same, else throw ExprEvalException
//     */
//    /*package*/private static boolean notSameAs(Node n1, Node n2) {
//        return notSameValueAs(NodeValue.makeNode(n1), NodeValue.makeNode(n2));
//    }
//
//    /**
//     * Return true if the two NodeValues are known to be different, return false if
//     * the two NodeValues are known to be the same, else throw ExprEvalException
//     */
//    /*package*/private static boolean notSameValueAs(NodeValue nv1, NodeValue nv2) {
//        return !sameValueAs(nv1, nv2);
//    }
//
//    // ----------------------------------------------------------------
//    // compare
//
//    // Compare by value code is here
//    // NodeUtils.compareRDFTerms for syntactic comparison
//
//    /**
//     * Compare by value if possible else compare by kind/type/lexical form Only use
//     * when you want an ordering regardless of form of NodeValue, for example in
//     * ORDER BY
//     *
//     * @param nv1
//     * @param nv2
//     * @return negative, 0, or positive for less than, equal, greater than.
//     */
//
//    /*package*/private static int $_compareAlways(NodeValue nv1, NodeValue nv2) {
//        try {
//            int x = compare(nv1, nv2, true);
//            // Same?
//            if ( x != CMP_EQUAL )
//                return x;
//        } catch (ExprNotComparableException ex) { /* Drop through */ }
//        return NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
//    }
//
//    /**
//     * Compare by value (and only value) if possible. Supports &lt;, &lt;=, &gt;,
//     * &gt;= but not = nor != (which are sameValueAs and notSameValueAs)
//     *
//     * @param nv1
//     * @param nv2
//     * @return Expr.CMP_LESS(-1), Expr.CMP_EQUAL(0) or Expr.CMP_GREATER(+1)
//     * @throws ExprNotComparableException for Expr.CMP_INDETERMINATE(+2)
//     */
//    /*package*/private static int $_compare(NodeValue nv1, NodeValue nv2) {
//        if ( nv1 == null || nv2 == null )
//            // raise(new ExprEvalException("Attempt to notSameValueAs on null") ;
//            throw new ARQInternalErrorException("Attempt to compare on null");
//        int x = compare(nv1, nv2, false);
//        if ( x == Expr.CMP_INDETERMINATE )
//            throw new ExprNotComparableException("Indeterminate comparison");
//        return x;
//    }
//
//    // E_GreaterThan/E_LessThan/E_GreaterThanOrEqual/E_LessThanOrEqual
//    // ==> compare(nv1, nv2) => compare (nv1, nv2, false)
//
//    // BindingComparator => compareAlways(nv1, nv2) => compare (nv1, nv2, true)
//
//    // E_Equals calls NodeValue.sameAs() ==>
//
//    // sortOrderingCompare means that the comparison should do something with
//    // normally unlike things,
//    // and split plain strings from xsd:strings.
//
//    private static int compare(NodeValue nv1, NodeValue nv2, boolean sortOrderingCompare) {
//        if ( nv1 == null && nv2 == null )
//            return CMP_EQUAL;
//
//        if ( nv1 == null )
//            return Expr.CMP_LESS;
//        if ( nv2 == null )
//            return Expr.CMP_GREATER;
//
//        ValueSpaceClassification compType = NodeValue.classifyValueOp(nv1, nv2);
//
//        // Special case - date/dateTime comparison is affected by timezones and may
//        // be
//        // indeterminate based on the value of the dateTime/date.
//        // Do this first, so that indeterminate can drop through to a general
//        // ordering.
//
//        switch (compType) {
//            case VSPACE_DATETIME :
//            case VSPACE_DATE :
//            case VSPACE_TIME :
//            case VSPACE_G_DAY :
//            case VSPACE_G_MONTH :
//            case VSPACE_G_MONTHDAY :
//            case VSPACE_G_YEAR :
//            case VSPACE_G_YEARMONTH : {
//                int x = XSDFuncOp.compareDateTime(nv1, nv2);
//                if ( x != Expr.CMP_INDETERMINATE )
//                    return x;
//                // Indeterminate => can't compare as strict values.
//                compType = ValueSpaceClassification.VSPACE_DIFFERENT;
//                break;
//            }
//            case VSPACE_DURATION : {
//                int x = XSDFuncOp.compareDuration(nv1, nv2);
//                // Fix up - Java (Oracle java7 at least) returns "equals" for
//                // "P1Y"/"P365D" and "P1M"/"P28D", and others split over
//                // YearMonth/DayTime.
//
//                // OR return Expr.CMP_INDETERMINATE ??
//                if ( x == CMP_EQUAL ) {
//                    Duration d1 = nv1.getDuration();
//                    Duration d2 = nv2.getDuration();
//                    if ( (XSDFuncOp.isDayTime(d1) && XSDFuncOp.isYearMonth(d2)) || (XSDFuncOp.isDayTime(d2) && XSDFuncOp.isYearMonth(d1)) )
//                        x = Expr.CMP_INDETERMINATE;
//                }
//                if ( x != Expr.CMP_INDETERMINATE )
//                    return x;
//                compType = ValueSpaceClassification.VSPACE_DIFFERENT;
//                break;
//            }
//
//            // No special cases.
//            case VSPACE_BOOLEAN :
//            case VSPACE_DIFFERENT :
//            case VSPACE_LANG :
//            case VSPACE_TRIPLE_TERM :
//            case VSPACE_NODE :
//            case VSPACE_NUM :
//            case VSPACE_STRING :
//            case VSPACE_SORTKEY :
//            case VSPACE_UNKNOWN :
//                // Drop through.
//        }
//
//        switch (compType) {
//            case VSPACE_DATETIME :
//            case VSPACE_DATE :
//            case VSPACE_TIME :
//            case VSPACE_G_DAY :
//            case VSPACE_G_MONTH :
//            case VSPACE_G_MONTHDAY :
//            case VSPACE_G_YEAR :
//            case VSPACE_G_YEARMONTH :
//            case VSPACE_DURATION :
//                throw new ARQInternalErrorException("Still seeing date/dateTime/time/duration compare type");
//
//            case VSPACE_NUM :
//                return XSDFuncOp.compareNumeric(nv1, nv2);
//            case VSPACE_STRING : {
//                int cmp = XSDFuncOp.compareString(nv1, nv2);
//
//                if ( !sortOrderingCompare )
//                    return cmp;
//                if ( cmp != CMP_EQUAL )
//                    return cmp;
//
//                // Equality.
//                if ( JenaRuntime.isRDF11 )
//                    // RDF 1.1 : No literals without datatype.
//                    return cmp;
//
//                // RDF 1.0
//                // Split plain literals and xsd:strings for sorting purposes.
//                // Same by string value.
//                String dt1 = nv1.asNode().getLiteralDatatypeURI();
//                String dt2 = nv2.asNode().getLiteralDatatypeURI();
//                if ( dt1 == null && dt2 != null )
//                    return Expr.CMP_LESS;
//                if ( dt2 == null && dt1 != null )
//                    return Expr.CMP_GREATER;
//                return CMP_EQUAL;  // Both plain or both xsd:string.
//            }
//            case VSPACE_SORTKEY :
//                return nv1.getSortKey().compareTo(nv2.getSortKey());
//
//            case VSPACE_BOOLEAN :
//                return XSDFuncOp.compareBoolean(nv1, nv2);
//
//            case VSPACE_LANG : {
//                // Two literals, both with language tags.
//                Node node1 = nv1.asNode();
//                Node node2 = nv2.asNode();
//
//                int x = StrUtils.strCompareIgnoreCase(node1.getLiteralLanguage(), node2.getLiteralLanguage());
//                if ( x != CMP_EQUAL ) {
//                    // Different lang tags
//                    if ( !sortOrderingCompare )
//                        raise(new ExprNotComparableException("Can't compare (different languages) " + nv1 + " and " + nv2));
//                    // Different lang tags - sorting
//                    return x;
//                }
//
//                // same lang tag (case insensitive)
//                x = StrUtils.strCompare(node1.getLiteralLexicalForm(), node2.getLiteralLexicalForm());
//                if ( x != CMP_EQUAL )
//                    return x;
//                // Same lexical forms, same lang tag by value
//                // Try to split by syntactic lang tags.
//                x = StrUtils.strCompare(node1.getLiteralLanguage(), node2.getLiteralLanguage());
//                // Maybe they are the same after all!
//                // Should be node.equals by now.
//                if ( x == CMP_EQUAL && !NodeFunctions.sameTerm(node1, node2) )
//                    throw new ARQInternalErrorException("Looks like the same (lang tags) but not node equals");
//                return x;
//            }
//
//            case VSPACE_TRIPLE_TERM : {
//                Triple t1 = nv1.asNode().getTriple();
//                Triple t2 = nv2.asNode().getTriple();
//                int x = nCompare(t1.getSubject(), t2.getSubject(), sortOrderingCompare);
//                if ( x != CMP_EQUAL )
//                    return x;
//                x = nCompare(t1.getPredicate(), t2.getPredicate(), sortOrderingCompare);
//                if ( x != CMP_EQUAL )
//                    return x;
//                return nCompare(t1.getObject(), t2.getObject(), sortOrderingCompare);
//            }
//
//            case VSPACE_NODE :
//                // Two non-literals don't compare except for sorting.
//                if ( sortOrderingCompare )
//                    return NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
//                else {
//                    raise(new ExprNotComparableException("Can't compare (nodes) " + nv1 + " and " + nv2));
//                    throw new ARQInternalErrorException("NodeValue.raise returned");
//                }
//
//            case VSPACE_UNKNOWN : {
//                // One or two unknown value spaces.
//                Node node1 = nv1.asNode();
//                Node node2 = nv2.asNode();
//                // Two unknown literals can be equal.
//                if ( NodeFunctions.sameTerm(node1, node2) )
//                    return CMP_EQUAL;
//
//                if ( sortOrderingCompare )
//                    return NodeCmp.compareRDFTerms(node1, node2);
//
//                raise(new ExprNotComparableException("Can't compare " + nv1 + " and " + nv2));
//                throw new ARQInternalErrorException("NodeValue.raise returned");
//            }
//
//            case VSPACE_DIFFERENT :
//                // Two literals, from different known value spaces
//                if ( sortOrderingCompare )
//                    return NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
//                return Expr.CMP_INDETERMINATE;
//// // ***
//// raise(new ExprNotComparableException("Can't compare (incompatible value
//// spaces)"+nv1+" and "+nv2)) ;
//// throw new ARQInternalErrorException("NodeValue.raise returned") ;
//        }
//        throw new ARQInternalErrorException("Compare failure " + nv1 + " and " + nv2);
//    }
//
//    private static int nCompare(Node n1, Node n2, boolean sortOrderingCompare) {
//        if ( n1.equals(n2) )
//            return CMP_EQUAL;
//        NodeValue nv1 = NodeValue.makeNode(n1);
//        NodeValue nv2 = NodeValue.makeNode(n2);
//        return compare(nv1, nv2, sortOrderingCompare);
//    }
//}
