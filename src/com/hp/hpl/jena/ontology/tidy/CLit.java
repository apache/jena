/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy;

import java.math.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * @author jjc
 *
 */
class CLit extends CBuiltin {
    static final Integer zero = new Integer(0);
    static final Integer one = new Integer(1);
	CLit(Node n, AbsChecker eg) {
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


/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

