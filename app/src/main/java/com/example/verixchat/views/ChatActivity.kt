package com.example.verixchat.views

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verixchat.R
import com.example.verixchat.databinding.ActivityChatBinding
import com.example.verixchat.models.ChatModel
import com.example.verixchat.viewModels.ChatViewModel
import kotlin.getValue

private var currentChatNo: Long = System.currentTimeMillis()
class ChatActivity : AppCompatActivity() {
    lateinit var binding: ActivityChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    // for Current Response Type
    private var currentResponseType: String = "Default"

    // for Quiz Mode
    private var isQuizMode = false

    // for Speech-To-Text
    val permissionsArray = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var speechRecognizerIntent: Intent

    // for Text-To-Speech
    private var isListening = false
    private var isMuted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // Adapter for Recycler
        observeChats() // Re-Render if any changes in List of Data
        setupSendButton() // Send Button Click Event
        setupToneSelector() // Response tone type selector
        setupQuizMode()
        setupSTT()
    }

    // Adapter for Recycler
    private fun setupRecyclerView(){
        chatAdapter = ChatAdapter()
        binding.rvConversations.adapter = chatAdapter
        binding.rvConversations.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
    }

    // Re-Render if any changes in List of Data
    private fun observeChats(){
        chatViewModel.allChats.observe(this) { chats ->
            val currentChatList = chats.filter { it.chatNo == com.example.verixchat.views.currentChatNo }

            Log.d("MainActivity", "Current Chat Messages: $currentChatList")
            chatAdapter.updateChatList(currentChatList)

            // Prevent crash if list is empty
            if (currentChatList.isNotEmpty()) {
                binding.rvConversations.smoothScrollToPosition(currentChatList.size - 1)
            }
            binding.tvWelcomeTxt.visibility =
                if (currentChatList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    // Send Button Click Event
    private fun setupSendButton(){
        binding.btnSendButton.setOnClickListener {
            val question = binding.etInputText.text.toString().trim()
            if (question.isNotEmpty()) {
                val chatModel = ChatModel(message = question, isUser = true, chatNo = currentChatNo, isBotMessagePending = false)
                chatViewModel.create(chatModel)
                binding.tvWelcomeTxt.visibility = View.GONE
                binding.etInputText.text.clear()

                // Modify prompt based on tone
                val prompt = buildString {
                    // Always start with tone instructions
                    when (currentResponseType) {
                        "Friendly" -> appendLine("""
                            Respond in a warm and casual tone, but always use well-formatted Markdown.
                            Use:
                            - **Bold** for key points
                            - Bullet points for lists
                            - Numbered lists where needed
                            - Code blocks for code
                        """.trimIndent())

                        "Professional" -> appendLine("""
                            Respond in a formal, concise tone, but always use well-formatted Markdown.
                            Use:
                            - **Bold** for key points
                            - Bullet points for lists
                            - Numbered lists where needed
                            - Code blocks for code
                        """.trimIndent())

                        else -> appendLine("""
                            Respond in well-formatted Markdown.
                            Use:
                            - **Bold** for key points
                            - Bullet points for lists
                            - Numbered lists where needed
                            - Code blocks for code
                        """.trimIndent())
                    }

                    // Add quiz mode instructions if quiz mode is ON
                    if (isQuizMode) {
                        appendLine()
                        appendLine("""
                            Format your response as a quiz question with:
                             - A clear **Question** section
                            - Multiple-choice options as bullet points
                            - Mark the **correct answer** clearly at the end
                        """.trimIndent())
                    }

                    // Finally append the actual user question
                    appendLine()
                    appendLine("Question: $question")
                }.trim()



                chatViewModel.sendMessageToGeminiAI(prompt, currentChatNo)
            }
        }
    }

    private fun setupToneSelector(){

        binding.tvFriendly.setOnClickListener {
            setResponseType("Friendly")
        }
        binding.tvProfessional.setOnClickListener {
            setResponseType("Professional")
        }
        binding.tvDefault.setOnClickListener {
            setResponseType("Default")
        }
    }
    //  Helper Functions for setUpToneSelector()
    private fun setResponseType(tone: String) {
        currentResponseType = tone
        binding.llResponseSelector.visibility = View.GONE
        // Change icon based on tone
        when (tone) {
            "Friendly" -> {
                binding.btnResponseType.setImageResource(R.drawable.ic_response_type_friendly)
            }
            "Professional" -> {
                binding.btnResponseType.setImageResource(R.drawable.ic_response_type_professional)
            }
            else -> {
                binding.btnResponseType.setImageResource(R.drawable.ic_response_type_default)
            }
        }
        Log.d("ChatActivity", "Tone set to: $tone")
    }

    // To control what to do on touch event -> (1. Hide ToneSelector, when clicking outside of that box)
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val selector = binding.llResponseSelector
            val btn = binding.btnResponseType

            val selectorRect = Rect()
            val btnRect = Rect()

            selector.getGlobalVisibleRect(selectorRect)
            btn.getGlobalVisibleRect(btnRect)

            val clickedInsideSelector = selectorRect.contains(ev.rawX.toInt(), ev.rawY.toInt())
            val clickedButton = btnRect.contains(ev.rawX.toInt(), ev.rawY.toInt())

            // Toggle logic
            if (clickedButton) {
                // If button clicked, toggle visibility
                selector.visibility =
                    if (selector.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                return super.dispatchTouchEvent(ev)
            }

            // If selector is open and click is outside both selector and button, close it
            if (selector.visibility == View.VISIBLE && !clickedInsideSelector) {
                selector.visibility = View.GONE
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    // Quiz Mode
    private fun setupQuizMode() {
        binding.btnQuizMode.setOnClickListener {
            isQuizMode = !isQuizMode
            Log.d("Main", "Clicking Quick Mode, isQuizMode $isQuizMode")


            if (isQuizMode) {
                // Active colors
                binding.btnQuizMode.backgroundTintList =
                    ColorStateList.valueOf(getThemeColor(R.attr.colorQuizActiveBG))
                binding.btnQuizMode.findViewById<TextView>(R.id.tv_quizMode)
                    .setTextColor(getThemeColor(R.attr.colorQuizElementsActive))
                binding.btnQuizMode.findViewById<ImageView>(R.id.iv_quizMode)
                    .setColorFilter(getThemeColor(R.attr.colorQuizElementsActive))
            } else {
                // Default colors
                binding.btnQuizMode.backgroundTintList =
                    ColorStateList.valueOf(getThemeColor(R.attr.colorQuizInactiveBG))
                binding.btnQuizMode.findViewById<TextView>(R.id.tv_quizMode)
                    .setTextColor(getThemeColor(R.attr.colorQuizElementsInactive))
                binding.btnQuizMode.findViewById<ImageView>(R.id.iv_quizMode)
                    .setColorFilter(getThemeColor(R.attr.colorQuizElementsInactive))
            }
        }

    }
    private fun getThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    // TTS
    private fun setupSTT(){
        // Speech to Text
        if(checkSelfPermission(permissionsArray[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, permissionsArray, 200)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        binding.btnMicTTS.setOnClickListener {
            if (!isListening) {
                // Start Listening
                binding.etInputText.setHint("Listening...")
                speechRecognizer.startListening(speechRecognizerIntent)
                binding.btnMicTTS.setImageResource(R.drawable.ic_micstt_on)
                isListening = true
            } else {
                // Stop Listening
                binding.etInputText.setHint("Enter your message")
                speechRecognizer.stopListening()
                binding.btnMicTTS.setImageResource(R.drawable.ic_micstt_off)
                isListening = false
            }
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onEndOfSpeech() {
                isListening = false
                binding.btnMicTTS.setImageResource(R.drawable.ic_micstt_off)
                binding.etInputText.setHint("Enter your message")
            }

            override fun onError(error: Int) {
                isListening = false
                binding.btnMicTTS.setImageResource(R.drawable.ic_micstt_off)
                binding.etInputText.setHint("Enter your message")
            }

            override fun onResults(results: Bundle?) {
                val speechToTextResult = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(!speechToTextResult.isNullOrEmpty()){
                    val existingText = binding.etInputText.text.toString()
                    val newText = speechToTextResult[0]
                    val finalText = "$existingText $newText".trim()

                    binding.etInputText.setText(finalText)
                    binding.etInputText.setSelection(finalText.length) // Move cursor to end
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

        })
    }
}
