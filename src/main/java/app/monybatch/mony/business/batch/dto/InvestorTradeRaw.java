package app.monybatch.mony.business.batch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InvestorTradeRaw {

    /* ===============================
       주식 기본 정보
     =============================== */

    /** stck_bsop_date : 주식 영업 일자 (YYYYMMDD) */
    @JsonProperty("stck_bsop_date")
    private String stckBsopDate;

    /** isuSrtCd : 주식 단축코드 */
    private String isuSrtCd;


    /** stck_clpr : 주식 종가 */
    @JsonProperty("stck_clpr")
    private String stckClpr;

    /** prdy_vrss : 전일 대비 */
    @JsonProperty("prdy_vrss")
    private String prdyVrss;

    /** prdy_vrss_sign : 전일 대비 부호 */
    @JsonProperty("prdy_vrss_sign")
    private String prdyVrssSign;

    /** prdy_ctrt : 전일 대비율 (%) */
    @JsonProperty("prdy_ctrt")
    private String prdyCtrt;

    /** acml_vol : 누적 거래량 (주) */
    @JsonProperty("acml_vol")
    private String acmlVol;

    /** acml_tr_pbmn : 누적 거래 대금 (백만원) */
    @JsonProperty("acml_tr_pbmn")
    private String acmlTrPbmn;

    /** stck_oprc : 주식 시가 */
    @JsonProperty("stck_oprc")
    private String stckOprc;

    /** stck_hgpr : 주식 최고가 */
    @JsonProperty("stck_hgpr")
    private String stckHgpr;

    /** stck_lwpr : 주식 최저가 */
    @JsonProperty("stck_lwpr")
    private String stckLwpr;

    /* ===============================
       순매수 수량 (Net Buy Qty)
     =============================== */

    /** frgn_ntby_qty : 외국인 순매수 수량 */
    @JsonProperty("frgn_ntby_qty")
    private String frgnNtbyQty;

    /** frgn_reg_ntby_qty : 외국인 등록 순매수 수량 */
    @JsonProperty("frgn_reg_ntby_qty")
    private String frgnRegNtbyQty;

    /** frgn_nreg_ntby_qty : 외국인 비등록 순매수 수량 */
    @JsonProperty("frgn_nreg_ntby_qty")
    private String frgnNregNtbyQty;

    /** prsn_ntby_qty : 개인 순매수 수량 */
    @JsonProperty("prsn_ntby_qty")
    private String prsnNtbyQty;

    /** orgn_ntby_qty : 기관계 순매수 수량 */
    @JsonProperty("orgn_ntby_qty")
    private String orgnNtbyQty;

    /** scrt_ntby_qty : 증권 순매수 수량 */
    @JsonProperty("scrt_ntby_qty")
    private String scrtNtbyQty;

    /** ivtr_ntby_qty : 투자신탁 순매수 수량 */
    @JsonProperty("ivtr_ntby_qty")
    private String ivtrNtbyQty;

    /** pe_fund_ntby_vol : 사모펀드 순매수 거래량 */
    @JsonProperty("pe_fund_ntby_vol")
    private String peFundNtbyVol;

    /** bank_ntby_qty : 은행 순매수 수량 */
    @JsonProperty("bank_ntby_qty")
    private String bankNtbyQty;

    /** insu_ntby_qty : 보험 순매수 수량 */
    @JsonProperty("insu_ntby_qty")
    private String insuNtbyQty;

    /** mrbn_ntby_qty : 종금 순매수 수량 */
    @JsonProperty("mrbn_ntby_qty")
    private String mrbnNtbyQty;

    /** fund_ntby_qty : 기금 순매수 수량 */
    @JsonProperty("fund_ntby_qty")
    private String fundNtbyQty;

    /** etc_ntby_qty : 기타 순매수 수량 */
    @JsonProperty("etc_ntby_qty")
    private String etcNtbyQty;

    /** etc_corp_ntby_vol : 기타 법인 순매수 거래량 */
    @JsonProperty("etc_corp_ntby_vol")
    private String etcCorpNtbyVol;

    /** etc_orgt_ntby_vol : 기타 단체 순매수 거래량 */
    @JsonProperty("etc_orgt_ntby_vol")
    private String etcOrgtNtbyVol;

    /* ===============================
       순매수 거래 대금 (백만원)
     =============================== */

    /** frgn_ntby_tr_pbmn : 외국인 순매수 거래 대금 */
    @JsonProperty("frgn_ntby_tr_pbmn")
    private String frgnNtbyTrPbmn;

    /** frgn_reg_ntby_pbmn : 외국인 등록 순매수 대금 */
    @JsonProperty("frgn_reg_ntby_pbmn")
    private String frgnRegNtbyPbmn;

    /** frgn_nreg_ntby_pbmn : 외국인 비등록 순매수 대금 */
    @JsonProperty("frgn_nreg_ntby_pbmn")
    private String frgnNregNtbyPbmn;

    /** prsn_ntby_tr_pbmn : 개인 순매수 거래 대금 */
    @JsonProperty("prsn_ntby_tr_pbmn")
    private String prsnNtbyTrPbmn;

    /** orgn_ntby_tr_pbmn : 기관계 순매수 거래 대금 */
    @JsonProperty("orgn_ntby_tr_pbmn")
    private String orgnNtbyTrPbmn;

    /** scrt_ntby_tr_pbmn : 증권 순매수 거래 대금 */
    @JsonProperty("scrt_ntby_tr_pbmn")
    private String scrtNtbyTrPbmn;

    /** pe_fund_ntby_tr_pbmn : 사모펀드 순매수 거래 대금 */
    @JsonProperty("pe_fund_ntby_tr_pbmn")
    private String peFundNtbyTrPbmn;

    /** ivtr_ntby_tr_pbmn : 투자신탁 순매수 거래 대금 */
    @JsonProperty("ivtr_ntby_tr_pbmn")
    private String ivtrNtbyTrPbmn;

    /** bank_ntby_tr_pbmn : 은행 순매수 거래 대금 */
    @JsonProperty("bank_ntby_tr_pbmn")
    private String bankNtbyTrPbmn;

    /** insu_ntby_tr_pbmn : 보험 순매수 거래 대금 */
    @JsonProperty("insu_ntby_tr_pbmn")
    private String insuNtbyTrPbmn;

    /** mrbn_ntby_tr_pbmn : 종금 순매수 거래 대금 */
    @JsonProperty("mrbn_ntby_tr_pbmn")
    private String mrbnNtbyTrPbmn;

    /** fund_ntby_tr_pbmn : 기금 순매수 거래 대금 */
    @JsonProperty("fund_ntby_tr_pbmn")
    private String fundNtbyTrPbmn;

    /** etc_ntby_tr_pbmn : 기타 순매수 거래 대금 */
    @JsonProperty("etc_ntby_tr_pbmn")
    private String etcNtbyTrPbmn;

    /** etc_corp_ntby_tr_pbmn : 기타 법인 순매수 거래 대금 */
    @JsonProperty("etc_corp_ntby_tr_pbmn")
    private String etcCorpNtbyTrPbmn;

    /** etc_orgt_ntby_tr_pbmn : 기타 단체 순매수 거래 대금 */
    @JsonProperty("etc_orgt_ntby_tr_pbmn")
    private String etcOrgtNtbyTrPbmn;

    /* ===============================
       매수 / 매도 거래량 & 대금 (주체별)
     =============================== */

    /** frgn_seln_vol : 외국인 매도 거래량 */
    @JsonProperty("frgn_seln_vol")
    private String frgnSelnVol;
    /** frgn_shnu_vol : 외국인 매수 거래량 */
    @JsonProperty("frgn_shnu_vol")
    private String frgnShnuVol;
    /** frgn_seln_tr_pbmn : 외국인 매도 거래 대금 */
    @JsonProperty("frgn_seln_tr_pbmn")
    private String frgnSelnTrPbmn;
    /** frgn_shnu_tr_pbmn : 외국인 매수 거래 대금 */
    @JsonProperty("frgn_shnu_tr_pbmn")
    private String frgnShnuTrPbmn;

    /** prsn_seln_vol : 개인 매도 거래량 */
    @JsonProperty("prsn_seln_vol")
    private String prsnSelnVol;
    /** prsn_shnu_vol : 개인 매수 거래량 */
    @JsonProperty("prsn_shnu_vol")
    private String prsnShnuVol;
    /** prsn_seln_tr_pbmn : 개인 매도 거래 대금 */
    @JsonProperty("prsn_seln_tr_pbmn")
    private String prsnSelnTrPbmn;
    /** prsn_shnu_tr_pbmn : 개인 매수 거래 대금 */
    @JsonProperty("prsn_shnu_tr_pbmn")
    private String prsnShnuTrPbmn;

    /** orgn_seln_vol : 기관계 매도 거래량 */
    @JsonProperty("orgn_seln_vol")
    private String orgnSelnVol;
    /** orgn_shnu_vol : 기관계 매수 거래량 */
    @JsonProperty("orgn_shnu_vol")
    private String orgnShnuVol;
    /** orgn_seln_tr_pbmn : 기관계 매도 거래 대금 */
    @JsonProperty("orgn_seln_tr_pbmn")
    private String orgnSelnTrPbmn;
    /** orgn_shnu_tr_pbmn : 기관계 매수 거래 대금 */
    @JsonProperty("orgn_shnu_tr_pbmn")
    private String orgnShnuTrPbmn;

    /* ===============================
       기타
     =============================== */

    /** bold_yn : BOLD 여부 */
    @JsonProperty("bold_yn")
    private String boldYn;

    /* ===============================
       누락된 필드 추가
     =============================== */

    @JsonProperty("frgn_reg_askp_qty")
    private String frgnRegAskpQty;

    @JsonProperty("frgn_reg_bidp_qty")
    private String frgnRegBidpQty;

    @JsonProperty("frgn_reg_askp_pbmn")
    private String frgnRegAskpPbmn;

    @JsonProperty("frgn_reg_bidp_pbmn")
    private String frgnRegBidpPbmn;

    @JsonProperty("frgn_nreg_askp_qty")
    private String frgnNregAskpQty;

    @JsonProperty("frgn_nreg_bidp_qty")
    private String frgnNregBidpQty;

    @JsonProperty("frgn_nreg_askp_pbmn")
    private String frgnNregAskpPbmn;

    @JsonProperty("frgn_nreg_bidp_pbmn")
    private String frgnNregBidpPbmn;

    @JsonProperty("scrt_seln_vol")
    private String scrtSelnVol;

    @JsonProperty("scrt_shnu_vol")
    private String scrtShnuVol;

    @JsonProperty("scrt_seln_tr_pbmn")
    private String scrtSelnTrPbmn;

    @JsonProperty("scrt_shnu_tr_pbmn")
    private String scrtShnuTrPbmn;

    @JsonProperty("ivtr_seln_vol")
    private String ivtrSelnVol;

    @JsonProperty("ivtr_shnu_vol")
    private String ivtrShnuVol;

    @JsonProperty("ivtr_seln_tr_pbmn")
    private String ivtrSelnTrPbmn;

    @JsonProperty("ivtr_shnu_tr_pbmn")
    private String ivtrShnuTrPbmn;

    @JsonProperty("pe_fund_seln_tr_pbmn")
    private String peFundSelnTrPbmn;

    @JsonProperty("pe_fund_seln_vol")
    private String peFundSelnVol;

    @JsonProperty("pe_fund_shnu_tr_pbmn")
    private String peFundShnuTrPbmn;

    @JsonProperty("pe_fund_shnu_vol")
    private String peFundShnuVol;

    @JsonProperty("bank_seln_vol")
    private String bankSelnVol;

    @JsonProperty("bank_shnu_vol")
    private String bankShnuVol;

    @JsonProperty("bank_seln_tr_pbmn")
    private String bankSelnTrPbmn;

    @JsonProperty("bank_shnu_tr_pbmn")
    private String bankShnuTrPbmn;

    @JsonProperty("insu_seln_vol")
    private String insuSelnVol;

    @JsonProperty("insu_shnu_vol")
    private String insuShnuVol;

    @JsonProperty("insu_seln_tr_pbmn")
    private String insuSelnTrPbmn;

    @JsonProperty("insu_shnu_tr_pbmn")
    private String insuShnuTrPbmn;

    @JsonProperty("mrbn_seln_vol")
    private String mrbnSelnVol;

    @JsonProperty("mrbn_shnu_vol")
    private String mrbnShnuVol;

    @JsonProperty("mrbn_seln_tr_pbmn")
    private String mrbnSelnTrPbmn;

    @JsonProperty("mrbn_shnu_tr_pbmn")
    private String mrbnShnuTrPbmn;

    @JsonProperty("fund_seln_vol")
    private String fundSelnVol;

    @JsonProperty("fund_shnu_vol")
    private String fundShnuVol;

    @JsonProperty("fund_seln_tr_pbmn")
    private String fundSelnTrPbmn;

    @JsonProperty("fund_shnu_tr_pbmn")
    private String fundShnuTrPbmn;

    @JsonProperty("etc_seln_vol")
    private String etcSelnVol;

    @JsonProperty("etc_shnu_vol")
    private String etcShnuVol;

    @JsonProperty("etc_seln_tr_pbmn")
    private String etcSelnTrPbmn;

    @JsonProperty("etc_shnu_tr_pbmn")
    private String etcShnuTrPbmn;

    @JsonProperty("etc_orgt_seln_vol")
    private String etcOrgtSelnVol;

    @JsonProperty("etc_orgt_shnu_vol")
    private String etcOrgtShnuVol;

    @JsonProperty("etc_orgt_seln_tr_pbmn")
    private String etcOrgtSelnTrPbmn;

    @JsonProperty("etc_orgt_shnu_tr_pbmn")
    private String etcOrgtShnuTrPbmn;

    @JsonProperty("etc_corp_seln_vol")
    private String etcCorpSelnVol;

    @JsonProperty("etc_corp_shnu_vol")
    private String etcCorpShnuVol;

    @JsonProperty("etc_corp_seln_tr_pbmn")
    private String etcCorpSelnTrPbmn;

    @JsonProperty("etc_corp_shnu_tr_pbmn")
    private String etcCorpShnuTrPbmn;
}
