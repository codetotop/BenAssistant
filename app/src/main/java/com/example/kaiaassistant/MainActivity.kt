package com.example.kaiaassistant

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kaiaassistant.agent.AlarmAgentImpl
import com.example.kaiaassistant.agent.MapAgentImpl
import com.example.kaiaassistant.llm.LlmRouterFactory
import com.example.kaiaassistant.repository.ChatRepositoryImpl
import com.example.kaiaassistant.room.AppDatabase
import com.example.kaiaassistant.room.ChatLog
import com.example.kaiaassistant.room.Role

class MainActivity : AppCompatActivity(), MainContract.View {

    private lateinit var presenter: MainContract.Presenter

    private lateinit var rootView: ConstraintLayout
    private lateinit var inputLayout: LinearLayout
    private lateinit var etInput: EditText
    private lateinit var btnRecent: TextView
    private lateinit var btnVoice: ImageButton
    private lateinit var btnSend: ImageButton
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: ChatAdapter

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

        initView()
        firstSetup()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        rootView = findViewById(R.id.main)
        recyclerView = findViewById(R.id.recyclerViewChat)
        inputLayout = findViewById(R.id.inputLayout)
        etInput = findViewById(R.id.etInput)
        btnRecent = findViewById(R.id.btnRecent)
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
                    btnSend.visibility = android.view.View.GONE
                    btnVoice.visibility = android.view.View.VISIBLE
                } else {
                    btnSend.visibility = android.view.View.VISIBLE
                    btnVoice.visibility = android.view.View.GONE
                }
            }
        })

        btnRecent.setOnClickListener {
            presenter.loadAllMessages()
        }

        recyclerView.setOnClickListener {
            hideKeyboard()
        }

        btnVoice.setOnClickListener {

        }
        btnSend.setOnClickListener {
            presenter.onUserSendMessage(etInput.text.toString())
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

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun addMessage(message: ChatLog) {
        adapter.addMessage(message)
    }

    override fun showError(message: String) {
        addMessage(ChatLog(role = Role.ASSISTANT, message = "Có lỗi xảy ra"))
        scrollToBottom()
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessages(messages: List<ChatLog>) {
        adapter.submitList(messages)
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
        presenter.detach()
    }
}
