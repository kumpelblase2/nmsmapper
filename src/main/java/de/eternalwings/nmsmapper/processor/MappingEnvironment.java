package de.eternalwings.nmsmapper.processor;

import de.eternalwings.nmsmapper.model.NMSMappingInfo;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.Map;

public class MappingEnvironment {
    private final Map<String, String> knownMappers;
    private final Collection<NMSMappingInfo> mappings;
    private final NMSProcessor processor;

    public MappingEnvironment(Map<String, String> knownMappers, Collection<NMSMappingInfo> mappings, NMSProcessor processor) {
        this.knownMappers = knownMappers;
        this.mappings = mappings;
        this.processor = processor;
    }

    public Map<String, String> getKnownMappers() {
        return knownMappers;
    }

    public Collection<NMSMappingInfo> getMappings() {
        return mappings;
    }

    public boolean hasMapperFor(String type) {
        return this.knownMappers.containsKey(type);
    }

    public String getMappedTypeOf(String interfaceType) {
        return this.knownMappers.get(interfaceType);
    }

    public Messager getMessager() {
        return processor.getMessager();
    }

    public Filer getFiler() {
        return processor.getFiler();
    }

    public Elements getElementUtils() {
        return processor.getElementUtils();
    }

    public Types getTypeUtils() {
        return processor.getTypeUtils();
    }
}
