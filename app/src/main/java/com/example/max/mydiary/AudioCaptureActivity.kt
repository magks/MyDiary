package com.example.max.mydiary


import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.Button
import java.io.IOException
import java.util.*
import android.widget.Toast
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.net.Uri
import android.support.v4.content.ContextCompat


import kotlinx.android.synthetic.main.activity_audio.*
import java.io.File

class AudioCaptureActivity : AppCompatActivity() {

    lateinit var buttonStart: Button
    lateinit var buttonStop: Button
    lateinit var buttonPlayLastRecordAudio:Button
    lateinit var buttonStopPlayingRecording: Button
    var AudioSavePathInDevice: String? = null
    lateinit var mediaRecorder: MediaRecorder
    var random: Random? = null
    var RandomAudioFileName: String = "ABCDEFGHIJKLMNOP"
    val RequestPermissionCode = 1
    var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        buttonStart = see_calendar_button
        buttonStop = button2
        buttonPlayLastRecordAudio = button3
        buttonStopPlayingRecording = button4

        buttonStop.isEnabled = false
        buttonPlayLastRecordAudio.isEnabled = false
        buttonStopPlayingRecording.isEnabled = false

        random = Random()

        buttonStart.setOnClickListener(View.OnClickListener {
            if (checkPermission()) {

                AudioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/" +
                        CreateRandomAudioFileName(5) + "AudioRecording.3gp"

                MediaRecorderReady()

                try {
                    mediaRecorder.prepare()
                    mediaRecorder.start()
                } catch (e: IllegalStateException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

                buttonStart.isEnabled = false
                buttonStop.isEnabled = true

                Toast.makeText(this, "Recording started",
                        Toast.LENGTH_LONG).show()
            } else {
                requestPermission()
            }
        })

        buttonStop.setOnClickListener(View.OnClickListener {
            mediaRecorder.stop()
            buttonStop.isEnabled = false
            buttonPlayLastRecordAudio.isEnabled = true
            buttonStart.isEnabled = true
            buttonStopPlayingRecording.isEnabled = false

            Toast.makeText(this, "Recording Completed",
                    Toast.LENGTH_LONG).show()
        })

        buttonPlayLastRecordAudio.setOnClickListener(View.OnClickListener {
            buttonStop.isEnabled = false
            buttonStart.isEnabled = false
            buttonStopPlayingRecording.isEnabled = true

            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer!!.setDataSource(AudioSavePathInDevice)
                mediaPlayer!!.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mediaPlayer!!.start()
            Toast.makeText(this, "Recording Playing",
                    Toast.LENGTH_LONG).show()
        })

        buttonStopPlayingRecording.setOnClickListener(View.OnClickListener {
            buttonStop.isEnabled = false
            buttonStart.isEnabled = true
            buttonStopPlayingRecording.isEnabled = false
            buttonPlayLastRecordAudio.isEnabled = true

            if (mediaPlayer != null) {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                MediaRecorderReady()
            }
        })

    }

    fun MediaRecorderReady() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder.setOutputFile(AudioSavePathInDevice)
    }

    fun CreateRandomAudioFileName(string: Int): String {
        val stringBuilder = StringBuilder(string)
        var i = 0
        while (i < string) {
            stringBuilder.append( RandomAudioFileName[ random!!.nextInt(RandomAudioFileName.length)] )
            i++
        }
        return stringBuilder.toString()
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO), RequestPermissionCode)
    }

    private fun sendResultBackToMainDiaryPage(fileName: String) {
        val fileUriString: String = Uri.fromFile(File(fileName)).toString()
        val intent = Intent().putExtra(Intent.EXTRA_TEXT, fileUriString)
        setResult(RESULT_OK, intent)
        finish()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            RequestPermissionCode -> {
                if (grantResults.isNotEmpty()) {
                    val StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED
                    val RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED

                    if (StoragePermission && RecordPermission)
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
                    else
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext,
                WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext,
                RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

        fun backToMainActivity(view: View) {
            //Create an Intent to go back to main activity
            //val backToMainActivityIntent = Intent(this, MainActivity::class.java)
            //Start the activity
            //startActivity(backToMainActivityIntent)
            sendResultBackToMainDiaryPage(AudioSavePathInDevice.toString())
        }
}


