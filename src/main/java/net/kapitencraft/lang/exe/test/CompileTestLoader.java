package net.kapitencraft.lang.exe.test;

import com.google.gson.*;
import net.kapitencraft.lang.compiler.Compiler;
import net.kapitencraft.lang.compiler.error.ErrorStorage;
import net.kapitencraft.lang.exe.Interpreter;
import net.kapitencraft.lang.exe.load.ClassLoader;
import net.kapitencraft.lang.exe.load.CompilerLoaderHolder;
import net.kapitencraft.tool.GsonHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CompileTestLoader {
    private static final Gson GSON = new GsonBuilder().create();
    private static final File TEST_CONFIG = new File("./run/compile_test.json");

    private record TestInstance(String target, String args, ErrorData[] output) {
        public boolean run(ClassLoader.PackageHolder<CompilerLoaderHolder> data) {
            CompilerLoaderHolder holder = data.getEntry(target);
            if (holder == null) {
                System.out.println("\u001B[31munknown class: " + target + "\u001B[0m");
                return true;
            }
            System.out.printf("\u001B[32mChecking: %s\u001B[0m\n", target);
            try {
                ErrorStorage errorInfo = holder.getErrorInfo();
                List<ErrorStorage.Message> errors = errorInfo.getMessages();
                boolean hadError = false;
                for (int i = 0; i < output.length; i++) {
                    ErrorData errorData = output[i];
                    if (errors.size() == i) {
                        System.out.printf("\u001B[31mless errors got (%s) than expected (%s)\u001B[0m\n", errors.size(), output.length);
                        return true;
                    }
                    ErrorStorage.Message message = errors.get(i);
                    if (errorData.type.matches(message)) {
                        if (!message.msg().equals(errorData.msg)) {
                            System.out.printf("\u001B[31mmessage doesn't match: expected: \"%s\" but got \"%s\"\u001B[0m\n", errorData.msg, message.msg());
                            hadError = true;
                        }
                    } else {
                        System.out.printf("\u001B[31mmessage type doesn't match: expected: %s but got %s\u001B[0m\n", errorData.type, message.getClass().getSimpleName());
                        hadError = true;
                    }
                }
                if (output.length < errors.size()) {
                    System.out.printf("\u001B[31mmore errors got (%s) than expected (%s)\u001B[0m\n", errors.size(), output.length);
                    return true;
                }
                return hadError;
            } catch (Exception e) {
                System.out.println("\u001B[31mprogram crashed: " + e.getMessage() + "\u001B[0m");
                return true;
            }
        }
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        try {
            JsonArray array = GSON.fromJson(new FileReader(TEST_CONFIG), JsonArray.class);
            List<TestInstance> tests = new ArrayList<>();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String target = GsonHelper.getAsString(object, "target");
                String testArgs = GsonHelper.getAsString(object, "args", "");
                ErrorData[] data = GsonHelper.getAsJsonArray(object, "output").asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(o -> {
                            ErrorType type = ErrorType.valueOf(GsonHelper.getAsString(o, "type"));
                            String msg = GsonHelper.getAsString(o, "msg");
                            return new ErrorData(type, msg);
                        }).toArray(ErrorData[]::new);
                tests.add(new TestInstance(target, testArgs, data));
            }
            ClassLoader.PackageHolder<CompilerLoaderHolder> holder = Compiler.compile(false, false, Compiler.ROOT, null);
            TestExecution execution = new TestExecution(holder);
            execution.setup();
            tests.forEach(execution::runTest);
            execution.clear();
            System.out.printf("test complete. %s / %s successful\n", execution.getSucceeded(), tests.size());
        } catch (FileNotFoundException e) {
            System.out.println("file not found: " + e.getMessage() + "\u001B[0m");
        }
    }

    private static class TestExecution {
        private final ClassLoader.PackageHolder<CompilerLoaderHolder> holders;
        private int succeeded = 0;
        private int outputIndex = 0;
        private boolean error = false;
        private TestInstance running;

        private TestExecution(ClassLoader.PackageHolder<CompilerLoaderHolder> holders) {
            this.holders = holders;
        }

        public void setup() {
            Interpreter.output = this::checkOutput;
        }

        @SuppressWarnings("ConstantValue")
        public void runTest(TestInstance instance) {
            this.running = instance;
            this.outputIndex = 0;
            this.error = false;
            error |= instance.run(this.holders);
            if (instance.output.length > this.outputIndex) {
                System.out.printf("\u001B[31mMissing outputs. got %s but expected %s\u001B[0m\n", this.outputIndex, instance.output.length);
                error = true;
            }
            if (error) {
                System.out.println("\u001B[31mError running class '" + instance.target + "'\u001B[0m");
            } else {
                succeeded++;
                System.out.println("\u001B[32mSuccessfully tested class '" + instance.target + "'. took " + Interpreter.elapsedMillis() + "ms\u001B[0m");
            }
        }

        public void clear() {
            Interpreter.output = System.out::println;
        }

        private void checkOutput(String output) {
            if (outputIndex >= running.output.length) {
                System.out.println("\u001B[31mTest for '" + running.target + "' failed. more outputs got than expected: " + output + "\u001B[0m");
                error = true;
            } else if (!output.equals(running.output[outputIndex])) {
                System.out.printf("\u001B[31mTest for '%s' failed at index %s. Expected \"%s\", but got: \"%s\"\u001B[0m\n", running.target, outputIndex, running.output[outputIndex], output);
                error = true;
            }
            outputIndex++;
        }

        public boolean error() {
            return this.error;
        }

        public int getSucceeded() {
            return succeeded;
        }
    }

    private record ErrorData(ErrorType type, String msg) {
    }

    private enum ErrorType {
        WARN(ErrorStorage.Warn.class),
        ERROR(ErrorStorage.Error.class),
        LOG(ErrorStorage.Log.class);

        private final Class<? extends ErrorStorage.Message> messageType;

        ErrorType(Class<? extends ErrorStorage.Message> messageType) {
            this.messageType = messageType;
        }

        public boolean matches(ErrorStorage.Message message) {
            return messageType.isInstance(message);
        }
    }
}
