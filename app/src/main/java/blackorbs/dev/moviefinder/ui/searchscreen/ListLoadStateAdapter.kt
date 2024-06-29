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

package blackorbs.dev.moviefinder.ui.searchscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import blackorbs.dev.moviefinder.databinding.LoadStateBinding

class ListLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<ListLoadStateAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) = holder.bind(loadState)

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder = ViewHolder(
        LoadStateBinding.inflate(LayoutInflater.from(parent.context)), retry
    )

    inner class ViewHolder(private var binding:LoadStateBinding, retry: () -> Unit): RecyclerView.ViewHolder(binding.root){
        init {
            binding.retryBtn.setOnClickListener { retry() }
        }
        fun bind(loadState: LoadState){
            with(binding){
                indicator.isVisible = loadState is LoadState.Loading
                retryBtn.isVisible = loadState is LoadState.Error
                errorMsg.isVisible = loadState is LoadState.Error
            }
        }
    }
}