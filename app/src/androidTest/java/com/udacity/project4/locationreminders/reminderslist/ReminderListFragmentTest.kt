package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.get
import org.mockito.AdditionalMatchers.not
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.matches
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

//    TODO: add testing for the error messages.

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = GlobalContext.get().koin.get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun test_for_ClickFab_NavigateToSaveReminderFragment() = runBlockingTest {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navigationController = mock(NavController::class.java)

        scenario.onFragment { Navigation.setViewNavController(it.view!!, navigationController) }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navigationController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun test_for_DataTextViewDisplayed_NoData() = runBlockingTest {
        repository.deleteAllReminders()
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun test_for_remindersRecyclerViewDisplayed_HasData() = runBlockingTest {
        repository.deleteAllReminders()
        val reminder1 = ReminderDTO(
            title = "Title test 1",
            description = "Description test 1",
            location = "Location test 1",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        val reminder2 = ReminderDTO(
            title = "Title test 2",
            description = "Description test ",
            location = "Location test 2",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).check(ViewAssertions.matches(hasChildCount(2)))
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(CoreMatchers.not(isDisplayed())))
        onView(withText(reminder1.title)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminder1.description)).check(ViewAssertions.matches(isDisplayed()))
        onView(withText(reminder1.location)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun test_for_DataTextViewGone_HasData() = runBlockingTest {
        repository.deleteAllReminders()
        val reminder = ReminderDTO(
            title = "Title test",
            description = "Description test",
            location = "Location test",
            latitude = 10.819689728300116,
            longitude = 106.65901825254176)
        repository.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(ViewAssertions.matches(CoreMatchers.not(isDisplayed())))
    }

}