package com.dataapplab.lendingclub.bolts;

import java.util.Map;

import com.dataapplab.lendingclub.property.Member;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;


public class DecisionBolt extends BaseRichBolt {

    private OutputCollector collector;

    public DecisionBolt() {}

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }


    public void execute(Tuple input) {

        Member member = (Member) input.getValueByField("FilterMember");

        int AnnualIncInt = Integer.parseInt(member.AnnualInc);

        if (member.Grade.matches("[EFG]")
                || AnnualIncInt < 20000 ) {
            System.out.println("DecisionBolt: Bad Applicant! ");
        } else {
            collector.emit(new Values(member));
        }

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("DecisionMember"));
    }


}
