package com.strangerhelp.app.ui.screens.path

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.strangerhelp.app.data.model.PlaceResult
import com.strangerhelp.app.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAutocompleteField(
    label: String,
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<PlaceResult>,
    onSuggestionSelected: (PlaceResult) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    expanded = it.isNotEmpty()
                },
                label = { Text(label) },
                placeholder = { Text(placeholder, color = Muted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                    }
                },
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded && suggestions.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = suggestion.display_name,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onSuggestionSelected(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
