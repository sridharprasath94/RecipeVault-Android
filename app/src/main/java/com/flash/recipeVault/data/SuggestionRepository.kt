package com.flash.recipeVault.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SuggestionsRepository(
    private val dao: SuggestionDao,
    private val base: List<String> = listOf(
        // Vegetables
        "Onion", "Red Onion", "White Onion", "Spring Onion", "Shallot",
        "Tomato", "Cherry Tomato", "Plum Tomato", "Sun-dried Tomato",
        "Potato", "Baby Potato", "Sweet Potato",
        "Ginger", "Garlic",
        "Green Chili", "Red Chili", "Jalapeno", "Serrano Chili",
        "Bell Pepper", "Red Bell Pepper", "Green Bell Pepper", "Yellow Bell Pepper",
        "Carrot", "Beetroot", "Radish", "Turnip",
        "Cucumber", "Zucchini", "Eggplant", "Brinjal",
        "Okra", "Lady Finger",
        "Cauliflower", "Cabbage", "Red Cabbage",
        "Broccoli", "Brussels Sprouts",
        "Spinach", "Baby Spinach", "Kale",
        "Fenugreek Leaves", "Methi Leaves",
        "Coriander Leaves", "Mint Leaves", "Curry Leaves",
        "Bottle Gourd", "Ridge Gourd", "Bitter Gourd",
        "Drumstick", "Pumpkin",
        "Mushroom", "Button Mushroom", "Shiitake Mushroom",
        "Corn", "Baby Corn",
        "Green Beans", "French Beans",
        "Peas", "Snow Peas",
        "Spring Greens",

        // Fruits
        "Apple", "Green Apple",
        "Banana",
        "Mango", "Raw Mango",
        "Pineapple",
        "Papaya",
        "Orange", "Lemon", "Lime",
        "Grapes", "Red Grapes", "Green Grapes",
        "Strawberry", "Blueberry",
        "Pomegranate",
        "Watermelon",
        "Coconut", "Grated Coconut",

        // Grains & Cereals
        "Basmati Rice", "Jasmine Rice", "Brown Rice",
        "Sona Masuri Rice",
        "Wheat Flour", "All-purpose Flour", "Maida",
        "Rice Flour",
        "Semolina", "Rava",
        "Oats", "Rolled Oats",
        "Quinoa",
        "Millet",
        "Barley",
        "Cornmeal",

        // Pulses & Beans
        "Toor Dal", "Moong Dal", "Masoor Dal",
        "Chana Dal", "Urad Dal",
        "Black Gram",
        "Chickpeas", "Black Chickpeas",
        "Kidney Beans", "Rajma",
        "Black Beans", "White Beans",
        "Lentils",

        // Dairy & Alternatives
        "Milk", "Full Cream Milk",
        "Curd", "Yogurt", "Greek Yogurt",
        "Paneer",
        "Cheese", "Cheddar Cheese", "Mozzarella",
        "Parmesan Cheese",
        "Butter", "Ghee",
        "Cream", "Fresh Cream",
        "Coconut Milk",

        // Oils & Fats
        "Oil", "Vegetable Oil",
        "Sunflower Oil",
        "Olive Oil", "Extra Virgin Olive Oil",
        "Mustard Oil",
        "Coconut Oil",
        "Sesame Oil",

        // Spices & Seasonings
        "Salt", "Sea Salt", "Rock Salt", "Black Salt",
        "Sugar", "Brown Sugar", "Jaggery",
        "Black Pepper", "White Pepper",
        "Red Chili Powder", "Kashmiri Chili Powder",
        "Turmeric Powder",
        "Cumin Seeds", "Cumin Powder",
        "Coriander Seeds", "Coriander Powder",
        "Mustard Seeds",
        "Fenugreek Seeds",
        "Fennel Seeds",
        "Nigella Seeds",
        "Cloves",
        "Cardamom", "Green Cardamom", "Black Cardamom",
        "Cinnamon",
        "Bay Leaf",
        "Star Anise",
        "Nutmeg", "Mace",
        "Asafoetida (Hing)",
        "Garam Masala",
        "Chaat Masala",
        "Curry Powder",
        "Sambar Powder",
        "Rasam Powder",
        "Biryani Masala",
        "Italian Seasoning",
        "Five Spice Powder",

        // Herbs
        "Basil", "Thai Basil",
        "Oregano",
        "Parsley",
        "Rosemary",
        "Thyme",
        "Dill",
        "Sage",

        // Sauces & Condiments
        "Soy Sauce", "Light Soy Sauce", "Dark Soy Sauce",
        "Oyster Sauce",
        "Hoisin Sauce",
        "Fish Sauce",
        "Chili Sauce", "Chili Garlic Sauce",
        "Sriracha",
        "Tomato Ketchup",
        "Mustard", "Dijon Mustard",
        "Mayonnaise",
        "Vinegar", "Apple Cider Vinegar",
        "Balsamic Vinegar",
        "Rice Vinegar",

        // Nuts & Seeds
        "Cashew Nuts",
        "Almonds",
        "Walnuts",
        "Peanuts",
        "Pistachios",
        "Raisins",
        "Sesame Seeds",
        "Sunflower Seeds",
        "Pumpkin Seeds",

        // Meat & Protein
        "Egg",
        "Chicken",
        "Chicken Breast",
        "Chicken Thighs",
        "Mutton",
        "Lamb",
        "Beef",
        "Pork",
        "Fish",
        "Salmon",
        "Tuna",
        "Prawns",
        "Shrimp",
        "Tofu",

        // Bakery & Extras
        "Bread",
        "Breadcrumbs",
        "Yeast",
        "Baking Powder",
        "Baking Soda",
        "Vanilla Extract",
        "Cocoa Powder",
        "Chocolate",
        "Dark Chocolate",
        "Honey",
        "Maple Syrup",

        // Misc
        "Tamarind",
        "Tamarind Paste",
        "Lemon Juice",
        "Lime Juice",
        "Stock",
        "Vegetable Stock",
        "Chicken Stock"
    ),
    private val baseByType: Map<SuggestionType, List<String>> = emptyMap()
) {

    fun observeAllMerged(type: SuggestionType): Flow<List<String>> {
        val base = baseByType[type].orEmpty()

        return dao.observeAll(type).map { dbList ->
            (base + dbList.map { it.value })
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sorted()
                .toList()
        }
    }

    /** Prefix search from Room only (fast + offline). */
    suspend fun searchPrefix(type: SuggestionType, query: String, limit: Int = 20): List<String> {
        val p = query.trim().lowercase()
        if (p.isBlank()) return emptyList()

        return dao.searchPrefix(type = type, prefix = p, limit = limit)
            .map { it.value }
    }

    /** Persist a user-added suggestion (IGNORE duplicates). */
    suspend fun add(type: SuggestionType, value: String) {
        val clean = value.trim()
        if (clean.isBlank()) return

        val lower = clean.lowercase()
        dao.insert(
            SuggestionEntity(
                key = "${type.name}:$lower",
                type = type,
                value = clean,
                valueLower = lower,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /** One-time seeding. Call this once on app startup (or after DB creation). */
    suspend fun seedDefaultsIfEmpty(type: SuggestionType, defaults: List<String>) {
        // If at least 1 exists, skip seeding
        val existing = dao.searchPrefix(type, prefix = "", limit = 1)
        if (existing.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val items = defaults
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .map { v ->
                val lower = v.lowercase()
                SuggestionEntity(
                    key = "${type.name}:$lower",
                    type = type,
                    value = v,
                    valueLower = lower,
                    createdAt = now
                )
            }

        dao.insertAll(items)
    }

    suspend fun addFromTriples(
        type: SuggestionType,
        values: List<String?>
    ) {
        values
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { value ->
                add(type, value)
            }
    }
}