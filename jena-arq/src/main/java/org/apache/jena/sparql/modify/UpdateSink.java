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

package org.apache.jena.sparql.modify;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.modify.request.QuadDataAccSink ;
import org.apache.jena.update.Update ;

/**
 * An {@link UpdateSink} is an object usually created by a container (such as a storage engine
 * or an {@link org.apache.jena.update.UpdateRequest}) that can process or store a single SPARQL Update
 * request which consists of one or more SPARQL Update operations.
 */
public interface UpdateSink extends Sink<Update>
{
    public Prologue getPrologue();

    public QuadDataAccSink createInsertDataSink();

    public QuadDataAccSink createDeleteDataSink();
}
