/******************************************************************
 * File:        IsDType.java
 * Created by:  Dave Reynolds
 * Created on:  24-Aug-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: IsDType.java,v 1.6 2004-12-07 09:53:54 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.builtins;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Tests whether the first argument is an instance of the datatype defined
 * by the resource in the second argument.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2004-12-07 09:53:54 $
 */
public class IsDType extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    public String getName() {
        return "isDType";
    }
    
    /**
     * Return the expected number of arguments for this functor or 0 if the number is flexible.
     */
    public int getArgLength() {
        return 2;
    }

    /**
     * This method is invoked when the builtin is called in a rule body.
     * @param args the array of argument values for the builtin, this is an array 
     * of Nodes, some of which may be Node_RuleVariables.
     * @param length the length of the argument list, may be less than the length of the args array
     * for some rule engines
     * @param context an execution context giving access to other relevant data
     * @return return true if the buildin predicate is deemed to have succeeded in
     * the current environment
     */
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        checkArgs(length, context);
        Node val = getArg(0, args, context);
        Node dt = getArg(1, args, context);
        return isTypeOK(val, dt);
    }
    
    /**
     * Check if a literal value node is a legal value for the given datatype.
     * @param val the literal value node
     * @param dt  the Node designating a datatype URI 
     */
    public static boolean isTypeOK(Node val, Node dt) {
        if (!dt.isURI()) return false;
        if (val.isBlank()) return true;
        if (val.isLiteral()) {
            LiteralLabel ll = val.getLiteral();
            if (ll.getDatatype() != null && (! ll.isWellFormed())) return false;
            if (dt.equals(RDFS.Nodes.Literal)) {
                return true;
            } else {
                RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName(dt.getURI());
                return dtype.isValidLiteral(val.getLiteral());   
            }
        }
        return false;
    }
    
}


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/