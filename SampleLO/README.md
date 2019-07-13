# SampleLO

Sample application for Datavenue Live Objects : https://liveobjects.orange-business.com/#/liveobjects


It is a simple sample which combines the 3 samples: SampleLOSendData + SampleLOSubscribeData + SampleLOConsumeCommand (check the ReadMe of these samples) : 
- It connects and subscribes to Live Objects as an application to the Fifo 'testFifo'
- It connects and subscribes to Live Objects as a device (urn:lo:nsid:sensor:SampleLO001_Command) ready to receive commands from the platform
- It connects to Live Objects as a device (urn:lo:nsid:sensor:SampleLO001) and send one payload then disconnect
<br>

Then the application will display the data and the commands you can send from the portal or the API<br>


<h1> Installation notes </h1>

1) Create an account on Live Objects. You can get a free account (10 MQTT devices for 1 year) at : <a>https://liveobjects.orange-business.com/#/request_account</a> <br>
Don't check "Lora" otherwise the account will not be instantly created.

2) create a Fifo which will collect and buffer the data, use the portal (<a>https://liveobjects.orange-business.com/#/datastore/fifo</a>) or the API (<a>https://liveobjects.orange-business.com/swagger-ui/index.html#!/Bus_management</a>)

3) create a route that will bind your source with the Fifo, use the portal (<a>https://liveobjects.orange-business.com/#/datastore/routing</a>) or the API (<a>https://liveobjects.orange-business.com/swagger-ui/index.html#!/Triggers_and_Actions/createUsingPOST_1</a>)

4) Generate your Application API key : menu Configuration/API Keys (<a>https://liveobjects.orange-business.com/#/config/apikeys</a>) click on "Add", select 'Application' profile rights

5) Create a MyKey class with the generated API key: 

	package com.test.SampleLOSubscribeData;

	public final class MyKey { 
		static String key = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; 
	}

6) You will find into the repository 4 jar files into the /lib. Add them as "external JARs" into you IDE (eg Eclipse).

<br>
<h1>Usefull links</h1>

If you need to generate data, use the SampleLOSendData sample or the Android app at : <a>https://play.google.com/store/apps/details?id=com.orange.lo.assetdemo</a>

For more information about the API uses : 
<a>https://github.com/DatavenueLiveObjects/Postman-collections-for-Live-Objects/blob/master/Live%20Objects%20Training%20publish.postman_collection.json</a>


<br>
<h1> Important note </h1>

The 'bridge' user name ('json+bridge' and 'payload+bridge') will be **deprecated end 2019**. Therefore the topic : '~event/v1/data/new/#' will no longer be supported.

