package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class SaveReminderViewModelTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var application: Application

    @Before
    fun setup(){
        fakeDataSource = FakeDataSource()
        application = ApplicationProvider.getApplicationContext()
        saveReminderViewModel = SaveReminderViewModel(application, fakeDataSource)
    }

    @Test
    fun test_for_ValidateEnteredData_NullTitle() = runBlockingTest {
        val reminder = ReminderDataItem(
            title = null,
            description = "Description test",
            location = "Location test",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        val result : Boolean = saveReminderViewModel.validateEnteredData(reminder)
        assertThat(result, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_enter_title))
    }

    @Test
    fun test_for_ValidateEnteredData_NullLocation() = runBlockingTest {
        val reminder = ReminderDataItem(
            title = "Title test",
            description = "Description test",
            location = null,
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        val result : Boolean = saveReminderViewModel.validateEnteredData(reminder)
        assertThat(result, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.value, `is`(R.string.err_select_location))
    }

    @Test
    fun test_for_ValidateEnteredData_ValidData() = runBlockingTest {
        val reminder = ReminderDataItem(
            title = "Title test",
            description = "Description test",
            location = "Location test",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        val result : Boolean = saveReminderViewModel.validateEnteredData(reminder)
        assertThat(result, `is`(true))
    }

    @Test
    fun test_for_SaveReminder() = runBlockingTest {
        val reminder = ReminderDataItem(
            title = "Title test",
            description = "Description test",
            location = "Location test",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        saveReminderViewModel.saveReminder(reminder)
        assertThat(saveReminderViewModel.showToast.value, `is`(application.getString(R.string.reminder_saved)))
    }
}