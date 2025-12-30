@Composable
fun CircularProgressWithText(progress: Float, label: String, subLabel: String) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(progress = 1f, strokeWidth = 8.dp, color = Color.LightGray)
        CircularProgressIndicator(progress = progress, strokeWidth = 8.dp, color = Color.Green)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.headlineMedium)
            Text(subLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}
