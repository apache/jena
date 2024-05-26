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

package org.apache.jena.riot ;

import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.rdf.model.impl.RDFWriterFImpl;
import org.apache.jena.riot.adapters.AdapterFileManager;
import org.apache.jena.riot.adapters.RDFReaderFactoryRIOT;
import org.apache.jena.riot.adapters.RDFWriterFactoryRIOT;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.util.FileManager;

public class IO_Jena
{
    static { JenaSystem.init(); }

    private static FileManager coreFileManager = null;
    private static boolean readersWiredIn      = false;
    private static boolean writersWiredIn      = false;

    /**
     * Wire the adapters in so jena-core operations "model.read" and "model.write"
     * are routed to RIOT.
     */
    public static void wireIntoJena() {
        wireReadersIntoJena();
        wireWritersIntoJena();
    }

    /**
     * Revert adapters in so jena-core operations "model.read" and "model.write"
     * are routed by jena-core (most syntaxes are not available).
     */
    public static void resetJena() {
        resetJenaCoreReaders() ;
        resetJenaCoreWriters();
    }

    @SuppressWarnings("deprecation")
    private static void wireReadersIntoJena() {
        if ( readersWiredIn )
            return ;
        readersWiredIn = true ;
        coreFileManager = FileManager.getInternalNoInit();
        RDFReaderFImpl.alternative(new RDFReaderFactoryRIOT());
        FileManager.setGlobalFileManager(AdapterFileManager.get());
    }

    @SuppressWarnings("deprecation")
    private static void resetJenaCoreReaders() {
        if ( ! readersWiredIn )
            return ;
        readersWiredIn = false ;
        RDFReaderFImpl.alternative(null);
        FileManager.setGlobalFileManager(coreFileManager) ;
    }

    private static void wireWritersIntoJena() {
        if ( writersWiredIn )
            return ;
        writersWiredIn = true ;
        RDFWriterFImpl.alternative(new RDFWriterFactoryRIOT());
    }

    private static void resetJenaCoreWriters() {
        if ( ! writersWiredIn )
            return ;
        writersWiredIn = false ;
        RDFWriterFImpl.alternative(null);
    }
}

