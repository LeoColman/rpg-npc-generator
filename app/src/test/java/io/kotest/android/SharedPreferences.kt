package io.kotest.android

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider

inline fun <reified T: Any> withSharedPreferences(
    sharedPreferencesName: String,
    key: String,
    value: T,
    context: Context = ApplicationProvider.getApplicationContext(),
    mode: Int = Context.MODE_PRIVATE,
    block: (context: Context) -> Unit
) {
    val sharedPreferences = context.getSharedPreferences(sharedPreferencesName, mode)
    val previous = sharedPreferences.getPrevious<T>(key)

    sharedPreferences.edit().put(key, value).commit()

    block(context)

    sharedPreferences.edit().put(key, previous).commit()
}

inline fun <reified T> SharedPreferences.getPrevious(key: String): T? {
    return if (contains(key)) {
        when (T::class) {
            String::class -> getString(key, null)
            MutableSet::class -> getStringSet(key, null)
            Boolean::class -> getBoolean(key, false)
            Int::class -> getInt(key, 0)
            Float::class -> getFloat(key, 0f)
            Long::class -> getLong(key, 0)
            else -> throw IllegalArgumentException()
        } as T
    } else null
}

inline fun <reified T> SharedPreferences.Editor.put(key: String, value: T?): SharedPreferences.Editor {
    return if(value == null) remove(key) else {
        when (value) {
            is String -> putString(key, value)
            is MutableSet<*> -> putStringSet(key, value as MutableSet<String>)
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            else -> throw IllegalArgumentException()
        }
    }
}
