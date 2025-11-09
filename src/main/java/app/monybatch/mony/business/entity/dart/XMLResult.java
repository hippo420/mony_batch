package app.monybatch.mony.business.entity.dart;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

// 최상위 <result> 태그를 나타내는 클래스
@JacksonXmlRootElement(localName = "result")
public class XMLResult {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "list")
    private List<DartMapping> list;
    // ✅ getter / setter 필수
    public List<DartMapping> getList() {
        return list;
    }

    public void setList(List<DartMapping> list) {
        this.list = list;
    }
}
