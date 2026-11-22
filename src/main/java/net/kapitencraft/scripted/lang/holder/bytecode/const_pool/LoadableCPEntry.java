package net.kapitencraft.scripted.lang.holder.bytecode.const_pool;

public interface LoadableCPEntry extends ConstantPoolEntry {

    Object load();
}
