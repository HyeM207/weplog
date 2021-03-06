package com.cookandroid.weplog

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Authentication : AppCompatActivity() {

    lateinit var auth_layoutStep1: ConstraintLayout
    lateinit var auth_layoutStep2: ConstraintLayout

    lateinit var auth_layoutUpload: ConstraintLayout
    lateinit var auth_btnskip: Button
    lateinit var auth_step2Preview : ImageView
    lateinit var auth_step1TPlace : TextView
    lateinit var auth_step1txt : TextView

    lateinit var currentPhotoPath: String
    val REQUEST_TAKE_PHOTO = 1


    lateinit var fileName : String
    lateinit var photoURI : Uri
    var authStep1 = false
    var authStep2 = false
    private lateinit var pushRef:DatabaseReference
    private var pushRefKey : String = ""


    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication)

        auth_layoutStep1 = findViewById(R.id.auth_layoutStep1)
        auth_layoutStep2 = findViewById(R.id.auth_layoutStep2)
        auth_layoutUpload = findViewById(R.id.auth_layoutUpload)
        auth_btnskip = findViewById(R.id.auth_btnskip)
        auth_step1TPlace = findViewById(R.id.auth_step1TPlace)
        auth_step1txt = findViewById(R.id.auth_step1txt)

        auth_layoutUpload.visibility = View.INVISIBLE
        var trashPlace : String = ""


        if(intent.hasExtra("trashplace")){
            Toast.makeText(this, "?????? ??????." + intent.getStringExtra("trashplace"), Toast.LENGTH_SHORT).show()
            auth_step1txt.visibility = View.INVISIBLE
            auth_step1TPlace.visibility = View.VISIBLE

            trashPlace = intent.getStringExtra("trashplace").toString()
            auth_step1TPlace.setSingleLine(false)
            auth_step1TPlace.setTextSize(16F)
            auth_step1TPlace.setTextColor(Color.parseColor("#000000"));
            auth_step1TPlace.text = trashPlace
            authStep1 = true
            auth_layoutStep1.isClickable = false
        }


        if (intent.hasExtra("pushRefKey")){
            pushRefKey = intent.getStringExtra("pushRefKey").toString()
            Log.e("1002", "$pushRefKey exists" )
        }

        // ?????? ?????? visible??? ?????????
        checkAuth()


        // 1??????. ???????????? ??????
        auth_layoutStep1.setOnClickListener {
            val intent = Intent(this, QRcodeScanner::class.java)
            intent.putExtra("page","Authentication")
            intent.putExtra("pushRefKey", pushRefKey)
            Log.e("1002", pushRefKey+"QR ????????? ?????? ??? ")
            startActivity(intent)
        }


        // 2??????. ???????????? ??????
        auth_layoutStep2.setOnClickListener {

            val cameraPermissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) { // ????????? ?????? ??????
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1000)
            } else { //????????? ?????? ??????
                val REQUEST_IMAGE_CAPTURE = 1
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                        dispatchTakePictureIntent()
                    }
                }
            }
        }


        // ?????? ??????
        auth_layoutUpload.setOnClickListener {
            uploadImage(photoURI)
            Log.i("firebase", "????????? ?????? ?????? ?????? ??????????????? ??? ??? ?????? $pushRefKey")
            uploadPost(trashPlace, pushRefKey)
        }

        // skip ??????
        auth_btnskip.setOnClickListener {
                this.finish() // ?????? ?????? ??????
        }

    }

    private fun uploadPost(trashPlace : String, pushRefKey:String) {

        val user = Firebase.auth.currentUser
        database = Firebase.database.reference

        if (user != null) {
            val key = database.child("users").child(user.uid).push().key


            database.child("users").child(user?.uid.toString()).get().addOnSuccessListener {
                lateinit var postValues : Map<String, Any?>
                //var nick : String = "init"
                var post = Post()

                post.postId = key
                post.writerId = user?.uid

                val nick = it.child("nickname").value.toString()
                Log.e("nick1", nick)
                post.writerNick = nick

                //Log.e("nick3", nick+"check")
                post.timestamp = System.currentTimeMillis()
                post.photoUrl = fileName // storage??? ????????? ??? ?????? ??????

                postValues = post.toMap()


                if (key != null ) {
                    //var myPost : MutableMap<String, Boolean> = mutableMapOf(key.toString() to false)
                    Toast.makeText(this, key.toString(), Toast.LENGTH_SHORT).show()
                    var mCalendar = Calendar.getInstance()
                    var todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()
                    database.child("users").child(user.uid).child("lastAuth").setValue(todayDate)
                    database.child("users").child(user.uid).child("lastAuthPost").setValue(post.postId)
                    database.child("users").child(user.uid).child("posts/$key").setValue(false)// ?????? ??? : post?????? id??? ??????

                    database.child("community").child(key).setValue(postValues)


                    // ?????? ?????? ??????
                    Log.i("firebase", "????????? ???????????? ??????????????? ??? ??? ?????? $pushRefKey")
                    pushRef=database.child("user/${user.uid}/Pedometer/date").child(todayDate).child("$pushRefKey/record")
                    pushRef.child("trashPlace").setValue(trashPlace)

                    var trashplace_str =trashPlace
                    var trashsplit=trashplace_str!!.split(" ")
                    Log.i("firebase", "check in trashsplit : $trashsplit")
                    var bigarea = trashsplit[0]
                    var midarea = trashsplit[1]
                    var tmplist=ArrayList<String>()
                    var trasharea=""
                    for (i in 2..trashsplit.size-1){
                        tmplist.add(trashsplit[i])
                    }
                    trasharea = tmplist.joinToString(" ")

                    Log.i("firebase", "check in auth big : $bigarea, mid : $midarea, trasharea : $trasharea")
                    database.child("user/${user.uid}/visit/$bigarea/$midarea/$trasharea/count").setValue("0")



                    val intent = Intent(this, NavigationActivity::class.java)
                    startActivity(intent)
                }

            }


        }


        Toast.makeText(this,"????????? ??????!", Toast.LENGTH_SHORT).show()

        this.finish() // ?????? ?????? ??????


    }


    // ????????? ????????? storage ??? ??????
    private fun uploadImage(uri : Uri) {

        var storage : FirebaseStorage ?= FirebaseStorage.getInstance()
        var uid = FirebaseAuth.getInstance().currentUser?.uid

        // ?????? ?????? ??????
        fileName = "IMAGE_${uid}_${SimpleDateFormat("yyyymmdd_HHmmss").format(Date())}_.png"

        // ???????????? ????????? ???????????? ?????????
        var imageRef = storage!!.reference.child("community").child(fileName)

        // ????????? ?????? ?????????
        imageRef.putFile(uri!!).addOnSuccessListener {
            Toast.makeText(this, " ????????? ??????" + imageRef.toString() , Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(this, " ????????? ??????" , Toast.LENGTH_SHORT).show()
        }
    }


    private fun checkAuth() {
        //Toast.makeText(this, authStep1.toString() + " " + authStep2.toString(), Toast.LENGTH_SHORT).show()
        if (authStep1 && authStep2){
            auth_layoutUpload.visibility = View.VISIBLE
        }

    }


    // ?????? ?????? ???
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ?????? preview
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val REQUEST_IMAGE_CAPTURE = 1

        auth_step2Preview = findViewById(R.id.auth_step2Preview)
        auth_step2Preview.visibility = View.VISIBLE

        when (requestCode){
            1 -> {
                if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){

                    // ?????????????????? ?????? ???????????? ??????????????????
                    val file = File(currentPhotoPath)
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media
                                .getBitmap(contentResolver, Uri.fromFile(file))  //Deprecated
                        auth_step2Preview.setImageBitmap(bitmap)
                    }
                    else{
                        val decode = ImageDecoder.createSource(this.contentResolver,
                                Uri.fromFile(file))
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        auth_step2Preview.setImageBitmap(bitmap)
                    }

                    authStep2 = true
                    auth_layoutStep2.isClickable = false
                    checkAuth()
                }
            }
        }

    }



    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                            this,"com.cookandroid.weplog.fileprovider", it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File ?= getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }



}