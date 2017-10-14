package com.dataapplab.lendingclub.bolts;

import java.util.Map;

import org.apache.log4j.Logger;

import com.dataapplab.lendingclub.property.Member;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;


public class SaveBolt extends BaseRichBolt {

    private static final Logger LOG = Logger.getLogger(SaveBolt.class);

    private String[] HBASE_CF; // Column families
    private String tableName;

    private OutputCollector collector;


    public SaveBolt(String hbase_cf, String tablename) {
        try {
            tableName = tablename;
            HBASE_CF = hbase_cf.split(",");
            HBaseInit.creatTable(tableName, HBASE_CF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

    }

    @Override
    public void execute(Tuple tuple) {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            Member member = (Member) tuple.getValueByField("SubmitMember");
            String result = (String) tuple.getValueByField("Result");

            String memberJSON = ow.writeValueAsString(member);

            HBaseInit.addRecord(this.tableName, member.MemberId + "-" + member.AnnualInc, "member", "memberJSON", memberJSON);
            HBaseInit.addRecord(this.tableName, member.MemberId + "-" + member.AnnualInc, "result", "resultJSON", result);

        } catch (Exception e) {
            LOG.error("Error inserting data into HBase table", e);
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // Nothing to do, since this is the last bolt

    }


}
