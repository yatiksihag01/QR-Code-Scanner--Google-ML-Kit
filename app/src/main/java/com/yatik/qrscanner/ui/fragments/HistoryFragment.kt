package com.yatik.qrscanner.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yatik.qrscanner.*
import com.yatik.qrscanner.adapters.BarcodeListAdapter
import com.yatik.qrscanner.databinding.FragmentHistoryBinding
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.ui.BarcodeViewModel
import com.yatik.qrscanner.ui.DetailsActivity
import com.yatik.qrscanner.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarcodeListAdapter

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
                    dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background))
                    dialog.show()
                    dialog.makeButtonTextRed()
                    vibrate()
                    true
                } else -> false
            }

        }

        binding.historyToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }

        recyclerView = binding.historyRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = BarcodeListAdapter()
        recyclerView.adapter = adapter

        adapter.setOnDeleteClickListener { deleteDialog(it) }

        adapter.setOnItemClickListener { barcodeData ->

            requireActivity().intent = Intent(requireContext(), DetailsActivity::class.java)
                .putExtra("barcodeData", barcodeData)
                .putExtra("retrievedFrom", "database")
            startActivity(requireActivity().intent)
        }

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

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val barcodeData = adapter.differ.currentList[position]
                barcodeViewModel.delete(barcodeData)
                Snackbar.make(view, "Item deleted successfully", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        barcodeViewModel.insert(barcodeData)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.historyRecyclerView)
        }

        barcodeViewModel.getAllBarcodes().observe(viewLifecycleOwner) { barcodesData ->
            barcodesData?.let { itemsList ->
                if (itemsList.isEmpty()) {
                    binding.noItemInHistory.root.visibility = View.VISIBLE
                } else {
                    binding.noItemInHistory.root.visibility = View.GONE
                }
                adapter.differ.submitList(itemsList)
            }
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
        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background))
        dialog.show()
        dialog.makeButtonTextBlue()
        view?.let { Snackbar.make(it, "You can simply swipe to delete", Snackbar.LENGTH_LONG).show() }

    }

    private fun AlertDialog.makeButtonTextRed() {
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.redButton))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
    }

    private fun AlertDialog.makeButtonTextBlue() {
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireActivity().getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(250, 125))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(250)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}