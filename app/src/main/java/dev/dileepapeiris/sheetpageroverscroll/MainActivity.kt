package dev.dileepapeiris.sheetpageroverscroll

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

const val TAG = "SheetPagerOverscroll"

/** Every pager fling: where the pager was, the fling velocity, and what was left for the stretch. */
object FlingLog {
  data class Entry(val place: String, val fling: Velocity, val remaining: Velocity)
  val entries = mutableListOf<Entry>()
}

/**
 * Wraps the pager's normal overscroll effect and records the velocity Foundation hands it after
 * a fling. The stretch animates `velocity - performFling(velocity)`, so a non-zero `remaining.x`
 * after the page has settled is the overscroll that plays against the swipe.
 */
private class RecordingOverscroll(private val delegate: OverscrollEffect, private val place: String) :
  OverscrollEffect by delegate {
  override suspend fun applyToFling(velocity: Velocity, performFling: suspend (Velocity) -> Velocity) {
    delegate.applyToFling(velocity) { v ->
      val consumed = performFling(v)
      val remaining = v - consumed
      Log.i(TAG, "place=$place fling.x=${v.x.toInt()} consumed.x=${consumed.x.toInt()} leftForOverscroll.x=${remaining.x.toInt()}")
      FlingLog.entries += FlingLog.Entry(place, v, remaining)
      consumed
    }
  }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { MaterialTheme { Screen() } }
  }
}

private val pageColors = listOf(Color(0xFFD84315), Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFF6A1B9A))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen() {
  var showSheet by remember { mutableStateOf(false) }

  Column(Modifier.fillMaxSize().safeDrawingPadding()) {
    Text("Material3 ${BuildConfig.MATERIAL3_VERSION}", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
    Text("Inline pager: settles cleanly", Modifier.padding(horizontal = 16.dp))
    Pager(place = "inline")
    Button(onClick = { showSheet = true }, modifier = Modifier.padding(16.dp).testTag("openSheet")) {
      Text("Open pager in ModalBottomSheet")
    }
  }

  if (showSheet) {
    ModalBottomSheet(
      onDismissRequest = { showSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
      Text("Pager in ModalBottomSheet: fling fast, watch the page after it settles", Modifier.padding(horizontal = 16.dp))
      Pager(place = "sheet")
      Spacer(Modifier.height(48.dp))
    }
  }
}

@Composable
fun Pager(place: String) {
  val state = rememberPagerState { pageColors.size }
  val base = rememberOverscrollEffect()
  val overscroll = remember(base, place) { base?.let { RecordingOverscroll(it, place) } }
  HorizontalPager(
    state = state,
    modifier = Modifier.fillMaxWidth().height(300.dp).testTag("pager-$place"),
    overscrollEffect = overscroll,
  ) { page ->
    Box(Modifier.fillMaxSize().padding(16.dp).background(pageColors[page])) {
      Text("Page ${page + 1}", Modifier.padding(16.dp), color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
  }
}
