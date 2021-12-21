package com.android.example.iuh_ad.ngoducbao.student_19487461.projecttranslatekotlin

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.ToggleButton
import com.google.android.material.textfield.TextInputEditText


class TranslateFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.translate_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val switchButton = view.findViewById<Button>(R.id.buttonSwitchLang)
        val sourceSyncButton = view.findViewById<ToggleButton>(R.id.buttonSyncSource)
        val targetSyncButton = view.findViewById<ToggleButton>(R.id.buttonSyncTarget)
        val srcTextView: TextInputEditText = view.findViewById(R.id.sourceText)
        val targetTextView = view.findViewById<TextView>(R.id.targetText)
        val downloadedModelsTextView = view.findViewById<TextView>(R.id.downloadedModels)
        val sourceLangSelector = view.findViewById<Spinner>(R.id.sourceLangSelector)
        val targetLangSelector = view.findViewById<Spinner>(R.id.targetLangSelector)
        val viewModel = ViewModelProviders.of(this).get(
            TranslateViewModel::class.java
        )

        // Get available language list and set up source and target language spinners
        // with default selections.
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_list, viewModel.availableLanguages,
        )
        sourceLangSelector.adapter = adapter
        targetLangSelector.adapter = adapter
        sourceLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("en")))
        targetLangSelector.setSelection(adapter.getPosition(TranslateViewModel.Language("es")))
        sourceLangSelector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setProgressText(targetTextView)
                viewModel.sourceLang.setValue(adapter.getItem(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                targetTextView.text = ""
            }
        }
        targetLangSelector.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setProgressText(targetTextView)
                viewModel.targetLang.setValue(adapter.getItem(position))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                targetTextView.text = ""
            }
        }
        switchButton.setOnClickListener {
            setProgressText(targetTextView)
            val sourceLangPosition = sourceLangSelector.selectedItemPosition
            sourceLangSelector.setSelection(targetLangSelector.selectedItemPosition)
            targetLangSelector.setSelection(sourceLangPosition)
        }

        // Set up toggle buttons to delete or download remote models locally.
        sourceSyncButton.setOnCheckedChangeListener { buttonView, isChecked ->
            val language = adapter.getItem(sourceLangSelector.selectedItemPosition)
            if (isChecked) {
                viewModel.downloadLanguage(language!!)
            } else {
                viewModel.deleteLanguage(language!!)
            }
        }
        targetSyncButton.setOnCheckedChangeListener { buttonView, isChecked ->
            val language = adapter.getItem(targetLangSelector.selectedItemPosition)
            if (isChecked) {
                viewModel.downloadLanguage(language!!)
            } else {
                viewModel.deleteLanguage(language!!)
            }
        }

        // Translate input text as it is typed
        srcTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                setProgressText(targetTextView)
                viewModel.sourceText.postValue(s.toString())
            }
        })
        viewModel.translatedText.observe(
            viewLifecycleOwner,
            { resultOrError ->
                if (resultOrError.error != null) {
                    srcTextView.setError(resultOrError.error!!.localizedMessage)
                } else {
                    targetTextView.text = resultOrError.result
                }
            }
        )

        // Update sync toggle button states based on downloaded models list.
        viewModel.availableModels.observe(
            viewLifecycleOwner,
            { translateRemoteModels ->
                val output = requireContext().getString(
                    R.string.downloaded_models_label,
                    translateRemoteModels
                )
                downloadedModelsTextView.text = output
                sourceSyncButton.isChecked =
                    translateRemoteModels!!.contains(
                        adapter.getItem(sourceLangSelector.selectedItemPosition)!!.code
                    )
                targetSyncButton.isChecked = translateRemoteModels.contains(
                    adapter.getItem(targetLangSelector.selectedItemPosition)!!.code
                )
            }
        )
    }

    private fun setProgressText(tv: TextView) {
        tv.text = requireContext().getString(R.string.translate_progress)
    }

    companion object {
        fun newInstance(): TranslateFragment {
            return TranslateFragment()
        }
    }
}