package com.dataapplab.lendingclub.bolts;

import java.util.Map;

import com.dataapplab.lendingclub.property.Member;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;


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
