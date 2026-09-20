package net.kapitencraft.lang.compiler.exe.text;

import net.kapitencraft.lang.compiler.VarTypeContainer;
import net.kapitencraft.lang.holder.oop.clazz.ClassConstructor;
import net.kapitencraft.lang.holder.token.Token;

public interface HolderParser {

    void apply(Token[] tokens, VarTypeContainer varTypeContainer);

    ClassConstructor parseFile(String fileName, String pck);
}
