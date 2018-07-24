package io.konduct.celia.ui.account

import android.util.Log
import androidx.databinding.Bindable
import androidx.databinding.ObservableInt
import androidx.databinding.adapters.Converters
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import io.konduct.celia.conf.Conf
import io.konduct.celia.util.Timer
import io.konduct.celia.util.fromTenthsToSeconds
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

const val THIRTY_MN = 30 * 60 * 10
const val TEN_MN = 10 * 60 * 10
const val ONE_MN = 1 * 60 * 10

class AccountViewModel(private val timer: Timer) : ViewModel() {
    /* Observable fields. When their values change (set method is called) they send updates to
    the UI automatically. */
    val playTimeLeft = ObservableInt(0) // tenths


    private val db = FirebaseFirestore.getInstance()
    private val accountsCollection = db.collection("accounts")
    private val confRef = accountsCollection.document("tony")


    init {

        confRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                var conf:Conf
                if (document.exists()) {
                    Log.d(this.javaClass.name, "DocumentSnapshot data: " + document.data!!)
                    conf = document.toObject(Conf::class.java)!!
                    val now = LocalDateTime.now()
                    val lastTimeCheck = LocalDateTime.ofEpochSecond(conf.lastTimeCheck.seconds, conf.lastTimeCheck.nanoseconds, ZoneOffset.UTC)
                    val d = ChronoUnit.DAYS.between(lastTimeCheck, now)
                    if (d > 0) {
                        conf.playTimeLeft += (60 * 60 * 10 * d).toInt()
                        val t = lastTimeCheck.plusDays(d)
                        conf.lastTimeCheck = Timestamp(t.toEpochSecond(ZoneOffset.UTC), 0)
                        confRef.set(conf)
                    }

                } else {
                    Log.d(this.javaClass.name, "No such document")
                    conf = Conf()
                    confRef.set(conf)
                }
                //tests
                //conf.playTimeLeft = TEN_MN + 2 * 10
                //conf.playTimeLeft = ONE_MN * 6 + 2 * 10
                //conf.playTimeLeft = ONE_MN + 2 * 10
                //conf.playTimeLeft =  2 * 10

                playTimeLeft.set(conf.playTimeLeft)

            } else {
                Log.d(this.javaClass.name, "get failed with ", task.exception)
            }
        }
    }


    private var state = TimerStates.STOPPED

    /**
     * Used to indicate to the UI that the timer is running and to receive start/pause commands.
     *
     * A @Bindable property is a more flexible way to create an observable field. Use it with
     * two-way data binding. When the property changes in the ViewModel,
     * `notifyPropertyChanged(BR.timerRunning)` must be called so the system fetches the new value
     * with the getter and notifies the observers.
     *
     * User actions come through the setter, using two-way data binding.
     */
    var timerRunning: Boolean
        @Bindable get() {
            return state == TimerStates.STARTED
        }
        set(value) {
            // These methods take care of calling notifyPropertyChanged()
            if (value) startButtonClicked() else stopButtonClicked()
        }


    /**
     * Resets timers and state. Called from the UI.
     */
    fun stopButtonClicked() {
        startedToStopped()
    }


    /**
     * Start the timer!
     */
    private fun startButtonClicked() {
        state = TimerStates.STARTED

        val task = object : TimerTask() {
            override fun run() {
                if (state == TimerStates.STARTED) {
                    updateCountdowns()
                }
            }
        }

        // Schedule timer every 100ms to update the counters.
        timer.start(task)
    }


    private fun updateCountdowns() {
        if (state == TimerStates.STOPPED) {
            return
        }

        val elapsed = timer.getElapsedTime()

        updateWorkCountdowns(elapsed)
    }


    private fun updateWorkCountdowns(elapsed: Long) {
        val oldTimeLeft = playTimeLeft.get()
        val newTimeLeft = oldTimeLeft - (elapsed / 100).toInt()
        if (newTimeLeft <= 0) {
            timerFinished()
        }
        else if (THIRTY_MN in oldTimeLeft..newTimeLeft) {
            Log.d(this.javaClass.name, "30 minutes")
            notificationFct?.invoke("30 minutes")
        }
        else if (TEN_MN in oldTimeLeft..newTimeLeft) {
            Log.d(this.javaClass.name, "10 minutes")
            notificationFct?.invoke("10 minutes")
        }
        else if ((oldTimeLeft < TEN_MN) && (oldTimeLeft / ONE_MN != newTimeLeft / ONE_MN)) {
            val t = oldTimeLeft / ONE_MN
            Log.d(this.javaClass.name, "%d minute%s".format(t, if (t > 1) "s" else ""))
            notificationFct?.invoke("%d minute%s".format(t, if (t > 1) "s" else ""))
        }

        //backup conf every 10 mn
        if (oldTimeLeft / TEN_MN != newTimeLeft / TEN_MN) {
            saveConf()
        }

        playTimeLeft.set(newTimeLeft.coerceAtLeast(0))
        updateNotification?.invoke(fromTenthsToSeconds(playTimeLeft.get()))
    }


    private fun timerFinished() {
        startedToStopped()
        Log.d(this.javaClass.name, "ALARM")
        alarmFct?.invoke(Unit)
    }

    var alarmFct:((Unit) -> Unit)? = null
    var stopAlarmFct:((Unit) -> Unit)? = null
    var notificationFct: ((String) -> Unit)? = null

    var updateNotification: ((String) -> Unit)? = null
    var closeNotification: ((Unit) -> Unit)? = null

    /* STARTED -> STOPPED */

    private fun startedToStopped() {
        state = TimerStates.STOPPED
        timer.reset()
        stopAlarmFct?.invoke(Unit)
        closeNotification?.invoke(Unit)
        saveConf()
    }

    private fun saveConf() {

        Log.d(this.javaClass.name, "Saving conf...")
        confRef.get().addOnSuccessListener { document ->
            var conf = if (document.exists()) document.toObject(Conf::class.java)!! else Conf()
            conf.playTimeLeft = playTimeLeft.get()
            confRef.set(conf)
            Log.d(this.javaClass.name, "Conf saved.")

        }
    }


}


enum class TimerStates {STOPPED, STARTED}
