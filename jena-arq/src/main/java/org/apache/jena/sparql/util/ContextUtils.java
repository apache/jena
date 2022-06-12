package org.apache.jena.sparql.util;

import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

/**
 * Utils to work with {@link Context}.
 */
public class ContextUtils {

    /**
     * Copies the given context also copying its registries
     * ({@link FunctionRegistry}, {@link PropertyFunctionRegistry} and {@link ServiceExecutorRegistry}).
     * If the input context is null, then method just creates a new empty instance.
     *
     * @param from {@link Context} or {@code null}
     * @return a new {@link Context} instance
     */
    public static Context copyWithRegistries(Context from) {
        FunctionRegistry fr = FunctionRegistry.createFrom(FunctionRegistry.get(from));
        PropertyFunctionRegistry pfr = PropertyFunctionRegistry.createFrom(PropertyFunctionRegistry.get(from));
        ServiceExecutorRegistry ser = ServiceExecutorRegistry.createFrom(ServiceExecutorRegistry.get(from));
        Context res = from == null ? new Context() : from.copy();
        FunctionRegistry.set(res, fr);
        PropertyFunctionRegistry.set(res, pfr);
        ServiceExecutorRegistry.set(res, ser);
        return res;
    }
}
