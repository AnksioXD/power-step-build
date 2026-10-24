package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.StarIntervalFrequency
import com.example.ui.viewmodel.StarStepViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StarStep", appName)
  }

  @Test
  fun `verify StarStepViewModel initial autonomous state`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = StarStepViewModel(app)
    assertNotNull(viewModel.starFrequency.value)
    assertTrue(viewModel.stepsUntilNextStar.value > 0)
    assertEquals(false, viewModel.isStarRushActive.value)
  }
}
