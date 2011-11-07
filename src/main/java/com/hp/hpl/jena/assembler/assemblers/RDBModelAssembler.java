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

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

public class RDBModelAssembler extends NamedModelAssembler implements Assembler
    {
    @Override
    protected Model openEmptyModel( Assembler a, Resource root, Mode mode )
        { return openModel( a, root, Content.empty, mode ); }    
    
    @Override
    protected Model openModel( Assembler a, Resource root, Content initial, Mode mode )
        {
        checkType( root, JA.RDBModel );
        String name = getModelName( root );
        ReificationStyle style = getReificationStyle( root );
        ConnectionDescription c = getConnection( a, root );
        Model m = openModel( root, c, name, style, initial, mode );
//        if (!initial.isEmpty()) addContent( root, m, initial );
        return m;
        }

    protected ConnectionDescription getConnection( Assembler a, Resource root )
        {
        Resource C = getRequiredResource( root, JA.connection );
        return (ConnectionDescription) a.open( C );        
        }

    protected Model openModel( Resource root, ConnectionDescription c, String name, ReificationStyle style, Content initial, Mode mode )
        {
        IDBConnection ic = c.getConnection();
        return isDefaultName( name )
            ? ic.containsDefaultModel() ? ModelRDB.open( ic ) : ModelRDB.createModel( ic )
            : openByMode( root, initial, name, mode, style, ic );
        }

    private Model openByMode( Resource root, Content initial, String name, Mode mode, ReificationStyle style, IDBConnection ic )
        {
        if (ic.containsModel( name ))
            {
            if (mode.permitUseExisting( root, name )) return consModel( ic, name, style, false );
            throw new AlreadyExistsException( name );
            }
        else
            {
            if (mode.permitCreateNew( root, name )) return initial.fill( consModel( ic, name, style, true ) );
            throw new NotFoundException( name );
            }
        }
    
    private static final String nameForDefault = "DEFAULT";

    private boolean isDefaultName( String name )
        { return name.equals( nameForDefault ) || name.equals( "" ); }
    
    protected Model consModel( IDBConnection c, String name, ReificationStyle style, boolean fresh )
        { return new ModelRDB( consGraph( c, name, style, fresh ) ); }
    
    protected GraphRDB consGraph( IDBConnection c, String name, ReificationStyle style, boolean fresh )
        {        
        Graph p = c.getDefaultModelProperties().getGraph();
        int reificationStyle = GraphRDB.styleRDB( style );
        return new GraphRDB( c, name, (fresh ? p : null), reificationStyle, fresh );
        }
    }
