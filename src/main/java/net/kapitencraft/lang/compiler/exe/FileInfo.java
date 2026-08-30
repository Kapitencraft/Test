package net.kapitencraft.lang.compiler.exe;

import net.kapitencraft.lang.compiler.error.ErrorStorage;

public record FileInfo(String content, String fileName, String pck, ErrorStorage errorStorage) {
}
