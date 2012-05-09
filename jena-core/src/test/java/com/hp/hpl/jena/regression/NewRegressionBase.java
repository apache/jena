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

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.regression.Regression.LitTestObj;

public class NewRegressionBase extends ModelTestBase
    {
    public NewRegressionBase( String name )
        { super( name ); }

    protected static final boolean tvBoolean = true;
    protected static final byte tvByte = 1;
    protected static final short tvShort = 2;
    protected static final int tvInt = -1;
    protected static final long tvLong = -2;
    protected static final char tvChar = '!';
    protected static final float tvFloat = (float) 123.456;
    protected static final double tvDouble = -123.456;
    protected static final String tvString = "test 12 string";
    protected static final Object tvLitObj = new LitTestObj(1234);
    protected static final LitTestObj tvObject = new LitTestObj(12345);
    static final double dDelta = 0.000000005;
    static final float fDelta = 0.000005f;
    }
