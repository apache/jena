package org.apache.jena.atlas.lib;

import java.util.function.Consumer;
import java.util.function.Function;

public class Union<T> {

    protected final T left, right;

    public Union(T left, T right) {
        this.left = left;
        this.right = right;
    }

    protected boolean both(Function<T, Boolean> op) {
        return op.apply(left) && op.apply(right);
    }

    protected boolean either(Function<T, Boolean> op) {
        return op.apply(left) || op.apply(right);
    }

    protected void forEach(Consumer<T> op) {
        op.accept(left);
        op.accept(right);
    }

}
