package com.bayramapuhan.phonecleaner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.domain.model.CategoryType
import com.bayramapuhan.phonecleaner.ui.theme.CatApps
import com.bayramapuhan.phonecleaner.ui.theme.CatAudio
import com.bayramapuhan.phonecleaner.ui.theme.CatImages
import com.bayramapuhan.phonecleaner.ui.theme.CatOther
import com.bayramapuhan.phonecleaner.ui.theme.CatVideos

fun CategoryType.color(): Color = when (this) {
    CategoryType.IMAGES -> CatImages
    CategoryType.VIDEOS -> CatVideos
    CategoryType.AUDIO -> CatAudio
    CategoryType.APPS -> CatApps
    CategoryType.OTHER -> CatOther
}

@Composable
fun CategoryType.label(): String = stringResource(
    when (this) {
        CategoryType.IMAGES -> R.string.storage_category_images
        CategoryType.VIDEOS -> R.string.storage_category_videos
        CategoryType.AUDIO -> R.string.storage_category_audio
        CategoryType.APPS -> R.string.storage_category_apps
        CategoryType.OTHER -> R.string.storage_category_other
    },
)
