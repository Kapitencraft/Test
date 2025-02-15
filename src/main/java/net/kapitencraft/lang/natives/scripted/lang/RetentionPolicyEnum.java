package net.kapitencraft.lang.natives.scripted.lang;

import net.kapitencraft.lang.oop.clazz.wrapper.NativeEnum;
import net.kapitencraft.lang.oop.method.builder.ConstructorContainer;
import net.kapitencraft.lang.oop.method.map.GeneratedMethodMap;

import java.util.List;
import java.util.Map;

public class RetentionPolicyEnum extends NativeEnum {
    public RetentionPolicyEnum() {
        super("RetentionPolicy", "scripted.lang.annotations", ConstructorContainer.builder(), GeneratedMethodMap.empty(), GeneratedMethodMap.empty(),
                Map.of("SOURCE", List.of(),
                        "CLASS", List.of(),
                        "RUNTIME", List.of()
                )
        );
    }
}
