package com.example.max.mydiary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import java.io.*


val MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 11
val MY_PERMISSIONS_CAMERA = 88
val MY_PERMISSIONS_VIDEO_CAMERA = 89

class MainActivity : AppCompatActivity() {

    private val MAIN_ERR_LOG_TAG = "mydiary.main-activity.error"
    private val MAIN_INFO_LOG_TAG = "mydiary.main-activity.info"
    private val MAIN_DBG_LOG_TAG = "mydiary.main-activity.debug"
    private val REQUEST_TAKE_PHOTO = 1
    private val REQUEST_TAKE_VIDEO = 2
    private val REQUEST_TAKE_NOTE = 3
    private val REQUEST_TAKE_AUDIONOTE = 4

    private lateinit var diaryPageDateDir: File
    private lateinit var diaryPageFileName: String
    private lateinit var mCurrentPhotoPath: String
    private lateinit var mCurrentVideoPath: String
    private lateinit var diaryPageData: ArrayList<String>

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private fun populateDiaryPage() {
        //fetch diary page file for today, if no file exists -- create it
        val diaryPageFileReader =  fetchDiaryPageFile() //BufferedReader(FileReader( getFileStreamPath(diaryPageFileName) ))
//read file into stream, try-with-resources
        try {
            diaryPageFileReader.lines().use { stream ->
                stream.forEach { diaryEntry ->
                    diaryPageData.add(diaryEntry)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
       // fillDiaryDataEntries()
    }





    private fun fetchDiaryPageFile(): BufferedReader {

        var diaryPageBufferedFileReader: BufferedReader? = null
        try {
            val reader = FileReader( getFileStreamPath(diaryPageFileName) )
            diaryPageBufferedFileReader = BufferedReader( reader )
        } catch( e: FileNotFoundException){
            //create the file
            val fileContents = diaryPageFileName
            this.openFileOutput(diaryPageFileName, Context.MODE_PRIVATE).use {
                it.write(fileContents.toByteArray())
            }
            val reader = FileReader( getFileStreamPath(diaryPageFileName) )
            diaryPageBufferedFileReader = BufferedReader( reader )

        }
        finally {
            return diaryPageBufferedFileReader!!
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //check for directory for the given date
        //create if necessary
        diaryPageFileName = "DiaryPageDay_"+ SimpleDateFormat("yyyy_MM_dd").format(Date()).toString()
        populateDiaryPage()
        if ( diaryPageData.isEmpty() )
            Log.e(MAIN_ERR_LOG_TAG, "WTF DIARY EMPTY!?")

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                debugToast("be able to save a file")
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE)

                // MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            diaryPageDateDir = getDateDirectory()
        }

        recyclerViewInitConfig()
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    diaryPageDateDir = getDateDirectory()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    debugToast("cannot write, permission not granted")
                }
                return
            }
            MY_PERMISSIONS_VIDEO_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakeVideoIntent()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    debugToast("cannot use camera, permission not granted")
                }
                return

            }
            MY_PERMISSIONS_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    dispatchTakePictureIntent()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    debugToast("cannot use camera, permission not granted")
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {

            REQUEST_TAKE_PHOTO -> {
                debugToast("Request \"take photo\" complete")
                debugToast("onActivityResult::requestCode=$requestCode::resultCode=$resultCode::intent=${intent.toString()}", Toast.LENGTH_LONG)
                diaryPageData.add(Uri.fromFile(File(mCurrentPhotoPath)).toString())
                Log.i(MAIN_INFO_LOG_TAG, Uri.fromFile(File(mCurrentPhotoPath)).toString())
                viewAdapter.notifyDataSetChanged()
            }

            REQUEST_TAKE_VIDEO -> {
                debugToast("Request \"take video\" complete")
                debugToast( intent!!.data.toString() )
                debugToast("onActivityResult::requestCode=$requestCode::resultCode=$resultCode::intent=${intent.toString()}", Toast.LENGTH_LONG)

                if (resultCode == Activity.RESULT_OK) {
                    //val videoUri = intent!!.data
                    //val videoUri = Uri.parse(mCurrentVideoPath)
                    val videoUri = Uri.fromFile(File(mCurrentVideoPath))
                    diaryPageData.add(Uri.fromFile(File(mCurrentVideoPath)).toString())
                    Log.i(MAIN_INFO_LOG_TAG, videoUri.toString())
                    viewAdapter.notifyDataSetChanged()
                    //videoView.setVideoURI(videoUri)
                    //videoView.resume()

                    debugToast("wow req_vid_capt result+code=ok!")
                }
                else if( resultCode == Activity.RESULT_CANCELED ) {
                    debugToast("video activity result canceled")
                }
            }

            REQUEST_TAKE_NOTE -> {
                if(resultCode == RESULT_OK) {
                    val noteUri  = Uri.fromFile(File(intent!!.getStringExtra(Intent.EXTRA_TEXT)))
                    diaryPageData.add(noteUri.toString())
                    Log.i(MAIN_INFO_LOG_TAG, noteUri.toString())
                    viewAdapter.notifyDataSetChanged()
                }
                else
                    debugToast("text activity result canceled")
            }

            REQUEST_TAKE_AUDIONOTE -> {
                if(resultCode == RESULT_OK) {
                    val audioNoteUri  = Uri.fromFile(File(intent!!.getStringExtra(Intent.EXTRA_TEXT)))
                    diaryPageData.add(audioNoteUri.toString())
                    Log.i(MAIN_INFO_LOG_TAG, audioNoteUri.toString())
                    viewAdapter.notifyDataSetChanged()
                }
                else
                    debugToast("audio note activity result canceled")
            }

            else -> {

                debugToast("else::onActivityResult::requestCode=$requestCode::resultCode=$resultCode::intent=${intent.toString()}", Toast.LENGTH_LONG)
            }

        }
    }


    fun seeTextActivity(view: View) {
        val seeTextActivity = Intent(this, TextActivity::class.java)
        startActivityForResult(seeTextActivity, REQUEST_TAKE_NOTE)
    }

    fun seeCalendarActivity(view: View) {
        //Create an Intent to start the Calendar  Activity
        val seeCalendarIntent = Intent(this, CalendarActivity::class.java)
        //Start the new Activity
        startActivity(seeCalendarIntent)
    }

    fun seeAudioCaptureActivity(view: View) {
        //Create an Intent to start the Calendar  Activity
        val seeAudioCaptureIntent = Intent(this, AudioCaptureActivity::class.java)
        //Start the new Activity
        startActivityForResult(seeAudioCaptureIntent, REQUEST_TAKE_AUDIONOTE)
    }

    fun startCamera(view: View) {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                debugToast("camera time")
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_PERMISSIONS_CAMERA)

                // MY_PERMISSIONS_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            dispatchTakePictureIntent()
        }

    }

    fun startVideo(view: View) {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                debugToast("video camera time")
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_PERMISSIONS_CAMERA)

                // MY_PERMISSIONS_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            if ( this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) )
                dispatchTakeVideoIntent()
            else
                debugToast("no feature camera for video??")
        }
    }

    private fun dispatchTakeVideoIntent() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (takeVideoIntent.resolveActivity(packageManager) != null) {
            // Create the File where the video should go
            var videoFile: File? = null
            try {
                videoFile = createVideoFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                debugToast("Error occurred during video file creation")
            }
            // Continue only if the File was successfully created
            if (videoFile != null) {
                val videoURI: Uri? = FileProvider.getUriForFile(this,
                        "com.example.max.mydiary.fileprovider", videoFile)
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI)
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri? = FileProvider.getUriForFile(this,
                    "com.example.max.mydiary.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = diaryPageDateDir
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    @Throws(IOException::class)
    private fun createVideoFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val videoFileName = "mp4_" + timeStamp + "_"
        val storageDir = diaryPageDateDir
        val video = File.createTempFile(
                videoFileName, /* prefix */
                ".mp4", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentVideoPath = video.absolutePath
        return video
    }

    fun getDateDirectory(): File {
        //"MMM.dd.yyyy.HH:mm:ssaaa"
        val mydiaryDate = "mydiary_" +  SimpleDateFormat("MMM.dd.yyyy")
                .format(Date()).toString()
        val dir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), mydiaryDate)
        debugToast(dir.toString())

        if ( dir.isDirectory  ) {
            // good! -- nothing to do but return
        }
        else if ( dir.isFile ) {
            //bad! -- bail
            debugToast("err:tried to make diary directory but its occupied by a file of the same name")
        }
        else {
            if( !dir.mkdirs() )
                Log.e(MAIN_ERR_LOG_TAG, "Directory not created")
        }
        return dir
    }


    /*Recyler View Config */
    private fun recyclerViewInitConfig() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = DiaryPageAdapter(diaryPageData)
        Log.d(MAIN_DBG_LOG_TAG,  "Diary Page Data:")
        if(diaryPageData.isNotEmpty())
        {
            diaryPageData.forEach {
                Log.d(MAIN_DBG_LOG_TAG, it)
            }
        }
        else
            Log.e(MAIN_ERR_LOG_TAG,  "ERROR: DIARY DATA EMPTY")

        recyclerView = findViewById<RecyclerView>(R.id.diaryPageRecyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }
    }
    /*End Recycler View Config */

    fun debugToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        val myToast = Toast.makeText(this, msg, duration)
        myToast.show()
    }
}
