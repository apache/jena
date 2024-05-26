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

package org.apache.jena.ontapi.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.ontapi.model.OntModel;

import java.util.Objects;

/**
 * Events that called on {@link OntModel}'s level.
 */
public class OntModelEvent extends GraphEvents {
    public static final String START_CHANGE_ID = "startChangeID";
    public static final String FINISH_CHANGE_ID = "endChangeID";
    public static final String START_ADD_DATA_GRAPH = "startAddDataGraph";
    public static final String START_DELETE_DATA_GRAPH = "startDeleteDataGraph";
    public static final String FINISH_ADD_DATA_GRAPH = "finishAddDataGraph";
    public static final String FINISH_DELETE_DATA_GRAPH = "finishDeleteDataGraph";
    public static final String START_READ_DATA_GRAPH = "startReadDataGraph";
    public static final String FINISH_READ_DATA_GRAPH = "endReadDataGraph";

    private final String type;

    public OntModelEvent(String title, Object content) {
        super(title, content);
        this.type = title;
    }

    public static boolean isEventOfType(Object any, String type) {
        if (any instanceof OntModelEvent) {
            return Objects.equals(((OntModelEvent) any).type, type);
        }
        if (any instanceof GraphEvents) {
            if (any == startRead && START_READ_DATA_GRAPH.equals(type)) {
                return true;
            }
            if (any == finishRead && FINISH_READ_DATA_GRAPH.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static OntModelEvent startChangeIDEvent() {
        return new OntModelEvent(START_CHANGE_ID, "CHANGE-ID");
    }

    public static OntModelEvent finishChangeIDEvent() {
        return new OntModelEvent(FINISH_CHANGE_ID, "CHANGE-ID");
    }

    public static OntModelEvent startAddDataGraphEvent(Graph graph) {
        return new OntModelEvent(START_ADD_DATA_GRAPH, graph);
    }

    public static OntModelEvent finishAddDataGraphEvent(Graph graph) {
        return new OntModelEvent(FINISH_ADD_DATA_GRAPH, graph);
    }

    public static OntModelEvent startDeleteDataGraphEvent(Graph graph) {
        return new OntModelEvent(START_DELETE_DATA_GRAPH, graph);
    }

    public static OntModelEvent finishDeleteDataGraphEvent(Graph graph) {
        return new OntModelEvent(FINISH_DELETE_DATA_GRAPH, graph);
    }

    public static GraphEvents startReadDataGraphEvent() {
        return startRead;
    }

    public static GraphEvents finishReadDataGraphEvent() {
        return finishRead;
    }
}
