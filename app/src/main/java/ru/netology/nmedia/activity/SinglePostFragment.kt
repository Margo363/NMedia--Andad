package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSinglePostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.Utils
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class SinglePostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val bundle = Bundle()
        val binding = FragmentSinglePostBinding.inflate(inflater, container, false)
        val id = arguments?.getLong("id")
        var singlePost: Post? = null

//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            state.posts.map { post ->
//                if (post.id == id) {
//                    singlePost = post
//                }
//            }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                it.map { post ->
                    if (post.id == id) {
                        singlePost = post
                    }
                }

                with(binding) {
                    author.text = singlePost?.author
                    content.text = singlePost?.content
                    published.text = singlePost?.published

                    like.text = singlePost?.let { it -> Utils.reductionInNumbers(it.likes) }
                    share.text = singlePost?.sharesCount?.let { it ->
                        Utils.reductionInNumbers(
                            it
                        )
                    }
                    like.isChecked = singlePost?.likedByMe == true
                    if (singlePost?.video != "") binding.group.visibility = View.VISIBLE
                    if (singlePost?.attachment != null) viewForImage.visibility = View.VISIBLE


                }

                binding.menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.post_options)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.post_remove -> {

                                    singlePost?.id?.let { id -> viewModel.removeById(id) }

                                    findNavController().navigate(
                                        R.id.action_singlePostFragment_to_feedFragment
                                    )
                                    true
                                }
                                R.id.post_edit -> {

                                    singlePost?.let { it -> viewModel.edit(it) }

                                    bundle.putString("content", singlePost?.content)

                                    findNavController().navigate(
                                        R.id.action_singlePostFragment_to_newPostFragment,
                                        bundle
                                    )
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }

        return binding.root
    }
}