package com.example.sbw_stander

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.ControlAction
import android.service.controls.templates.ControlButton
import android.service.controls.templates.ToggleTemplate
import androidx.annotation.RequiresApi
import com.example.sbw_stander.device.Device
import io.reactivex.Flowable
import io.reactivex.processors.ReplayProcessor
import org.reactivestreams.FlowAdapters
import java.util.concurrent.Flow
import java.util.function.Consumer

@RequiresApi(Build.VERSION_CODES.R)
class DeviceControllerService : ControlsProviderService() {

    private lateinit var updatePublisher: ReplayProcessor<Control>

    private val devices = hashMapOf(
        "torch" to Device().apply {
            id = "torch"
            title = "FlashLight"
            subtitle = "click to switch"
            structure = "Local"
            deviceType = DeviceTypes.TYPE_LIGHT
            actionDes = "button click to switch torch"
        }
    )

    private fun performAction(id:String){
        when(id){
            "torch"->{
                FlashUtils.torch_on = !FlashUtils.torch_on
                if (FlashUtils.torch_on){
                    FlashUtils.open(this)
                }else{
                    FlashUtils.close()
                }
            }
        }
    }

    override fun createPublisherForAllAvailable(): Flow.Publisher<Control> {
        val intent = Intent(this,MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(baseContext, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val controlList = mutableListOf<Control>()

        devices.forEach { (k, v) ->
            controlList.add(
                Control.StatelessBuilder(k,pendingIntent)
                    .setTitle(v.title)
                    .setSubtitle(v.subtitle)
                    .setStructure(v.structure)
                    .setDeviceType(v.deviceType)
                    .build()
            )
        }


        return FlowAdapters.toFlowPublisher(Flowable.fromIterable(controlList))
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        FlashUtils.init(this)


        updatePublisher = ReplayProcessor.create()

        controlIds.forEach {
            val device = devices.getOrDefault(it, Device())
            val btn = ControlButton(FlashUtils.torch_on, device.actionDes)
            val intent = Intent(this,MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(baseContext, 12, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val control = Control.StatefulBuilder(device.id,pendingIntent)
                .setTitle(device.title)
                .setSubtitle(device.subtitle)
                .setStructure(device.structure)
                .setStatus(Control.STATUS_OK)
                .setControlTemplate(ToggleTemplate(device.id, btn))
                .setDeviceType(device.deviceType)
                .setStatusText(if(FlashUtils.torch_on)"已开启" else "已关闭")
                .build()
            updatePublisher.onNext(control)
        }

        return FlowAdapters.toFlowPublisher(updatePublisher)
    }


    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {
        val intent = Intent(this,MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(baseContext, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        consumer.accept(ControlAction.RESPONSE_OK)
        val device = devices.getOrDefault(controlId,Device())

        performAction(controlId)





        val btn = ControlButton(FlashUtils.torch_on, device.actionDes)

        val control = Control.StatefulBuilder(controlId, pendingIntent)
            .setTitle(device.title)
            .setSubtitle(device.subtitle)
            .setStructure(device.structure)
            .setDeviceType(device.deviceType)
            .setStatus(Control.STATUS_OK)
            .setControlTemplate(ToggleTemplate(controlId, btn))
            .setStatusText(if(FlashUtils.torch_on)"已开启" else "已关闭")
            .build()

        updatePublisher.onNext(control)
    }
}