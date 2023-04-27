package com.example.myapplication


import android.util.Log
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException

/**
 * Create a new client socket manager
 */
class ClientSocket(private val addr: String, private val port: Int,
                   private val mMessageListener: OnMessageReceived,
                   private val terminate: Runnable,
                   private val firstData:String? = null,
                   private val lastData:String? = null) {
    private var isRunning = false

    // used to send messages
    private var mBufferOut: PrintWriter? = null
    // used to read messages from the server
    private var mBufferIn: BufferedReader? = null


    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    fun sendMessage(message: String?) {
        if (message != null && mBufferOut != null && !mBufferOut!!.checkError()) {
            Log.i(tag, "sending message")
            mBufferOut!!.println(message)
            mBufferOut!!.flush()
        }
    }

    /**
     * Close the connection
     */
    fun stopClient() {
        sendMessage(lastData)
        isRunning = false

        if (mBufferOut != null) {
            mBufferOut!!.flush()
            mBufferOut!!.close()
        }
        mBufferIn = null
        mBufferOut = null
        terminate.run()
    }

    fun run() {
        isRunning = true
        try {
            val serverAddr = InetAddress.getByName(addr)
            Log.i(tag, "Connecting...")
            val socket = Socket(serverAddr, port)
            try {
                mBufferOut = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                mBufferIn = BufferedReader(InputStreamReader(socket.getInputStream()))

                sendMessage(firstData)

                while (isRunning) {
                    val msg = mBufferIn!!.readLine()
                    Log.e(tag, "S: Received Message: '$msg'")
                    if(msg != null){
                        mMessageListener.messageReceived(msg)
                    }else {
                        stopClient()
                        return
                    }
                }


            } catch (e: Exception) {
                Log.e(tag, "S: Error", e)
            } finally {
                socket.close()
                terminate.run()
            }
        } catch (ex: UnknownHostException) {
            Log.e(tag, "UnknownHostException", ex)
        } catch (ex: IOException) {
            Log.e(tag, "IOException", ex)
        } catch (ex: SecurityException) {
            Log.e(tag, "SecurityException", ex)
        } catch (ex: IOException) {
            Log.e(tag, "IOException", ex)
        } catch (ex: IllegalArgumentException) {
            Log.e(tag, "IllegalArgumentException", ex)
        } catch (ex: Exception) {
            Log.e(tag, "C: Error", ex)
        }finally {
            terminate.run()
        }

    }

    interface OnMessageReceived {
        fun messageReceived(message: String)
    }

    companion object {
        private const val tag = "TAD::SocketClient"
    }
}