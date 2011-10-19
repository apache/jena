/******************************************************************
 * File:        RDFSCMPPreprocessHook.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RDFSCMPPreprocessHook.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.*;

/**
 * A rule preprocessor that scans all supplied data looking for instances
 * of container membership properties and adds those to the deductions set.  
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:33 $
 */
public class RDFSCMPPreprocessHook implements RulePreprocessHook {
    protected static String memberPrefix = RDF.getURI() + "_";

    /**
     * Invoke the preprocessing hook. This will be called during the
     * preparation time of the hybrid reasoner.
     * @param infGraph the inference graph which is being prepared,
     * the hook code can use this to addDeductions or add additional
     * rules (using addRuleDuringPrepare).
     * @param dataFind the finder which packages up the raw data (both
     * schema and data bind) and any cached transitive closures.
     * @param inserts a temporary graph into which the hook should insert
     * all new deductions that should be seen by the rules.
     */
    @Override
    public void run(FBRuleInfGraph infGraph, Finder dataFind, Graph inserts) {
        ExtendedIterator<Triple> it = dataFind.find(new TriplePattern(null, null, null));
        HashSet<Node> properties = new HashSet<Node>();
        while (it.hasNext()) {
            Triple triple = it.next();
            Node prop = triple.getPredicate();
            if (prop.equals(RDF.Nodes.type) && triple.getObject().equals(RDF.Nodes.Property) ) {
                prop = triple.getSubject();
            }
            if (properties.add(prop)) {
                if (prop.getURI().startsWith(memberPrefix)) {
                    // A container property
                    inserts.add(new Triple(prop, RDF.Nodes.type, RDFS.Nodes.ContainerMembershipProperty));
                }
            }
        }
    }
    
    /**
     * Validate a triple add to see if it should reinvoke the hook. If so
     * then the inference will be restarted at next prepare time. Incremental
     * re-processing is not yet supported but in this case would be useful.
     */
    @Override
    public boolean needsRerun(FBRuleInfGraph infGraph, Triple t) {
        return (t.getPredicate().getURI().startsWith(memberPrefix));
    }

}



/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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