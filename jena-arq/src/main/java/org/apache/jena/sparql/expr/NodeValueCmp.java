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

import static org.apache.jena.sparql.expr.Expr.CMP_EQUAL;
import static org.apache.jena.sparql.expr.Expr.CMP_GREATER;
import static org.apache.jena.sparql.expr.Expr.CMP_INDETERMINATE;
import static org.apache.jena.sparql.expr.Expr.CMP_LESS;

import java.util.Objects;

import javax.xml.datatype.Duration;

import org.apache.jena.JenaRuntime;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.util.NodeCmp;

public class NodeValueCmp {

    // ----------------------------------------------------------------
    // ---- sameValueAs

    // Disjoint value spaces : dateTime and dates are not comparable
    // Every langtag implies another value space as well.

    /**
     * Return true if the two NodeValues are known to be the same value return false
     * if known to be different values, throw ExprEvalException otherwise
     */
    public static boolean sameValueAs(NodeValue nv1, NodeValue nv2) {
        if ( nv1 == null || nv2 == null )
            throw new ARQInternalErrorException("Attempt to sameValueAs on a null");

        ValueSpace compType = NodeValue.classifyValueOp(nv1, nv2);

        if ( nv1 == nv2 )
            return true;
        if ( nv1.hasNode() && nv2.hasNode() ) {
            // Fast path - same RDF term => sameValue
            if ( nv1.getNode().equals(nv2.getNode()) )
                return true;
        }

        // Special case - date/dateTime comparison is affected by timezones and may
        // be indeterminate based on the value of the dateTime/date.

        switch (compType) {
            case VSPACE_NUM :
                return XSDFuncOp.compareNumeric(nv1, nv2) == CMP_EQUAL;

            case VSPACE_DATETIME :
            case VSPACE_DATE :
            case VSPACE_TIME : {
                int x = XSDFuncOp.compareDateTime(nv1, nv2);
                if ( x == Expr.CMP_INDETERMINATE )
                    throw new ExprNotComparableException("Indeterminate dateTime comparison");
                return x == CMP_EQUAL;
            }
            case VSPACE_DURATION : {
                int x = XSDFuncOp.compareDuration(nv1, nv2);
                if ( x == Expr.CMP_INDETERMINATE )
                    throw new ExprNotComparableException("Indeterminate duration comparison");
                return x == CMP_EQUAL;
            }

            case VSPACE_STRING :
                return XSDFuncOp.compareString(nv1, nv2) == CMP_EQUAL;
            case VSPACE_BOOLEAN :
                return XSDFuncOp.compareBoolean(nv1, nv2) == CMP_EQUAL;

            case VSPACE_QUOTED_TRIPLE : {
                Triple t1 = nv1.getNode().getTriple();
                Triple t2 = nv2.getNode().getTriple();
                return nSameValueAs(t1.getSubject(),   t2.getSubject()) &&
                       nSameValueAs(t1.getPredicate(), t2.getPredicate()) &&
                       nSameValueAs(t1.getObject(),    t2.getObject());
            }

            case VSPACE_LANG :
                return NodeFunctions.sameTerm(nv1.asNode(), nv2.asNode());

            case VSPACE_URI :
            case VSPACE_BLANKNODE :
            case VSPACE_UNDEF :
            case VSPACE_VARIABLE :
                // Two non-literals
                return NodeFunctions.sameTerm(nv1.getNode(), nv2.getNode());

            case VSPACE_UNKNOWN : {
                // One or two unknown value spaces, or one has a lang tag (but not both).
                Node node1 = nv1.asNode();
                Node node2 = nv2.asNode();

                if ( !SystemARQ.ValueExtensions )
                    // No value extensions => raw rdfTermEquals
                    return NodeFunctions.rdfTermEquals(node1, node2);

                // Some "value spaces" are know to be not equal (no overlap).
                // Like one literal with a language tag, and one without can't be
                // sameAs.

                if ( !node1.isLiteral() || !node2.isLiteral() )
                    // Can't both be non-literals - that's VSPACE_NODE
                    // One or other not a literal => not sameAs
                    return false;

                // Two literals at this point.

                if ( NodeFunctions.sameTerm(node1, node2) )
                    return true;

                if ( !node1.getLiteralLanguage().equals("") || !node2.getLiteralLanguage().equals("") )
                    // One had lang tag but weren't sameNode => not equals
                    return false;

                raise(new ExprEvalException("Unknown equality test: " + nv1 + " and " + nv2));
                throw new ARQInternalErrorException("raise returned (sameValueAs)");
            }
            case VSPACE_SORTKEY :
                return nv1.getSortKey().compareTo(nv2.getSortKey()) == 0;

            case VSPACE_DIFFERENT :
                // Known to be incompatible.
                if ( !SystemARQ.ValueExtensions && (nv1.isLiteral() && nv2.isLiteral()) )
                    raise(new ExprEvalException("Incompatible: " + nv1 + " and " + nv2));
                // Not same node.
                return false;
        }

        throw new ARQInternalErrorException("sameValueAs failure " + nv1 + " and " + nv2);
    }

    /** Worker for sameAs. */
    private static boolean nSameValueAs(Node n1, Node n2) {
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        return sameValueAs(nv1, nv2);
    }

    /**
     * Return true if the two Nodes are known to be different, return false if the
     * two Nodes are known to be the same, else throw ExprEvalException
     */
    /*package*/private static boolean notSameAs(Node n1, Node n2) {
        return notSameValueAs(NodeValue.makeNode(n1), NodeValue.makeNode(n2));
    }

    /**
     * Return true if the two NodeValues are known to be different, return false if
     * the two NodeValues are known to be the same, else throw ExprEvalException
     */
    /*package*/private static boolean notSameValueAs(NodeValue nv1, NodeValue nv2) {
        return !sameValueAs(nv1, nv2);
    }

    // ==== Compare

    public static int compareByValue(NodeValue nv1, NodeValue nv2) {
        return compareByValue$(nv1, nv2, false);
    }

    public static int compareAlways(NodeValue nv1, NodeValue nv2) {
        return compareWithOrdering(nv1, nv2);
    }

    static int compareByValue$(NodeValue nv1, NodeValue nv2, boolean sortOrderingCompare) {
//        Objects.requireNonNull(nv1);
//        Objects.requireNonNull(nv2);

        if ( nv1 == null && nv2 == null )
            return CMP_EQUAL;

        if ( nv1 == null )
            return CMP_LESS;
        if ( nv2 == null )
            return CMP_GREATER;

        if ( nv1.hasNode() && nv2.hasNode() ) {
            // Fast path - same RDF term => CMP_EQUAL
            if ( nv1.getNode().equals(nv2.getNode()) )
                return CMP_EQUAL;
        }

        ValueSpace compType = classifyValueOp(nv1, nv2) ;

        // Special case - date/dateTime comparison is affected by timezones and may be
        // indeterminate based on the value of the dateTime/date.
        // Do this first, so that indeterminate can drop through to a general ordering.

        switch (compType)
        {
            case VSPACE_UNDEF:
                // Not value compatible.
                if ( sortOrderingCompare )
                    return NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
                raise(new ExprNotComparableException("Can't compare (incompatible value spaces) "+nv1+" and "+nv2)) ;
                throw new ARQInternalErrorException("NodeValue.raise returned") ;

            case VSPACE_DATETIME:
            case VSPACE_DATE:
            case VSPACE_TIME:
                if ( sortOrderingCompare )
                    return XSDFuncOp.compareDateTimeFO(nv1, nv2) ;
                // Must be same URI
                if ( nv1.getDatatypeURI().equals(nv2.getDatatypeURI()) )
                    // Indeterminate possible.
                    return XSDFuncOp.compareDateTime(nv1, nv2);
                raise(new ExprNotComparableException("Can't compare (incompatible temporal value spaces) "+nv1+" and "+nv2)) ;

//            case VSPACE_DURATION_DAYTIME:
//            case VSPACE_DURATION_YEARMONTH:
            case VSPACE_DURATION: {
                int x = XSDFuncOp.compareDuration(nv1, nv2) ;
                // Fix up - Java (Oracle java7 at least) returns "equals" for
                // "P1Y"/"P365D" and "P1M"/"P28D", and others split over
                // YearMonth/DayTime.

                // OR return CMP_INDETERMINATE ??
                if ( x == CMP_EQUAL ) {
                    Duration d1 = nv1.getDuration() ;
                    Duration d2 = nv2.getDuration() ;
                    if ( ( XSDFuncOp.isDayTime(d1) && XSDFuncOp.isYearMonth(d2) ) ||
                            ( XSDFuncOp.isDayTime(d2) && XSDFuncOp.isYearMonth(d1) ) )
                        x = CMP_INDETERMINATE ;
                }
                return x;
            }

            case VSPACE_NUM:
                return XSDFuncOp.compareNumeric(nv1, nv2) ;

            case VSPACE_SORTKEY :
                return nv1.getSortKey().compareTo(nv2.getSortKey());

            case VSPACE_BOOLEAN:
                return XSDFuncOp.compareBoolean(nv1, nv2) ;

            case VSPACE_LANG: {
                // Two literals, both with language tags.
                // Compare by lang tag then by lexical form.
                Node node1 = nv1.asNode() ;
                Node node2 = nv2.asNode() ;

                int x = StrUtils.strCompareIgnoreCase(node1.getLiteralLanguage(), node2.getLiteralLanguage()) ;
                if ( x != CMP_EQUAL ) {
                    // Different lang tags
                    if ( !sortOrderingCompare )
                        raise(new ExprNotComparableException("Can't compare (different languages) " + nv1 + " and " + nv2));
                    // Different lang tags - sorting
                    return x;
                }

                // same lang tag (case insensitive)
                x = strcompare(node1.getLiteralLexicalForm(), node2.getLiteralLexicalForm()) ;
                if ( x != CMP_EQUAL )
                    return x ;
                // Same lexical forms, same lang tag by value
                // Try to split by syntactic lang tags.
                x = StrUtils.strCompare(node1.getLiteralLanguage(), node2.getLiteralLanguage()) ;
                // Maybe they are the same after all!
                // Should be node.equals by now.
                if ( x == CMP_EQUAL  && ! NodeFunctions.sameTerm(node1, node2) )
                    throw new ARQInternalErrorException("Looks like the same (lang tags) but not node equals") ;
                return x ;
            }

            case VSPACE_STRING: {
                int x = XSDFuncOp.compareString(nv1, nv2) ;
                if ( JenaRuntime.isRDF11 )
                    return x;

                // Equality.
                // RDF 1.0
                // Split plain literals and xsd:strings for sorting purposes.
                // Same by string value.
                String dt1 = nv1.asNode().getLiteralDatatypeURI() ;
                String dt2 = nv2.asNode().getLiteralDatatypeURI() ;
                if ( dt1 == null && dt2 != null )
                    return CMP_LESS;
                if ( dt2 == null && dt1 != null )
                    return CMP_GREATER;
                return CMP_EQUAL;  // Both plain or both xsd:string.
            }

            case VSPACE_QUOTED_TRIPLE: {
                Triple t1 = nv1.asNode().getTriple();
                Triple t2 = nv2.asNode().getTriple();
                int x = nCompare(t1.getSubject(), t2.getSubject(), sortOrderingCompare);
                if ( x != CMP_EQUAL )
                    return x;
                x = nCompare(t1.getPredicate(), t2.getPredicate(), sortOrderingCompare);
                if ( x != CMP_EQUAL )
                    return x;
                return nCompare(t1.getObject(), t2.getObject(), sortOrderingCompare);
            }

            case VSPACE_BLANKNODE : {
                String label1 = nv1.asNode().getBlankNodeLabel();
                String label2 = nv2.asNode().getBlankNodeLabel();
                int x = compareRepresentations(label1, label2, sortOrderingCompare);
                if ( x != CMP_INDETERMINATE )
                    return x;
                raise(new ExprNotComparableException("Can't compare blank nodes as values "+nv1+" and "+nv2)) ;
            }

            case VSPACE_URI : {
                String uri1 = nv1.asNode().getURI();
                String uri2 = nv2.asNode().getURI();
                int x = compareRepresentations(uri1, uri2, sortOrderingCompare);
                if ( x != CMP_INDETERMINATE )
                    return x;
                raise(new ExprNotComparableException("Can't compare URIs as values "+nv1+" and "+nv2)) ;
            }
            case VSPACE_VARIABLE : {
                String name1 = nv1.asNode().getName();
                String name2 = nv2.asNode().getName();
                int x = compareRepresentations(name1, name2, sortOrderingCompare);
                if ( x != CMP_INDETERMINATE )
                    return x;
                raise(new ExprNotComparableException("Can't compare valiables as values "+nv1+" and "+nv2)) ;
            }

            case VSPACE_UNKNOWN : {
                // One or two unknown value spaces.
                Node node1 = nv1.asNode() ;
                Node node2 = nv2.asNode() ;
                // Two unknown literals can be equal.
                if ( NodeFunctions.sameTerm(node1, node2) )
                    return CMP_EQUAL ;

                if ( sortOrderingCompare )
                    return NodeCmp.compareRDFTerms(node1, node2) ;

                raise(new ExprNotComparableException("Can't compare "+nv1+" and "+nv2)) ;
                throw new ARQInternalErrorException("NodeValue.raise returned") ;
            }

            case VSPACE_DIFFERENT:
                raise(new ExprNotComparableException("Can't compare "+nv1+" and "+nv2)) ;
        }
        throw new ARQInternalErrorException("Compare failure "+nv1+" and "+nv2) ;
    }

    /**
     * Compare: if values, only equals or not-equals;
     * If ordering, an arbitrary, consistent order.
     */
    private static int compareRepresentations(String string1, String string2, boolean sortOrderingCompare) {
        if ( sortOrderingCompare )
            return strcompare(string1, string2);
        if ( string1.equals(string2) )
            return Expr.CMP_EQUAL;
        return Expr.CMP_UNEQUAL;
    }

    private static ValueSpace classifyValueOp(NodeValue nv1, NodeValue nv2) {
        ValueSpace vs1 = ValueSpace.valueSpace(nv1);
        ValueSpace vs2 = ValueSpace.valueSpace(nv2);
        if ( vs1 == vs2 )
            return vs1;
        return ValueSpace.VSPACE_UNDEF;
    }

    // Point to catch all exceptions.
    public static void raise(ExprException ex) {
        throw ex;
    }

    /**
     * The sort order is to apply the rules in the following order:
     * <ol>
     * <li>Order by value space
     * <li>Within a value space, order by value (if possible)
     * <li>Order by RDF term (syntax)
     * </ol>
     * <p>
     * The implicit timezone for dateTime/date etc is fixed UTC to ensure sorting is the same everywhere.
     *
     */
    public static int compareWithOrdering(NodeValue nv1, NodeValue nv2) {
        Objects.requireNonNull(nv1);
        Objects.requireNonNull(nv2);

        if ( nv1.hasNode() && nv2.hasNode() ) {
            // Fast path - same RDF term => CMP_EQUAL
            if ( nv1.getNode().equals(nv2.getNode()) )
                    return CMP_EQUAL;
        }
        ValueSpace vs1 = ValueSpace.valueSpace(nv1);
        ValueSpace vs2 = ValueSpace.valueSpace(nv2);

        // Unknown is always last in the ordering.
        if ( vs1 == ValueSpace.VSPACE_UNKNOWN || vs2 == ValueSpace.VSPACE_UNKNOWN ) {
            if ( vs1 != ValueSpace.VSPACE_UNKNOWN )
                return CMP_LESS;
            if ( vs2 != ValueSpace.VSPACE_UNKNOWN )
                return CMP_GREATER;
            return NodeCmp$compareRDFTerms(nv1, nv2);
        }

        // XXX G and Date cases.
        int vsOrder = ValueSpace.comparisonOrder(vs1, vs2);

        if ( vsOrder < 0 )
            return CMP_LESS;
        if ( vsOrder > 0 )
            return CMP_GREATER;

        // Compare by value. if equal (and we know nv1 and nv2 are not .equals), or indeterminate, compare by terms.
        // XXX Review NodeValue.compare(nv1, nv2);
        //  Pass ValueSpaces
            // NodeValue.compare(nv1, nv2);

        // XXX Lang tags
        try {
            int x1 = compareByValue(nv1, nv2);
            if ( x1 == CMP_LESS || x1 == CMP_GREATER )
                return x1;
        } catch (ExprNotComparableException ex) {
            // Go on - this is for ordering.
        }
        // EQUAL (but different RDFterms, indeterminate comparision)
        return NodeCmp$compareRDFTerms(nv1, nv2);
    }

    private static int NodeCmp$compareRDFTerms(NodeValue nv1, NodeValue nv2) {
        return NodeCmp.compareRDFTerms(nv1.asNode(), nv2.asNode());
    }

    /** Worker for compare. */
    private static int nCompare(Node n1, Node n2, boolean sortOrderingCompare) {
        if ( n1.equals(n2) )
            return CMP_EQUAL;
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        if ( sortOrderingCompare )
            return compareWithOrdering(nv1, nv2);
        else
            return compareByValue(nv1, nv2);
    }

    private static int strcompare(String string1, String string2) {
        // StrUtils.strCompare(string1, string2) ;
        return result(string1.compareTo(string2));
    }

    /**
     * Return of comparision - always -1,0 or +1
     */
    private static int result(int x) {
        return ( x == 0 ) ? Expr.CMP_EQUAL
            : ( ( x > 0 ) ? Expr.CMP_GREATER : Expr.CMP_LESS );
    }
}
