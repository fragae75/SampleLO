package com.test.SampleLO;

import java.util.HashMap;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

/*
 *  
 * Thread that will subscribe to the sTopicName as a device and display the messages to the console
 * 
 */
public class RunConsumeCommands implements Runnable {

		private String sTopicName;
		private String sAPIKey;
		private String sServerAddress;
	    private MqttClient mqttClient = null;
	    private String sDeviceUuid;
		
		/*
		 * Constructor : just keep the topic
		 */
	    public RunConsumeCommands(String sTopicName, String sAPIKey, String sServerAddress, String sDeviceUuid){
			this.sTopicName = sTopicName;
			this.sAPIKey = sAPIKey;
			this.sServerAddress = sServerAddress;
			this.sDeviceUuid = sDeviceUuid;
		}
		
		/*
		 * Make sure we have disconnected
		 */
		public void finalize(){
			
	        System.out.println(sTopicName + " - Finalize");
	        // close client
	        if (mqttClient != null && mqttClient.isConnected()) {
	            try {
	                mqttClient.disconnect();
		            System.out.println(sTopicName + " - Queue Disconnected");
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
	        private Gson gson = new Gson();
	        private Integer counter = 0;

	        public SimpleMqttCallback(MqttClient mqttClient) {
	            this.mqttClient = mqttClient;
	        }

	        public void connectionLost(Throwable throwable) {
	            System.out.println("Connection lost");
	            mqttClient.notifyAll();
	        }

	        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
	            // parse message as command
	            DeviceCommand command = gson.fromJson(new String(mqttMessage.getPayload()), DeviceCommand.class);
	            System.out.println("received command: " + command);

	            // return response
	            final DeviceCommandResponse response = new DeviceCommandResponse();
	            response.cid = command.cid;
	            response.res = new HashMap<String, Object>();
	            response.res.put("msg", "hello friend!");
	            response.res.put("method", command.req);
	            response.res.put("counter", this.counter++);

	            new Thread(new Runnable() {
	                public void run() {
	                    try {
	                        mqttClient.publish("dev/cmd/res", gson.toJson(response).getBytes(), 0, false);
	        	            System.out.println("answer to command: " + gson.toJson(response));
	                    } catch (MqttException me) {
	                        System.out.println("reason " + me.getReasonCode());
	                        System.out.println("msg " + me.getMessage());
	                        System.out.println("loc " + me.getLocalizedMessage());
	                        System.out.println("cause " + me.getCause());
	                        System.out.println("excep " + me);
	                        me.printStackTrace();
	                    }
	                }
	            }).start();
	        }

	        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
	            // nothing
	        }
	    }

		
		@Override
		public void run() {
	        String APP_ID = sDeviceUuid;
//	        String APP_ID = "app:" + UUID.randomUUID().toString();

	        MqttClient mqttClient = null;
	        try {
	            mqttClient = new MqttClient(sServerAddress, APP_ID, new MemoryPersistence());

	            // register callback (to handle received commands
	            mqttClient.setCallback(new SimpleMqttCallback(mqttClient));

	            MqttConnectOptions connOpts = new MqttConnectOptions();
	            connOpts.setUserName("json+device"); // selecting mode "Bridge"
	            connOpts.setPassword(sAPIKey.toCharArray()); // passing API key value as password
	            connOpts.setCleanSession(true);

	            // Connection
	            System.out.printf("Subscribe as a device - Connecting to broker: %s ...\n", sServerAddress);
	            mqttClient.connect(connOpts);
	            System.out.println("Subscribe as a device ... connected.");

	            // Subscribe to data
	            System.out.printf("Consuming from device with filter '%s'...\n", sTopicName);
	            mqttClient.subscribe(sTopicName);
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
