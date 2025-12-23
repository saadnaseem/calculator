package com.example.calculator

import android.content.Context
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.calculator.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorUiTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearPreferences() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            context.testHistoryDataStore.edit { prefs ->
                prefs.clear()
            }
        }
    }

    @Test
    fun basic_addition_displays_result() {
        tapKeys("1", "+", "2")
        tapEquals()

        composeRule.onNodeWithContentDescription("display_result")
            .assertTextContains("3", substring = false)
    }

    @Test
    fun precedence_multiplication_before_add() {
        tapKeys("1", "+", "2", "×", "3")
        tapEquals()

        composeRule.onNodeWithContentDescription("display_result")
            .assertTextContains("7", substring = false)
    }

    @Test
    fun trig_deg_mode_sin_30() {
        tapKeys("sin", "(", "3", "0", ")")
        tapEquals()

        composeRule.onNodeWithContentDescription("display_result")
            .assertTextContains("0.5", substring = false)
    }

    @Test
    fun history_flow_reloads_expression() {
        tapKeys("1", "+", "1")
        tapEquals()

        composeRule.onNodeWithContentDescription("open_history").performClick()
        composeRule.onNodeWithText("1+1").performClick()

        // Expression should reload; run again to confirm result stays 2
        tapEquals()
        composeRule.onNodeWithContentDescription("display_result")
            .assertTextContains("2", substring = false)
    }

    private fun tapKeys(vararg keys: String) {
        keys.forEach { label ->
            composeRule.onNodeWithTag("key_$label").performClick()
        }
    }

    private fun tapEquals() {
        composeRule.onNodeWithTag("key_=").performClick()
    }
}

// Local preferencesDataStore with the same name as production to clear state between tests.
private val Context.testHistoryDataStore by androidx.datastore.preferences.preferencesDataStore(
    name = "calculator_prefs"
)


