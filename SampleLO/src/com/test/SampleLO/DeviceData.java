package com.test.SampleLO;

import java.util.List;
import java.util.Map;


public class DeviceData {
	// Stream identifier
	public String s;
	// timestamp (ISO8601 format)
	public String ts;
	// Data "model"
	public String m;
	// Value
	public Map<String, Object> v;
	// Tags
	public List<String> t;
	 // Location [latitude, longitude]
	public Double[] loc;
	}

