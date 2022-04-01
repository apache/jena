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

package org.apache.jena.shex.expressions;

import static java.lang.String.format;

import java.util.Objects;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.shex.sys.ReportItem;
import org.apache.jena.shex.sys.ShexLib;
import org.apache.jena.shex.sys.ValidationContext;

public class NumLengthConstraint extends NodeConstraintComponent {

    private final NumLengthKind lengthType;
    private final int length;

    public NumLengthConstraint(NumLengthKind lengthType, int len) {
        Objects.requireNonNull(lengthType);
        this.lengthType = lengthType;
        this.length = len;
    }

    public NumLengthKind getLengthType() {
        return lengthType;
    }

    public int getLength() {
        return length;
    }

    @Override
    public ReportItem nodeSatisfies(ValidationContext vCxt, Node n) {
        if ( ! n.isLiteral() ) {
            String msg = format("NumericConstraint: Not numeric: %s ", ShexLib.displayStr(n));
            return new ReportItem(msg, n);
        }

        RDFDatatype rdfDT = n.getLiteralDatatype();
        if ( ! ( rdfDT instanceof XSDDatatype ) ) {
            String msg = format("NumericConstraint: Not a numeric: %s ", ShexLib.displayStr(n));
            return new ReportItem(msg, n);
        }

        if ( XSDDatatype.XSDfloat.equals(rdfDT) || XSDDatatype.XSDdouble.equals(rdfDT) ) {
            String msg = format("NumericConstraint: Numeric not compatible with xsd:decimal: %s ", ShexLib.displayStr(n));
            return new ReportItem(msg, n);
        }
        String lexicalForm = n.getLiteralLexicalForm();
        if ( ! rdfDT.isValid(lexicalForm) ) {
            String msg = format("NumericConstraint: Not a valid xsd:decimal: %s ", ShexLib.displayStr(n));
            return new ReportItem(msg, n);
        }

        String str = lexicalForm;
        int N = str.length();
        int idx = str.indexOf('.');

        switch (lengthType) {
            case FRACTIONDIGITS : {
                // Does not include trailing zeros.
                if ( idx < 0 )
                    return null;
                //int before = idx;
                int after = str.length()-idx-1;
                for(int i = N-1 ; i > idx ; i-- ) {
                    if ( str.charAt(i) != '0' )
                        break;
                    after--;
                }
                if ( after <= length )
                    return null;
                break;
            }
            case TOTALDIGITS : {
                // Canonical form.
                int start = 0;
                char ch1 = str.charAt(0);
                if ( ch1 == '+' || ch1 == '-' )
                    start++;
                // Leading zeros
                for( int i = start ; i < N ; i++ ) {
                    if ( str.charAt(i) != '0' )
                        break;
                    start++;
                }
                int finish = N ;
                // Trailing zeros
                if ( idx >= 0 ) {
                    finish--;
                    for(int i = N-1 ; i > idx ; i-- ) {
                        if ( str.charAt(i) != '0' )
                            break;
                        finish--;
                    }
                }
                int digits = finish-start;

                if ( digits <= length )
                    return null;
                break;
            }
            default :
                break;
        }

        String msg = format("Expected %s %d : got = %d", lengthType.label(), length, str.length());
        return new ReportItem(msg, n);
    }

    @Override
    public void visit(NodeConstraintVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "NumLength["+lengthType.label()+" "+length+"]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(length, lengthType);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        NumLengthConstraint other = (NumLengthConstraint)obj;
        return length == other.length && lengthType == other.lengthType;
    }
}
