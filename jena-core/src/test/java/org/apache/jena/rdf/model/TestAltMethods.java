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

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestAltMethods extends AbstractContainerMethods {

    @Override
    protected Container createContainer() {
        return model.createAlt();
    }

    @Override
    protected Resource getContainerType() {
        return RDF.Alt;
    }

    @Test
    public void testDefaults() {
        final Alt a = model.createAlt();
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        // Resource tvResObj = model.createResource( new ResTestObjF() );
        final Bag tvBag = model.createBag();
        final Alt tvAlt = model.createAlt();
        final Seq tvSeq = model.createSeq();
        //
        final Resource tvResource = model.createResource();
        assertEquals(tvLiteral, a.setDefault(tvLiteral).getDefault());
        assertEquals(tvLiteral, a.getDefaultLiteral());
        assertEquals(tvResource, a.setDefault(tvResource).getDefaultResource());
        assertEquals(AbstractModelTestBase.tvByte, a.setDefault(AbstractModelTestBase.tvByte).getDefaultByte());
        assertEquals(AbstractModelTestBase.tvShort, a.setDefault(AbstractModelTestBase.tvShort).getDefaultShort());
        assertEquals(AbstractModelTestBase.tvInt, a.setDefault(AbstractModelTestBase.tvInt).getDefaultInt());
        assertEquals(AbstractModelTestBase.tvLong, a.setDefault(AbstractModelTestBase.tvLong).getDefaultLong());
        assertEquals(AbstractModelTestBase.tvFloat, a.setDefault(AbstractModelTestBase.tvFloat).getDefaultFloat(),
                            AbstractModelTestBase.fDelta);
        assertEquals(AbstractModelTestBase.tvDouble, a.setDefault(AbstractModelTestBase.tvDouble).getDefaultDouble(),
                            AbstractModelTestBase.dDelta);
        assertEquals(AbstractModelTestBase.tvChar, a.setDefault(AbstractModelTestBase.tvChar).getDefaultChar());
        assertEquals(AbstractModelTestBase.tvString, a.setDefault(AbstractModelTestBase.tvString).getDefaultString());
        // assertEquals(tvResObj, a.setDefault( tvResObj ).getDefaultResource()
        // );
        // assertEquals(tvLitObj, a.setDefault( tvLitObj ).getDefaultObject(
        // new LitTestObjF() ) );
        assertEquals(tvAlt, a.setDefault(tvAlt).getDefaultAlt());
        assertEquals(tvBag, a.setDefault(tvBag).getDefaultBag());
        assertEquals(tvSeq, a.setDefault(tvSeq).getDefaultSeq());
    }
}
