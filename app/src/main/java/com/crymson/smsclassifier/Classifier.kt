package com.crymson.smsclassifier

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class Classifier(context: Context, jsonFilename: String, inputMaxLen : Int ) {

    private var context : Context? = context

    // word dictionary
    private var filename : String? = jsonFilename

    // Jumlah maksimal input sequence
    private var maxlen : Int = inputMaxLen

    private var vocabData : HashMap< String , Int >? = null

    // load word dictionary
    private fun loadJSONFromAsset(filename : String? ): String? {
        var json: String?
        try {
            val inputStream = context!!.assets.open(filename!! )
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer)
        }
        catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun processVocab( callback: VocabCallback ) {
        CoroutineScope( Dispatchers.Main ).launch {
            loadVocab( callback , loadJSONFromAsset( filename )!! )
        }
    }

    // Tokenisasi Teks yang diterima
    fun tokenize ( message : String ): IntArray {
        val parts : List<String> = message.split(" " )
        val tokenizedMessage = ArrayList<Int>()
        for ( part in parts ) {
            if (part.trim() != ""){
                var index : Int? = 0
                index = if ( vocabData!![part] == null ) {
                    0
                } else{
                    vocabData!![part]
                }
                tokenizedMessage.add( index!! )
            }
        }
        return tokenizedMessage.toIntArray()
    }

    // menyamakan ukuran sequence agar bisa diinput
    fun padSequence ( sequence : IntArray ) : IntArray {
        val maxlen = this.maxlen
        if ( sequence.size > maxlen ) {
            return sequence.sliceArray( 0..maxlen )
        }
        else if ( sequence.size < maxlen ) {
            val array = ArrayList<Int>()
            array.addAll( sequence.asList() )
            for ( i in array.size until maxlen ){
                array.add(0)
            }
            return array.toIntArray()
        }
        else{
            return sequence
        }
    }


    interface VocabCallback {
        fun onVocabProcessed()
    }

    private fun loadVocab(callback : VocabCallback, json : String )  {
        with( Dispatchers.Default ) {
            val jsonObject = JSONObject( json )
            val iterator : Iterator<String> = jsonObject.keys()
            val data = HashMap< String , Int >()
            while ( iterator.hasNext() ) {
                val key = iterator.next()
                data[key] = jsonObject.get( key ) as Int
            }
            with( Dispatchers.Main ){
                vocabData = data
                callback.onVocabProcessed()
            }
        }
    }

}