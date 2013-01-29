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

package com.hp.hpl.jena.reasoner.rulesys.impl;

/**
 * The signature of classes that can go on the LPEngines processing
 * agenda. 
 */
public interface LPAgendaEntry {

    /**
     * Cycle this object, recording new results in any associated memoization
     * table until hit a stop or suspend point.
     */
    public void pump();
    
    /**
     * Tests true if this state is ready to be usefully run.
     */
    public boolean isReady();
    
    /**
     * Return the generator associated with this entry (might be the entry itself)
     */
    public Generator getGenerator();
    
}
