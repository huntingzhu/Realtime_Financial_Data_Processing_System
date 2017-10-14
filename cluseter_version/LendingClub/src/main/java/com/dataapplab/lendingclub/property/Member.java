package com.dataapplab.lendingclub.property;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Member implements Serializable {
    @JsonProperty("member_id")
    public String MemberId;
    @JsonProperty("bc_open_to_buy")
    public String BcOpenToBuy;
    @JsonProperty("total_il_high_credit_limit")
    public String HighLimit;
    @JsonProperty("dti")
    public String Dti;
    @JsonProperty("annual_inc")
    public String AnnualInc;
    @JsonProperty("bc_util")
    public String BcUtil;
    @JsonProperty("int_rate")
    public String Rate;
    @JsonProperty("term")
    public String Term;
    @JsonProperty("loan_amnt")
    public String LoanAmnt;
    @JsonProperty("fund_rate")
    public String FundRate;
    @JsonProperty("funded_amnt")
    public String FundedAmnt;
    @JsonProperty("grade")
    public String Grade;

}
