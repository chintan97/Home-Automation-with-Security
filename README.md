-----Home Automation with Security-----

Project team:

https://github.com/chintan97

https://github.com/Pratik-Das

IoT project for providing home automation with security mechanism at very low cost.

Abstract:

This project aims to provide facility to automate home appliances automatically as well as manually by the user. This is done with the help of technologically advanced and easy to use system i.e. 'Internet of Things' system. Sensors and relay are controlled by NodeMCU. An android mobile can be used to control home appliances (lights, fans) and changing modes.
	
When a person enters or exits the room, he/she is sensed using 2 PIR sensors. On a person entering, counter variable is increased by 1. When a person leaves the room, counter variable is decreased by 1. The count of persons present in the room is stored in counter variable.

Micro-controller turns lights/fans ON automatically if someone is present in room i.e. counter variable greater than zero. When person counter variable becomes zero, the lights/fans turn OFF automatically to save electricity. The switches of lights/fans are controlled with relays. Further the lights would not turn ON automatically if sunlight is enough in the room. It also provides functionality to turn lights ON/OFF manually using android mobiles with an Android app. 

User can configure the system to 'SecureMode' by the use of an Android app. If an intruder enters in home while ‘SecureMode’ is ON, a notification is sent to user on mobile that 'An intrusion is detected'. User can turn OFF 'Security Mode' anytime when he/she comes at home.

Notes:

1> Circuit diagram is given in Diagrams folder. Arduino UNO is used for 5v power supply for PIR sensors and photoresistors. If you have external power supply, you can use directly.

2> Website address is changed to example.com, you have to provide yours.

3> apk files are deleted due to security reasons.

4> In NodeMCU_HAUSE.ino file, you have to provide your ssid and password on line 15 and 17.
