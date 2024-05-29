/*
 * Copyright 2023 Yatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yatik.qrscanner.ui.fragments.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yatik.qrscanner.R
import com.yatik.qrscanner.adapters.history.HistoryAdapter
import com.yatik.qrscanner.databinding.FragmentHistoryBinding
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.ui.MainActivity
import com.yatik.qrscanner.utils.Utilities
import com.yatik.qrscanner.utils.Utilities.Companion.getColorFromAttr
import com.yatik.qrscanner.utils.Utilities.Companion.makeButtonTextTeal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var searchView: SearchView
    private val barcodeViewModel: BarcodeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = binding.historyRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        binding.historyToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }

        setMenu()
        setupSwipeAction(view)

        adapter.setOnDeleteClickListener { deleteDialog(it) }
        adapter.setOnItemClickListener { barcodeDetails ->
            val bundle = Bundle().apply {
                putParcelable("barcodeDetails", barcodeDetails)
            }
            findNavController().navigate(
                R.id.action_historyFragment_to_detailsFragment,
                bundle
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            barcodeViewModel.combinedFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        adapter.addLoadStateListener { loadState ->
            binding.noItemInHistory.root.visibility = if (
                loadState.source.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached
                && adapter.itemCount < 1
            ) View.VISIBLE else View.GONE
        }
    }

    private fun setMenu() {
        val menu = binding.historyToolbar.menu
        searchView = menu.findItem(R.id.search_history).actionView as SearchView
        setupSearch(searchView)
        binding.historyToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_all -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("WARNING")
                        .setIcon(R.drawable.warning_24)
                        .setMessage(R.string.deleteAllWarning)
                        .setPositiveButton("Yes") { _, _ ->
                            barcodeViewModel.deleteAll()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    val dialog = builder.create()
                    dialog.window?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.dialog_background
                        )
                    )
                    dialog.show()
                    dialog.makeButtonTextRed()
                    Utilities().vibrateIfAllowed(requireContext(), true, 250)
                    true
                }

                else -> false
            }
        }
    }

    private fun setupSwipeAction(view: View) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun isLongPressDragEnabled(): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val barcodeDetails = adapter.getItemOnPosition(position)
                barcodeViewModel.delete(barcodeDetails)
                Snackbar.make(view, getString(R.string.deleted_item), Snackbar.LENGTH_LONG).apply {
                    setAction(getString(R.string.undo)) {
                        barcodeViewModel.undoDeletion(barcodeDetails)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.historyRecyclerView)
        }
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.queryHint = getString(R.string.searchHint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchQuery: String?): Boolean {
                return if (searchQuery != null) {
                    barcodeViewModel.searchFromBarcodes(searchQuery)
                    searchView.clearFocus()
                    true
                } else false
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                return if (searchQuery != null) {
                    barcodeViewModel.searchFromBarcodes(searchQuery)
                    true
                } else false
            }

        })
    }

    private fun deleteDialog(barcodeDetails: BarcodeDetails?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.delete)
        builder.setMessage(R.string.delete_confirmation)
        builder.setPositiveButton(R.string.yes) { _, _ ->
            if (barcodeDetails != null) {
                barcodeViewModel.delete(barcodeDetails)
                view?.let {
                    Snackbar.make(it, getString(R.string.swipe_delete_info), Snackbar.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.cannot_delete_warning,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            view?.let {
                Snackbar.make(it, getString(R.string.swipe_delete_info), Snackbar.LENGTH_LONG)
                    .show()
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dialog_background
            )
        )
        dialog.show()
        dialog.makeButtonTextTeal(requireContext())

    }

    private fun AlertDialog.makeButtonTextRed() {
        this.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(context, R.color.redButton))
        this.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(
                requireContext().getColorFromAttr(
                    com.google.android.material.R.attr.colorSecondaryVariant
                )
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}