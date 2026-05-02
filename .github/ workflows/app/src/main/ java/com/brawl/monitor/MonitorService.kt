package com.brawl.monitor

import android.net.VpnService
import android.os.ParcelFileDescriptor
import java.nio.ByteBuffer

class MonitorService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        vpnInterface = Builder()
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .setSession("BrawlMonitor")
            .build()

        Thread {
            val buffer = ByteBuffer.allocate(32767)
            while (true) {
                val length = vpnInterface?.fileDescriptor?.let { fd ->
                    android.system.Os.read(fd, buffer)
                } ?: 0
                if (length > 0) {
                    val dPort = ((buffer.get(22).toInt() and 0xff) shl 8) or (buffer.get(23).toInt() and 0xff)
                    if (dPort in 9330..9340) {
                        val ip = "${buffer.get(16).toInt() and 0xff}.${buffer.get(17).toInt() and 0xff}." +
                                 "${buffer.get(18).toInt() and 0xff}.${buffer.get(19).toInt() and 0xff}"
                        android.util.Log.d("BRAWL_SERVER", "IP: $ip")
                    }
                }
                buffer.clear()
            }
        }.start()
        return START_STICKY
    }
}
