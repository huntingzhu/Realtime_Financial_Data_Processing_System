package com.dataapplab.lendingclub.bolts;


import java.io.IOException;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.dataapplab.lendingclub.property.Member;

public class FilterBolt extends BaseRichBolt {

    private OutputCollector collector;

    public FilterBolt() {}

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple input) {
        //data cleaning
        String applicantS = input.getString(0).toString();

        ObjectMapper mapper = new ObjectMapper();

        Member member;

        boolean isValid = true; // must be a local variable;

        try {
            // map json file to a java object
            member = mapper.readValue(applicantS, Member.class);

            // trim data
            member.Rate=member.Rate.replace("%", "").trim();
            member.Term=member.Term.replace(" months","").trim();

            if (member.BcOpenToBuy == "") {
                member.BcOpenToBuy = "0";
            }

            if (member.BcUtil == "") {
                member.BcUtil = "0";
            }

            if (member.HighLimit == "" || member.HighLimit == "None") {
                member.HighLimit = "0";
            }

            // filter invalid data
            if (member.Dti.matches(".*[a-zA-Z]+.*")
                    || member.AnnualInc.matches(".*[a-zA-Z]+.*")) {
                isValid = false;

            }

            // if the data is valid, then emit
            if (isValid == true) {

                collector.emit(new Values(member));
            } else {
                System.out.println("FilterBolt: Invalid member! ");
            }

        } catch (JsonParseException e) {
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

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("FilterMember"));
    }




}
