package net.kapitencraft.lang.env.abst;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class DequeStack<T> {
    private final ArrayDeque<T> stack;
    private final Supplier<T> def;

    /**
     * @param def default value added on creation
     */
    public DequeStack(Supplier<T> def) {
        this.stack = new ArrayDeque<>();
        this.stack.add(def.get());
        this.def = def;
    }

    /**
     * push the stack; use pop to revert changes made after push
     */
    public void push() {
        stack.addLast(def.get());
    }

    protected T getLast() {
        return stack.getLast();
    }

    /**
     * pop the stack; removes any changes made since the last `'push' call
     */
    public void pop() {
        stack.removeLast();
        if (stack.isEmpty()) throw new IllegalStateException("leveled has been completely cleared");
    }

    protected int size() {
        return stack.size();
    }

    //TODO use arraylist (pain!)
    protected T get(int i) {
        return stack.
    }
}