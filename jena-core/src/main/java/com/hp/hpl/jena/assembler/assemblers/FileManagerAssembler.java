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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.*;

/**
    A FileManagerAssembler creates a FileManager object which may be
    initialised with a LocationMapper specified by the object of a ja:locationMapper
    property.
*/
public class FileManagerAssembler extends AssemblerBase
    {
    @Override
    public Object open( Assembler a, Resource root, Mode irrelevant )
        { 
        checkType( root, JA.FileManager );
        FileManager fm = new FileManager( getLocationMapper( a, root ) );
        FileManager.setStdLocators( fm );
        return fm; 
        }

    private LocationMapper getLocationMapper( Assembler a, Resource root )
        {
        Resource lm = getUniqueResource( root, JA.locationMapper );
        return lm == null ? new LocationMapper() : (LocationMapper) a.open( lm );
        }
    }
