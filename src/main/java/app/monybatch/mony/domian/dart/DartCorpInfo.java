package app.monybatch.mony.domian.dart;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DartCorpInfo {



    /**
     * 상장회사 종목코드 (6자리)
     * 예: 005930
     */
    private String stockCode;

    /**
     * 대표자명
     * 예: 한종희
     */
    private String ceoNm;

    /**
     * 법인구분
     * Y : 유가증권시장(KOSPI)
     * K : 코스닥(KOSDAQ)
     * N : 코넥스(KONEX)
     * E : 기타법인
     */
    private String corpCls;

    /**
     * 법인등록번호
     */
    private String jurirNo;

    /**
     * 사업자등록번호
     */
    private String bizrNo;

    /**
     * 회사 주소
     */
    private String adres;

    /**
     * 회사 홈페이지 URL
     */
    private String hmUrl;

    /**
     * IR 홈페이지 URL
     */
    private String irUrl;

    /**
     * 대표 전화번호
     */
    private String phnNo;

    /**
     * 팩스번호
     */
    private String faxNo;

    /**
     * 업종코드
     * (KRX 또는 DART 기준 산업코드)
     */
    private String indutyCode;

    /**
     * 설립일
     * 형식: YYYYMMDD
     * 예: 19690113
     */
    private String estDt;

    /**
     * 결산월
     * 형식: MM
     * 예: 12
     */
    private String accMt;
}
