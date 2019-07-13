package com.test.SampleLO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

public class SampleLO {
	
	/*
	 * doSubscribeDataTopics() : create a thread that subscribe to data topics
	 */
	public static void doSubscribeDataElements(String sTopicName, String sAPIKey, String sServerAddress, String sUserName)
	{
		Thread t;
		RunConsumeQueue consumeQueue = new RunConsumeQueue(sTopicName, sAPIKey, sServerAddress, sUserName);

		t = new Thread(consumeQueue);
		t.start();
        System.out.println("Thread : consume Data on Fifo\n");
	}
	
	/*
	 * doSubscribeDeviceTopics() : create a thread that subscribe to device topics
	 */
	public static void doSubscribeDeviceTopics(String sTopicName, String sAPIKey, String sServerAddress, String sDeviceUrn)
	{
		Thread t;
		RunConsumeCommands consumeCommands = new RunConsumeCommands(sTopicName, sAPIKey, sServerAddress, sDeviceUrn);

		t = new Thread(consumeCommands);
		t.start();
        System.out.println("Thread : consume Commands\n" + sTopicName);
	}
	
	/*
	 * The application does : 
	 * - Subscribe to Live Objects as an application
	 * - Subscribe to Live Objects as a device (urn:lo:nsid:sensor:SampleLO001_Command) ready to receive commands from the platform
	 * - Connect to Live Objects as a device (urn:lo:nsid:sensor:SampleLO001) and send one payload
	 * 
	 * Reminder:
	 * in order to collect data as an application
	 * 1) create a Fifo (here testFifo) which will collect and buffer the data, use the portal (or the API) : https://liveobjects.orange-business.com/#/datastore/fifo
	 * 2) create a route that will bind your source with the Fifo, use the portal (or the API) : https://liveobjects.orange-business.com/#/datastore/routing
	 * 3) connect your MQTT client to Live Objects with 'application' user name (see RunConsumeQueue.java)
	 * 
	 * Note : The 'bridge' user name ('json+bridge' and 'payload+bridge') will be deprecated end 2019. Therefore 
	 *  the topic : '~event/v1/data/new/#' will no longer be supported
	 *  
	 *  
	 * 
	 */
	public static void main(String[] args) {
        Random rand = new Random();

        String API_KEY = MyKey.key; // <-- REPLACE by your API key !
        String SERVER = "tcp://liveobjects.orange-business.com:1883";
        String DEVICE_URN = "urn:lo:nsid:sensor:SampleLO001";
        String MY_FIFO = "testFifo";
        String MQTT_USER_NAME = "application";


		/*
		 * the 'bridge' mode will be deprecated end 2019 !!!
         * Subscribe to the router : "router/~event/v1/data/new/#"
         * doSubscribeDataElements("router/~event/v1/data/new/#", API_KEY, SERVER, "payload+bridge");
        */
        
        // To subscribe a fifo : "fifo/" + fifo name
        doSubscribeDataElements("fifo/" + MY_FIFO, API_KEY, SERVER, MQTT_USER_NAME);

        // Wait 1 sec to let it subscribe & get the data below and make the logs well sequenced :)
        try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Subscribe to the topic : "dev/cmd"
        doSubscribeDeviceTopics("dev/cmd", API_KEY, SERVER, DEVICE_URN);

        // Wait 1 sec to let it subscribe & get the data below and make the logs well sequenced :)
        try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        

        // *** data to push ***
        DeviceData data = new DeviceData();
        // streamId
        data.s = "StreamSampleLO001";
        // value: JSON object...
        data.v = new HashMap<String, Object>();
        // Hygrometrie : 0 - 100
    	data.v.put("hygrometry", rand.nextInt(100));
    	// T° from -20 to 120
    	data.v.put("temperature", rand.nextInt(140) - 20);
		// Rev/min : 0 - 9999
    	data.v.put("revmin", rand.nextInt(9999));
        // location (lat/lon)
        data.loc = new Double[] { 45.759723, 4.84223 };
        // model : same as the Android app
        data.m = "demo";
        // tags
        data.t = Arrays.asList("SampleLO");
        // encoding to JSON
        String CONTENT = new Gson().toJson(data);

        try {
            MqttClient sampleClient = new MqttClient(SERVER, DEVICE_URN, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("json+device"); // selecting mode "Device"
            connOpts.setPassword(API_KEY.toCharArray()); // passing API key value as password
            connOpts.setCleanSession(true);

            // Connection
            System.out.println("Publish - Connecting to broker: " + SERVER);
            sampleClient.connect(connOpts);
            System.out.println("Publish - Connected");

            // Publish data
            System.out.println("Publishing message: " + CONTENT);
            MqttMessage message = new MqttMessage(CONTENT.getBytes());
            message.setQos(0);
            sampleClient.publish("dev/data", message);
            System.out.println("Message published");

            // Disconnection
            sampleClient.disconnect();
            System.out.println("Disconnected");

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }


	}

}
