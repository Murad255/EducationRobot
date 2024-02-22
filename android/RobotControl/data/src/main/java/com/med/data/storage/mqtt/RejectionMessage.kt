package com.med.data.storage.mqtt

import java.util.*

/**
 * Check received messages on repeat
 */
class RejectionMessage (private val uidListMaxSize: Long = 100) {
    private val pastMessagaUid: Queue<Long> =
        LinkedList<Long>() // список Uid прошлых сообщений для отбраковки


    fun Check(uid: Long): Boolean {
        if (pastMessagaUid.size > uidListMaxSize) {
            pastMessagaUid.poll()
        }
        return pastMessagaUid.filter { mu -> mu.equals(uid) }.isEmpty()
    }
}