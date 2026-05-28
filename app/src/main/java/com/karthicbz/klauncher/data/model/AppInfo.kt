package com.karthicbz.klauncher.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    val banner: Drawable? = null,
    val isHidden: Boolean = false,
    val categoryId: Long
)
