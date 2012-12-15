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

package org.apache.jena.riot.tokens;

/** Do nothing checker */
public class TokenCheckerBase implements TokenChecker
{

    @Override
    public void checkBlankNode(String blankNodeLabel)
    {}

    @Override
    public void checkControl(int code)
    {}

    @Override
    public void checkDirective(int cntrlCode)
    {}

    @Override
    public void checkKeyword(String lexical)
    {}

    @Override
    public void checkLiteralDT(String lexicalForm, Token datatype)
    {}

    @Override
    public void checkLiteralLang(String lexicalForm, String langTag)
    {}

    @Override
    public void checkNumber(String lexical, String datatypeURI)
    {}

    @Override
    public void checkPrefixedName(String prefixName, String localName)
    {}

    @Override
    public void checkString(String string)
    {}

    @Override
    public void checkURI(String uriStr)
    {}

    @Override
    public void checkVariable(String tokenImage)
    {}

}
