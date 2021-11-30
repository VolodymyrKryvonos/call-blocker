package com.call_blocke.app

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var taskManager: TaskManager
    private lateinit var taskRepository: TaskRepository

    @Before
    fun init() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        RepositoryImp.init(appContext)

        taskRepository = RepositoryImp.taskRepository

        taskManager = TaskManager(context = appContext)
    }

    fun runShellCommand(command: String) {
        // Run the command
        val process = Runtime.getRuntime().exec(command)
        val bufferedReader = BufferedReader(
            InputStreamReader(process.inputStream)
        )

        // Grab the results
        val log = StringBuilder()
        var line: String?
        line = bufferedReader.readLine()
        while (line != null) {
            log.append(line + "\n")
            line = bufferedReader.readLine()
        }
        val Reader = BufferedReader(
            InputStreamReader(process.errorStream)
        )

        // if we had an error during ex we get here
        val error_log = StringBuilder()
        var error_line: String?
        error_line = Reader.readLine()
        while (error_line != null) {
            error_log.append(error_line + "\n")
            error_line = Reader.readLine()
        }
        if (error_log.toString() != "")
            Log.i("ADB_COMMAND", "command : $command $log error $error_log")
        else
            Log.i("ADB_COMMAND", "command : $command $log")
    }

    @Test
    fun killVodafone() = runBlocking(Dispatchers.IO) {
        println("start test ")

        val receiver = "+380666131032"

        var errorCount = 0
        var successCount = 0

        repeat(100) {
            println("onStart task $it")

            val task = TaskEntity(
                id = it,
                sendTo = receiver,
                simSlot = 0,
                message = "Hey sms num $it, successCount: $successCount, errorCount $errorCount"
            )

            taskRepository.save(task)

            if (taskManager.doTask(task)) {
                successCount++
            } else {
                errorCount++
            }

            println("onDone task $it")
        }

        println("successCount: $successCount ")
        println("errorCount $errorCount")
    }


}