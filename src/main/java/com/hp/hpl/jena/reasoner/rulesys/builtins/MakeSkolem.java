/******************************************************************
 * File:        MakeSkolem.java
 * Created by:  Dave Reynolds
 * Created on:  28 Mar 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * [See end of file]
 * $Id: MakeSkolem.java,v 1.1 2010-03-28 11:59:36 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.builtins;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.xerces.impl.dv.util.Base64;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.reasoner.rulesys.RuleContext;
import com.hp.hpl.jena.shared.JenaException;

/**
 * Bind a blank node to the first argument.
 * For any given combination of the remaining arguments
 * the same blank node will be returned. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $
 */
public class MakeSkolem extends BaseBuiltin {

    /**
     * Return a name for this builtin, normally this will be the name of the 
     * functor that will be used to invoke it.
     */
    @Override
    public String getName() {
        return "makeSkolem";
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
    @Override
    public boolean bodyCall(Node[] args, int length, RuleContext context) {
        StringBuilder key = new StringBuilder();
        for (int i = 1; i < length; i++) {
            Node n = getArg(i, args, context);
            if (n.isBlank()) {
                key.append("B"); key.append(n.getBlankNodeLabel());
            } else if (n.isURI()) {
                key.append("U"); key.append(n.getURI());
            } else if (n.isLiteral()) {
                key.append("L"); key.append(n.getLiteralLexicalForm()); 
                if (n.getLiteralLanguage() != null) key.append("@" + n.getLiteralLanguage());
                if (n.getLiteralDatatypeURI() != null) key.append("^^" + n.getLiteralDatatypeURI());
            } else {
                key.append("O"); key.append(n.toString());
            }
        }
        
        try {
            
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.reset();
            byte[] digest = digester.digest(key.toString().getBytes());
            Node skolem = Node.createAnon( new AnonId( Base64.encode(digest) ) );
            return context.getEnv().bind(args[0], skolem); 
            
        } catch (NoSuchAlgorithmException e) {
            throw new JenaException(e);
        }
    }
        
}

/*
    (c) Copyright 2010 Epimorphics Limited
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
