package com.jobdori.common.sequence

import java.net.NetworkInterface
import java.security.SecureRandom
import java.time.Instant

class SnowflakeIdGenerator : IdGenerator {

    private val nodeId: Long
    private val customEpoch: Long

    @Volatile
    private var lastTimestamp = -1L

    @Volatile
    private var sequence = 0L

    init {
        nodeId = createNodeId()
        customEpoch = DEFAULT_CUSTOM_EPOCH
    }

    @Synchronized
    override fun nextId(): Long {
        var currentTimestamp = timestamp()
        check(currentTimestamp >= lastTimestamp) { "Invalid System Clock! current: ($currentTimestamp) last: ($lastTimestamp)" }
        if (currentTimestamp == lastTimestamp) {
            sequence = sequence + 1 and MAX_SEQUENCE
            if (sequence == 0L) {
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            sequence = 0
        }
        lastTimestamp = currentTimestamp
        return (currentTimestamp shl NODE_ID_BITS + SEQUENCE_BITS or (nodeId shl SEQUENCE_BITS) or sequence)
    }

    private fun timestamp(): Long {
        return Instant.now()
            .toEpochMilli() - customEpoch
    }

    private fun waitNextMillis(currentTimestamp: Long): Long {
        var timestamp = currentTimestamp
        while (timestamp == lastTimestamp) {
            timestamp = timestamp()
        }
        return timestamp
    }

    private fun createNodeId(): Long {
        val nodeId = try {
            val sb = StringBuilder()
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val mac = networkInterface.hardwareAddress
                if (mac != null) {
                    for (macPort in mac) {
                        sb.append(String.format("%02X", macPort))
                    }
                }
            }
            sb.toString()
                .hashCode()
                .toLong()
        } catch (exception: Exception) {
            SecureRandom().nextInt()
                .toLong()
        }
        return nodeId and MAKE_NODE_ID
    }

    override fun toString(): String {
        return (
            "SnowflakeIdGenerator Settings [EPOCH_BITS=" + EPOCH_BITS + ", NODE_ID_BITS=" + NODE_ID_BITS +
                ", SEQUENCE_BITS=" + SEQUENCE_BITS + ", CUSTOM_EPOCH=" + customEpoch +
                ", NodeId=" + nodeId + "]"
            )
    }

    companion object {
        private const val EPOCH_BITS = 41
        private const val NODE_ID_BITS = 10
        private const val SEQUENCE_BITS = 12
        private const val MAKE_NODE_ID = (1L shl NODE_ID_BITS) - 1
        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1

        // Custom Epoch (2020-01-01T00:00:00Z)
        private const val DEFAULT_CUSTOM_EPOCH = 1_577_836_800_000L
    }

}
