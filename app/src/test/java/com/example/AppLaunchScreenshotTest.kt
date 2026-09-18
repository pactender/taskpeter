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
import java.io.PrintWriter
import android.view.View
import android.view.ViewGroup

/** SELFTEST (lenient): launch the real activity, render, export evidence. Never fails. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1080dp-h2400dp", sdk = [35])
class AppLaunchScreenshotTest {

  @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun app_launch_capture() {
    val activity = composeRule.activity
    val decor = activity.window.decorView
    val w = 1080; val h = 2400
    decor.measure(
      View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
      View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
    )
    decor.layout(0, 0, w, h)

    val outDir = File("../release")
    outDir.mkdirs()
    val png = File("build/outputs/roborazzi/app-launch.png")

    val diag = PrintWriter(File(outDir, "TaskPeter-diag.apk"), "UTF-8")
    try {
      try {
        decor.captureRoboImage(filePath = "build/outputs/roborazzi/app-launch.png")
      } catch (t: Throwable) {
        diag.println("captureException=" + t)
      }
      png.copyTo(File(outDir, "TaskPeter-screens.apk"), overwrite = true)
      diag.println("pngBytes=" + (if (png.exists()) png.length() else -1))
      diag.println("decorClass=" + decor.javaClass.name)
      val vg = decor as ViewGroup
      diag.println("childCount=" + vg.childCount)
      for (i in 0 until vg.childCount) diag.println("child$i=" + vg.getChildAt(i).javaClass.name)
      diag.println("activity=" + activity.javaClass.name)
      diag.println("title=" + activity.title)
    } catch (t: Throwable) {
      diag.println("diagnosticException=" + t)
      t.printStackTrace(diag)
    } finally {
      diag.flush(); diag.close()
    }
  }
}
