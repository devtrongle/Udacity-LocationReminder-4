package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup(){
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries()
            .build()

        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() = database.close()

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    @Test
    fun test_for_SaveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO(
            title = "Title test",
            description = "Description test",
            location = "Location test",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id)

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success


        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.location, `is`(reminder.location))
    }

    @Test
    fun test_for_SaveReminder_retrievesReminders() = runBlocking {
        val reminder = ReminderDTO(
            title = "Title test",
            description = "Description test",
            location = "Location test",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminders()

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success


        assertThat(result.data.isEmpty(), `is`(false))
        assertThat(result.data.first().id, `is`(reminder.id))
        assertThat(result.data.first().title, `is`(reminder.title))
        assertThat(result.data.first().description, `is`(reminder.description))
        assertThat(result.data.first().latitude, `is`(reminder.latitude))
        assertThat(result.data.first().longitude, `is`(reminder.longitude))
        assertThat(result.data.first().location, `is`(reminder.location))
    }

}