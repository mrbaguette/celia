package io.konduct.celia

import android.app.Notification
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import io.konduct.celia.ui.account.AccountFragment
import io.konduct.celia.ui.account.AccountViewModel
import io.konduct.celia.ui.account.AccountViewModelFactory
import android.media.RingtoneManager
import android.speech.tts.TextToSpeech
import java.util.*
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build


const val CHANNEL_ID = "default"


class AccountActivity : FragmentActivity() {


    private lateinit var viewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, AccountViewModelFactory)
                .get(AccountViewModel::class.java)

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val r = RingtoneManager.getRingtone(applicationContext, notification)

        viewModel.alarmFct = {
            r.play()
        }

        viewModel.stopAlarmFct = {
            r.stop()
        }

        val t1 = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {

            }
        })
        t1.language = Locale.US
        var t1Bundle = Bundle()
        t1Bundle.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)

        viewModel.notificationFct = { txt ->
            t1.speak(txt, TextToSpeech.QUEUE_FLUSH, t1Bundle,null)
        }


        createNotificationChannel()

        val nb = Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Timer")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_PROGRESS)

//                .setContentText(textContent)

        val notificationManager = getSystemService(NotificationManager::class.java)
        viewModel.updateNotification = {txt ->
            nb.setContentText(txt)
            notificationManager?.notify(0, nb.build())
        }

        viewModel.closeNotification = {
            notificationManager?.cancel(0)
        }
        setContentView(R.layout.account_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AccountFragment.newInstance())
                    .commitNow()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "default" //getString(R.string.channel_name)
            val description = "Timer" //getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.enableLights(false)
            channel.enableVibration(false)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}
