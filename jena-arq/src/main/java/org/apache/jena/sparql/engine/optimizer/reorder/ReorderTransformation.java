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

package org.apache.jena.sparql.engine.optimizer.reorder;

import org.apache.jena.sparql.core.BasicPattern ;

public interface ReorderTransformation
{
    /** Return the reordering function based on a basic graph pattern. */
    public ReorderProc reorderIndexes(BasicPattern pattern) ;
    
    /** Return the reordered basic graph pattern. */
    public BasicPattern reorder(BasicPattern pattern) ;
}
