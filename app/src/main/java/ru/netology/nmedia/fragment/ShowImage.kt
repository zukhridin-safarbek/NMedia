package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.adapter.getContentImageFromServer
import ru.netology.nmedia.databinding.FragmentShowPhotoBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.fragment.FeedFragment.Companion.imageUrl
import ru.netology.nmedia.fragment.FeedFragment.Companion.postId
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class ShowImage : Fragment() {
    private lateinit var binding: FragmentShowPhotoBinding
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentShowPhotoBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.data.observe(viewLifecycleOwner) { feed ->
            feed.posts.map { post ->
                if (arguments?.postId?.toLong() == post.id) {
                    getContentImageFromServer(post.attachment?.url.toString(), binding.image)
                    binding.showLikes.text = post.likes.toString()
                }
            }
        }
        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}