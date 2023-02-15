package com.yatik.qrscanner.ui

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.yatik.qrscanner.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@MediumTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class MainActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun pressHistoryButton_navigatesToHistoryFragment() {

        launch(MainActivity::class.java)

        onView(withId(R.id.history)).perform(click())
        onView(withId(R.id.history_fragment)).check(matches(isDisplayed()))

        pressBack()
        onView(withId(R.id.main_layout)).check(matches(isDisplayed()))

    }

}