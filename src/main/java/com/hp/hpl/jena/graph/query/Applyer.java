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

package com.hp.hpl.jena.graph.query;

/**
    An Applyer object will run the StageElement <code>next</code> over all the
    extensions of the Domain <code>d</code> which are derived from applying
    the Matcher <code>m</code> to some internal supply of triples.
    
    @author kers
*/
public abstract class Applyer
    {   
    public abstract void applyToTriples( Domain d, Matcher m, StageElement next );

    /**
        An Applyer that never calls its <code>next</code> StageElement.
    */
    public static final Applyer empty = new Applyer()
        {
        @Override
        public void applyToTriples( Domain d, Matcher m, StageElement next )
            {}
        };
    }
