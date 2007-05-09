/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: AssemblerGroup.java,v 1.9 2007-05-09 15:28:00 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.NoImplementationException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;

public abstract class AssemblerGroup extends AssemblerBase implements Assembler
    {    
    public abstract AssemblerGroup implementWith( Resource type, Assembler a );

    public abstract Assembler assemblerFor( Resource type );

    public Model openModel( Resource resource )
        { return (Model) open( resource ); }
    
    public static AssemblerGroup create()
        { return new ExpandingAssemblerGroup(); }
    
    public AssemblerGroup copy()
        {
        ExpandingAssemblerGroup result = (ExpandingAssemblerGroup) create();
        result.internal.mappings.putAll( ((ExpandingAssemblerGroup) this).internal.mappings );
        return result;
        }
    
    public static class ExpandingAssemblerGroup extends AssemblerGroup
        {
        PlainAssemblerGroup internal = new PlainAssemblerGroup();
        Model implementTypes = ModelFactory.createDefaultModel();
        
        public Object open( Assembler a, Resource suppliedRoot, Mode mode )
            {
            Resource root = AssemblerHelp.withFullModel( suppliedRoot );
            root.getModel().add( implementTypes );
            loadClasses( root.getModel() );
            return internal.open( a, root, mode );
            }

        public void loadClasses( Model model )
            { AssemblerHelp.loadClasses( this, model ); }

        public AssemblerGroup implementWith( Resource type, Assembler a )
            {
            implementTypes.add( type, RDFS.subClassOf, JA.Object );
            internal.implementWith( type, a );
            return this;
            }

        public Assembler assemblerFor( Resource type )
            { return internal.assemblerFor( type ); }
        }
    
    static class PlainAssemblerGroup extends AssemblerGroup
        {
        Map mappings = new HashMap();
        
        public Object open( Assembler a, Resource root, Mode mode )
            {
            Resource type = AssemblerHelp.findSpecificType( root );
            Assembler toUse = assemblerFor( type );
            if (toUse == null)
                throw new NoImplementationException( this, root, type );
            else
                return toUse.open( a, root, mode );
            }
        
        public AssemblerGroup implementWith( Resource type, Assembler a )
            {
            mappings.put( type, a );
            return this;
            }

        public Assembler assemblerFor( Resource type )
            { return (Assembler) mappings.get( type ); }
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