package net.kapitencraft.lang.run;

import net.kapitencraft.lang.exception.runtime.AbstractScriptedException;
import net.kapitencraft.tool.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Consumer;

public class CallStack {
    public static final int MAX_STACK_SIZE = 200;

    Deque<Pair<String, String>> stack = new ArrayDeque<>();
    Deque<Integer> lineIndexes = new ArrayDeque<>();

    public void pushLineIndex(int index) {
        if (stack.size() != lineIndexes.size() + 1) {
            throw new IllegalStateException("push line only if there's already a location applied!");
        }
        lineIndexes.push(index);
    }

    public void push(String methodRef, String callClass) {
        if (stack.size() > MAX_STACK_SIZE) throw AbstractScriptedException.createException(VarTypeManager.STACK_OVERFLOW_EXCEPTION, "");
        stack.push(Pair.of(methodRef, callClass));
    }

    public void printStackTrace(Consumer<String> stackTracePrinter) {
        if (stack.size() != lineIndexes.size()) throw new IllegalStateException("can only print stack trace when there's the same amount of indexes and locations");
        Iterator<Pair<String, String>> stackIterator = stack.iterator();
        Iterator<Integer> indexesIterator = lineIndexes.iterator();
        while (stackIterator.hasNext()) {
            Pair<String, String> location = stackIterator.next();
            int index = indexesIterator.next();
            if (index == -1)
                stackTracePrinter.accept(String.format("\tat %s(NativeMethod)", location.left()));
            else
                stackTracePrinter.accept(String.format("\tat %s(%s.scr:%s)", location.left(), location.right(), index));
        }
    }

    public void pop() {
        lineIndexes.pop();
        stack.pop();
    }

    public void clear() {
        stack.clear();
        lineIndexes.clear();
    }

    public void resetToSize(int stackIndex) {
        this.lineIndexes.pop();
        while (this.stack.size() > stackIndex) {
            this.pop();
        }
    }
}
