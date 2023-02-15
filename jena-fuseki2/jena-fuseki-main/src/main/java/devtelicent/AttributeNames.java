/*
 * Copyright (C) 2023 Telicent Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package devtelicent;

/**
 * Useful constants for metric and tracing attributes
 */
public class AttributeNames {

    private AttributeNames() {}

    /**
     * Attribute for the type of items, used with many of the metrics
     */
    public static final String ITEMS_TYPE = "items.type";

    /**
     * Attribute for the ID of a given unique instance of a metric producer
     */
    public static final String INSTANCE_ID = "instance.id";
}
