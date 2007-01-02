/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: PrefixMappingAssembler.java,v 1.6 2007-01-02 11:52:55 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

public class PrefixMappingAssembler extends AssemblerBase implements Assembler
    {
    public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        checkType( root, JA.PrefixMapping );
        return getPrefixes( a, root, PrefixMapping.Factory.create() ); 
        }

    public static PrefixMapping getPrefixes( Assembler a, Resource root, PrefixMapping result )
        {
        setSimplePrefixes( root, result );
        setIncludedPrefixes( a, root, result );
        return result;
        }

    private static void setIncludedPrefixes( Assembler a, Resource root, PrefixMapping result )
        {
        for (StmtIterator it = root.listProperties( JA.includes ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            PrefixMapping sub = (PrefixMapping) a.open( getResource( s ) );
            result.setNsPrefixes( sub );
            }
        }

    private static void setSimplePrefixes( Resource root, PrefixMapping result )
        {
        if (root.hasProperty( JA.prefix ))
            {
            Literal prefix = getUniqueLiteral( root, JA.prefix );
            Literal namespace = getUniqueLiteral( root, JA.namespace );
            result.setNsPrefix( prefix.getLexicalForm(), namespace.getLexicalForm() );
            }
        }

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