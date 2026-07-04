package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.domian.dart.entity.DisclosureType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DisclosureProcessorFactory {

    private final Map<DisclosureType, DisclosureProcessor> processorMap = new EnumMap<>(DisclosureType.class);

    public DisclosureProcessorFactory(List<DisclosureProcessor> processors) {
        processors.forEach(p -> processorMap.put(p.getSupportedType(), p));
    }

    public DisclosureProcessor getProcessor(DisclosureType type) {
        return processorMap.get(type);
    }
}
