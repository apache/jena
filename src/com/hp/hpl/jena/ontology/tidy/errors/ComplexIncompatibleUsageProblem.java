/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.errors;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.tidy.impl.*;

/**
 * @author Jeremy J. Carroll
 *
 */
public class ComplexIncompatibleUsageProblem extends MultipleTripleProblem {
    final private int field;
    
    final private int wanted[] = new int[2];
    final private int given[] = new int[2];
    
    static final String messages[] = {
            "The predicate %p is used elsewhere as %1g." +
            " The object %o is used elsewhere as %2g." +
            " With the subject %s, such predicates expect an object" +
            " which is %2w," +
            " and such objects expect a predicate which is %1w.",
            "The subject %s is used elsewhere as %0g." +
            " The object %o is used elsewhere as %2g." +
            " With the predicate %p, such subjects expect an object" +
            " which is %2w," +
            " and such objects expect a subject which is %0w.",
            "The subject %s is used elsewhere as %0g. " +
            " The predicate %p is used elsewhere as %1g." +
            " With the object %o, such subjects expect a predicate" +
            " which is %1w," +
            " and such predicates expect a subject which is %0w."
    };
    /**
     * @param msg
     */
    public ComplexIncompatibleUsageProblem(int field) {
        super(messages[field]);
        this.field = field;
    }

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.ontology.tidy.SyntaxProblem2#getTypeCode()
     */
    public int getTypeCode() {
        // TODO reduce complexity of this expression
        return DC_DOM_RANGE+(7^(1<<field));
    }
    
    /** Not part of API */
    public WantedGiven wantedGiven(int i) {
       final int ix = i>field?i-1:i; 
        return new WantedGiven(){

            public int getWanted() {
               return wanted[ix];
            }

            public int getGiven() {
                return given[ix];
            }

            public void setWanted(int w) {
                wanted[ix]=w;
            }

            public void setGiven(int g) {
             given[ix]=g;   
            }
            
        };
    }

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.ontology.tidy.impl.MultipleTripleProblem#getNode1()
     */
    public Node getNode1() {
        return field==0?triple.getPredicate():triple.getSubject();
    }

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.ontology.tidy.impl.MultipleTripleProblem#getNode2()
     */
    public Node getNode2() {
        return field==2?triple.getPredicate():triple.getObject();
    }
    
    public String toString() {
        String rslt = super.toString();
        rslt =
            rslt.replaceAll("%0g",CategorySetNames.getName(given[0]))
            .replaceAll("%0w",CategorySetNames.getName(wanted[0]))
        .replaceAll("%2g",CategorySetNames.getName(given[1]))
        .replaceAll("%2w",CategorySetNames.getName(wanted[1]));

        
        if (field==0) {
            rslt =
            rslt.replaceAll("%1g",CategorySetNames.getName(given[0]))
            .replaceAll("%1w",CategorySetNames.getName(wanted[0]));
        }
        if (field==2) {
            rslt =
                rslt.replaceAll("%1g",CategorySetNames.getName(given[1]))
                .replaceAll("%1w",CategorySetNames.getName(wanted[1]));
                
        }
        return rslt;
    }

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 
