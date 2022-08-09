package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false

    fun setShouldReturnError(shouldReturn: Boolean) {
        this.shouldReturnError = shouldReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(
                RESULT_ERROR
            )
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    fun saveReminders(r: MutableList<ReminderDTO>) {
        reminders.addAll(r)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val result = reminders.firstOrNull { it.id == id}
        if (shouldReturnError) {
            return Result.Error(
                RESULT_ERROR
            )
        } else {
            result?.let {  return Result.Success(it) }
        }
        return Result.Error(RESULT_ERROR)
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}

const val RESULT_ERROR = "Reminders not found!"