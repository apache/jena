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

package org.apache.jena.sparql.core;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

/**
 * FROM / FROM NAMED processing.
 */
@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")
public class TestDynamicDatasetMem extends AbstractTestDynamicDataset
{
    private static Stream<Arguments> provideArgs() {
        Creator<Dataset> datasetGeneralMaker = ()-> DatasetFactory.createGeneral();
        Creator<Dataset> datasetTxnMemMaker = ()-> DatasetFactory.createTxnMem();
        List<Arguments> x = List.of
                (Arguments.of( "General",  datasetGeneralMaker ),
                 Arguments.of( "TxnMem",   datasetTxnMemMaker )
                );
        return x.stream();
    }

    private final Creator<Dataset> maker;

    public TestDynamicDatasetMem(String name, Creator<Dataset> maker) {
        this.maker = maker;
    }

    @Override
    protected Dataset createDataset()
    {
        return maker.create();
    }

    @Override
    protected void releaseDataset(Dataset ds) {}

}

