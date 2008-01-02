/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: FileModelAssembler.java,v 1.8 2008-01-02 12:09:36 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.io.File;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.impl.FileGraph.NotifyOnClose;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileUtils;

public class FileModelAssembler extends NamedModelAssembler implements Assembler
    {
    protected Model openModel( Assembler a, Resource root, Mode mode )
        {
        checkType( root, JA.FileModel );
        File fullName = getFileName( root );
        boolean mayCreate = mode.permitCreateNew( root, fullName.toString() );
        boolean mayReuse = mode.permitUseExisting( root, fullName.toString() );
        boolean create = mayCreate;
        boolean strict = mayCreate != mayReuse;
        String lang = getLanguage( root, fullName );
        ReificationStyle style = getReificationStyle( root );
        return createFileModel( fullName, lang, create, strict, style );
        }
    
    public Model createFileModel( File fullName, String lang, boolean create, boolean strict, ReificationStyle style )
        {
        NotifyOnClose notify = NotifyOnClose.ignore;
        Graph fileGraph = new FileGraph( notify, fullName, lang, create, strict, style );
        return ModelFactory.createModelForGraph( fileGraph );
        }

    protected String getLanguage( Resource root, File fullName )
        {
        Statement s = getUniqueStatement( root, JA.fileEncoding );
        return s == null ? FileUtils.guessLang( fullName.toString() ) : getString( s );
        }    

    protected File getFileName( Resource root )
        {
        String name = getModelName( root );
        boolean mapName = getBoolean( root, JA.mapName, false );
        String dir = getDirectoryName( root );
        return new File( dir, (mapName ? FileGraphMaker.toFilename( name ): name) );
        }
    
    private boolean getBoolean( Resource root, Property p, boolean ifAbsent )
        {
        Resource r = getUniqueResource( root, p );
        return r == null ? ifAbsent : r.equals( JA.True );
        }
    
    private String getDirectoryName( Resource root )
        {
        return getRequiredResource( root, JA.directory ).getURI().replaceFirst( "file:", "" );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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