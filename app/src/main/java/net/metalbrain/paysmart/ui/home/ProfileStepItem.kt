package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.text.AnnotatedString
 import androidx.compose.ui.text.SpanStyle
 import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
 import androidx.compose.ui.text.withStyle
 import androidx.compose.ui.unit.dp

@Composable
fun ProfileStepItem(title: String, completed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (completed) Color(0xFF00C853) else Color.Gray
        )

        Text(
            text = if (completed) title.strikeThrough() else AnnotatedString(title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight =  if (completed) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

 fun String.strikeThrough(): AnnotatedString = buildAnnotatedString {
     withStyle(
         style = SpanStyle(
             textDecoration = TextDecoration.LineThrough,
             color = Color.Gray
         )
     ) {
         append(this@strikeThrough)
     }
 }
