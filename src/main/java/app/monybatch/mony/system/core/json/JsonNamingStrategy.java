package app.monybatch.mony.system.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

public class JsonNamingStrategy extends PropertyNamingStrategy {
    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        // 전체 대문자 처리
        if(defaultName.equals(defaultName.toUpperCase())) {
            return defaultName.toUpperCase();
        }
        return defaultName;
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return nameForField(config, null, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return nameForField(config, null, defaultName);
    }
}
