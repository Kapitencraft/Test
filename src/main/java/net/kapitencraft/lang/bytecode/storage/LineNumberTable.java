package net.kapitencraft.lang.bytecode.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kapitencraft.tool.GsonHelper;
import net.kapitencraft.tool.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;

public record LineNumberTable(Pair<Integer, Integer>[] lines) {

    public static LineNumberTable read(JsonArray data) {
        return new LineNumberTable(data.asList().stream().map(JsonElement::getAsJsonObject)
                .map(o -> Pair.of(GsonHelper.getAsInt(o, "pc"), GsonHelper.getAsInt(o, "line")))
                .toArray(Pair[]::new));
    }

    public JsonArray save() {
        return Arrays.stream(this.lines).map(p -> {
            JsonObject object = new JsonObject();
            object.addProperty("pc", p.left());
            object.addProperty("line", p.right());
            return object;
        }).collect(GsonHelper.toJsonArray());
    }

    public int getLineAt(int ip) {
        int i = 0;
        while (i < lines.length - 1 && lines[i].left() < ip) i++;
        return lines[i].right();
    }

    public static class Builder {
        private final List<Pair<Integer, Integer>> lineChanges = new ArrayList<>();

        public void change(int pc, int lineNumber) {
            lineChanges.add(Pair.of(pc, lineNumber));
        }

        public LineNumberTable build() {
            return new LineNumberTable(lineChanges.toArray(Pair[]::new));
        }

        public void changeIfNecessary(int line, int pc) {
            if (this.lineChanges.isEmpty() || this.lineChanges.get(this.lineChanges.size() - 1).right() != line) {
                this.lineChanges.add(Pair.of(pc, line));
            }
        }

        public void clear() {
            this.lineChanges.clear();
        }
    }
}
