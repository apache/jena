package com.hp.hpl.jena.ontology.tidy;

import java.math.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * @author jjc
 *
 */
class CLit extends CBuiltin {
    static final Integer zero = new Integer(0);
    static final Integer one = new Integer(1);
	CLit(Node n, EnhGraph eg) {
		super(n, eg, literalCategory(n));
	}
    /** 
     * Decide whether this literal node is a
     * nonNegativeInteger, (or compatible),
     * and if so is it 0 or 1.
     * @param n    Must be a Literal node.
     * @return int
     */
    static int literalCategory(Node n) {
        LiteralLabel l = n.getLiteral();
        Object v = l.getValue();
        if (XSDDatatype.XSDnonNegativeInteger.isValidValue(v)) {
            // v must be a java.Number at this point so...
        if (!(v instanceof BigInteger) && 
            !(v instanceof BigDecimal) &&
            ((Number)v).longValue() >= 0 &&
        ((Number)v).longValue() <= 1) {
                return Grammar.liteInteger;
            }
            return Grammar.dlInteger;
        }

        return Grammar.literal;
    }


}
