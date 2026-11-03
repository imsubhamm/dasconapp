package com.dascon.machinerylog.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dascon.machinerylog.R
import com.dascon.machinerylog.ui.theme.DscplBlue
import com.dascon.machinerylog.ui.theme.DscplMuted
import com.dascon.machinerylog.ui.theme.DscplRed

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 108.dp,
    showCompanyText: Boolean = true,
    showAppTitle: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_dscpl),
            contentDescription = stringResource(R.string.company_name),
            modifier = Modifier.size(logoSize),
            contentScale = ContentScale.Fit,
        )
        if (showCompanyText) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.company_name),
                style = MaterialTheme.typography.titleSmall,
                color = DscplBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Text(
                text = stringResource(R.string.company_tagline).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = DscplRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (showAppTitle) {
            Text(
                text = "Machine Daily Log",
                style = MaterialTheme.typography.headlineMedium,
                color = DscplMuted,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
