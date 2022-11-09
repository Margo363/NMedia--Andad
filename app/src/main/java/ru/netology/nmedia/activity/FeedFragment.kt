package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.card_post.*
import kotlinx.android.synthetic.main.card_post.view.*
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.fragment_feed.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostCallback
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.RetryType
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )


        val viewModelAuth: AuthViewModel by viewModels()

        val bundle = Bundle()


        val adapter = PostsAdapter(object : PostCallback {

            override fun onLike(post: Post) {
                if (viewModelAuth.authenticated) {
                    if (!post.likedByMe) viewModel.likeById(post.id) else viewModel.unlikeById(post.id)
                } else {
                    findNavController().navigate(R.id.action_feedFragment_to_authFragment)
                    if (viewModelAuth.authenticated) {
                        if (!post.likedByMe) viewModel.likeById(post.id) else viewModel.unlikeById(
                            post.id
                        )
                    }
                }
            }


            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }

                val shareIntent = Intent.createChooser(intent, getString(R.string.share_post))
                startActivity(shareIntent)
                viewModel.shareById(post.id)
            }

            override fun remove(post: Post) {
                viewModel.removeById(post.id)


            }

            override fun edit(post: Post) {
                viewModel.edit(post)
                bundle.putString("content", post.content)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment, bundle)
            }

            override fun onVideo(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                val videoIntent = Intent.createChooser(intent, getString(R.string.video_chooser))
                startActivity(videoIntent)
            }

            override fun onPost(post: Post) {
                val id = post.id
                bundle.putLong("id", id)

                findNavController().navigate(R.id.action_feedFragment_to_singlePost, bundle)
            }

            override fun onImage(post: Post) {
                bundle.putString("url", post.attachment?.url)
                findNavController().navigate(
                    R.id.action_feedFragment_to_singleImageFragment,
                    bundle
                )
            }

        })

        binding.list.adapter = adapter
        binding.list.animation = null // отключаем анимацию

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing =
                    it.prepend is LoadState.Loading ||
                            it.append is LoadState.Loading ||
                            it.refresh is LoadState.Loading
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) {
                        when (state.retryType) {
                            RetryType.REMOVE -> viewModel.removeById(state.retryId)
                            RetryType.LIKE -> viewModel.likeById(state.retryId)
                            RetryType.UNLIKE -> viewModel.unlikeById(state.retryId)
                            else -> viewModel.refreshPosts()
                        }
                    }
                    .show()
            }
        }

//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            with(binding.newEntry) {
//                if (it > 0) {
//                    text = "${getString(R.string.new_posts)} $it"
//                    visibility = View.VISIBLE
//                }
//            }
//        }

        viewModelAuth.data.observe(viewLifecycleOwner) { adapter.refresh() }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
            binding.newEntry.visibility = View.GONE
        }

        binding.newEntry.setOnClickListener {
            binding.newEntry.visibility = View.GONE
            viewModel.loadNewPosts()
        }

        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id == 0L) {
                return@observe
            }

        }

        binding.floatingActionButton.setOnClickListener {
            if (viewModelAuth.authenticated) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_authFragment)
            }
        }


        //скроллинг постов
        lifecycleScope.launch {
            val shouldScrollToTop = adapter.loadStateFlow
                .distinctUntilChangedBy { it.source.refresh }
                .map { it.source.refresh is LoadState.NotLoading }

            shouldScrollToTop.collectLatest { shouldScroll ->
                if (shouldScroll) binding.list.scrollToPosition(0)
            }
        }


        return binding.root
    }

}



