@file:Suppress("UnusedReceiverParameter")

package com.ramcosta.samples.playground.ui.screens.profile

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.annotation.parameters.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.samples.playground.commons.ProfileGraph

@OptIn(ExperimentalSharedTransitionApi::class)
@Destination<ProfileGraph>(
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ],
    style = ProfileTransitions::class,
    navArgs = ProfileScreenNavArgs::class
)
@Composable
fun SharedTransitionScope.ProfileScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    uiState: ProfileUiState,
    uiEvents: ProfileUiEvents
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCDEC0))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile id: ${uiState.id}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Belongs to: ${uiState.groupName}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${uiState.likeCount} likes"
            )

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = {
                    uiEvents.onLikeButtonClick()
                },
                modifier = Modifier
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "love-icon"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .size(128.dp)
                    .background(
                        color = Color.Red,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "like",
                    tint = Color.White,
                )
            }
        }
    }
}