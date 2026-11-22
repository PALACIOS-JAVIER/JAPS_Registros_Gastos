package com.gastoclaro.ui.bubble

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.gastoclaro.MainActivity
import com.gastoclaro.R
import com.gastoclaro.core.di.ApplicationScope
import com.gastoclaro.core.notification.AppNotifications
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.PendingQuestionRepository
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.domain.usecase.AnswerQuestionUseCase
import com.gastoclaro.ui.theme.GastoClaroTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BubbleService : Service() {

    @Inject
    lateinit var pendingQuestionRepository: PendingQuestionRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var answerQuestion: AnswerQuestionUseCase

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    private val host = ServiceLifecycleHost()

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var expanded by mutableStateOf(false)
    private var queueJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        host.handleEvent(Lifecycle.Event.ON_CREATE)
        startAsForeground()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val view = ComposeView(this)
        view.setViewTreeLifecycleOwner(host)
        view.setViewTreeViewModelStoreOwner(host)
        view.setViewTreeSavedStateRegistryOwner(host)
        view.setContent {
            GastoClaroTheme {
                val questions by pendingQuestionRepository.observeQuestions()
                    .collectAsState(initial = emptyList())
                val categories by categoryRepository.observeCategories()
                    .collectAsState(initial = emptyList())
                BubbleContent(
                    expanded = expanded,
                    onExpandChange = ::updateExpanded,
                    questions = questions,
                    categories = categories,
                    onDragBy = ::onDragBy,
                    onDragEnd = ::onDragEnd,
                    onAnswer = { q, text -> appScope.launch { answerQuestion.answer(q, text) } },
                    onMarkInternal = { q -> appScope.launch { answerQuestion.markInternal(q) } },
                    onPickCategory = { q, id -> appScope.launch { answerQuestion.assignCategory(q, id, null) } },
                    onDismiss = { stopSelf() }
                )
            }
        }

        val (x, y) = defaultPosition()
        val params = WindowManager.LayoutParams(
            bubbleSizePx(),
            bubbleSizePx(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        windowManager.addView(view, params)
        composeView = view
        layoutParams = params

        restorePositionAsync()
        observeQueue()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        host.handleEvent(Lifecycle.Event.ON_START)
        return START_NOT_STICKY
    }

    private fun restorePositionAsync() {
        appScope.launch {
            val sx = settingsRepository.bubbleLastX.first()
            val sy = settingsRepository.bubbleLastY.first()
            val p = layoutParams ?: return@launch
            if (sx >= 0 && sy >= 0) {
                p.x = sx
                p.y = sy
                clampToScreen(p)
                runCatching { windowManager.updateViewLayout(composeView, p) }
            }
        }
    }

    private fun observeQueue() {
        queueJob = appScope.launch {
            pendingQuestionRepository.observeQuestions().collectLatest { questions ->
                if (questions.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    private fun startAsForeground() {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification: Notification = NotificationCompat.Builder(this, AppNotifications.CHANNEL_BUBBLE)
            .setSmallIcon(R.drawable.ic_stat_gastoclaro)
            .setContentTitle("GastoClaro")
            .setContentText("Preguntas pendientes")
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FOREGROUND_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(FOREGROUND_ID, notification)
        }
    }

    private fun updateExpanded(newExpanded: Boolean) {
        if (expanded == newExpanded) return
        expanded = newExpanded
        val p = layoutParams ?: return
        p.width = if (newExpanded) dp(300) else bubbleSizePx()
        p.height = if (newExpanded) dp(420) else bubbleSizePx()
        clampToScreen(p)
        runCatching { windowManager.updateViewLayout(composeView, p) }
    }

    private fun onDragBy(dx: Float, dy: Float) {
        val p = layoutParams ?: return
        p.x += dx.toInt()
        p.y += dy.toInt()
        runCatching { windowManager.updateViewLayout(composeView, p) }
    }

    private fun onDragEnd() {
        val p = layoutParams ?: return
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        if (p.y > screenHeight - dp(120)) {
            stopSelf()
            return
        }

        p.x = if (p.x + p.width / 2 < screenWidth / 2) 0 else screenWidth - p.width
        clampToScreen(p)
        runCatching { windowManager.updateViewLayout(composeView, p) }
        appScope.launch { settingsRepository.setBubblePosition(p.x, p.y) }
    }

    private fun clampToScreen(p: WindowManager.LayoutParams) {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        if (p.x < 0) p.x = 0
        if (p.y < 0) p.y = 0
        if (p.x + p.width > screenWidth) p.x = (screenWidth - p.width).coerceAtLeast(0)
        if (p.y + p.height > screenHeight) p.y = (screenHeight - p.height).coerceAtLeast(0)
    }

    private fun defaultPosition(): Pair<Int, Int> {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val size = bubbleSizePx()
        return (screenWidth - size - dp(8)) to (screenHeight / 3)
    }

    private fun bubbleSizePx(): Int = dp(56)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        queueJob?.cancel()
        composeView?.let { runCatching { windowManager.removeView(it) } }
        host.clear()
        host.handleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        private const val FOREGROUND_ID = 2001
    }
}
