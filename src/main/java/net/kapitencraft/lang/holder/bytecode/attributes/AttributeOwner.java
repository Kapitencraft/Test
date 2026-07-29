package net.kapitencraft.lang.holder.bytecode.attributes;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;

public interface AttributeOwner {

    <T extends AttributeInfo> Collection<T> getAttribute(String name);

    default  <T extends AttributeInfo> T getSingleAttribute(String name) {
        return (T) getAttribute(name).toArray()[0];
    }

    boolean hasAttribute(String name);

    static Multimap<String, AttributeInfo> createLookup(AttributeInfo[] infos) {
        ImmutableMultimap.Builder<String, AttributeInfo> builder = new ImmutableMultimap.Builder<>();
        for (AttributeInfo info : infos) {
            builder.put(info.name(), info);
        }
        return builder.build();
    }
}
