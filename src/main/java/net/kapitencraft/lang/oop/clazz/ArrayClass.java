package net.kapitencraft.lang.oop.clazz;

public class ArrayClass extends PrimitiveClass {
    private final LoxClass enclosed;

    public ArrayClass(LoxClass enclosed) {
        super(enclosed.name() + "[]", null, null);
        this.enclosed = enclosed;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public String absoluteName() {
        return "[" + super.absoluteName();
    }
}
