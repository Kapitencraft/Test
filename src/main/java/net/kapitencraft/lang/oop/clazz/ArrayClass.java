package net.kapitencraft.lang.oop.clazz;

public class ArrayClass extends PrimitiveClass {
    private final LoxClass enclosed;

    public ArrayClass(LoxClass enclosed) {
        super(enclosed.name() + "[]", null);
        this.enclosed = enclosed;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean is(LoxClass other) {
        return other instanceof ArrayClass arrayClass ? arrayClass.enclosed.is(this.enclosed) :
                other instanceof PrimitiveClass primitiveClass && primitiveClass.is(this);
    }

    @Override
    public String absoluteName() {
        return "[" + super.absoluteName();
    }
}
