package com.example.game

import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.app.Activity
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@Composable
fun SpeakerIcon(enabled: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.35f)
            lineTo(size.width * 0.40f, size.height * 0.35f)
            lineTo(size.width * 0.70f, size.height * 0.15f)
            lineTo(size.width * 0.70f, size.height * 0.85f)
            lineTo(size.width * 0.40f, size.height * 0.65f)
            lineTo(size.width * 0.15f, size.height * 0.65f)
            close()
        }
        drawPath(path = path, color = Color.White)
        
        if (!enabled) {
            drawLine(
                color = Color(0xFFEF4444),
                start = Offset(size.width * 0.15f, size.height * 0.15f),
                end = Offset(size.width * 0.85f, size.height * 0.85f),
                strokeWidth = 3.dp.toPx()
            )
        } else {
            drawArc(
                color = Color.White,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width * 0.42f, size.height * 0.25f),
                size = Size(size.width * 0.5f, size.height * 0.5f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun CustomPauseIcon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.fillMaxHeight().width(5.dp).background(Color.White, RoundedCornerShape(2.dp)))
        Box(modifier = Modifier.fillMaxHeight().width(5.dp).background(Color.White, RoundedCornerShape(2.dp)))
    }
}

@Composable
fun MainGameApp(viewModel: GameViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    
    Box(modifier = Modifier.fillMaxSize()) {
        GameBackground()
        
        Crossfade(targetState = activeScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                GameScreen.MAIN_MENU -> MainMenuScreen(viewModel)
                GameScreen.GAMEPLAY -> GameplayScreen(viewModel)
                GameScreen.SETTINGS -> SettingsScreen(viewModel)
                GameScreen.LEVEL_SELECTION -> LevelSelectionScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainMenuScreen(viewModel: GameViewModel) {
    val highScore by viewModel.highScore.collectAsStateWithLifecycle()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp, start = 24.dp, end = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // High Score banner styled with a fine indigo border and deep-blue backing
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(Color(0x1F1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x33818CF8), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "High Score",
                    tint = Color(0xFFFACC15), // bright yellow-400
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BEST SCORE: $highScore",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
            }

            // Title Branding Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LogoBlock(char = 'B', colorIdx = 1)
                    LogoBlock(char = 'L', colorIdx = 2)
                    LogoBlock(char = 'O', colorIdx = 3)
                    LogoBlock(char = 'C', colorIdx = 4)
                    LogoBlock(char = 'K', colorIdx = 5)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LogoBlock(char = 'P', colorIdx = 6)
                    LogoBlock(char = 'U', colorIdx = 1)
                    LogoBlock(char = 'Z', colorIdx = 2)
                    LogoBlock(char = 'Z', colorIdx = 3)
                    LogoBlock(char = 'L', colorIdx = 4)
                    LogoBlock(char = 'E', colorIdx = 5)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "C L A S S I C",
                    color = Color(0xFF22D3EE), // Beautiful Cyan-400
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    letterSpacing = 5.sp
                )
            }

            // Action Play Buttons
            Button(
                onClick = {
                    activity?.let { AdManager.recordClickValue(it) }
                    viewModel.setScreen(GameScreen.LEVEL_SELECTION)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06B6D4) // Cyan 500
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(58.dp)
                    .scale(pulseScale)
                    .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .testTag("play_game_button"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADVENTURE (LEVELS)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }

            Button(
                onClick = {
                    activity?.let { AdManager.recordClickValue(it) }
                    viewModel.startEndlessGame()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .border(1.5.dp, Color(0x7F818CF8), RoundedCornerShape(14.dp))
                    .testTag("endless_game_button"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF818CF8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CLASSIC (ENDLESS)",
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }

            // Beautiful AdMob Native Ad Card placed perfectly in the center segment
            AdManager.NativeAdCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            IconButton(
                onClick = {
                    activity?.let { AdManager.recordClickValue(it) }
                    viewModel.setScreen(GameScreen.SETTINGS)
                },
                modifier = Modifier
                    .background(Color(0x0FFFFFFF), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .size(54.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Anchor Banner Ad to the bottom overlay
        AdManager.BannerAdView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
fun LogoBlock(char: Char, colorIdx: Int) {
    val (baseColor, accentColor) = when (colorIdx) {
        1 -> Pair(Color(0xFFEF4444), Color(0xFFFCA5A5))
        2 -> Pair(Color(0xFF06B6D4), Color(0xFF67E8F9))
        3 -> Pair(Color(0xFF8B5CF6), Color(0xFFC4B5FD))
        4 -> Pair(Color(0xFFFACC15), Color(0xFFFEF08A))
        5 -> Pair(Color(0xFF10B981), Color(0xFF6EE7B7))
        6 -> Pair(Color(0xFFF97316), Color(0xFFFDBA74))
        else -> Pair(Color(0xFF64748B), Color(0xFFCBD5E1))
    }
    
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(baseColor)
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(2.dp)
            .drawBehind {
                // Inline glass highlights representing Geometric Balance top reflection
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.4f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = Color.Black.copy(alpha = 0.22f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            modifier = Modifier.offset(1.dp, 1.dp)
        )
        Text(
            text = char.toString(),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
    }
}

@Composable
fun GameplayScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    val board by viewModel.board.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val highScore by viewModel.highScore.collectAsStateWithLifecycle()
    val tray by viewModel.tray.collectAsStateWithLifecycle()
    val comboCount by viewModel.comboCount.collectAsStateWithLifecycle()
    val gameOver by viewModel.gameOver.collectAsStateWithLifecycle()
    val particles by viewModel.particles.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    
    val clearedRowsFlash by viewModel.clearedRowsFlash.collectAsStateWithLifecycle()
    val clearedColsFlash by viewModel.clearedColsFlash.collectAsStateWithLifecycle()

    val currentPlayMode by viewModel.currentPlayMode.collectAsStateWithLifecycle()
    val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
    val levelCompleted by viewModel.levelCompleted.collectAsStateWithLifecycle()

    val shakeOffsetX = remember { Animatable(0f) }
    val shakeOffsetY = remember { Animatable(0f) }

    LaunchedEffect(clearedRowsFlash, clearedColsFlash) {
        val totalCleared = clearedRowsFlash.size + clearedColsFlash.size
        if (totalCleared > 0) {
            val baseIntensity = (totalCleared * 5f).coerceIn(12f, 26f)
            for (i in 0 until 6) {
                val decay = 1f - (i * 0.15f)
                val intensity = baseIntensity * decay
                val targetX = if (i % 2 == 0) intensity else -intensity
                val targetY = if (i % 3 == 0) -intensity else intensity
                
                launch {
                    shakeOffsetX.animateTo(
                        targetValue = targetX,
                        animationSpec = tween(durationMillis = 35, easing = LinearEasing)
                    )
                }
                launch {
                    shakeOffsetY.animateTo(
                        targetValue = targetY,
                        animationSpec = tween(durationMillis = 35, easing = LinearEasing)
                    )
                }
                delay(35)
            }
            launch {
                shakeOffsetX.animateTo(0f, animationSpec = tween(durationMillis = 40))
            }
            launch {
                shakeOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 40))
            }
        }
    }

    var showPauseDialog by remember { mutableStateOf(false) }

    // Touch and Drag Layout Measurement Tracking State
    val density = LocalDensity.current
    var boardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val slotCoordinates = remember { mutableStateMapOf<Int, LayoutCoordinates>() }
    
    // Global active dragging status
    var activeDragId by remember { mutableStateOf<Int?>(null) }
    var activeDragShape by remember { mutableStateOf<BlockShape?>(null) }
    val dragOffsets = remember { mutableStateMapOf<Int, Offset>() }
    
    var ghostPlacement by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header panel: Scores with beautiful design-driven asymmetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (currentPlayMode == "LEVELS") {
                    // Active Level progress: Left
                    Column {
                        Text(
                            text = "LEVEL $selectedLevel",
                            color = Color(0xFF22D3EE),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "TARGET: ${viewModel.getTargetScoreForLevel(selectedLevel)}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = score.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 34.sp,
                            lineHeight = 38.sp,
                            letterSpacing = (-1).sp
                        )
                    }

                    // Level target percentage progress: Right
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = viewModel.getLevelDescription(selectedLevel),
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val targetScore = viewModel.getTargetScoreForLevel(selectedLevel)
                        val progress = if (targetScore > 0) (score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f) else 0f
                        
                        LinearProgressIndicator(
                            progress = progress,
                            color = Color(0xFF10B981),
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .width(110.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${(progress * 100).toInt()}% READY",
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                } else {
                    // Active Score: Left, massive focus, accented in vivid Cyan-400
                    Column {
                        Text(
                            text = "SCORE",
                            color = Color(0xFF22D3EE),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = score.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            lineHeight = 42.sp,
                            letterSpacing = (-1).sp
                        )
                    }

                    // Best Score: Right, side-aligned, smaller and sophisticated Slate-400 tint
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BEST",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = highScore.toString(),
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 26.sp
                        )
                    }
                }
            }

            // Combo Alert Banner styled as a floating pill badge
            Box(
                modifier = Modifier
                    .height(35.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (comboCount > 1) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFACC15), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMBO x$comboCount! 🔥",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 10x10 Puzzle Board (Centered area)
            // Beautiful slate-900 equivalent backing with prominent slate-800 borders
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // force square board
                    .offset { IntOffset(shakeOffsetX.value.roundToInt(), shakeOffsetY.value.roundToInt()) }
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x800F172A)) // bg-slate-900/50
                    .border(3.dp, Color(0x661E293B), RoundedCornerShape(16.dp)) // border-4 border-slate-800/40
                    .padding(8.dp) // p-2
                    .onGloballyPositioned { boardCoordinates = it }
            ) {
                val cellSize = maxWidth / 10f

                // Base Empty Grid Layout
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0..9) {
                        Row(modifier = Modifier.weight(1f)) {
                            for (c in 0..9) {
                                val isCellClearedRowFlash = r in clearedRowsFlash
                                val isCellClearedColFlash = c in clearedColsFlash
                                
                                // Determine if this cell should display a ghost preview
                                var displaysGhost = false
                                var ghostColorIdx = 0
                                ghostPlacement?.let { (ghostRow, ghostCol) ->
                                    val dragShape = activeDragShape
                                    if (dragShape != null) {
                                        val shapeRow = r - ghostRow
                                        val shapeCol = c - ghostCol
                                        if (shapeRow in 0 until dragShape.height && shapeCol in 0 until dragShape.width) {
                                            if (dragShape.grid[shapeRow][shapeCol] == 1) {
                                                displaysGhost = true
                                                ghostColorIdx = dragShape.colorIndex
                                            }
                                        }
                                    }
                                }

                                val fillIdx = board[r][c]
                                BlockTile(
                                    colorIndex = if (fillIdx != 0) fillIdx else (if (displaysGhost) ghostColorIdx else 0),
                                    isGhost = displaysGhost && fillIdx == 0,
                                    clearedFlash = isCellClearedRowFlash || isCellClearedColFlash,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.5.dp)
                                )
                            }
                        }
                    }
                }

                // Real-time canvas particles overlapping the board cells
                ParticlesOverlay(
                    particles = particles,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom tray supporting 3 piece docks matching `bg-white/5 rounded-xl border border-white/10`
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0FFFFFFF), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x1FDDDDDD), RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (slotIndex in 0..2) {
                    val shape = if (slotIndex < tray.size) tray[slotIndex] else null
                    
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .onGloballyPositioned { coords ->
                                if (shape != null) {
                                    slotCoordinates[shape.id] = coords
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (shape != null) {
                            val isBeingDragged = activeDragId == shape.id
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(if (isBeingDragged) 0.35f else 1.0f)
                                    .pointerInput(shape.id) {
                                        detectDragGestures(
                                            onDragStart = { _ ->
                                                activeDragId = shape.id
                                                activeDragShape = shape
                                                dragOffsets[shape.id] = Offset.Zero
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val accumulated = dragOffsets[shape.id] ?: Offset.Zero
                                                dragOffsets[shape.id] = accumulated + dragAmount
                                                
                                                // Calculate real-time ghost position over board
                                                val bCoords = boardCoordinates
                                                val sCoords = slotCoordinates[shape.id]
                                                if (bCoords != null && sCoords != null) {
                                                    val slotInRoot = sCoords.positionInRoot()
                                                    val boardInRoot = bCoords.positionInRoot()
                                                    val currentOffset = dragOffsets[shape.id] ?: Offset.Zero
                                                    
                                                    val rootTouchX = slotInRoot.x + currentOffset.x
                                                    val rootTouchY = slotInRoot.y + currentOffset.y
 
                                                    val bWidth = bCoords.size.width.toFloat()
                                                    val cellWidthPx = bWidth / 10f
 
                                                    val shapeW = shape.width * cellWidthPx
                                                    val shapeH = shape.height * cellWidthPx
 
                                                    // Visual thumb offset (shift piece upwards slightly)
                                                    val visualUpwardOffsetY = -130f
                                                    
                                                    val sLeft = rootTouchX - shapeW / 2f
                                                    val sTop = rootTouchY - shapeH / 2f + visualUpwardOffsetY
 
                                                    val xOnBoard = sLeft - boardInRoot.x
                                                    val yOnBoard = sTop - boardInRoot.y
 
                                                    val hoverCol = (xOnBoard / cellWidthPx + 0.5f).toInt()
                                                    val hoverRow = (yOnBoard / cellWidthPx + 0.5f).toInt()
 
                                                    if (viewModel.canPlaceShape(shape, hoverRow, hoverCol, board)) {
                                                        ghostPlacement = Pair(hoverRow, hoverCol)
                                                    } else {
                                                        ghostPlacement = null
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                // Resolve drops
                                                val bCoords = boardCoordinates
                                                val sCoords = slotCoordinates[shape.id]
                                                if (bCoords != null && sCoords != null) {
                                                    val slotInRoot = sCoords.positionInRoot()
                                                    val boardInRoot = bCoords.positionInRoot()
                                                    val currentOffset = dragOffsets[shape.id] ?: Offset.Zero
                                                    
                                                    val rootTouchX = slotInRoot.x + currentOffset.x
                                                    val rootTouchY = slotInRoot.y + currentOffset.y
 
                                                    val bWidth = bCoords.size.width.toFloat()
                                                    val cellWidthPx = bWidth / 10f
 
                                                    val shapeW = shape.width * cellWidthPx
                                                    val shapeH = shape.height * cellWidthPx
                                                    val visualUpwardOffsetY = -130f
 
                                                    val sLeft = rootTouchX - shapeW / 2f
                                                    val sTop = rootTouchY - shapeH / 2f + visualUpwardOffsetY
 
                                                    val xOnBoard = sLeft - boardInRoot.x
                                                    val yOnBoard = sTop - boardInRoot.y
 
                                                    val hoverCol = (xOnBoard / cellWidthPx + 0.5f).toInt()
                                                    val hoverRow = (yOnBoard / cellWidthPx + 0.5f).toInt()
 
                                                    val placed = viewModel.placeShape(shape.id, hoverRow, hoverCol)
                                                    if (placed) {
                                                        activity?.let { AdManager.recordClickValue(it) }
                                                    }
                                                }
                                                // Cancel drag overlays
                                                activeDragId = null
                                                activeDragShape = null
                                                ghostPlacement = null
                                                dragOffsets[shape.id] = Offset.Zero
                                            },
                                            onDragCancel = {
                                                activeDragId = null
                                                activeDragShape = null
                                                ghostPlacement = null
                                                dragOffsets[shape.id] = Offset.Zero
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw miniaturized block shapes inside tray dock
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    for (r in 0 until shape.height) {
                                        Row {
                                            for (c in 0 until shape.width) {
                                                val cellVal = shape.grid[r][c]
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .padding(1.dp)
                                                ) {
                                                    if (cellVal == 1) {
                                                        BlockTile(colorIndex = shape.colorIndex, modifier = Modifier.fillMaxSize())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer bar matching the HTML specification exactly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .alpha(0.6f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button on Left: `w-10 h-10 rounded-full border border-white/20 flex items-center justify-center`
                IconButton(
                    onClick = { viewModel.setScreen(GameScreen.SETTINGS) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Action buttons on Right (Quick Mute, Restart, and Pause side-by-side)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Quick Mute / Unmute Speaker Toggle Button
                    IconButton(
                        onClick = {
                            activity?.let { act -> AdManager.recordClickValue(act) }
                            viewModel.toggleSound()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("mute_unmute_button")
                    ) {
                        SpeakerIcon(
                            enabled = soundEnabled,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Refresh / Restart button
                    IconButton(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Game",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pause button
                    IconButton(
                        onClick = { showPauseDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("pause_button")
                    ) {
                        CustomPauseIcon(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Anchor Banner Ad to the bottom-center of the Gameplay screen
        AdManager.BannerAdView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )

        // 100% SCALE FLOATING OVERLAY: Renders the active dragging shape following the finger coordinates
        if (activeDragId != null && activeDragShape != null) {
            val sId = activeDragId!!
            val sCur = activeDragShape!!
            val dragOffset = dragOffsets[sId] ?: Offset.Zero
            val slotCoord = slotCoordinates[sId]
            val boardCoord = boardCoordinates

            if (slotCoord != null && boardCoord != null) {
                val slotInRoot = slotCoord.positionInRoot()
                val cellWidthDp = boardCoord.size.width.toFloat() / 10f
                
                val fingerRootX = slotInRoot.x + dragOffset.x
                val fingerRootY = slotInRoot.y + dragOffset.y

                val shapeW = sCur.width * cellWidthDp
                val shapeH = sCur.height * cellWidthDp
                
                // Keep the center of the shape aligned with finger but shifted upwards by 50dp
                val visualUpwardOffsetY = -130f
                val shapeLeft = fingerRootX - shapeW / 2f
                val shapeTop = fingerRootY - shapeH / 2f + visualUpwardOffsetY

                val layoutLeftDp = with(density) { shapeLeft.toDp() }
                val layoutTopDp = with(density) { shapeTop.toDp() }

                val singleCellDp = with(density) { cellWidthDp.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = layoutLeftDp, y = layoutTopDp)
                        .scale(1.05f) 
                ) {
                    Column {
                        for (r in 0 until sCur.height) {
                            Row {
                                for (c in 0 until sCur.width) {
                                    val cellVal = sCur.grid[r][c]
                                    Box(
                                        modifier = Modifier
                                            .size(singleCellDp)
                                            .padding(1.5.dp)
                                    ) {
                                        if (cellVal == 1) {
                                            BlockTile(
                                                colorIndex = sCur.colorIndex,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay Dialogs
        if (showPauseDialog) {
            PauseMenuDialog(
                onDismiss = { showPauseDialog = false },
                onRestart = {
                    viewModel.startNewGame()
                    showPauseDialog = false
                },
                onQuit = {
                    viewModel.setScreen(GameScreen.MAIN_MENU)
                    showPauseDialog = false
                }
            )
        }

        if (gameOver) {
            GameOverDialog(
                viewModel = viewModel,
                score = score,
                highScore = highScore,
                onRestart = {
                    viewModel.startNewGame()
                },
                onQuit = {
                    viewModel.setScreen(GameScreen.MAIN_MENU)
                }
            )
        }

        if (levelCompleted && currentPlayMode == "LEVELS") {
            LevelCompleteDialog(
                viewModel = viewModel,
                levelNo = selectedLevel,
                score = score,
                onNextLevel = {
                    viewModel.advanceToNextLevel()
                },
                onLevelSelection = {
                    viewModel.setScreen(GameScreen.LEVEL_SELECTION)
                },
                onQuit = {
                    viewModel.setScreen(GameScreen.MAIN_MENU)
                }
            )
        }
    }
}

@Composable
fun ScoreBox(label: String, valStr: String, isHighlight: Boolean) {
    Column(
        modifier = Modifier
            .background(
                if (isHighlight) Color(0x333B82F6) else Color(0x1F000000),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isHighlight) Color(0x663B82F6) else Color(0x14FFFFFF),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isHighlight) Color(0xFF93C5FD) else Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = valStr,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp, start = 24.dp, end = 24.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title banner with geometric cyan icon highlight
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF22D3EE),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 3.sp
                )
            }

            // Settings Items Card matching `bg-slate-800/40 border border-white/10`
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x501E293B), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SpeakerIcon(
                            enabled = soundEnabled,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sound & Music",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Background melody and sound effects",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.toggleSound() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF06B6D4), // Cyan-500
                            checkedTrackColor = Color(0x6606B6D4)  // half opacity Cyan
                        ),
                        modifier = Modifier.testTag("sound_toggle")
                    )
                }

                HorizontalDivider(color = Color(0x0FFFFFFF))

                // Game Rules Card
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF10B981), // Emerald-500
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "How to Play",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Drag blocks from the bottom tray onto the 10x10 board.\n" +
                                    "• Fill columns or horizontal rows to clear lines.\n" +
                                    "• Clear multiple rows/columns together for COMBO multiplier points!\n" +
                                    "• No time limit. Game ends when no tray pieces can fit anywhere.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Beautiful AdMob Native Ad Card in settings view
            AdManager.NativeAdCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Back button styled with the elegant cyan transparent borders style
            Button(
                onClick = {
                    activity?.let { AdManager.recordClickValue(it) }
                    viewModel.setScreen(GameScreen.MAIN_MENU)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .testTag("back_button")
            ) {
                Text(
                    text = "BACK TO MENU",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Anchor Banner Ad to bottom center of Settings
        AdManager.BannerAdView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
fun PauseMenuDialog(
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC020617)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0x3322D3EE), RoundedCornerShape(16.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("dialog_resume_button")
                ) {
                    Text("RESUME GAME", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .testTag("dialog_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTART CLASSIC", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                TextButton(
                    onClick = onQuit,
                    modifier = Modifier.testTag("dialog_quit_button")
                ) {
                    Text(
                        "Exit to Main Menu",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    viewModel: GameViewModel,
    score: Int,
    highScore: Int,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    val isNewHighScore = score >= highScore && score > 0
    val context = LocalContext.current
    val activity = context as? Activity

    var isReviveUsed by remember { mutableStateOf(false) }
    var isDoubleUsed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {}, 
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEB020617)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0x44EF4444), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0x1AEF4444), CircleShape)
                        .border(1.5.dp, Color(0x55EF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "NO MOVES REMAIN",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "GAME OVER",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp
                )

                if (isNewHighScore) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFBF00).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFFBF00), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏆 NEW PERSONAL BEST!",
                            color = Color(0xFFFFBF00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x15000000), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FINAL SCORE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(score.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color(0x14FFFFFF)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BEST SCORE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(highScore.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // AD: WATCH REWARDED TO REVIVE (+3 BLOCKS & CLEAR 3 ROWS)
                if (!isReviveUsed) {
                    Button(
                        onClick = {
                            activity?.let { act ->
                                AdManager.showRewarded(act) {
                                    viewModel.reviveGame()
                                    isReviveUsed = true
                                    Toast.makeText(act, "Game Revived! 3 rows cleared!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Emerald Green
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("REVIVE (CLEAR 3 ROWS) 📺", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // AD: WATCH REWARDED INTERSTITIAL TO DOUBLE SCORE
                if (!isDoubleUsed && score > 0) {
                    Button(
                        onClick = {
                            activity?.let { act ->
                                AdManager.showRewardedInterstitial(act) {
                                    viewModel.doubleFinalScore()
                                    isDoubleUsed = true
                                    Toast.makeText(act, "Score doubled successfully! 🎉", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308)), // Yellow/Gold
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                    ) {
                        Text("DOUBLE FINAL SCORE 📺", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("game_over_restart_button")
                ) {
                    Text("PLAY AGAIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        activity?.let { act -> AdManager.recordClickValue(act) }
                        onQuit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .testTag("game_over_menu_button")
                ) {
                    Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ParticlesOverlay(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cellW = size.width / 10f
        val cellH = size.height / 10f
        
        for (p in particles) {
            val colorList = when (p.colorIndex) {
                1 -> listOf(Color(0xFFFF3366), Color(0xFFFF1744))
                2 -> listOf(Color(0xFF33CCFF), Color(0xFF00B0FF))
                3 -> listOf(Color(0xFF39FF14), Color(0xFF00E676))
                4 -> listOf(Color(0xFFFFE600), Color(0xFFFFD740))
                5 -> listOf(Color(0xFFFF7B00), Color(0xFFFF6E40))
                6 -> listOf(Color(0xFFCC33FF), Color(0xFFD500F9))
                else -> listOf(Color.White, Color.LightGray)
            }
            
            drawCircle(
                color = colorList[0].copy(alpha = p.alpha),
                radius = p.size * (0.4f + p.alpha * 0.6f),
                center = Offset(p.x * cellW, p.y * cellH)
            )
        }
    }
}

@Composable
fun LevelSelectionScreen(viewModel: GameViewModel) {
    val maxUnlockedLevel by viewModel.maxUnlockedLevel.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CAMPAIGN",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = 3.sp
            )
            Text(
                text = "Reach the targets to unlock levels",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        }

        // Levels Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val levelRows = remember { GameViewModel.levelsList.chunked(2) }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(levelRows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (level in row) {
                            val isUnlocked = level.levelNo <= maxUnlockedLevel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.4f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isUnlocked) Color(0x1F22D3EE) else Color(0x151E293B)
                                    )
                                    .border(
                                        width = 1.2.dp,
                                        color = if (isUnlocked) Color(0xFF22D3EE).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = isUnlocked) {
                                        activity?.let { AdManager.recordClickValue(it) }
                                        viewModel.startLevelGame(level.levelNo)
                                    }
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "LVL ${level.levelNo}",
                                            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.3f),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp
                                        )
                                        if (isUnlocked) {
                                            if (level.levelNo < maxUnlockedLevel) {
                                                // Completed level
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(Color(0xFF10B981), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            } else {
                                                // Unlocked current level
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(Color(0xFF06B6D4), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            // Locked level
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Locked",
                                                tint = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = level.description,
                                            color = if (isUnlocked) Color(0xFFCBD5E1) else Color.White.copy(alpha = 0.2f),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Target: ${level.targetScore}",
                                            color = if (isUnlocked) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.2f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Native Ad
        AdManager.NativeAdCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Back Button
        Button(
            onClick = {
                activity?.let { AdManager.recordClickValue(it) }
                viewModel.setScreen(GameScreen.MAIN_MENU)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(54.dp)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .testTag("back_to_menu_button")
        ) {
            Text(
                text = "BACK TO MENU",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun LevelCompleteDialog(
    viewModel: GameViewModel,
    levelNo: Int,
    score: Int,
    onNextLevel: () -> Unit,
    onLevelSelection: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xE00F172A)) // Semi-translucent dark slate background
                .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Checkmark success badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0x2210B981), CircleShape)
                        .border(1.5.dp, Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Success",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "LEVEL COMPLETE!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Outstanding! You successfully conquered Level $levelNo and unlocked the next challenge.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Score Stat Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x3F1E293B), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL SCORE",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = score.toString(),
                        color = Color(0xFF22D3EE),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                }

                AdManager.NativeAdCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Actions Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (levelNo < GameViewModel.levelsList.size) {
                        Button(
                            onClick = onNextLevel,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("next_level_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NEXT LEVEL",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onLevelSelection,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2AFFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .testTag("select_level_button")
                        ) {
                            Text(
                                text = "CAMPAIGN",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onQuit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x11FFFFFF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .testTag("quit_level_button")
                        ) {
                            Text(
                                text = "MAIN MENU",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
