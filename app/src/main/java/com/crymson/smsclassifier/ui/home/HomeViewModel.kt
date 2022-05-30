package com.crymson.smsclassifier.ui.home

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.lang.Exception
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
    // Name of TFLite model ( in /assets folder ).
    val MODEL_ASSETS_PATH = "spam_model.tflite"

    // Max Length of input sequence. The input shape for the model will be ( None , INPUT_MAXLEN ).
    val INPUT_MAXLEN = 63

    var tfLiteInterpreter : Interpreter? = null

    @Throws(IOException::class)
    fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_ASSETS_PATH)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Perform inference, given the input sequence.
    fun classifySequence (context: Context, sequence : IntArray ): FloatArray {
        // Input shape -> ( 1 , INPUT_MAXLEN )
        val inputs : Array<FloatArray> = arrayOf( sequence.map { it.toFloat() }.toFloatArray() )
        // Output shape -> ( 1 , 2 ) ( as numClasses = 2 )
        val outputs : Array<FloatArray> = arrayOf( FloatArray( 3 ) )
        try {
            tfLiteInterpreter?.run( inputs , outputs )
        } catch (e: Exception){
            HomeFragment().clear()
            Toast.makeText(context, "Ukuran Pesan Terlalu Panjang!", Toast.LENGTH_LONG).show()
            Log.e(this.toString(), "${e.message}")
        }
        return outputs[0]
    }
}