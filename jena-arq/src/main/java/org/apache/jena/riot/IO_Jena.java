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

import org.apache.jena.riot.system.IO_JenaReaders ;
import org.apache.jena.riot.system.IO_JenaWriters ;
import org.apache.jena.sys.JenaSystem ;

public class IO_Jena
{
    static { JenaSystem.init(); }

    public static void wireIntoJena() {
       IO_JenaReaders.wireIntoJena() ;
       IO_JenaWriters.wireIntoJena() ;
    }

    public static void resetJena() {
        IO_JenaReaders.resetJena() ;
        IO_JenaWriters.resetJena() ;
    }
}
