# SampleLO

Sample application for Datavenue Live Objects : https://liveobjects.orange-business.com/#/liveobjects

It is a simple sample that sends a MQTT payload to Live Objects as a MQTT device ("json+device") and collect a MQTT data from Live Objects as an application ("payload+bridge").
It is a merging of the 2 samples : SampleLOSendData + SampleLOSubscribeData. Check the README.md files of these samples.<br>

<h2> Installation notes </h2>

1) Create an account on Live Objects. You can get a free account (10 MQTT devices for 1 year) at : https://liveobjects.orange-business.com/#/request_account <br>
Don't check "Lora" otherwise the account will not be instantly created.

2) Generate your API key : menu Configuration/API Keys click on "Add"

3) Create a MyKey class : <br>


	package com.test.SampleLOSendData; 
	
	public final class MyKey { 
		static String key = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; 
	}


4) You will find into the repository 4 jar files into the /lib. Add them as "external JARs" into you IDE (eg Eclipse).
