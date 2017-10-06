<?php
	// Fetch parameters passed in URL, called by both NodeMCU and Android app whenever required
	$Light_state = $_GET['light_state'];
    $Fan_state = $_GET['fan_state'];
	$Person_count = $_GET['person_count'];
	$Mode = $_GET['mode'];         // Manual/ Automatic
	$Security = $_GET['security'];
	$data = array();
	
	// Prepare array to write in data.json file
	$state[] = array('light_state' => $Light_state, 'fan_state' => $Fan_state, 'person_count' => (int) $Person_count, 'mode' => $Mode, 'security' => $Security);
	
	$data['data'] = $state;
	
	$file_pointer = fopen('data.json', 'w');
	fwrite($file_pointer, json_encode($data));    // Write in data.json file
	fclose($file_pointer);
?>