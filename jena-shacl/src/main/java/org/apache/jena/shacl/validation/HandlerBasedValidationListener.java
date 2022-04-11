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

package org.apache.jena.shacl.validation;

import org.apache.jena.shacl.validation.event.EventUtil;
import org.apache.jena.shacl.validation.event.ValidationEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * ValidationListener implementation that allows for registering event handlers on a per-type basis.
 * </p>
 * <p>
 * The handlers can be registered with any subclass or sub-interface of 'ValidationEvent', i.e., concrete
 *  event classes their superclasses or interfaces.
 *  </p>
 * Example:
 * <pre>
 *  ValidationListener myListener =
 *                         HandlerBasedValidationListener
 *                                         .builder()
 *                                         .forEventType(FocusNodeValidationStartedEvent.class)
 *                                         .addSimpleHandler(e -> {
 *                                              // ...
 *                                         })
 *                                         .forEventType(ConstraintEvaluatedEvent.class)
 *                                         .addSimpleHandler(e -> {
 *                                             // will be called for any subclass of ConstraintEvaluatedEvent
 *                                         })
 *                                         .build();
 * </pre>
 *
 *
 */
public class HandlerBasedValidationListener implements ValidationListener {
    private final Map<Class<? extends ValidationEvent>, List<Consumer<ValidationEvent>>> eventHandlers = new HashMap<>();
    private final HandlerSelectionStrategy handlerSelectionStrategy;

    private HandlerBasedValidationListener(HandlerSelectionStrategy handlerSelectionStrategy){
        this.handlerSelectionStrategy = handlerSelectionStrategy;
    }

    public static Builder builder(HandlerSelectionStrategy handlerSelectionStrategy){
        return new Builder(handlerSelectionStrategy);
    }

    public static Builder builder(){
        return new Builder(new ClassHierarchyStrategy());
    }

    @Override public void onValidationEvent(ValidationEvent e) {
        Objects.requireNonNull(e);
        handlerSelectionStrategy.findHandlers(this.eventHandlers, e)
                        .forEach(handler -> handler.accept(e));
    }

    private <T extends ValidationEvent> void registerHandlerInternal(Class<T> eventType, Consumer<? super T> handler) {
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(handler);
        eventHandlers.compute(eventType, (k, l) -> {
            List<Consumer<ValidationEvent>> ret = Optional.ofNullable(l).orElse(new ArrayList<>());
            //noinspection unchecked
            ret.add((Consumer<ValidationEvent>) handler);
            return ret;
        });
        handlerSelectionStrategy.onNewHandlerRegistered(eventType);
    }

    private static class FilteredEventHandler
                    implements Consumer<ValidationEvent> {
        private final Predicate<ValidationEvent> filter;
        private final Consumer<ValidationEvent> handler;
        public FilteredEventHandler(Predicate<ValidationEvent> filter, Consumer<ValidationEvent> handler) {
            this.filter = filter;
            this.handler = handler;
        }

        @Override public void accept(ValidationEvent validationEvent) {
            if (filter.test(validationEvent)){
                handler.accept(validationEvent);
            }
        }
    }

    public static class HandlerAdder<T extends ValidationEvent> {
        private final Builder parent;
        private final Class<T>[] eventTypes;

        @SafeVarargs
        public HandlerAdder(Builder parent, Class<T>... eventTypes) {
            this.eventTypes = eventTypes;
            this.parent = parent;
        }

        @SafeVarargs
        public final Builder addSimpleHandlers(Consumer<? super T>... handlers) {
            for(Consumer<? super T> handler:handlers) {
                Builder ignoreMe = addSimpleHandler(handler);
            }
            return parent;
        }

        public Builder addSimpleHandler(Consumer<? super T> handler){
            for(Class<T> eventType: eventTypes) {
                parent.registerHandlerInternal(eventType, handler);
            }
            return parent;
        }

        public Builder addHandler(HandlerConfigurer<T> handlerConfigurer){
            HandlerBuilder<T> hb = new HandlerBuilder<>();
            handlerConfigurer.configure(hb);
            for(Class<T> eventType: eventTypes) {
                parent.registerHandlerInternal(eventType, hb.build());
            }
            return parent;
        }
    }

    public static class Builder{
        private HandlerBasedValidationListener listener;

        private Builder(HandlerSelectionStrategy handlerSelectionStrategy) {
            this.listener = new HandlerBasedValidationListener(handlerSelectionStrategy);
        }

        public <T extends ValidationEvent> HandlerAdder<T> forEventType(Class<T> eventType){
            return new HandlerAdder<>(Builder.this, eventType);
        }

        @SafeVarargs
        @SuppressWarnings("unchecked")
        public final HandlerAdder<ValidationEvent> forEventTypes(Class<? extends ValidationEvent>... eventType){
            return new HandlerAdder<>(Builder.this, (Class<ValidationEvent>[]) eventType);
        }

        public HandlerBasedValidationListener build(){
            HandlerBasedValidationListener ret = listener;
            listener = null;
            return ret;
        }

        private <T extends ValidationEvent> void registerHandlerInternal(Class<T> eventType,
                        Consumer<? super T> handler) {
            listener.registerHandlerInternal(eventType, handler);
        }
    }

    public interface HandlerSelectionStrategy {
        Collection<Consumer<ValidationEvent>> findHandlers(Map<Class<? extends ValidationEvent>, List<Consumer<ValidationEvent>>> handlers, ValidationEvent event);

        void onNewHandlerRegistered(Class<? extends ValidationEvent> eventType);
    }

    private static class ClassHierarchyStrategy implements HandlerSelectionStrategy  {
        private final Map<Class<? extends ValidationEvent>, List<Consumer<ValidationEvent>>> registeredHandlersCache = new HashMap<>();
        public ClassHierarchyStrategy() {
        }

        @Override public void onNewHandlerRegistered(Class<? extends ValidationEvent> eventType) {
            registeredHandlersCache.clear();
        }

        @Override public Collection<Consumer<ValidationEvent>> findHandlers(
                        Map<Class<? extends ValidationEvent>, List<Consumer<ValidationEvent>>> handlers,
                        ValidationEvent event) {
            return registeredHandlersCache.computeIfAbsent(event.getClass(), e -> {
                List<Class<? extends ValidationEvent>> eventTypes =  EventUtil.getSuperclassesAndInterfaces(event.getClass()).collect(
                                Collectors.toUnmodifiableList());
                return getHandlers(handlers, eventTypes);
            });
        }

        private List<Consumer<ValidationEvent>> getHandlers(Map<Class<? extends ValidationEvent>, List<Consumer<ValidationEvent>>> handlers, List<Class<? extends ValidationEvent>> eventTypes) {
            return eventTypes.stream().flatMap(t -> handlers.getOrDefault(t, List.of()).stream()).collect(Collectors.toList());
        }
    }

    public interface HandlerConditionCustomizer<T extends ValidationEvent> {
        HandlerCustomizer<T> iff(Predicate<ValidationEvent> predicate);
        void handle(Consumer<T> consumer);
    }

    public interface HandlerCustomizer<T extends ValidationEvent> {
        void handle(Consumer<T> consumer);
    }


    public interface HandlerConfigurer<T extends ValidationEvent> {
        void configure(HandlerConditionCustomizer<T> handlerCustomizer);
    }

    public static class HandlerBuilder<T extends ValidationEvent> implements HandlerCustomizer<T>, HandlerConditionCustomizer<T> {
        private Predicate<ValidationEvent> predicate = null;
        private Consumer<T> handler = null;
        public HandlerBuilder<T> iff(Predicate<ValidationEvent> predicate){
            this.predicate = predicate;
            return this;
        }
        public void handle(Consumer<T> handler) {
            this.handler = handler;
        }
        @SuppressWarnings("unchecked")
        public Consumer<T> build(){
            Objects.requireNonNull(handler);
            if (predicate == null) {
                return handler;
            }
            return (Consumer<T>) new FilteredEventHandler(predicate, (Consumer<ValidationEvent>) handler);
        }
    }

}
