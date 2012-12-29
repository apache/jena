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

package org.apache.jena.riot.lang;

import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Tokenizer ;


public abstract class LangBase extends LangEngine implements LangRIOT
{
    protected final StreamRDF dest ; 

    protected LangBase(Tokenizer tokens, ParserProfile profile, StreamRDF dest)
    {
        super(tokens, profile) ;
        this.dest = dest ;
    }

    @Override
    public void parse()
    {
        dest.base(profile.getPrologue().getBaseURI()) ;
        dest.start() ;
        try { 
            runParser() ;
        } finally {
            dest.finish() ;
            tokens.close();
        }
    }
    
    /** Run the parser - events have been handled. */
    protected abstract void runParser() ;

    @Override
    public ParserProfile getProfile()
    {
        return profile ;
    }

    @Override
    public void setProfile(ParserProfile profile)
    {
        super.profile = profile ; 
    }
}
