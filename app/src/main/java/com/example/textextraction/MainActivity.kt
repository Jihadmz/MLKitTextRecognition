package com.example.textextraction

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.example.textextraction.ui.theme.TextExtractionTheme
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageUtils
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : ComponentActivity() {

    //  Image to be displayed
    private lateinit var image: MutableState<Bitmap>

    //  Text to be extracted
    private lateinit var text: MutableState<String>

    //  Captured Image Uri
    private lateinit var capturedImageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            //  Checking for the camera permission if it is granted or not, if not, request it
            if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions()
            }

            image = remember { // here we're setting a temp image
                mutableStateOf(Bitmap.createBitmap(getDrawable(R.drawable.temp)!!.toBitmap()))
            }

            text = remember {
                mutableStateOf("")
            }

            TextExtractionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Image(
                            bitmap = image.value.asImageBitmap(),
                            contentDescription = "Captured Image"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SelectionContainer() { // selection container to make the user able to select text and copy it
                            Text(text = text.value)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(onClick = { // button to take a pic with the camera
                            takePic()
                        }) {
                            Text(text = "Capture Image")
                        }

                        Button(onClick = { // button to choose an image
                            mGetContent.launch("image/*")
                        }) {
                            Text(text = "Choose Image")
                        }

                        Button(onClick = { // button to extract the text from the image
                            detectText()
                        }) {
                            Text(text = "Extract Text")
                        }
                    }
                }
            }
        }
    }

    private fun takePic(){
        //  Navigating to the camera app on the phone to take a photo
        val takePictureIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        startActivity(takePictureIntent)
    }

    private fun detectText(){
        // here we are getting an input image from the uri of the chosen image to process it with the text recognizer instance
        val inputImage = InputImage.fromFilePath(this,capturedImageUri.toUri())
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result: Task<Text> = textRecognizer.process(inputImage).addOnSuccessListener {
            text.value = it.text // after processing, we will set the extracted text from the image to the text variable
        }.addOnFailureListener {
            Toast.makeText(this, "Extraction of text failed", Toast.LENGTH_SHORT).show()
        }
    }

    private var mGetContent = registerForActivityResult(
        GetContent()
    ) { uri ->
        capturedImageUri = uri.toString()
        //  after choosing the desired image, we want to get a bitmap from the uri
        image.value = ImageUtils.getInstance().zza(contentResolver, uri)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 12 && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText( this, "Granted", Toast.LENGTH_SHORT ).show()
        }
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                12
            )
        }
    }
}