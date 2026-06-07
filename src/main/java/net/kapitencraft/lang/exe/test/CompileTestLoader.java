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
    private static final File TEST_SRC = new File("./run/compile_test_source");

    private record TestInstance(String target, ErrorData[] output) {

        /**
         * @param data the loaded class info
         * @return whether this test had an error
         */
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

                        if (message.msg().equals(errorData.msg)) {
                            if (message.lineIndex() != errorData.line) {
                                System.out.printf("\u001B[31mmessage at index %s on wrong line: expected: %s but got %s\u001B[0m\n", i, errorData.line, message.lineIndex());
                                hadError = true;
                            }
                        } else {
                            System.out.printf("\u001B[31mmessage doesn't match at index %s: expected: \"%s\" but got \"%s\"\u001B[0m\n", i, errorData.msg, message.msg());
                            hadError = true;
                        }
                    } else {
                        System.out.printf("\u001B[31mmessage type at index %s doesn't match: expected: %s but got %s\u001B[0m\n", i, errorData.type, message.getClass().getSimpleName());
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
                ErrorData[] data = GsonHelper.getAsJsonArray(object, "output").asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(o -> {
                            ErrorType type = ErrorType.valueOf(GsonHelper.getAsString(o, "type").toUpperCase());
                            String msg = GsonHelper.getAsString(o, "msg");
                            int line = GsonHelper.getAsInt(o, "line");
                            return new ErrorData(type, line, msg);
                        }).toArray(ErrorData[]::new);
                tests.add(new TestInstance(target, data));
            }
            ClassLoader.PackageHolder<CompilerLoaderHolder> holder = Compiler.compile(false, false, TEST_SRC, null);
            TestExecution execution = new TestExecution(holder);
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
        private boolean error = false;

        private TestExecution(ClassLoader.PackageHolder<CompilerLoaderHolder> holders) {
            this.holders = holders;
        }

        public void runTest(TestInstance instance) {
            this.error = false;
            Interpreter.start();
            error |= instance.run(this.holders);
            if (error) {
                System.out.println("\u001B[31mError testing class '" + instance.target + "'\u001B[0m");
            } else {
                succeeded++;
                System.out.println("\u001B[32mSuccessfully tested class '" + instance.target + "'. took " + Interpreter.elapsedMillis() + "ms\u001B[0m");
            }
        }

        public void clear() {
            Interpreter.output = System.out::println;
        }

        public boolean error() {
            return this.error;
        }

        public int getSucceeded() {
            return succeeded;
        }
    }

    private record ErrorData(ErrorType type, int line, String msg) {
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
