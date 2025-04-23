package net.kapitencraft.lang.run.test;

import com.google.gson.*;
import net.kapitencraft.lang.holder.class_ref.ClassReference;
import net.kapitencraft.lang.run.Interpreter;
import net.kapitencraft.lang.run.VarTypeManager;
import net.kapitencraft.lang.run.load.ClassLoader;
import net.kapitencraft.tool.GsonHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TestLoader {
    private static final Gson GSON = new GsonBuilder().create();
    private static final File TEST_CONFIG = new File("./run/test.json");

    private record TestInstance(String target, String args, String[] output) {
        public void run(Interpreter interpreter) {
            ClassReference reference = VarTypeManager.getClassForName(target);
            if (reference == null || !reference.exists()) {
                System.err.println("unknown class: " + target);
                return;
            }
            interpreter.runMainMethod(reference.get(), args, false, false);
        }
    }

    public static void main(String[] args) {
        try {
            JsonArray array = GSON.fromJson(new FileReader(TEST_CONFIG), JsonArray.class);
            List<TestInstance> tests = new ArrayList<>();
            for (JsonElement element : array.asList()) {
                JsonObject object = element.getAsJsonObject();
                String target = GsonHelper.getAsString(object, "target");
                String testArgs = GsonHelper.getAsString(object, "args", "");
                String[] output = GsonHelper.getAsJsonArray(object, "output").asList().stream().map(JsonElement::getAsString).toArray(String[]::new);
                tests.add(new TestInstance(target, testArgs, output));
            }
            ClassLoader.loadClasses();
            TestExecution execution = new TestExecution();
            execution.setup();
            tests.forEach(execution::runTest);
            execution.clear();
        } catch (FileNotFoundException e) {
            System.err.println("file not found: " + e.getMessage());
        }
    }

    private static class TestExecution {
        private int outputIndex = 0;
        private boolean error = false;
        private TestInstance running;
        private Interpreter interpreter;

        public void setup() {
            this.interpreter = Interpreter.INSTANCE;
            interpreter.output = this::checkOutput;
        }

        public void runTest(TestInstance instance) {
            this.running = instance;
            this.outputIndex = 0;
            this.error = false;
            instance.run(interpreter);
            if (error) {
                System.out.println("\u001B[31mError running class '" + instance.target + "'\u001B[0m");
            } else {
                System.out.println("\u001B[32mSuccessfully tested class '" + instance.target + "'. took " + interpreter.elapsedMillis() + "ms\u001B[0m");
            }
        }

        public void clear() {
            this.interpreter.output = System.out::println;
        }

        private void checkOutput(String output) {
            if (outputIndex >= running.output.length) {
                System.err.println("Test for '" + running.target + "' failed. more outputs got than expected");
                error = true;
            } else if (!output.equals(running.output[outputIndex])) {
                System.err.println("Test for '" + running.target + "' failed at index " + outputIndex + ". Expected \"" + running.output[outputIndex] + "\", but got: \"" + output + "\"");
                error = true;
            }
            outputIndex++;
        }
    }
}
