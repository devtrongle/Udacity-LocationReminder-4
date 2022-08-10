package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.RESULT_ERROR
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P)
class RemindersListViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var application: Application
    private val reminders: MutableList<ReminderDTO> = mutableListOf()

    @Before
    fun setup() {
        reminders.add(
            ReminderDTO(
                title = "Title test 1",
                description = "Description test 1",
                location = "Location test 1",
                latitude = 10.819689728300116,
                longitude = 106.65901825254176
            )
        )
        reminders.add(
            ReminderDTO(
                title = "Title test 2",
                description = "Description test 2",
                location = "Location test 2",
                latitude = 10.819689728300116,
                longitude = 106.65901825254176
            )
        )
        fakeDataSource = FakeDataSource()
        application = ApplicationProvider.getApplicationContext()
        remindersListViewModel = RemindersListViewModel(application, fakeDataSource)
    }

    @Test
    fun test_for_loadReminders_ResultError() {
        fakeDataSource.setShouldReturnError(true)

        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`(RESULT_ERROR)
        )
    }

    @Test
    fun test_for_loadReminders_ResultSuccessHasData() = runBlockingTest {
        fakeDataSource.setShouldReturnError(false)
        fakeDataSource.saveReminders(reminders)

        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
        MatcherAssert.assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
        MatcherAssert.assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue(),
            `is`(reminders.map { reminder ->
                ReminderDataItem(
                    reminder.title,
                    reminder.description,
                    reminder.location,
                    reminder.latitude,
                    reminder.longitude,
                    reminder.id
                )
            })
        )
    }

    @Test
    fun test_for_loadReminders_ResultSuccessNoData() = runBlockingTest {
        fakeDataSource.setShouldReturnError(false)
        fakeDataSource.deleteAllReminders()
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

        MatcherAssert.assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
        MatcherAssert.assertThat(remindersListViewModel.remindersList.getOrAwaitValue(), `is`(emptyList()))
    }
}