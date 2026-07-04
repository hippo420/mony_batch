package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import org.json.simple.JSONObject;

import java.util.List;

public interface DisclosureProcessor {
    DisclosureType getSupportedType();
    List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem);
}
