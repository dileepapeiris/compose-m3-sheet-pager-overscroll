package dev.dileepapeiris.sheetpageroverscroll

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Flings the same pager inline and inside a ModalBottomSheet and reports the velocity Foundation
 * leaves for the pager's overscroll after each fling. Expected: 0 in both places.
 * Material3 1.5.0-alpha10 .. 1.5.0-beta02: about the full fling velocity, reversed, in the sheet.
 */
@RunWith(AndroidJUnit4::class)
class SheetPagerOverscrollTest {
  @get:Rule val rule = createComposeRule()

  private fun fling(tag: String, times: Int) {
    val pager = rule.onNodeWithTag(tag)
    repeat(times) { i ->
      val toLeft = i % 2 == 0
      pager.performTouchInput {
        val from = if (toLeft) Offset(width * 0.9f, centerY) else Offset(width * 0.1f, centerY)
        val to = if (toLeft) Offset(width * 0.1f, centerY) else Offset(width * 0.9f, centerY)
        swipeWithVelocity(start = from, end = to, endVelocity = 4000f)
      }
      rule.waitForIdle()
      rule.mainClock.advanceTimeBy(2_000)
      rule.waitForIdle()
    }
  }

  private fun report(place: String): List<FlingLog.Entry> {
    val entries = FlingLog.entries.filter { it.place == place }
    entries.forEach {
      Log.i(TAG, "RESULT material3=${BuildConfig.MATERIAL3_VERSION} place=$place fling.x=${it.fling.x.toInt()} leftForOverscroll.x=${it.remaining.x.toInt()}")
    }
    return entries
  }

  @Test
  fun pagerFling_leavesNothingForOverscroll_inlineAndInSheet() {
    FlingLog.entries.clear()
    rule.setContent { MaterialTheme { Screen() } }
    rule.waitForIdle()

    fling("pager-inline", times = 4)
    rule.onNodeWithTag("openSheet").performClick()
    rule.waitForIdle()
    rule.mainClock.advanceTimeBy(1_000)
    rule.waitForIdle()
    fling("pager-sheet", times = 4)

    val inline = report("inline")
    val sheet = report("sheet")
    assertTrue("no inline flings recorded", inline.isNotEmpty())
    assertTrue("no sheet flings recorded", sheet.isNotEmpty())
    inline.forEach { assertEquals("inline: fling ${it.fling.x} left ${it.remaining.x} for overscroll", 0f, it.remaining.x, 1f) }
    sheet.forEach { assertEquals("sheet: fling ${it.fling.x} left ${it.remaining.x} for overscroll", 0f, it.remaining.x, 1f) }
  }
}
