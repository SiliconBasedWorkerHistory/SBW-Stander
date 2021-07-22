package com.example.sbw_stander


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FlashUtils.init(this).also {
            findViewById<Switch>(R.id.switch1).apply {
                text = "Off"
                setOnCheckedChangeListener { buttonView, isChecked ->
                    run {
                        buttonView.text = if (isChecked) "On" else "Off"
                        if (isChecked){
                            FlashUtils.open(this@MainActivity)
                        }else{
                            FlashUtils.close()
                        }
                    }
                }
            }
        }
    }


}