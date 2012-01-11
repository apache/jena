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

package tdb.examples;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.tdb.TDBFactory ;

/**
 * Using an assembler description (see wiki for details of the assembler format for TDB)
 * This way, you can change the model being used without changing the code.
 * The assembler file is a configuration file.
 * The same assembler description will work as part of a Joseki configuration file. 
 */

public class ExTDB2
{
    public static void main(String... argv)
    {
        String assemblerFile = "Store/tdb-assembler.ttl" ;

        Dataset ds = TDBFactory.assembleDataset(assemblerFile) ;
        
        // ... do work ...
        
        ds.close() ;
    }
}
