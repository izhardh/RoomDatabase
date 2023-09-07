package com.example.roomdatabase

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.R
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdatabase.databinding.ActivityMainBinding
import com.example.roomdatabase.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
        val employeeDao = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDao)
        }
        lifecycleScope.launch {
            employeeDao.fetchAllEmployees().collect{
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list, employeeDao)
            }
        }

    }

    fun addRecord(employeeDao: EmployeeDao){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmail?.text.toString()

        if(name.isNotEmpty() && email.isNotEmpty()){
            lifecycleScope.launch {
                employeeDao.insert(EmployeeEntity(name = name, email = email))
                Toast.makeText(applicationContext, "Record Save", Toast.LENGTH_SHORT).show()
                binding?.etName?.text?.clear()
                binding?.etEmail?.text?.clear()
            }
        }else{
            Toast.makeText(applicationContext, "Name and Email Cannot Empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListOfDataIntoRecyclerView(
        employeeList: ArrayList<EmployeeEntity>,
        employeeDao: EmployeeDao){
        if (employeeList.isNotEmpty()) {


            // Adapter class is initialized and list is passed in the param.
            val itemAdapter = ItemAdapter(employeeList,{updateId ->
                updateRecordDialog(updateId,employeeDao)
            }){ deleteId->
                lifecycleScope.launch {
                    employeeDao.fetchAllEmployeesById(deleteId).collect {
                        if (it != null) {
                            deleteRecordAlertDialog(deleteId, employeeDao, it)
                        }
                    }
                }

            }
            // Set the LayoutManager that this RecyclerView will use.
            binding?.rvItemLists?.layoutManager = LinearLayoutManager(this)
            // adapter instance is set to the recyclerview to inflate the items.
            binding?.rvItemLists?.adapter = itemAdapter
            binding?.rvItemLists?.visibility = View.VISIBLE
            binding?.tvNoRecords?.visibility = View.GONE
        } else {

            binding?.rvItemLists?.visibility = View.GONE
            binding?.tvNoRecords?.visibility = View.VISIBLE
        }
//        if(employeeList.isNotEmpty()){
//            val itemAdapter = ItemAdapter(employeeList,{
//
//                updateId -> updateRecordDialog(updateId, employeeDao)
//
//            },{
//                deleteId -> deleteRecordAlertDialog(deleteId, employeeDao, it)
//            }
//            )
////            binding?.rvItemLists?.layoutManager = LinearLayoutManager(this)
////            binding?.rvItemLists?.adapter = itemAdapter
////            binding?.rvItemLists?.visibility = View.VISIBLE
////            binding?.tvNoRecords?.visibility = View.GONE
////        }else{
////            binding?.rvItemLists?.visibility = View.GONE
////            binding?.tvNoRecords?.visibility = View.VISIBLE
//        }

    }

    private fun updateRecordDialog(id:Int, employeeDao: EmployeeDao){
        val updateDialog = Dialog(this, R.style.Theme_AppCompat_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchAllEmployeesById(id).collect{
                binding.etUpdateName.setText(it.name)
                binding.etUpdateEmailId.setText(it.email)
            }
        }

        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()

            if(name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id,name,email))
                    Toast.makeText(applicationContext, "Record Updated.", Toast.LENGTH_LONG )
                        .show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(applicationContext, "Name or Email cannot be blank.", Toast.LENGTH_LONG )
                    .show()
            }
        }
        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }
        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id:Int,employeeDao: EmployeeDao,employee: EmployeeEntity) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Delete Record")
        //set message for alert dialog
        builder.setMessage("Are you sure you wants to delete ${employee.name}.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(
                    applicationContext,
                    "Record deleted successfully.",
                    Toast.LENGTH_LONG
                ).show()

                dialogInterface.dismiss() // Dialog will be dismissed
            }

        }


        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
}