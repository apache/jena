/******************************************************************
 * File:        MonitorModel.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: MonitorModel.java,v 1.4 2008-01-02 12:07:44 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.util;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

/**
 * Model wrapper which provides normal access to an underlying model but
 * also maintains a snapshot of the triples it was last known to contain. 
 * A snapshot action
 * causes the set of changes between this and the previous snapshot to
 * be calculated and the cache updated. The snapshot process will also 
 * fire change notification.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $
 */

public class MonitorModel extends ModelCom {

    /**
     * Create a monitor over the given underlying base model.
     */
    public MonitorModel(Model base) {
        super(new MonitorGraph(base.getGraph()));
    }
    
    /**
     * Compute the differences between the current monitored graph and the last
     * snapshot. The changes will also be forwarded to any listeners.
     * Then take a new snapshot.
     * @param additions a place in which the set of newly added statements should be noted, can be null
     * @param deletions a place in which the set of newly deleted statements should be noted, can be null
     */
    public void snapshot(List additions, List deletions) {
        List additionsTemp = (additions != null) ? new ArrayList() : null;
        List deletionsTemp = (deletions != null) ? new ArrayList() : null;
        ((MonitorGraph)getGraph()).snapshot(additionsTemp, deletionsTemp);
        if (additions != null) {
            for (Iterator i = additionsTemp.iterator(); i.hasNext(); ) {
                additions.add( this.asStatement((Triple)i.next()));
            }
        }
        if (deletions != null) {
            for (Iterator i = deletionsTemp.iterator(); i.hasNext(); ) {
                deletions.add( this.asStatement((Triple)i.next()));
            }
        }
    }
    
    /**
     * Compute the differences between the current monitored graph and the last
     * snapshot, forward any changes to registered listeners, then take a new snapshot.
     */
    public void snapshot() {
        snapshot(null, null);
    }
    
}


/*
    (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
