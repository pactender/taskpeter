package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import android.view.View

/**
 * Launches the real MainActivity on the JVM (Robolectric) and captures a
 * rendered screenshot. Fails the build if the window ends up blank.
 * The PNG is copied to ../release/ so the release workflow uploads it
 * as an asset named TaskPeter-screens.apk (branch-only debug hack).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1080dp-h2400dp", sdk = [35])
class AppLaunchScreenshotTest {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun app_launches_without_crash_or_blank_screen() {
    val activity = composeRule.activity
    val decor = activity.window.decorView

    val w = 1080; val h = 2400
    decor.measure(
      View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
      View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
    )
    decor.layout(0, 0, w, h)

    decor.captureRoboImage(filePath = "build/outputs/roborazzi/app-launch.png")

    val png = File("build/outputs/roborazzi/app-launch.png")
    val outDir = File("../release")
    outDir.mkdirs()
    png.copyTo(File(outDir, "TaskPeter-screens.apk"), overwrite = true)

    val diag = buildString {
      appendLine("decorClass=" + decor.javaClass.name)
      appendLine("childCount=" + (decor as android.view.ViewGroup).childCount)
      appendLine("pngBytes=" + (if (png.exists()) png.length() else -1))
      appendLine("activity=" + activity.javaClass.name)
      appendLine("windowFocus=" + activity.window.isActive)
      for (i in 0 until (decor as android.view.ViewGroup).childCount) {
        appendLine("child$i=" + decor.getChildAt(i).javaClass.name)
      }
    }
    File(outDir, "TaskPeter-diag.apk").writeText(diag)

    check(png.exists() && png.length() > 2_000) {
      "BLANK SCREEN: screenshot suspiciously small (${png.length()} bytes)"
    }
  }
}
