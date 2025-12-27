package com.flash.recipeVault.ui

import SuggestionAutoCompleteField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flash.recipeVault.ui.theme.RecipeVaultTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SuggestionAutoCompleteFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun typingFilters_andSelectingItemSetsText() {
        composeRule.setContent {
            RecipeVaultTheme {
                var v by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                    mutableStateOf(TextFieldValue(""))
                }

                SuggestionAutoCompleteField(
                    value = v,
                    onValueChange = { v = it },
                    suggestions = listOf("Onion", "Garlic", "Tomato"),
                    label = "Ingredient",
                    matchMode = MatchMode.Contains,
                    showDropdownIcon = true
                )
            }
        }

        // Type a query
        composeRule.onNodeWithText("Ingredient").performTextInput("oni")

        // Suggestion appears
        composeRule.onNodeWithText("Onion").assertIsDisplayed()

        // Select it
        composeRule.onNodeWithText("Onion").performClick()

        // TextField now contains the selected item
        composeRule.onNodeWithText("Onion").assertIsDisplayed()
    }

    @Test
    fun whenNoMatch_menuDoesNotShow() {
        composeRule.setContent {
            RecipeVaultTheme {
                var v by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                    mutableStateOf(TextFieldValue(""))
                }
                SuggestionAutoCompleteField(
                    value = v,
                    onValueChange = { v = it },
                    suggestions = listOf("Onion", "Garlic"),
                    label = "Ingredient",
                    matchMode = MatchMode.Prefix,
                    showDropdownIcon = true
                )
            }
        }

        composeRule.onNodeWithText("Ingredient").performTextInput("zzz")

        // No suggestion item should appear.
        composeRule.onNodeWithText("Onion").assertDoesNotExist()
        composeRule.onNodeWithText("Garlic").assertDoesNotExist()
    }
}
