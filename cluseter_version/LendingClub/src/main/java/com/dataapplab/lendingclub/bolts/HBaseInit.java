package com.dataapplab.lendingclub.bolts;

import java.io.IOException;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;


public class HBaseInit {

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

    /**
     * Create a table
     */
    public static void createTable(String tableName, String[] ColFamilies) throws Exception {


        // Old Version
//        HBaseAdmin admin = new HBaseAdmin(conf);
//        if (admin.tableExists(tableName)) {
//            System.out.println("HBase Initialization: table already exists!");
//        } else {
//
//            HTableDescriptor tableDesc = new HTableDescriptor(tableName);
//            for (int i = 0; i < ColFamilies.length; i++) {
//                tableDesc.addFamily(new HColumnDescriptor(ColFamilies[i]));
//            }
//            admin.createTable(tableDesc);
//            System.out.println("HBase Initialization: create table " + tableName + " ok.");
//        }

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
        // Old Version
//        try {
//            HBaseAdmin admin = new HBaseAdmin(conf);
//            admin.disableTable(tableName);
//            admin.deleteTable(tableName);
//            System.out.println("HBase Initialization: delete table " + tableName + " ok.");
//        } catch (MasterNotRunningException e) {
//            e.printStackTrace();
//        } catch (ZooKeeperConnectionException e) {
//            e.printStackTrace();
//        }


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

//        try {
//            HTable table = new HTable(conf, tableName);
//            Put put = new Put(Bytes.toBytes(rowKey));
//            put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes
//                    .toBytes(value));
//            table.put(put);
//            System.out.println("HBase Initialization: insert recored " + rowKey + " to table "
//                    + tableName + " ok.");
//            // closing
//            table.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


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
     * Delete a row
     */
//    public static void delRecord(String tableName, String rowKey)
//            throws IOException {
//        HTable table = new HTable(conf, tableName);
//        List<Delete> list = new ArrayList<Delete>();
//        Delete del = new Delete(rowKey.getBytes());
//        list.add(del);
//        table.delete(list);
//        System.out.println("HBase Initialization: del recored " + rowKey + " ok.");
//    }

    /**
     * Get a row
     */
    public static void getOneRecord (String tableName, String rowKey,
                                     String colFamily, String qualifier) throws IOException{
        // Old Version
//        HTable table = new HTable(conf, tableName);
//        Get get = new Get(rowKey.getBytes());
//        Result rs = table.get(get);
//        for(KeyValue kv : rs.raw()){
//            System.out.print(new String(kv.getRow()) + " " );
//            System.out.print(new String(kv.getFamily()) + ":" );
//            System.out.print(new String(kv.getQualifier()) + " " );
//            System.out.print(kv.getTimestamp() + " " );
//            System.out.println(new String(kv.getValue()));
//        }

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
    public static void getAllRecord (String tableName, String colFamily,
                                     String qualifier) throws IOException {
        // Old Version
//        try{
//            HTable table = new HTable(conf, tableName);
//            Scan s = new Scan();
//            ResultScanner ss = table.getScanner(s);
//            for(Result r:ss){
//                for(KeyValue kv : r.raw()){
//                    System.out.print(new String(kv.getRow()) + " ");
//                    System.out.print(new String(kv.getFamily()) + ":");
//                    System.out.print(new String(kv.getQualifier()) + " ");
//                    System.out.print(kv.getTimestamp() + " ");
//                    System.out.println(new String(kv.getValue()));
//                }
//            }
//        }

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
}
