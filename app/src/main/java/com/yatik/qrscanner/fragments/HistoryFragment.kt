package com.yatik.qrscanner.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatik.qrscanner.*
import com.yatik.qrscanner.database.BarcodeData
import com.yatik.qrscanner.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarcodeListAdapter
    private val barcodeViewModel: BarcodeViewModel by viewModels {
        BarcodeViewModelFactory((activity?.application as BarcodeDataApplication).repository)
    }

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
        adapter = BarcodeListAdapter()
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : BarcodeListAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val barcodeData = barcodeViewModel.allBarcodes.value?.get(position)
                val format = barcodeData?.format
                val title = barcodeData?.title
                val decryptedText = barcodeData?.decryptedText
                val others = barcodeData?.others
                val valueType = barcodeData?.type
                requireActivity().intent = Intent(requireContext(), DetailsActivity::class.java)
                    .putExtra("format", format)
                    .putExtra("title", title)
                    .putExtra("decryptedText", decryptedText)
                    .putExtra("others", others)
                    .putExtra("valueType", valueType)
                    .putExtra("retrievedFrom", "database")
                startActivity(requireActivity().intent)
            }

            override fun onDeleteClick(position: Int) {
                val barcodeData = barcodeViewModel.allBarcodes.value?.get(position)
                deleteDialog(barcodeData)
            }

        })

        barcodeViewModel.allBarcodes.observe(viewLifecycleOwner) { barcodesData ->
            // Update the cached copy of the words in the adapter.
            barcodesData?.let { adapter.submitList(it) }
        }
    }

    private fun deleteDialog(barcodeData: BarcodeData?){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete")
        builder.setMessage("Are you sure to delete this item?")
        builder.setPositiveButton("Yes") { _, _ ->
            if (barcodeData != null) {
                barcodeViewModel.delete(barcodeData)
            } else {
                Toast.makeText(activity, "Sorry, Unable to delete this item", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.makeButtonTextBlue()


    }
    private fun AlertDialog.makeButtonTextBlue() {
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}