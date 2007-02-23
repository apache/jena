/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.plan;

import java.util.List;

import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.sparql.engine.engine1.PlanStructureVisitor;
import com.hp.hpl.jena.sparql.engine.engine1.PlanVisitor;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase;
import com.hp.hpl.jena.sparql.util.Utils;

/** Extension point for other systems to create specializes query plan elements
 *  (do not confuse with ARQ EXT extensions which are language extensions).
 * 
 * @author Andy Seaborne
 * @version $Id: PlanElementExternalBase.java,v 1.12 2007/01/02 11:19:33 andy_seaborne Exp $
 */

public abstract class PlanElementExternalBase
    extends PrintSerializableBase 
    implements PlanElementExternal
{
    // If Context/EngineConfig is dropped, this can inherit from PlanElementBase
    protected PlanElementExternalBase() { }

    public final void visit(PlanVisitor visitor) { visitor.visit(this) ; }

    /** Default printing function - best to override and print something more useful */
    public void output(IndentedWriter out, SerializationContext sContext)
    { output(out, this) ; }

    private static void output(IndentedWriter out, PlanElementExternal ext)
    {
        out.print(Plan.startMarker) ;
        out.print(Utils.className(ext)) ;
        out.print(Plan.finishMarker) ;
    }

    // Typically, no sub plan elements
    public PlanElement getSubElement(int i)  { return null ; }
    public int numSubElements()              { return 0 ; }
    public List getSubElements()             { return null ; }

    public final void visit(PlanStructureVisitor visitor)
    { visitor.visit(this)  ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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