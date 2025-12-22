package com.example.benassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.benassistant.agent.AlarmAgentImpl
import com.example.benassistant.agent.MapAgentImpl
import com.example.benassistant.llm.LlmRouterFactory
import com.example.benassistant.repository.ChatRepositoryImpl
import com.example.benassistant.room.AppDatabase
import com.example.benassistant.room.ChatLog
import com.example.benassistant.R

class MainActivity : AppCompatActivity(), MainContract.View {

    private lateinit var presenter: MainContract.Presenter

    private lateinit var rootView: ConstraintLayout
    private lateinit var inputLayout: LinearLayout
    private lateinit var etInput: EditText
    private lateinit var btnRemove: TextView
    private lateinit var btnVoice: ImageButton
    private lateinit var btnSend: ImageButton
    private lateinit var recyclerView: RecyclerView

    private lateinit var voiceInputManager: VoiceInputManager

    // Loading overlay
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: ChatAdapter

    companion object {
        private const val REQ_RECORD_AUDIO = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val llmRouter = LlmRouterFactory.create(this)
        presenter = MainPresenter(
            repository = ChatRepositoryImpl(
                llmRouter = llmRouter,
                chatDao = AppDatabase.getInstance(this).chatLogDao(),
                alarmAgent = AlarmAgentImpl(this),
                mapAgent = MapAgentImpl(this)
            )
        )
        presenter.attach(this)

        // Khởi tạo VoiceInputManager
        voiceInputManager = VoiceInputManager(
            context = this,
            onResult = { text ->
                setMicListening(false)
                presenter.onUserSendMessage(text)
            },
            onError = { error ->
                setMicListening(false)
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )

        initView()
        firstSetup()
        setupLoading()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        rootView = findViewById(R.id.main)
        recyclerView = findViewById(R.id.recyclerViewChat)
        inputLayout = findViewById(R.id.inputLayout)
        etInput = findViewById(R.id.etInput)
        btnRemove = findViewById(R.id.btnRemove)
        btnVoice = findViewById(R.id.btnVoice)
        btnSend = findViewById(R.id.btnSend)

        // Khởi tạo adapter
        adapter = ChatAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        etInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    btnSend.visibility = View.GONE
                    btnVoice.visibility = View.VISIBLE
                } else {
                    btnSend.visibility = View.VISIBLE
                    btnVoice.visibility = View.GONE
                }
            }
        })

        btnRemove.setOnClickListener {
            presenter.removeAll()
        }

        recyclerView.setOnClickListener {
            hideKeyboard()
        }

        btnVoice.setOnClickListener {
            if (hasAudioPermission()) {
                setMicListening(true)
                voiceInputManager.startListening()
            } else {
                requestAudioPermission()
            }
        }
        btnSend.setOnClickListener {
            presenter.onUserSendMessage(etInput.text.toString())
        }
    }

    fun setMicListening(isListening: Boolean) {
        // Switch background via resource id
        if (isListening) {
            btnVoice.background = ContextCompat.getDrawable(this, R.drawable.ic_micro_listening)
            btnVoice.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.anim_micro_listening)
            )
        } else {
            btnVoice.background = ContextCompat.getDrawable(this,R.drawable.ic_micro_idle)
            btnVoice.clearAnimation()
        }
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQ_RECORD_AUDIO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_RECORD_AUDIO) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                voiceInputManager.startListening()
                setMicListening(true)
            } else {
                Toast.makeText(this, "Audio permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firstSetup() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            // 1. Padding cho status + navigation bar
            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            // 2. Tính toán padding bottom cho recyclerview
            val extraPadding = (16 * resources.displayMetrics.density).toInt()
            var bottomPadding = 0

            // 3. Đẩy inputLayout theo bàn phím
            var translationY: Float
            if (imeInsets.bottom > 0) {
                bottomPadding += imeInsets.bottom + 3 * extraPadding
                translationY = -(imeInsets.bottom - systemBars.bottom).toFloat()
            } else {
                bottomPadding += inputLayout.height + 2 * extraPadding
                translationY = 0f
            }
            // 4. Set padding for rcvChat
            recyclerView.setPadding(
                recyclerView.paddingLeft,
                recyclerView.paddingTop,
                recyclerView.paddingRight,
                bottomPadding
            )

            inputLayout.animate()
                .translationY(translationY)
                .setDuration(150)
                .start()

            scrollToBottom()
            insets
        }

        etInput.post {
            etInput.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // Create a full-screen overlay with a centered ProgressBar
    private fun setupLoading() {
        loadingOverlay = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(0x66000000) // semi-transparent black
            visibility = View.GONE
            isClickable = true
            isFocusable = true
        }

        progressBar = ProgressBar(this).apply {
            val size = (48 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
        }

        loadingOverlay.addView(progressBar)

        // Add overlay to root container
        if (rootView.parent is FrameLayout) {
            (rootView.parent as FrameLayout).addView(loadingOverlay)
        } else {
            // Wrap rootView inside a FrameLayout to host the overlay
            val content = rootView.parent as FrameLayout
            content.addView(loadingOverlay)
        }
    }

    override fun showLoading() {
        hideKeyboard()
        loadingOverlay.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        scrollToBottom()
        loadingOverlay.visibility = View.GONE
    }

    override fun addMessage(message: ChatLog) {
        adapter.addMessage(message)
    }

    override fun showError(message: String) {
        //addMessage(ChatLog(role = Role.ASSISTANT, message = "Có lỗi xảy ra"))
        scrollToBottom()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessages(messages: List<ChatLog>?) {
        adapter.submitList(messages)
        scrollToBottom()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: etInput
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun clearInput() {
        etInput.text.clear()
    }

    override fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceInputManager.release()
        presenter.detach()
    }
}
