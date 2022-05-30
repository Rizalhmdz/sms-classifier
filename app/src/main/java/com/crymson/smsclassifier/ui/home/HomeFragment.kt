package com.crymson.smsclassifier.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crymson.smsclassifier.databinding.FragmentHomeBinding
import android.app.ProgressDialog
import android.text.TextUtils
import android.view.View.GONE
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.crymson.smsclassifier.Classifier
import org.tensorflow.lite.Interpreter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var classifier: Classifier

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        binding.lifecycleOwner = this
//        binding.viewModel = viewModel
        binding.homeFragment = this

        binding.result.visibility = GONE

        // Init the classifier.
        classifier = Classifier( requireContext() , "spam_word_dict.json" , viewModel.INPUT_MAXLEN )
        // Init TFLiteInterpreter
        viewModel.tfLiteInterpreter = Interpreter( viewModel.loadModelFile(requireContext()) )

        // Start vocab processing, show a ProgressDialog to the user.
        val progressDialog = ProgressDialog( requireContext() )
        progressDialog.setMessage( "Parsing word_dict.json ..." )
        progressDialog.setCancelable( false )
        progressDialog.show()
        classifier.processVocab( object: Classifier.VocabCallback {
            override fun onVocabProcessed() {
                // Processing done, dismiss the progressDialog.
                progressDialog.dismiss()
            }
        })

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun submit(){
        val message = binding.editText.text.toString().lowercase().trim()
        if ( !TextUtils.isEmpty( message ) ){
            // Tokenize and pad the given input text.
            val tokenizedMessage = classifier.tokenize( message )
            val paddedMessage = classifier.padSequence( tokenizedMessage )

            val results = viewModel.classifySequence(requireContext(), paddedMessage )
            val normal_msg = results[0]
            val spam_msg = results[1]
            val other_msg = results[2]
            binding.result.text = "NETRAL : $normal_msg\nSPAM : $spam_msg\nINFO : $other_msg"

            if(results[0] == results[1] && results[1] == results[2] && results[2] == results[0]) {
                binding.result.visibility = View.GONE
            }
            else binding.result.visibility = View.VISIBLE
        }
        else{
            Toast.makeText( requireContext(), "Masukkan Pesan!", Toast.LENGTH_LONG).show();
        }

    }
    fun clear(){
        binding.result.text = ""
        binding.result.visibility = View.GONE
        binding.editText.text?.clear()
    }

}