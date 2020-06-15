/**
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

package org.apache.jena.riot.system;

import org.apache.jena.rdf.model.impl.RDFReaderFImpl ;
import org.apache.jena.riot.adapters.AdapterFileManager ;
import org.apache.jena.riot.adapters.RDFReaderFactoryRIOT ;
import org.apache.jena.util.FileManager ;

public class IO_JenaReaders
{
//    private static String riotBase = "http://jena.apache.org/riot/" ;
//    private static String streamManagerSymbolStr = riotBase+"streammanager" ;
//    public static Symbol streamManagerSymbol = Symbol.create(streamManagerSymbolStr) ;

    private static FileManager coreFileManager = null;
    private static boolean     isWiredIn       = false;

    @SuppressWarnings("deprecation")
    public static void wireIntoJena() {
        if ( isWiredIn )
            return ;
        isWiredIn = true ;
        if ( coreFileManager == null )
            coreFileManager = FileManager.getInternal();
        FileManager.setGlobalFileManager(AdapterFileManager.get());
        RDFReaderFImpl.alternative(new RDFReaderFactoryRIOT());
    }

    @SuppressWarnings("deprecation")
    public static void resetJena() {
        if ( ! isWiredIn )
            return ;
        isWiredIn = false ;
        RDFReaderFImpl.alternative(null);
        // Or set to null - this forces reinitialization if ever used.
        FileManager.setGlobalFileManager(coreFileManager) ;
    }

    /** Register for use with Model.read (old style compatibility) */
    @Deprecated
    public static void registerForModelRead(String name, Class<? > cls) {
        RDFReaderFImpl.setBaseReaderClassName(name, cls.getName());
    }
}
