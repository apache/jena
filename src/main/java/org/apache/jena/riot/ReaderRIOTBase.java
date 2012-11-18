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

package org.apache.jena.riot;

import java.io.InputStream ;

import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.ContentType ;

import com.hp.hpl.jena.sparql.util.Context ;

public abstract class ReaderRIOTBase<T> implements ReaderRIOT<T>
{
    @Override
    public void read(InputStream in, String baseURI, ContentType ct, Sink<T> sink, Context context)
    {
        Lang2 lang = Langs.contentTypeToLang(ct) ;
        read(in, baseURI,lang, sink, context) ;
    }

    public abstract void read(InputStream in, String baseURI, Lang2 lang, Sink<T> sink, Context context) ;
}

