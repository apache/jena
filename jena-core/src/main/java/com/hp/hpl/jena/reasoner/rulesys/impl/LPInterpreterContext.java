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
 * The context in which an LPInterpreter instance is running.
 * The context the entity that should be notified when a branch has been
 * suspended awaiting further results for a given generator.
 */
public interface LPInterpreterContext extends LPInterpreterState {

    /** Notify this context that a branch was suspended awaiting futher
     *  results for the given choice point. */
    public void notifyBlockedOn(ConsumerChoicePointFrame ccp);
    
    /** Test if one of our top level choice points is ready to be reactivated */
    public boolean isReady();
    
    /** Notify this context that the given choice point has terminated
     *  and can be remove from the wait list. */
    public void notifyFinished(ConsumerChoicePointFrame ccp);
    
    /** Called by a generating choice point to indicate we can be run
     * because the indicated choice point is ready. */
    public void setReady(ConsumerChoicePointFrame ccp);

}
