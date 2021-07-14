package com.instamojo.android.helpers

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

/**
 * a kotlin friendly mock that handles generics
 */
inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

inline fun <reified T> argumentCaptor(): ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)

private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)