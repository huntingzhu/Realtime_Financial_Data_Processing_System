package com.dataapplab.lendingclub.bolts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import com.dataapplab.lendingclub.property.Member;
import com.dataapplab.lendingclub.property.PostData;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;


public class SubmitBolt extends BaseRichBolt {

    private OutputCollector collector;

    public SubmitBolt() {}

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple input) {

        Member member = (Member) input.getValueByField("DecisionMember");
        String result = null;
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {

            // Do the calculation work before submitting data to the bank server
            float loanAmntFloat = Float.parseFloat(member.LoanAmnt);
            float fundedAmntFloat = Float.parseFloat(member.FundedAmnt);
            float fundRate = fundedAmntFloat / loanAmntFloat * 100;


            member.FundRate = String.valueOf(fundRate);

            // map the Member object to the PostData object
            ObjectMapper mapper = new ObjectMapper();
            PostData postData = mapper.convertValue(member, PostData.class);

            // convert PostData object to json string
            String json = ow.writeValueAsString(postData);

            // post data to bank api server
            result = sendRequest(json);


            if (result != null) {
                collector.emit(new Values(member,result));
            } else {
                System.out.println("SubmitBolt: Bank API has no response! ");
            }

        } catch (JsonGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String sendRequest(String input)
    {

        try {

            String url = "http://fintech.dataapplab.com:33334/api/v1.0/FinTech";
            //String url = "http://0.0.0.0:5000/api/v1.0/bank/loandecision";
            URL urlo = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlo.openConnection();

            // optional default is GET
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            OutputStream os = con.getOutputStream();
            os.write(input.getBytes());


            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }

            rd.close();


            return response.toString();


        }catch (Exception ex) {

            //handle exception here
            ex.printStackTrace();
        }
        return null;
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("SubmitMember","Result"));
    }







}
