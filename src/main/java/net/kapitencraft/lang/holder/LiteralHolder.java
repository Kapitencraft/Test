package net.kapitencraft.lang.holder;

import net.kapitencraft.lang.oop.LoxClass;

public class LiteralHolder {
    final Object value;
    final LoxClass type;

    public LiteralHolder(Object value, LoxClass type) {
        this.value = value;
        this.type = type;
    }

    public LoxClass getType() {
        return type;
    }
}
