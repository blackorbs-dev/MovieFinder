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

package blackorbs.dev.moviefinder.ui.moviescreen

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.text.color
import blackorbs.dev.moviefinder.R


internal class ExpandableTextView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : com.google.android.material.textview.MaterialTextView(context!!, attrs, defStyleAttr){

    private var originalText: CharSequence? = null
    private var trimmedText: CharSequence? = null
    private var bufferType: BufferType? = null
    private var trimLength = 500

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, android.R.attr.webViewStyle) {
        val typedArray = context!!.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView)
        this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, trimLength)
        typedArray.recycle()
        requestFocusFromTouch()
        setOnClickListener { trimmedText?.let { super.setText(getDisplayableText(), bufferType) } }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        originalText = text
        trimmedText = getTrimmedText()
        bufferType = type
        super.setText(getDisplayableText(), bufferType)
    }

    private fun getDisplayableText(): CharSequence {
        return if (trimmedText == null || text.equals(trimmedText)) originalText?:"" else trimmedText?:""
    }

    private fun getTrimmedText(): CharSequence? {
        return if (originalText != null && originalText!!.length > trimLength) {
            SpannableStringBuilder(originalText, 0, trimLength + 1).color(ContextCompat.getColor(context, R.color.white_100)){append(context.getString(R.string.ellipsis))}
        }
        else null
    }

}