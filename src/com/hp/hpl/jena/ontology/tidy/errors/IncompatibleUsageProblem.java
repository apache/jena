/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.ontology.tidy.errors;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.tidy.impl.*;
import com.hp.hpl.jena.shared.BrokenException;

/**
 * @author Jeremy J. Carroll
 *  
 */
public class IncompatibleUsageProblem extends MultipleTripleProblem implements
        WantedGiven {

    final private int field;

    static final private String fNames[] = { "subject %s", "predicate %p",
            "object %o" };

    /**
     * @param msg
     */
    public IncompatibleUsageProblem(int field) {
        super("The " + fNames[field]
                + " is used as %w here, but elsewhere as %g.");
        code = DIFFERENT_CATS + (1 << field);
        this.field = field;
    }

    final private int code;

    private int wanted, given;

    /** Not part of API */
    public int getWanted() {
        return wanted;
    }

    /** Not part of API */
    public int getGiven() {
        return given;
    }

    /** Not part of API */
    public void setWanted(int w) {
        wanted = w;
    }

    /** Not part of API */
    public void setGiven(int g) {
        given = g;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.ontology.tidy.SyntaxProblem2#getTypeCode()
     */
    public int getTypeCode() {
        return code;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.ontology.tidy.impl.MultipleTripleProblem#getNode1()
     */
    public Node getNode1() {
        switch (field) {
        case 0:
            return triple.getSubject();
        case 1:
            return triple.getPredicate();
        case 2:
            return triple.getObject();
        default:
            throw new BrokenException("Illegal field code: " + field);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hp.hpl.jena.ontology.tidy.impl.MultipleTripleProblem#getNode2()
     */
    public Node getNode2() {
        return null;
    }

    public String toString() {
        return super.toString().replaceAll("%g",CategorySetNames.getName(given))
.replaceAll("%w",CategorySetNames.getName(wanted));
    }
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

