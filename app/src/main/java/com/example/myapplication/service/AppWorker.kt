package com.example.myapplication.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.util.Log
import androidx.work.*
import com.google.common.util.concurrent.*
import java.util.concurrent.TimeUnit


class AppWorker(appContext: Context, params: WorkerParameters) :
    ListenableWorker(appContext, params) {

    override fun startWork(): ListenableFuture<Result> {
        // Do your work here.
        log("starting background work")
        INSTANCE = this

//        val executorService: ListeningExecutorService =
//            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())
//        return executorService.submit<Result>(
//            Callable<Result> {
//                val ussdCode = "*123#" // replace with your USSD code
//
//                val encodedUssd = Uri.encode(ussdCode)
//
//                val ussdLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
//                    StartActivityForResult()
//                ) { result ->
//                    if (result.getResultCode() === Activity.RESULT_OK) {
//                        val data: Intent = result.getData()
//                        if (data != null) {
//                            val ussdResponse =
//                                data.getStringExtra("com.android.phone.extra.USSD_MESSAGE")
//                            // handle the USSD response message
//                            if (ussdResponse!!.contains("Enter your PIN")) {
//                                sendUssdResponse("1234")
//                            } else if (ussdResponse!!.contains("Enter amount")) {
//                                sendUssdResponse("100")
//                            } else if (ussdResponse!!.contains("Confirm payment?")) {
//                                sendUssdResponse("1")
//                            } else if (ussdResponse!!.contains("Payment successful")) {
//                                // end the USSD session
//                            } else {
//                                // handle unknown messages or errors
//                            }
//                        }
//                    }
//                }
//
//                val intent = Intent("android.intent.action.CALL", Uri.parse("tel:$encodedUssd"))
//                intent.putExtra("com.android.phone.extra.slot", 0)
//                intent.putExtra("com.android.phone.extra.using_sim_index", 0)
//                ussdLauncher.launch(intent)
//
//                log("done ussd task")
//                Result.success()
//            }
//        )


        return ListenableFutureTask.create(object : AppWorkerTask() {
            override val ctx: Context
                get() = applicationContext

            override fun terminate(response: String, succeeded: Boolean) {
                cancel(ctx)
            }
        }, Result.failure())


    }

    override fun onStopped() {
        // Cleanup because you are being stopped.
        log("stop backGroundWork before complete")
        INSTANCE = null
    }
//
//    private fun doWork() {
//        Thread(object : AppWorkerTask<Result>() {
//            override val ctx: Context
//                get() = applicationContext
//
//            override fun terminate(response: String): Result {
//                cancel(ctx)
//                return Result.success()
//            }
//        }).start()
//    }

    companion object {
        // uniquely identifies the job
        private const val APP_WORKER_ID = "app-main-worker"
        private const val tag = "TAD::AppWorker"

        @Volatile
        private var INSTANCE: AppWorker? = null

        private fun log(value: String) {
            Log.d(tag, value)
        }

        fun init(ctx: Context) {
            synchronized(this) {
                if (INSTANCE == null) {
                    log("instance is null")

                    val inputData = workDataOf("some_key" to "some_val")

                    val constraints: Constraints = Constraints.Builder().apply {
                        setRequiredNetworkType(NetworkType.CONNECTED)
                        setRequiresCharging(true)
                    }.build()

                    val request: PeriodicWorkRequest =
                        PeriodicWorkRequestBuilder<AppWorker>(15, TimeUnit.MINUTES)
                            // Sets the input data for the ListenableWorker
                            .setInputData(inputData)
                            // If you want to delay the start of work by 60 seconds
                            .setInitialDelay(60, TimeUnit.SECONDS)
                            // Set a backoff criteria to be used when retry-ing
                            .setBackoffCriteria(
                                BackoffPolicy.EXPONENTIAL,
                                30000,
                                TimeUnit.MILLISECONDS
                            )
                            // Set additional constraints
                            .setConstraints(constraints)
                            .build()

                    WorkManager.getInstance(ctx)
                        .enqueueUniquePeriodicWork(
                            APP_WORKER_ID,
                            ExistingPeriodicWorkPolicy.KEEP,
                            request
                        )
                } else {
                    log("instance not null")
//                    AppWorkerTask.sock?.stopClient()
//                    INSTANCE?.doWork()
                }
            }
        }

        fun cancel(ctx: Context) {
            WorkManager.getInstance(ctx).cancelUniqueWork(APP_WORKER_ID)
            log("Job Cancelled")
        }

        fun getInstance(): AppWorker? {
            return INSTANCE
        }
    }
}
