package io.vuihoc.agora_native.ui.activity.play

import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.vuihoc.agora_native.R
import io.vuihoc.agora_native.data.model.CLOUD_ROOT_DIR
import io.vuihoc.agora_native.data.model.FileConvertStep
import io.vuihoc.agora_native.data.model.LoadState
import io.vuihoc.agora_native.data.model.ResourceType
import io.vuihoc.agora_native.databinding.ComponentCloudBinding
import io.vuihoc.agora_native.ui.manager.RoomOverlayManager
import io.vuihoc.agora_native.ui.view.FooterAdapter
import io.vuihoc.agora_native.util.folderName
import io.vuihoc.agora_native.util.showToast

class ClassCloudComponent(
    activity: ClassRoomActivity,
    rootView: FrameLayout,
) : BaseComponent(activity, rootView) {
    private lateinit var binding: ComponentCloudBinding
    private lateinit var cloudStorageAdapter: CloudStorageAdapter
    private lateinit var footerAdapter: FooterAdapter

    private val viewModel: ClassCloudViewModel by activity.viewModels()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        initView()
        observeState()
    }

    private fun observeState() {
        lifecycleScope.launchWhenResumed {
            RoomOverlayManager.observeShowId().collect { areaId ->
                val visiable = areaId == RoomOverlayManager.AREA_ID_CLOUD_STORAGE
                binding.layoutCloudStorage.root.isVisible = visiable
                if (visiable) {
                    viewModel.reloadFileList()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                cloudStorageAdapter.setDataSet(it.files)
                binding.layoutCloudStorage.listEmpty.isVisible = it.files.isEmpty()
                binding.layoutCloudStorage.cloudStorageList.isVisible = it.files.isNotEmpty()
                footerAdapter.updateState(it.loadUiState.append)
                updateTitleByDir(viewModel.state.value.dirPath)
            }
        }
    }

    private fun initView() {
        binding = ComponentCloudBinding.inflate(activity.layoutInflater, rootView, true)

        footerAdapter = FooterAdapter()
        footerAdapter.setOnFooterClickListener {
            viewModel.loadMoreFileList()
        }
        cloudStorageAdapter = CloudStorageAdapter()
        cloudStorageAdapter.setOnItemClickListener {
            when (it.resourceType) {
                ResourceType.Directory -> {
                    viewModel.enterFolder(it.fileName)
                }
                else -> {
                    if (it.convertStep == FileConvertStep.Done) {
                        viewModel.insertCourseware(it)
                        RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_CLOUD_STORAGE, false)
                    } else {
                        activity.showToast(R.string.cloud_preview_transcoding_hint)
                    }
                }
            }
        }
        val concatAdapter = ConcatAdapter(cloudStorageAdapter, footerAdapter)
        binding.layoutCloudStorage.cloudStorageList.adapter = concatAdapter
        binding.layoutCloudStorage.cloudStorageList.layoutManager = LinearLayoutManager(activity)
        binding.layoutCloudStorage.close.setOnClickListener {
            RoomOverlayManager.setShown(RoomOverlayManager.AREA_ID_CLOUD_STORAGE, false)
        }
        binding.layoutCloudStorage.cloudStorageList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val loadState = viewModel.state.value.loadUiState.append
                val size = viewModel.state.value.files.size
                val isLoading = loadState == LoadState.Loading
                val isFullLoaded = loadState is LoadState.NotLoading && loadState.end
                if (isLoading || isFullLoaded || recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    return
                }
                val layoutManager = binding.layoutCloudStorage.cloudStorageList.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisible >= size - 1) {
                    viewModel.loadMoreFileList()
                }
            }
        })

        binding.layoutCloudStorage.root.setOnClickListener {
            // block event
        }
    }

    private fun updateTitleByDir(dirPath: String) {
        if (dirPath == CLOUD_ROOT_DIR) {
            binding.layoutCloudStorage.cloudTitleImage.setImageResource(R.drawable.ic_class_room_cloud)
            binding.layoutCloudStorage.cloudTitle.text = activity.getString(R.string.title_cloud_storage)
            binding.layoutCloudStorage.cloudTitleImage.setOnClickListener(null)
        } else {
            binding.layoutCloudStorage.cloudTitleImage.setImageResource(R.drawable.ic_arrow_left)
            binding.layoutCloudStorage.cloudTitle.text = dirPath.folderName()
            binding.layoutCloudStorage.cloudTitleImage.setOnClickListener { viewModel.backFolder() }
        }
    }
}
