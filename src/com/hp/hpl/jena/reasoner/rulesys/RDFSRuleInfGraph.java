/******************************************************************
 * File:        RDFSRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RDFSRuleInfGraph.java,v 1.1 2003-06-22 16:10:31 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
 * Customization of the generic rule inference graph for RDFS inference.
 * In fact all the rule processing is unchanged, the only extenstion is
 * the validation support.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-22 16:10:31 $
 */
public class RDFSRuleInfGraph extends FBRuleInfGraph {

    /** Optional map of property node to datatype ranges */
    protected HashMap dtRange = null;

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     */
    public RDFSRuleInfGraph(Reasoner reasoner, List rules, Graph schema) {
        super(reasoner, rules, schema);
    }

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     * @param data the data graph to be processed
     */
    public RDFSRuleInfGraph(Reasoner reasoner, List rules, Graph schema, Graph data) {
        super(reasoner, rules, schema, data);
    }
    
    /**
     * Test the consistency of the bound data. For RDFS this checks that all
     * instances of datatype-ranged properties have correct data values.
     * 
     * @return a ValidityReport structure
     */
    public ValidityReport validate() {
        StandardValidityReport report = new StandardValidityReport();
        HashMap dtRange = getDTRange();
        for (Iterator props = dtRange.keySet().iterator(); props.hasNext(); ) {
            Node prop = (Node)props.next();
            for (Iterator i = find(null, prop, null); i.hasNext(); ) {
                Triple triple = (Triple)i.next();
                report.add(checkLiteral(prop, triple.getObject()));
            }
        }
        return report;
    }
    
    /**
     * Check a given literal value for a property against the set of
     * known range constraints for it.
     * @param prop the property node whose range is under scrutiny
     * @param value the literal node whose value is to be checked
     * @return null if the range is legal, otherwise a ValidityReport.Report
     * which describes the problem.
     */
    public ValidityReport.Report checkLiteral(Node prop, Node value) {
        List range = (List) getDTRange().get(prop);
        if (range != null) {
            if (!value.isLiteral()) {
                return new ValidityReport.Report(true, "dtRange", 
                    "Property " + prop + " has a typed range but was given a non literal value " + value);
            }
            LiteralLabel ll = value.getLiteral();   
            for (Iterator i = range.iterator(); i.hasNext(); ) {
                RDFDatatype dt = (RDFDatatype)i.next();
                if (!dt.isValidLiteral(ll)) {
                    return new ValidityReport.Report(true, "dtRange", 
                        "Property " + prop + " has a typed range " + dt +
                        "that is not compatible with " + value);
                }
            }
        }
        return null;
    }

    /**
     * Return a map from property nodes to a list of RDFDatatype objects
     * which have been declared as the range of that property.
     */
    private HashMap getDTRange() {
        if (dtRange == null) {
            dtRange = new HashMap();
            for (Iterator i = find(null, RDFS.range.asNode(), null); i.hasNext(); ) {
                Triple triple = (Triple)i.next();
                Node prop = triple.getSubject();
                Node rangeValue = triple.getObject();
                if (rangeValue.isURI()) {
                    RDFDatatype dt = TypeMapper.getInstance().getTypeByName(rangeValue.getURI());
                    if (dt != null) {
                        List range = (ArrayList) dtRange.get(prop);
                        if (range == null) {
                            range = new ArrayList();
                            dtRange.put(prop, range);
                        }
                        range.add(dt);
                    }
                }
            }
        }
        return dtRange;
    }

}



/*
    (c) Copyright Hewlett-Packard Company 2003
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