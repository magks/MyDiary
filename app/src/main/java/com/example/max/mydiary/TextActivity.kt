package com.example.max.mydiary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_text.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*


class TextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)
    }

    fun saveNote(view: View) {
        //check for write permissions
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                debugToast("be able to save a file")
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE)
            }
        } else {
            // Permission has already been granted
            save()
        }
    }

    private fun save() = try {
        val fileName = makeFileName()
        val fileOut: FileOutputStream = openFileOutput(fileName, MODE_PRIVATE)
        val outputWriter = OutputStreamWriter(fileOut)

        sendResultBackToMainDiaryPage(fileName)

        outputWriter.write(editText.text.toString())
        outputWriter.close()

        Toast.makeText(baseContext, "File Saved Successfully: $fileName",
                Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun makeFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val textFileName = "note_" + timeStamp + ".txt"
        return textFileName
    }

    private fun sendResultBackToMainDiaryPage(fileName: String) {
        val fileUriString: String = Uri.fromFile(File(fileName)).toString()
        val intent = Intent().putExtra(Intent.EXTRA_TEXT, fileUriString)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // write-related task you need to do.
                    save()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    debugToast("cannot write, permission not granted")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun debugToast(msg: String) {
        val myToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        myToast.show()
    }
}
