package com.trello.activities

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.trello.R
import com.trello.adapters.MembersListItemAdapter
import com.trello.databinding.ActivityMembersBinding
import com.trello.firbase.FireStoreClass
import com.trello.models.Board
import com.trello.models.User
import com.trello.utils.Constants
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {
    private lateinit var binding : ActivityMembersBinding
    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangesMade: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()

        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedListMembersListDetails(this, mBoardDetails.assignedTo)
    }

    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarMembersActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }
        binding.toolbarMembersActivity.setNavigationOnClickListener { onBackPressed() }

    }

    fun memberDetails(user:User){
        mBoardDetails.assignedTo.add(user.id)
        FireStoreClass().assignMemberTOBoard(this, mBoardDetails, user)
    }

    fun setUpMembersList(list: ArrayList<User>){
        mAssignedMembersList = list
        hideProgressBarDialog()
        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        val adapter = MembersListItemAdapter(this, list)

        binding.rvMembersList.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        when(item.itemId){
            R.id.action_add_member ->{
                dialogSearchMember()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun memberAssignedSuccess(user: User){
        hideProgressBarDialog()
        mAssignedMembersList.add(user)
        setUpMembersList(mAssignedMembersList)

        anyChangesMade = true
    }

    override fun onBackPressed() {
        if(anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
            val email = dialog.et_email_search_member.text.toString()
            if(email.isNotEmpty()){
                showProgressDialog(resources.getString(R.string.please_wait))
                 FireStoreClass().getMemberDetails(this, email)
                dialog.dismiss()

            }else{
                Toast.makeText(this, "Please search a name", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}