
# Some Modifications in the Cluter Version of FinTech Project

## 1 pom.xml

### 1.1 properties

"storm-core" and "storm-kafka", these two dependencies need to be changed to 1.0.1 version, and they should be consistent.

From:
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <storm.version>0.9.5</storm.version>
    <logback.version>1.1.2</logback.version>
    <joda-time.version>2.6</joda-time.version>
    <guava.version>18.0</guava.version>
    <maven-compiler-plugin.version>3.2</maven-compiler-plugin.version>
    <exec-maven-plugin>1.3.2</exec-maven-plugin>
    <maven-jdk-version>1.8</maven-jdk-version>
</properties>
```

To:
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <storm.version>1.0.1</storm.version>
    <storm-kafka.version>1.0.1</storm-kafka.version>
    <logback.version>1.1.2</logback.version>
    <joda-time.version>2.6</joda-time.version>
    <guava.version>18.0</guava.version>
    <maven-compiler-plugin.version>3.2</maven-compiler-plugin.version>
    <exec-maven-plugin>1.3.2</exec-maven-plugin>
    <maven-jdk-version>1.8</maven-jdk-version>
</properties>
```

### 1.2 dependencies

#### 1.2.1 storm-core
Add **\<scope\>provided\<\/scope\>** into the "storm-core" dependency. This is because there already exists one storm-core file on the cluster. When we use "storm jar" command line to submit our topology, the storm-core file on the cluster will be used. Only when we run storm project locally on Cloudera, we need this dependency. So here we should use a **\<scope\>provided\<\/scope\>** to exclude it **from the jar-with-dependencies**

```xml
<dependency>
   <groupId>org.apache.storm</groupId>
   <artifactId>storm-core</artifactId>
   <version>${storm.version}</version>
   <!-- keep storm out of the jar-with-dependencies -->
   <scope>provided</scope>
   <exclusions>
       <exclusion>
           <groupId>commons-logging</groupId>
           <artifactId>commons-logging-api</artifactId>
       </exclusion>
       <!-- exclusion because of conflict with SLF4J and Logback -->
       <exclusion>
           <groupId>org.slf4j</groupId>
           <artifactId>slf4j-log4j12</artifactId>
       </exclusion>
       <exclusion>
           <groupId>com.google.guava</groupId>
           <artifactId>guava</artifactId>
       </exclusion>
   </exclusions>
</dependency>
```

#### 1.2.2 kafka_2.10 & kafka-clients

To use KafkaSpout correctly, we have to change the version of these two dependencies, kafka_2.10 & kafka-clients.
```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka_2.10</artifactId>
    <version>0.10.0.0</version>
    <!-- old version : <version>0.8.1.1</version> -->
    <exclusions>
        <exclusion>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
        <exclusion>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
<groupId>org.apache.kafka</groupId>
<artifactId>kafka-clients</artifactId>
<!-- old version: <version>0.8.2.1</version>-->
<version>0.10.0.0</version>
</dependency>


<dependency>
    <groupId>com.googlecode.json-simple</groupId>
    <artifactId>json-simple</artifactId>
    <version>1.1.1</version>
</dependency>
```




## 2 JAVA code

### 2.1 Topology & Bolts & Spouts

Since we changed the version of storm-core & storm-kafka to fit the version of storm on our cluster, we need to change some package category names.

From:

```java
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
```
To:
```java
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
```

For Spouts and Bolts, the same work also needs to be done.

### 2.2 HBaseInit
In order to connect to the HBase on our cluster, you have to do some configuration work:

```java
private static Configuration conf = null;
    /**
     * Initialization
     */
    static {
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("hbase.zookeeper.quorum", "m1.mt.dataapplab.com,m2.mt.dataapplab.com,m3.mt.dataapplab.com");
        conf.set("zookeeper.znode.parent", "/hbase-unsecure");
        conf.set("hbase.master", "m3.mt.dataapplab.com:16000");
    }
```


In the old version, the HBaseInit class used so many deprecated class and method. Therefore, I rewrote all the code of HBaseInit, including connect, insert, findOne and so on.

For detail, you can view the HBaseInit.java.

```java
    /**
     * Create a table
     */
    public static void createTable(String tableName, String[] ColFamilies) throws Exception {

        // New Version
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Admin admin = connection.getAdmin()) {

            if (admin.tableExists(TableName.valueOf(tableName))) {
                System.out.println("HBase Initialization: table already exists!");
            } else {
                // Instantiating table descriptor class
                HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
                // Adding column families to table descriptor
                for (int i = 0; i < ColFamilies.length; i++) {
                    tableDescriptor.addFamily(new HColumnDescriptor(ColFamilies[i]));
                }
                admin.createTable(tableDescriptor);
                System.out.println("HBase Initialization: create table " + tableName + " ok.");

            }

        }


    }

    /**
     * Delete a table
     */
    public static void deleteTable(String tableName) throws Exception {

        // New version
        try (Connection connection = ConnectionFactory.createConnection(conf);
             Admin admin = connection.getAdmin()) {
            admin.disableTable(TableName.valueOf(tableName));
            admin.deleteTable(TableName.valueOf(tableName));
            System.out.println("HBase Initialization: delete table " + tableName + " ok.");

        }


    }

    /**
     * Put (or insert) a row
     */
    public static void addRecord(String tableName, String rowKey,
                                 String family, String qualifier, String value) throws Exception {


        // New Version
        try (Connection conn = ConnectionFactory.createConnection(conf);
             Table hTable = conn.getTable(TableName.valueOf(tableName))) {
            //
            // Instantiating Put class
            // accepts a row name.
            Put p = new Put(Bytes.toBytes(rowKey));

            // adding values using add() method
            // accepts column family name, qualifier/row name ,value
            p.addColumn(Bytes.toBytes(family),
                    Bytes.toBytes(qualifier),Bytes.toBytes(value));

            // Saving the put Instance to the HTable.
            hTable.put(p);
            System.out.println("HBase Initialization: insert recored " + rowKey + " to table "
                    + tableName + " ok.");
        }

    }

    /**
     * Get a row
     */
    public static void getOneRecord (String tableName, String rowKey,
                                     String colFamily, String qualifier) throws IOException{

        // New version
        try (Connection conn = ConnectionFactory.createConnection(conf);
             Table hTable = conn.getTable(TableName.valueOf(tableName))) {

            // Instantiating Get class
            Get g = new Get(Bytes.toBytes(rowKey));

            // Reading the data
            Result result = hTable.get(g);

            // Reading values from Result class object
            byte [] value = result.getValue(Bytes.toBytes(colFamily),Bytes.toBytes(qualifier));



            System.out.println("rowKey: " + rowKey);
            System.out.println("colFamily: " + colFamily);
            System.out.println("qualifier: " + qualifier);
            System.out.println("value: " + value);
        }
    }

    /**
     * Scan (or list) a table
     */
    public static void getAllRecord (String tableName, String colFamily,String qualifier) throws IOException {


        try (Connection conn = ConnectionFactory.createConnection(conf);
             Table hTable = conn.getTable(TableName.valueOf(tableName))) {

            // Instantiating the Scan class
            Scan scan = new Scan();

            // Scanning the required columns
            scan.addColumn(Bytes.toBytes(colFamily),  Bytes.toBytes(qualifier));

            // Getting the scan result
            try (ResultScanner scanner = hTable.getScanner(scan)) {

                // Reading values from scan result
                for (Result result = scanner.next(); result != null; result = scanner.next()) {
                    System.out.println("Found row : " + result);
                }
            }
        }
    }
```
