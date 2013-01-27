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

package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.shared.JenaException;

/**
 * Exception signalling some generic problem with the reasoning subsystem.
 * Subclasses of this exception may be used to report more specific problems.
 * <p>In the future there may be a top level JenaException which this exception
 * should extend.
 */
public class ReasonerException extends JenaException {
    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     */
    public ReasonerException(String msg) {
        super(msg);
    }
    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     * @param cause a nested exception which prompted this error
     */
    public ReasonerException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
