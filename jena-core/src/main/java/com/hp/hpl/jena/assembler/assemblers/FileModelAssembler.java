/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.assembler.assemblers;

import java.io.File ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.assembler.Mode ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.impl.FileGraph ;
import com.hp.hpl.jena.graph.impl.FileGraph.NotifyOnClose ;
import com.hp.hpl.jena.graph.impl.FileGraphMaker ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.util.FileUtils ;

public class FileModelAssembler extends NamedModelAssembler implements Assembler
    {
    @Override
    protected Model openEmptyModel( Assembler a, Resource root, Mode mode )
        {
        checkType( root, JA.FileModel );
        File fullName = getFileName( root );
        boolean mayCreate = mode.permitCreateNew( root, fullName.toString() );
        boolean mayReuse = mode.permitUseExisting( root, fullName.toString() );
        boolean create = getBoolean( root, JA.create, mayCreate );
        boolean strict = getBoolean( root, JA.strict, mayCreate != mayReuse );
        String lang = getLanguage( root, fullName );
        return createFileModel( fullName, lang, create, strict );
        }
    
    public Model createFileModel( File fullName, String lang, boolean create, boolean strict )
        {
        NotifyOnClose notify = NotifyOnClose.ignore;
        Graph fileGraph = new FileGraph( notify, fullName, lang, create, strict );
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
        RDFNode r = getUnique( root, p );
        return 
            r == null ? ifAbsent 
            : r.isLiteral() ? booleanSpelling( r.asNode().getLiteralLexicalForm() )
            : r.isURIResource() ? booleanSpelling( r.asNode().getLocalName() )
            : false
            ;
        }
    
    private boolean booleanSpelling( String spelling )
        {
        if (spelling.equalsIgnoreCase( "true" )) return true;
        if (spelling.equalsIgnoreCase( "t" )) return true;
        if (spelling.equalsIgnoreCase( "1" )) return true;
        if (spelling.equalsIgnoreCase( "false" )) return false;
        if (spelling.equalsIgnoreCase( "f" )) return false;
        if (spelling.equalsIgnoreCase( "0" )) return false;
        throw new IllegalArgumentException( "boolean requires spelling true/false/t/f/0/1" );
        }

    private String getDirectoryName( Resource root )
        {
        return getRequiredResource( root, JA.directory ).getURI().replaceFirst( "file:", "" );
        }
    }
