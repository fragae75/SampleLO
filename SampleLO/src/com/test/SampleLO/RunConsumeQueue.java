package com.test.SampleLO;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/*
 *  
 * Thread that will subscribe to the sTopicName and display the messages to the console
 * 
 */
public class RunConsumeQueue implements Runnable {

	private String sFifoName;
	private String sAPIKey;
	private String sServerAddress;
	private String sMQTTUserName;
    private MqttClient mqttClient = null;
	
	/*
	 * Constructor 
	 */
    public RunConsumeQueue(	String sFifoName, String sAPIKey, String sServerAddress, String sMQTTUserName){
		this.sFifoName = sFifoName;
		this.sAPIKey = sAPIKey;
		this.sServerAddress = sServerAddress;
		this.sMQTTUserName = sMQTTUserName;
	}
	
	/*
	 * Make sure we have disconnected
	 */
	public void finalize(){
		
        System.out.println(sFifoName + " - Finalize");
        // close client
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
	            System.out.println(sFifoName + " - Fifo Disconnected");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
	}

	
	
    /**
     * Basic "MqttCallback" that handles messages as JSON device commands,
     * and immediately respond.
     */
    public static class SimpleMqttCallback implements MqttCallback {
        private MqttClient mqttClient;

        public SimpleMqttCallback(MqttClient mqttClient) {
            this.mqttClient = mqttClient;
        }

        public void connectionLost(Throwable throwable) {
            System.out.println("Connection lost");
            mqttClient.notifyAll();
        }

        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            System.out.println("Received message - " + mqttMessage);
        }

        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // nothing
        }
    }

	
	@Override
	public void run() {
        String APP_ID = "app:" + UUID.randomUUID().toString();

        MqttClient mqttClient = null;
        try {
            mqttClient = new MqttClient(sServerAddress, APP_ID, new MemoryPersistence());

            // register callback (to handle received commands
            mqttClient.setCallback(new SimpleMqttCallback(mqttClient));

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(sMQTTUserName); 
            connOpts.setPassword(sAPIKey.toCharArray()); // passing API key value as password
            connOpts.setCleanSession(true);

            // Connection
            System.out.printf("Connecting to the broker: %s as '%s'...\n", sServerAddress, sMQTTUserName);
            mqttClient.connect(connOpts);
            System.out.println("... connected.");

            // Subscribe to data
            System.out.printf("Subscribing to the fifo: %s\n", sFifoName);
            mqttClient.subscribe(sFifoName);
            System.out.println("... subscribed.");

            synchronized (mqttClient) {
                mqttClient.wait();
            }
        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();

        } finally {
            // close client
            if (mqttClient != null && mqttClient.isConnected()) {
                try {
                    mqttClient.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
	}

}
