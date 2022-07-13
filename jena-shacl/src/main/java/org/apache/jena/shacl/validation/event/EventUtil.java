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

package org.apache.jena.shacl.validation.event;

import org.apache.jena.graph.Node;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class EventUtil {
    @SuppressWarnings("unchecked") public static Stream<Class<? extends ValidationEvent>> getSuperclassesAndInterfaces(
                    Class<? extends ValidationEvent> eventType) {
        Stream<Class<? extends ValidationEvent>> superInterfaces = Arrays.stream(eventType.getInterfaces())
                        .filter(ValidationEvent.class::isAssignableFrom)
                        .flatMap(iface -> getSuperclassesAndInterfaces((Class<? extends ValidationEvent>) iface));
         Class<?> superType =  eventType.getSuperclass();
        Stream<Class<? extends ValidationEvent>> superclasses;
         if (superType != null && ValidationEvent.class.isAssignableFrom(superType)){
             superclasses = getSuperclassesAndInterfaces(
                            (Class<? extends ValidationEvent>) superType);
         } else {
             superclasses = Stream.empty();
         }
         return Stream.concat(superInterfaces, Stream.concat(superclasses, Stream.of(eventType)));
    }

    public static boolean nodeUriEquals(Node node, String uri) {
        if (node.isURI()) {
            return node.getURI().equals(uri);
        }
        return false;
    }
}
