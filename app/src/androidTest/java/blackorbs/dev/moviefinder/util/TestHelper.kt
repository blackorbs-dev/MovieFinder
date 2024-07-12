/*
 * Copyright 2024 BlackOrbs (blackorbs@icloud.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package blackorbs.dev.moviefinder.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.fragment.testing.manifest.R
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasImeAction
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.supportsInputMethods
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import blackorbs.dev.moviefinder.HiltTestActivity
import blackorbs.dev.moviefinder.ui.searchscreen.ListAdapter
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoilApi::class)
object TestHelper {
    const val IO_EXCEPTION = "IO_EXCEPTION"

    fun performSearch(query: String){
        onView(allOf(supportsInputMethods(), isDescendantOfA(withId(blackorbs.dev.moviefinder.R.id.movie_search_bar))))
            .check(matches(isDisplayed())).check(matches(hasImeAction(IME_ACTION_SEARCH)))
            .perform(clearText(), typeText(query), pressImeActionButton())
    }

    //RecyclerView Helpers
    fun assertItemCount(expected: Int){
        onView(withId(blackorbs.dev.moviefinder.R.id.movies_list)).check{ view, e -> if(e!=null) throw e
            assertEquals(expected, (view as RecyclerView).adapter?.itemCount)
        }
    }

    fun<T: RecyclerView.ViewHolder> performItemClick(pos: Int, childResId: Int?){
        onView(withId(blackorbs.dev.moviefinder.R.id.movies_list)).perform(actionOnItemAtPosition<T>(
            pos, if(childResId==null) click() else clickChildView(childResId)
        ))
    }
    private fun clickChildView(resId: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints() = null
            override fun getDescription() = "Click on view with id:: $resId"
            override fun perform(uiController: UiController?, view: View?) =
                click().perform(uiController, view?.findViewById(resId))
        }
    }

    fun assertItemDisplayedAndClickable(pos: Int, text: String?, drawable: Drawable){
        scrollTo(pos).check(matches(itemAt(pos, hasDescendant(withText(text))))).check(matches(itemAt(pos, isClickable())))
            .check(matches(itemAt(pos, matchChildItem(blackorbs.dev.moviefinder.R.id.image, withDrawable(drawable)))))
    }

    fun assertItemDisplayedAndClickable(pos: Int, text: String?, imageRes: Int){
        scrollTo(pos).check(matches(itemAt(pos, hasDescendant(withText(text))))).check(matches(itemAt(pos, isClickable())))
            .check(matches(itemAt(pos, matchChildItem(blackorbs.dev.moviefinder.R.id.image, withDrawable(imageRes)))))
    }

    fun assertItemDisplayed(pos: Int, text: String?){
        scrollTo(pos).check(matches(itemAt(pos, hasDescendant(withText(text)))))
    }

    fun scrollTo(pos: Int): ViewInteraction {
        return onView(withId(blackorbs.dev.moviefinder.R.id.movies_list)).perform(scrollToPosition<ListAdapter.ViewHolder>(pos))
    }

    private fun itemAt(pos: Int, matcher: Matcher<View>) = object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java){
        override fun describeTo(description: Description?) {
            description?.appendText("View matches recycler view at position:: $pos")
            matcher.describeTo(description)
        }
        override fun matchesSafely(view: RecyclerView?): Boolean =
            matcher.matches(view?.findViewHolderForAdapterPosition(pos)?.itemView)
    }

    private fun matchChildItem(resId: Int, matcher: Matcher<View>) = object : TypeSafeMatcher<View>(){
        override fun describeTo(description: Description?) {
            description?.appendText("View matches child view with id:: $resId")
            matcher.describeTo(description)
        }
        override fun matchesSafely(view: View?): Boolean = matcher.matches(view?.findViewById(resId))
    }

    // Coil Fake Image Loading Helper
    fun getImageLoader(context: Context, throwable: Throwable): ImageLoader{
        return ImageLoader.Builder(context)
            .components{ add(FakeImageLoaderEngine.Builder().default{throw throwable}.build()) }
            .build()
    }

    fun getImageLoader(context: Context): ImageLoader{
        return ImageLoader.Builder(context).components{ add(FakeImageLoaderEngine(ColorDrawable(Color.BLUE))) }.build()
    }

    // General View Helpers
    fun assertDisplayed(resId: Int){
        onView(withId(resId)).check(matches(isDisplayed()))
    }

    fun assertDisplayed(resId: Int, text: String?){
        onView(withId(resId)).check(matches(isDisplayed())).check(matches(withText(text)))
    }

    fun assertDisplayed(resId: Int, drawable: Drawable){
        onView(withId(resId)).check(matches(isDisplayed())).check(matches(withDrawable(drawable)))
    }

    fun assertDisplayed(resId: Int, imageRes: Int){
        onView(withId(resId)).check(matches(isDisplayed())).check(matches(withDrawable(imageRes)))
    }

    fun assertDisplayedWithScroll(resId: Int, text: String?){
        onView(withId(resId)).perform(scrollTo()).check(matches(isDisplayed())).check(matches(withText(text)))
    }

    fun assertNotDisplayed(resId: Int, text: String){
        onView(withId(resId)).check(matches(not(isDisplayed()))).check(matches(withText(text)))
    }

    fun assertNotDisplayed(resId: Int){
        onView(withId(resId)).check(matches(not(isDisplayed())))
    }

    fun assertViewRemoved(resId: Int){
        onView(withId(resId)).check(doesNotExist())
    }

    fun assertClickable(resId: Int){
        onView(withId(resId)).check(matches(isDisplayed())).check(matches(isClickable()))
    }

    fun performClick(resId: Int) {
        onView(withId(resId)).perform(click())
    }

    private fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>(){
        override fun describeTo(description: Description?) {
            description?.appendText("Imageview with drawable same as drawable with:: $id")
        }
        override fun matchesSafely(view: View?): Boolean {
            return view is ImageView && getBitmap(view.drawable)?.sameAs(view.context.getDrawable(id)?.toBitmap()) ?: false
        }
    }

    private fun withDrawable(drawable: Drawable?) = object : TypeSafeMatcher<View>(){
        override fun describeTo(description: Description?) {
            description?.appendText("Imageview with drawable same as $drawable")
        }
        override fun matchesSafely(view: View?): Boolean {
            return view is ImageView && getBitmap(view.drawable)?.sameAs(getBitmap(drawable)) ?: false
        }
    }

    fun getBitmap(drawable: Drawable?): Bitmap?{
        return if(drawable is ColorDrawable) {
            val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val drawableCopy = drawable.constantState?.newDrawable()?.mutate()
            drawableCopy?.setBounds(0, 0, canvas.width, canvas.height)
            drawableCopy?.draw(canvas)
            bitmap
        } else drawable?.toBitmap()
    }

    // Hilt fragment launcher
    inline fun <reified T : Fragment> launchFragmentInHiltContainer(
        fragmentArgs: Bundle? = null, @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
        crossinline action: Fragment.() -> Unit = {}) {
        val startActivityIntent = Intent.makeMainActivity(ComponentName(ApplicationProvider.getApplicationContext(), HiltTestActivity::class.java))
            .putExtra("androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY", themeResId)

        ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
            val fragment: Fragment = activity.supportFragmentManager.fragmentFactory
                .instantiate(Preconditions.checkNotNull(T::class.java.classLoader), T::class.java.name)
            fragment.arguments = fragmentArgs
            activity.supportFragmentManager.beginTransaction().add(android.R.id.content, fragment, "").commitNow()
            fragment.action()
        }
    }
}