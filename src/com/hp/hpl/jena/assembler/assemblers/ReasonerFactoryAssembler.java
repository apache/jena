/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ReasonerFactoryAssembler.java,v 1.3 2006-01-09 16:02:17 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

public class ReasonerFactoryAssembler extends AssemblerBase implements Assembler
    {
    public Object open( Assembler a, Resource root, Mode irrelevant )
        { 
        checkType( root, JA.ReasonerFactory );
        return addRules( root, a, getReasonerFactory( root ) );
        }

    private ReasonerFactory addRules( Resource root, Assembler a, final ReasonerFactory r )
        {
        final List rules = RuleSetAssembler.addRules( new ArrayList(), a, root );
        if (rules.size() > 0l)
            if (r instanceof GenericRuleReasonerFactory)
                {
                return new ReasonerFactory()
                    {
                    public Reasoner create( Resource configuration )
                        {
                        GenericRuleReasoner result = (GenericRuleReasoner) r.create( configuration );
                        result.addRules( rules );
                        return result;
                        }

                    public Model getCapabilities()
                        { return r.getCapabilities(); }

                    public String getURI()
                        { return r.getURI(); }
                    };
                }
            else
                throw new CannotHaveRulesException( root );
        return r;
        }

    protected Reasoner getReasoner( Resource root )
        { return getReasonerFactory( root ).create( root ); }
    
    protected static ReasonerFactory getReasonerFactory( Resource root )
        {
        Resource r = getUniqueResource( root, JA.reasonerURL );
        if (r == null)
            return GenericRuleReasonerFactory.theInstance(); 
        else
            {
            String url = r.getURI();
            ReasonerFactory factory = ReasonerRegistry.theRegistry().getFactory( url );
            if (factory == null) throw new UnknownReasonerException( root, r );
            return factory;
            }
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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