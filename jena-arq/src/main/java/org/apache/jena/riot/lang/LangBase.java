/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.riot.lang;

import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.sparql.util.Context;


public abstract class LangBase extends LangEngine implements LangRIOT
{
    protected final StreamRDF dest;

    protected LangBase(Tokenizer tokens, ParserProfile profile, StreamRDF dest, Context context) {
        super(tokens, profile, profile.getErrorHandler());
        this.dest = dest;
    }

    protected boolean isStrictMode() {
        return profile.isStrictMode();
    }

    @Override
    public void parse() {
        dest.start();
        try {
            runParser();
        } finally {
            dest.finish();
            tokens.close();
        }
    }

    protected int chooseRecursionLimit(Context context, int defaultRecursionLimit) {
        if ( context == null || ! context.isDefined(RIOT.symTurtleParserRecursionDepth) )
            return defaultRecursionLimit;
        int x =  context.getInt(RIOT.symTurtleParserRecursionDepth, defaultRecursionLimit);
        // 2026-08 :: 3000 is too big and in all causes stack overflow for all cases so can't be right.
        if ( x < 0 || x > 3000 )
            throw new RiotException("Bad recursion limit (must be 10 and 3000)");
        return x;
    }

    /** Run the parser - events have been handled. */
    protected abstract void runParser();
}
