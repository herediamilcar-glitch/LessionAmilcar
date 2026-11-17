package com.example.lession.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.R
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionPurplePrimary
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(LessionPurplePrimary)
                    .border(3.dp, LessionLimeAccent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "LESSION Logo",
                    modifier = Modifier.size(110.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LESSION",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = LessionLimeAccent,
                letterSpacing = 4.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Banda Inteligente EMG",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextWhite,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Registro físico, análisis muscular en tiempo real y seguimiento de rendimiento deportivo.",
                fontSize = 14.sp,
                color = LessionTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LessionCardDark, RoundedCornerShape(16.dp))
                .border(1.dp, LessionPurplePrimary, RoundedCornerShape(16.dp))
                .padding(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Hardware: ESP32-C3 BLE",
                    fontSize = 13.sp,
                    color = LessionLimeAccent,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Sensor de Envolvente EMG • 50 Hz",
                    fontSize = 12.sp,
                    color = LessionTextMuted,
                )
            }
        }

        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LessionLimeAccent,
                contentColor = LessionBackgroundDark,
            ),
        ) {
            Text(
                text = "COMENZAR",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
            )
        }
    }
}
