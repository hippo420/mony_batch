package app.monybatch.mony.common.core.utils.parser;

import java.util.List;

public interface  ParserUtil<T> {
    List<T> parseHtml(String data,String date);

    void parseHtmlDetail(String data, T object);
}
