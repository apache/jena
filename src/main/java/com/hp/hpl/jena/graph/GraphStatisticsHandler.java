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

package com.hp.hpl.jena.graph;

/**
    A graph's StatisticsHandler offers access to some statistics about that
    graph's contents that might be useful for optimisation.
    
 */
public interface GraphStatisticsHandler
    {
    /**
        Answer a good estimate of the number of triples that would match the
        pattern <code>(S, P, O)</code>, or -1 if no good estimate is available.
        The estimate is good until the graph is updated.
    */
    public long getStatistic( Node S, Node P, Node O );
    }
