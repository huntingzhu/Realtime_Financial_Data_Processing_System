package com.dataapplab.lendingclub.spouts;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.storm.utils.Utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class KafkaReadApiProducer extends Thread {
    private static final String topicName = "fintech-lendingclub";
    private final String USER_AGENT = "Mozilla/5.0";

    private final KafkaProducer<String, String> producer;
    private final Boolean isAsync;

    public KafkaReadApiProducer(String topic, Boolean isAsync) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "m1.mt.dataapplab.com:6667"); // Kafka Server address
        props.put("client.id", "DemoProducer");
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(props);
        this.isAsync = isAsync;
    }

    public void run() {
        KafkaReadApiProducer producer = new KafkaReadApiProducer(topicName, false);
        int lineCount = 0;
        try {

            String line = GetResponse();

            while (line  != null) {
                lineCount++;
                producer.sendMessage(lineCount + "", line);
                line = GetResponse();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    private String GetResponse() throws IOException {
        String url = "http://fintech.dataapplab.com:33334/api/v1.0/FinTech/streamingdata";
        //String url = "http://0.0.0.0:5000/api/v1.0/streaming/FinTechData";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public void sendMessage(String key, String value) {
        long startTime = System.currentTimeMillis();
        if (isAsync) { // Send asynchronously
            producer.send(new ProducerRecord<String, String>(topicName, key),
                    (Callback) new DemoCallBack(startTime, key, value));
        } else { // Send synchronously
            try {
                producer.send(new ProducerRecord<String, String>(topicName, key, value))
                        .get();
                System.out.println("Sent message: (" + key + ", " + value + ")");
                Utils.sleep(5000); // 500 milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }




}

class DemoCallBack implements Callback {

    private long startTime;
    private String key;
    private String message;

    public DemoCallBack(long startTime, String key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }

    /**
     * A callback method the user can implement to provide asynchronous handling
     * of request completion. This method will be called when the record sent to
     * the server has been acknowledged. Exactly one of the arguments will be
     * non-null.
     *
     * @param metadata
     *            The metadata for the record that was sent (i.e. the partition
     *            and offset). Null if an error occurred.
     * @param exception
     *            The exception thrown during processing of this record. Null if
     *            no error occurred.
     */
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            System.out.println("message(" + key + ", " + message
                    + ") sent to partition(" + metadata.partition() + "), "
                    + "offset(" + metadata.offset() + ") in " + elapsedTime
                    + " ms");
        } else {
            exception.printStackTrace();
        }
    }
}
