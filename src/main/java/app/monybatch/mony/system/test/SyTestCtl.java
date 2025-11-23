package app.monybatch.mony.system.test;

import app.monybatch.mony.business.batch.reader.OpenAPIListReader;
import app.monybatch.mony.business.entity.Stock;
import app.monybatch.mony.system.core.constant.DataType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class SyTestCtl {

    @RequestMapping("dart/baedang")
    public void baedang() throws Exception {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        //params.add(BASDD,basDd);
        List<Stock> resList = (List<Stock>) new OpenAPIListReader<>(Stock.class, params,"DART","", DataType.DATA_ZIP).read();
    }
}
