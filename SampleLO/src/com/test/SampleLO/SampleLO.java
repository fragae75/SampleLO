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
	public static void doSubscribeDataTopics(String sTopicName, String sAPIKey, String sServerAddress)
	{
		Thread t;
		RunConsumeQueue consumeQueue = new RunConsumeQueue(sTopicName, sAPIKey, sServerAddress);

		t = new Thread(consumeQueue);
		t.start();
        System.out.println("Thread : consume Queue" + sTopicName);
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
        System.out.println("Thread : consume Commands" + sTopicName);
	}
	
	/*
	 * 
	 * Subscribe to the topic : "~event/v1/data/new/#"
	 * Send a payload to dev/data
	 * 
	 */
	public static void main(String[] args) {
        Random rand = new Random();

        String API_KEY = MyKey.key; // <-- REPLACE by your API key !
        String SERVER = "tcp://liveobjects.orange-business.com:1883";
        String DEVICE_URN = "urn:lo:nsid:sensor:SampleLO001";

        // Subscribe to the router : "~event/v1/data/new/#"
        doSubscribeDataTopics("~event/v1/data/new/#", API_KEY, SERVER);
        
        // Subscribe to the router : "dev/cmd"
        doSubscribeDeviceTopics("dev/cmd", API_KEY, SERVER, DEVICE_URN);
        
        // Wait 1 sec to let if subscribe & get the data below
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
