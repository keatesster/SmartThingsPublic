metadata {
        definition (name: "TZ88 Smart Energy Plug in Switch and Meter (Z-Wave)", 
        			namespace: "keatesster",
                    author: "J Keates extending SmartThings base") {
                capability "Actuator"
                capability "Switch"
                capability "Polling"
                capability "Refresh"
                capability "Power Meter"		//JMK
                capability "Energy Meter"		//JMK
                capability "Sensor"
                
                //attribute "current", "number"	//JMK
                //attribute "voltage", "number"	//JMK
                
                fingerprint deviceId:"0x1001", inClusters:"0x5E, 0x86, 0x72, 0x98, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x20, 0x27, 0x32, 0x70, 0x71, 0x75, 0x7A", manufacturer: "TKBHOME", model: "TZ88E"
                // fingerprint    0 0 0x1001 0 0 0 10 0x5E 0x86 0x72 0x98 0x5A 0x85 0x59 0x73 0x25 0x20 0x27 0x32 0x70 0x71 0x75 0x7A
        }

        simulator {
                // These show up in the IDE simulator "messages" drop-down to test
                // sending event messages to your device handler
                status "basic report on":
                       zwave.basicV1.basicReport(value:0xFF).incomingMessage()
                status "basic report off":
                        zwave.basicV1.basicReport(value:0).incomingMessage()
                //status "dimmer switch on at 70%":
                //       zwave.switchMultilevelV1.switchMultilevelReport(value:70).incomingMessage()
                status "basic set on":
                           zwave.basicV1.basicSet(value:0xFF).incomingMessage()
                //status "temperature report 70°F":
                //                zwave.sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: 70.0, precision: 1, sensorType: 1, scale: 1).incomingMessage()
                //status "low battery alert":
                //       zwave.batteryV1.batteryReport(batteryLevel:0xFF).incomingMessage()
                status "multichannel sensor":
                       zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1).encapsulate(zwave.sensorBinaryV1.sensorBinaryReport(sensorValue:0)).incomingMessage()
				
                status "on":  "command: 2003, payload: FF"  // TBC
				status "off": "command: 2003, payload: 00"	// TBC

                // simulate turn on
                reply "2001FF,delay 5000,2002": "command: 2503, payload: FF"

                // simulate turn off
                reply "200100,delay 5000,2002": "command: 2503, payload: 00"
        }

tiles(scale: 2) {
	multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
	    tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
		    attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
        tileAttribute ("device.power", key: "SECONDARY_CONTROL") {		// perhaps only power
				attributeState "power", label:'${currentValue} W'
		}        
    }
    
        standardTile("switch", "device.switch", width: 2, height: 2,
                      canChangeIcon: true) {
                 state "on", label: '${name}', action: "switch.off",
                             icon: "st.switches.switch.on", backgroundColor: "#79b821"
                 state "off", label: '${name}', action: "switch.on",
                             icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }

        valueTile("energy", "device.energy", decoration: "flat") {
                 state "energy", label:'${currentValue} kWh'                 // , unit:""
        }
                
        valueTile("power", "device.power", decoration: "flat") {
  			  state "power", label:'${currentValue} W'
        }
  
  		valueTile("voltage", "device.voltage", decoration: "flat") {		// was voltage
        	   state "voltage", label:'${currentValue} V'
        }
        
        valueTile("current", "device.current", decoration: "flat") {		// current, myMap['key2']
        		state "current", label:'${currentValue} A'
        }

		standardTile("refresh", "command.refresh",
                       decoration: "flat") {
                state "default", label:'R', action:"refresh.refresh"
                       icon:"st.secondary.refresh"
        }
                
                main (["status"])
                details (["status", "switch", "energy", "power", "voltage", "current", "refresh"])
}

/*        tiles {
                standardTile("switch", "device.switch", width: 2, height: 2,
                            canChangeIcon: true) {
                        state "on", label: '${name}', action: "switch.off",
                              icon: "st.switches.switch.on", backgroundColor: "#79b821"
                        state "off", label: '${name}', action: "switch.on",
                              icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                }
                standardTile("refresh", "command.refresh", inactiveLabel: false,
                             decoration: "flat") {
                        state "default", label:'', action:"refresh.refresh",
                              icon:"st.secondary.refresh"
                }

                //valueTile("battery", "device.battery", inactiveLabel: false,
                //          decoration: "flat") {
                //        state "battery", label:'${currentValue}% battery', unit:""
                //}

                valueTile("temperature", "device.temperature") {
                        state("temperature", label:'${currentValue}°',
                                backgroundColors:[
                                        [value: 31, color: "#153591"],
                                        [value: 44, color: "#1e9cbb"],
                                        [value: 59, color: "#90d2a7"],
                                        [value: 74, color: "#44b621"],
                                        [value: 84, color: "#f1d801"],
                                        [value: 95, color: "#d04e00"],
                                        [value: 96, color: "#bc2323"]
                                ]
                        )
                }

                //main (["switch", "temperature"])
                //details (["switch", "temperature", "refresh", "battery"])
                main (["switch"])
                details (["switch", "refresh"])
        }
*/
}

/*// JMK
def installed() {			
  subscribe(meter, "power", meterHandler)
}

// JMK 
def meterHandler(evt) {
  log.debug "meterHandler"
  def meterValue = evt.value as double
  def thresholdValue = threshold as int
  //if (meterValue > thresholdValue) {
  //  log.debug "${meter} reported energy consumption above ${threshold}. Turning of switches."
  //  switches.off()
  //}
}*/

def parse(String description) {
        def result = null
        def cmd = zwave.parse(description, [0x60: 3])
        if (cmd) {
                result = zwaveEvent(cmd)
                log.debug "Parsed .... ${cmd} .... to .... ${result.inspect()} ...."
                
        } else {
                log.debug "Non-parsed event: ${description}"
        }
        result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
		log.debug "basic v1 BasicReport"
        def result = []
        result << createEvent(name:"switch", value: cmd.value ? "on" : "off")

        // For a multilevel switch, cmd.value can be from 1-99 to represent
        // dimming levels
        result << createEvent(name:"level", value: cmd.value, unit:"%",
                    descriptionText:"${device.displayName} dimmed ${cmd.value==255 ? 100 : cmd.value}%")

        result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
		log.debug "switchbinary v1 SwitchBinaryReport"
        createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
		log.debug "switchmultilevelv3 SwitchMultilevelReport"
        def result = []
        result << createEvent(name:"switch", value: cmd.value ? "on" : "off")
        result << createEvent(name:"level", value: cmd.value, unit:"%",
                     descriptionText:"${device.displayName} dimmed ${cmd.value==255 ? 100 : cmd.value}%")
        result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
		log.debug "meterv1 MeterReport"
        def result
        if (cmd.scale == 0) {
                result = createEvent(name: "energy", value: cmd.scaledMeterValue,
                                     unit: "kWh")
        } else if (cmd.scale == 1) {
                result = createEvent(name: "energy", value: cmd.scaledMeterValue,
                                     unit: "kVAh")
        } else {
                result = createEvent(name: "power",
                                     value: Math.round(cmd.scaledMeterValue), unit: "W")
        }

        result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
		log.debug "meterv3 MeterReport"
        def map = null
        if (cmd.meterType == 1) {
                if (cmd.scale == 0) {
                        map = [name: "energy", value: cmd.scaledMeterValue,
                               unit: "kWh"]
                } else if (cmd.scale == 1) {
                        map = [name: "energy", value: cmd.scaledMeterValue,
                               unit: "kVAh"]
                } else if (cmd.scale == 2) {
                        map = [name: "power", value: cmd.scaledMeterValue, unit: "W"]
                } //
                else if (cmd.scale == 4) {
                		log.debug "VOLTAGE VOLTAGE"
                        map = [name: "voltage", value: cmd.scaledMeterValue, unit: "V"]                
                }
                else if (cmd.scale == 5) {
                        map = [name: "current", value: cmd.scaledMeterValue, unit: "A"]                   
                }
			/*               
                else {
                        map = [name: "electric", value: cmd.scaledMeterValue]
                        map.unit = ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3]
                }*/
        } else if (cmd.meterType == 2) {
                map = [name: "gas", value: cmd.scaledMeterValue]
                map.unit =      ["m^3", "ft^3", "", "pulses", ""][cmd.scale]
        } else if (cmd.meterType == 3) {
                map = [name: "water", value: cmd.scaledMeterValue]
                map.unit = ["m^3", "ft^3", "gal"][cmd.scale]
        }
        if (map) {
                if (cmd.previousMeterValue && cmd.previousMeterValue != cmd.meterValue) {
                        map.descriptionText = "${device.displayName} ${map.name} is ${map.value} ${map.unit}, previous: ${cmd.scaledPreviousMeterValue}"
                }
                createEvent(map)
        } else {
                null
        }
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
		log.debug "sensorbinaryv2 SensorBinaryReport"
        def result
        switch (cmd.sensorType) {
                case 2:
                        result = createEvent(name:"smoke",
                                value: cmd.sensorValue ? "detected" : "closed")
                        break
                case 3:
                        result = createEvent(name:"carbonMonoxide",
                                value: cmd.sensorValue ? "detected" : "clear")
                        break
                case 4:
                        result = createEvent(name:"carbonDioxide",
                                value: cmd.sensorValue ? "detected" : "clear")
                        break
                case 5:
                        result = createEvent(name:"temperature",
                                value: cmd.sensorValue ? "overheated" : "normal")
                        break
                case 6:
                        result = createEvent(name:"water",
                                value: cmd.sensorValue ? "wet" : "dry")
                        break
                case 7:
                        result = createEvent(name:"temperature",
                                value: cmd.sensorValue ? "freezing" : "normal")
                        break
                case 8:
                        result = createEvent(name:"tamper",
                                value: cmd.sensorValue ? "detected" : "okay")
                        break
                case 9:
                        result = createEvent(name:"aux",
                                value: cmd.sensorValue ? "active" : "inactive")
                        break
                case 0x0A:
                        result = createEvent(name:"contact",
                                value: cmd.sensorValue ? "open" : "closed")
                        break
                case 0x0B:
                        result = createEvent(name:"tilt", value: cmd.sensorValue ? "detected" : "okay")
                        break
                case 0x0C:
                        result = createEvent(name:"motion",
                                value: cmd.sensorValue ? "active" : "inactive")
                        break
                case 0x0D:
                        result = createEvent(name:"glassBreak",
                                value: cmd.sensorValue ? "detected" : "okay")
                        break
                default:
                        result = createEvent(name:"sensor",
                                value: cmd.sensorValue ? "active" : "inactive")
                        break
        }
        result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
		log.debug "sensorbinaryv1 SensorBinaryReport"
        // Version 1 of SensorBinary doesn't have a sensor type
        createEvent(name:"sensor", value: cmd.sensorValue ? "active" : "inactive")
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
		log.debug "sensormultilevelv5 SensorMultilevelReport"
        def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
        switch (cmd.sensorType) {
                case 1:
                        map.name = "temperature"
                        map.unit = cmd.scale == 1 ? "F" : "C"
                        break;
                case 2:
                        map.name = "value"
                        map.unit = cmd.scale == 1 ? "%" : ""
                        break;
                case 3:
                        map.name = "illuminance"
                        map.value = cmd.scaledSensorValue.toInteger().toString()
                        map.unit = "lux"
                        break;
                case 4:
                        map.name = "power"
                        map.unit = cmd.scale == 1 ? "Btu/h" : "W"
                        break;
                case 5:
                        map.name = "humidity"
                        map.value = cmd.scaledSensorValue.toInteger().toString()
                        map.unit = cmd.scale == 0 ? "%" : ""
                        break;
                case 6:
                        map.name = "velocity"
                        map.unit = cmd.scale == 1 ? "mph" : "m/s"
                        break;
                case 8:
                case 9:
                        map.name = "pressure"
                        map.unit = cmd.scale == 1 ? "inHg" : "kPa"
                        break;
                case 0xE:
                        map.name = "weight"
                        map.unit = cmd.scale == 1 ? "lbs" : "kg"
                        break;
                case 0xF:
                        map.name = "voltage"
                        map.unit = cmd.scale == 1 ? "mV" : "V"
                        break;
                case 0x10:
                        map.name = "current"
                        map.unit = cmd.scale == 1 ? "mA" : "A"
                        break;
                case 0x12:
                        map.name = "air flow"
                        map.unit = cmd.scale == 1 ? "cfm" : "m^3/h"
                        break;
                case 0x1E:
                        map.name = "loudness"
                        map.unit = cmd.scale == 1 ? "dBA" : "dB"
                        break;
        }
        createEvent(map)
}

// Many sensors send BasicSet commands to associated devices.
// This is so you can associate them with a switch-type device
// and they can directly turn it on/off when the sensor is triggered.
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
		log.debug "basicv1 BasicSet"
        createEvent(name:"sensor", value: cmd.value ? "active" : "inactive")
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        createEvent(map)
}

// Battery powered devices can be configured to periodically wake up and
// check in. They send this command and stay awake long enough to receive
// commands, or until they get a WakeUpNoMoreInformation command that
// instructs them that there are no more commands to receive and they can
// stop listening.
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
        def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

        // Only ask for battery if we haven't had a BatteryReport in a while
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
                result << response(zwave.batteryV1.batteryGet())
                result << response("delay 1200")  // leave time for device to respond to batteryGet
        }
        result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
        result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {

        def result = []
        if (cmd.nodeId.any { it == zwaveHubNodeId }) {
                result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
        } else if (cmd.groupingIdentifier == 1) {
                // We're not associated properly to group 1, set association
                result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
                result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
        }
        result
}

// Devices that support the Security command class can send messages in an
// encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])

        // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

// MultiChannelCmdEncap and MultiInstanceCmdEncap are ways that devices
// can indicate that a message is coming from one of multiple subdevices
// or "endpoints" that would otherwise be indistinguishable
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1])

        // can specify command class versions here like in zwave.parse
        log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")

        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiInstanceCmdEncap cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1])

        // can specify command class versions here like in zwave.parse
        log.debug ("Command from instance ${cmd.instance}: ${encapsulatedCommand}")

        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
        createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def on() {
          delayBetween([
                zwave.basicV1.basicSet(value: 0xFF).format(),	
                zwave.basicV1.basicGet().format()
          ])
//			, 5000)  // 5 second delay for dimmers that change gradually, can be left out for immediate switches
}

def off() {
          delayBetween([
                zwave.basicV1.basicSet(value: 0x00).format(),	
                zwave.basicV1.basicGet().format()
          ])
//		    , 5000)  // 5 second delay for dimmers that change gradually, can be left out for immediate switches
}

def refresh() {
        // Some examples of Get commands
        delayBetween([
                zwave.switchBinaryV1.switchBinaryGet().format(),
                zwave.switchMultilevelV1.switchMultilevelGet().format(),
                //zwave.meterV2.meterGet(scale: 0).format(),      // get kWh
                //zwave.meterV2.meterGet(scale: 2).format(),      // get Watts
                zwave.meterV3.meterGet(scale: 0).format(),		// JMK
                zwave.meterV3.meterGet(scale: 1).format(),		// JMK
                zwave.meterV3.meterGet(scale: 2).format(),		// JMK
                zwave.meterV3.meterGet(scale: 3).format(),		// JMK
                zwave.meterV3.meterGet(scale: 4).format(),		// JMK
                zwave.meterV3.meterGet(scale: 5).format(),		// JMK
                zwave.meterV3.meterGet(scale: 6).format(),		// JMK
                zwave.sensorMultilevelV1.sensorMultilevelGet().format(),
                zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format(),  // get temp in Fahrenheit
                zwave.batteryV1.batteryGet().format(),
                zwave.basicV1.basicGet().format(),
        ], 1200)
}

// If you add the Polling capability to your device type, this command
// will be called approximately every 5 minutes to check the device's state
def poll() {
        zwave.basicV1.basicGet().format()
        zwave.meterV3.meterGet(scale: 0).format()		// JMK
        zwave.meterV3.meterGet(scale: 2).format()		// JMK
}

// If you add the Configuration capability to your device type, this
// command will be called right after the device joins to set
// device-specific configuration commands.
def configure() {
        delayBetween([
                // Note that configurationSet.size is 1, 2, or 4 and generally
                // must match the size the device uses in its configurationReport
                zwave.configurationV1.configurationSet(parameterNumber:1, size:2, scaledConfigurationValue:100).format(),

                // Can use the zwaveHubNodeId variable to add the hub to the
                // device's associations:
                zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format(),

                // Make sure sleepy battery-powered sensors send their
                // WakeUpNotifications to the hub every 4 hours:
                zwave.wakeUpV1.wakeUpIntervalSet(seconds:4 * 3600, nodeid:zwaveHubNodeId).format(),
        ])
}