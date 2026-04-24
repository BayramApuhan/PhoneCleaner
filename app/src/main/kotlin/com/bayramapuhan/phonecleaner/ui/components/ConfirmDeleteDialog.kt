package com.bayramapuhan.phonecleaner.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bayramapuhan.phonecleaner.R
import com.bayramapuhan.phonecleaner.util.formatSize

@Composable
fun ConfirmDeleteDialog(
    count: Int,
    totalBytes: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_delete_title)) },
        text = {
            Text(
                stringResource(
                    R.string.confirm_delete_body,
                    count,
                    totalBytes.formatSize(),
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.action_delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
