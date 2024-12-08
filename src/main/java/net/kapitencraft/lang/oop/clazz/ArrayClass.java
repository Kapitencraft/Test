package net.kapitencraft.lang.oop.clazz;

public class ArrayClass extends PrimitiveClass {
    private final LoxClass component;

    public ArrayClass(LoxClass component) {
        super(component.name() + "[]", null);
        this.component = component;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean is(LoxClass other) {
        return other instanceof ArrayClass arrayClass ? arrayClass.component.is(this.component) :
                other instanceof PrimitiveClass primitiveClass && primitiveClass.is(this);
    }

    @Override
    public LoxClass getComponentType() {
        return component;
    }

    @Override
    public String absoluteName() {
        return super.absoluteName();
    }
}
