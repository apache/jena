/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1;

import com.hp.hpl.jena.sparql.engine.engine1.plan.*;


public class PlanVisitorBase implements PlanVisitor
{
    // Basic patterns
    public void visit(PlanTriples planElt) {}
    public void visit(PlanTriplesBlock planElt) {}

    // Graph combinations
    public void visit(PlanGroup planElt) {}
    public void visit(PlanUnion planElt) {}
    public void visit(PlanOptional planElt) {}
    public void visit(PlanUnsaid planElt) {}
    public void visit(PlanFilter planElt) {}
    public void visit(PlanNamedGraph planElt) {}

    // Other
    public void visit(PlanPropertyFunction planElt) {}
    public void visit(PlanExtension planElt) {}
    public void visit(PlanDataset planElt)   {}
    public void visit(PlanElementExternal planElt)  {}
    
    // Solution sequence modifiers
    public void visit(PlanDistinct planElt) {}
    public void visit(PlanProject planElt) {}
    public void visit(PlanOrderBy planElt) {}
    public void visit(PlanLimitOffset planElt) {}
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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