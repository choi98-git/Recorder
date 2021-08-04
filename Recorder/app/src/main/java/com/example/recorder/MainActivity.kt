package com.example.recorder

import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import java.lang.NullPointerException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val resetButton: Button by lazy {
        findViewById(R.id.resetButton)
    }

    private val recordButton: RecordButton by lazy {
        findViewById(R.id.recordButton)
    }

    private val requiredPermission = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val recorderdingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recoding.3gp"
    }

    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null

    private var state = State.BEFORE_RECORDING

    set(value) {
        field = value
        resetButton.isEnabled = (value ==  State.AFTER_RECORDING) ||
                (value == State.ON_PLAYING)
        recordButton.updateIconWithState(value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestAudioPermission()
        initViews()
        bindViews()
        initVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Permission 허용 여부를 받음
        val audioRecordPermissionGranted = requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        // 권한 부여 x
        if(!audioRecordPermissionGranted){
            finish()
        }

    }


    // 권한 허용 여부 요청
    private fun requestAudioPermission(){
        requestPermissions(requiredPermission, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    // 상태에 따른 recordButton Icon 변경
    private fun initViews() {
        recordButton.updateIconWithState(state)
    }


    private fun bindViews() {

        resetButton.setOnClickListener {
            stopPlaying()
            state = State.BEFORE_RECORDING
        }

        // recordButton 클릭시 상태에 따른 실행
        recordButton.setOnClickListener {
            when(state){
                State.BEFORE_RECORDING -> {
                    startRecording()
                }
                State.ON_RECORDING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING ->{
                    startPlaying()
                }
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }
        }

    }

    private fun initVariables(){
        state = State.BEFORE_RECORDING
    }

    // 녹음을 위한 설정
    private fun startRecording(){
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recorderdingFilePath)
            prepare()
        }
        recorder?.start()
        state = State.ON_RECORDING
    }

    private fun stopRecording(){
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        state = State.AFTER_RECORDING
    }

    //녹음 내용 재생
    private fun startPlaying(){
        player = MediaPlayer().apply {
            setDataSource(recorderdingFilePath)
            prepare()
        }
        player?.start()
        state = State.ON_PLAYING
    }

    private fun stopPlaying(){
        player?.release()
        player = null
        state = State.AFTER_RECORDING
    }

    companion object{
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}