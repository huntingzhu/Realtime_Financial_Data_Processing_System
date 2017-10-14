package com.dataapplab.lendingclub.topology;


import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;


import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.*;


import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dataapplab.lendingclub.bolts.DecisionBolt;
import com.dataapplab.lendingclub.bolts.FilterBolt;
import com.dataapplab.lendingclub.bolts.SaveBolt;
import com.dataapplab.lendingclub.bolts.SubmitBolt;
import com.dataapplab.lendingclub.spouts.KafkaReadApiProducer;


public class Topology implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Topology.class);
    private static final String TOPOLOGY_NAME = "fintech-lending-club";

    public static final void main(final String[] args) {
        try {
            String configFileLocation = "config.properties";
            Properties topologyConfig = new Properties();
            topologyConfig.load(ClassLoader.getSystemResourceAsStream(configFileLocation));

            // get properties
            String kafkaserver = topologyConfig.getProperty("kafkaserver");
            String zkConnString = topologyConfig.getProperty("zookeeper");
            String topicName = topologyConfig.getProperty("topic");
            String hbase_cf = topologyConfig.getProperty("hbase_cf");
            String tablename = topologyConfig.getProperty("hbase_table");

            System.out.println("\n");
            System.out.println("\n");
            System.out.println("\n");

            LOGGER.info(zkConnString);
            LOGGER.info(topicName);
            LOGGER.info(hbase_cf);
            LOGGER.info(tablename);

            System.out.println("\n");
            System.out.println("\n");
            System.out.println("\n");

            // kafka API reader producer
            KafkaReadApiProducer apiReaderProducer = new KafkaReadApiProducer(topicName,false);
            apiReaderProducer.start();

            // build the topology
            final Config config = new Config();
            config.setMessageTimeoutSecs(20);
            TopologyBuilder topologyBuilder = new TopologyBuilder();

            // initialize Kafka Spout
            BrokerHosts hosts = new ZkHosts(zkConnString);
            SpoutConfig spoutConfig = new SpoutConfig(hosts, topicName, "/" + topicName, UUID.randomUUID().toString());
            spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
            KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);

            // set up all spouts and bolts
            topologyBuilder.setSpout("ApiSpout", kafkaSpout, 1);
            topologyBuilder.setBolt("FilterBolt", new FilterBolt(),3).shuffleGrouping("ApiSpout");
            topologyBuilder.setBolt("DecisionBolt", new DecisionBolt(),3).shuffleGrouping("FilterBolt");
            topologyBuilder.setBolt("SubmitBolt", new SubmitBolt(),1).shuffleGrouping("DecisionBolt");
            topologyBuilder.setBolt("SaveBolt", new SaveBolt(hbase_cf,tablename)).shuffleGrouping("SubmitBolt");

            // Submit it to the cluster or run it locally
            if (null != args && 0 < args.length) {
                config.setNumWorkers(3);
                StormSubmitter.submitTopology(args[0], config, topologyBuilder.createTopology());
            } else {
                config.setMaxTaskParallelism(10);
                final LocalCluster localCluster = new LocalCluster();
                localCluster.submitTopology(TOPOLOGY_NAME, config, topologyBuilder.createTopology());

                Utils.sleep(3600 * 1000); // 1h

                LOGGER.info("Shutting down the cluster");
                localCluster.killTopology(TOPOLOGY_NAME);
                localCluster.shutdown();
            }

        } catch (InvalidTopologyException exception) {
            exception.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AlreadyAliveException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }


    }



}
