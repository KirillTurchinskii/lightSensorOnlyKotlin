package com.example.lightsensoronlykotlin

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.lightsensoronlykotlin.manager.MQTTConnectionParams
import com.example.lightsensoronlykotlin.manager.MQTTmanager
import com.example.lightsensoronlykotlin.protocols.UIUpdaterInterface
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener, UIUpdaterInterface {


    private lateinit var sensorManager: SensorManager
    private var lux: Sensor? = null
    private lateinit var textView: TextView
    private lateinit var statusLabl: TextView
    private lateinit var messageField: TextInputEditText
    private lateinit var connectBtn: Button
    private lateinit var sendBtn: Button
    private lateinit var messageHistoryView: EditText
    var mqttManager: MQTTmanager? = null
    private var serverURI: String = "tcp://broker.emqx.io:1883"
    private var host: String = "n8n9rj.messaging.internetofthings.ibmcloud.com"
    private var clientId: String = "d:n8n9rj:Android:12"
    private var username: String = "use-token-auth"
    private var password: String = "secret123"
    private var topic: String = "iot-2/evt/message/fmt/json"
//    private var mqttConnectOption:MqttConnectOptions = MqttConnectOptions();

    companion object {
        const val TAG = "AndroidMqttClient"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusLabl = findViewById(R.id.statusLabl)
        messageField = findViewById(R.id.messageField)
        connectBtn = findViewById(R.id.connectBtn)
        sendBtn = findViewById(R.id.sendBtn)
        messageHistoryView = findViewById(R.id.messageHistoryView)
        textView = findViewById(R.id.lux_tv)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lux = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        resetUIWithConnection(false)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val lightLux = event.values[0]
        textView.text = lightLux.toString()

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lux, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun resetUIWithConnection(status: Boolean) {
//        ipAddressField.isEnabled  = !status
//        topicField.isEnabled      = !status
        messageField.isEnabled = status
        connectBtn.isEnabled = !status
        sendBtn.isEnabled = status

        // Update the status label.
        if (status) {
            updateStatusViewWith("Connected")
        } else {
            updateStatusViewWith("Disconnected")
        }
    }

    override fun updateStatusViewWith(status: String) {
        statusLabl.text = status
    }

    override fun update(message: String) {
        val text = messageHistoryView.text.toString()
//        val newText = """
//            $text
//            $message
//            """
        var newText = text + "\n" + message +  "\n"
        messageHistoryView.setText(newText)
        messageHistoryView.setSelection(messageHistoryView.text.length)
    }

    fun connect(view: View) {

        if (Objects.nonNull(host) && Objects.nonNull(topic)) {
            val host = "tcp://" + host + ":1883"
            val topic = topic
            val connectionParams = MQTTConnectionParams(clientId, host, topic, username, password)
            mqttManager = MQTTmanager(connectionParams, applicationContext, this)
            mqttManager?.connect()
        } else {
            updateStatusViewWith("Please enter all valid fields")
        }

    }

    fun sendMessage(view: View) {

        mqttManager?.publishEvent(topic,messageField.text.toString(),0,false)
//        mqttManager?.publish(topic,messageField.text.toString())

        messageField.setText("")
    }

}