package io.konduct.celia.conf

import com.google.firebase.Timestamp

class Conf(var playTimeLeft: Int = 60 * 60 * 10, var lastTimeCheck: Timestamp = Timestamp.now())