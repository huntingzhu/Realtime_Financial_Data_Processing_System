package com.dataapplab.lendingclub.property;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostData {

    @JsonProperty("bc_open_to_buy")
    public float BcOpenToBuy;
    @JsonProperty("total_il_high_credit_limit")
    public float HighLimit;
    @JsonProperty("annual_inc")
    public float AnnualInc;
    @JsonProperty("bc_util")
    public float BcUtil;
    @JsonProperty("dti")
    public float Dti;
    @JsonProperty("int_rate")
    public float Rate;
    @JsonProperty("term")
    public int Term;
    @JsonProperty("loan_amnt")
    public float LoanAmnt;
    @JsonProperty("fund_rate")
    public float FundRate;
    @JsonProperty("funded_amnt")
    public float FundedAmnt;
}
