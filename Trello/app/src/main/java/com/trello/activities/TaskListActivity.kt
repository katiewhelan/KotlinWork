package com.trello.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.trello.R
import com.trello.adapters.TaskListItemsAdapter
import com.trello.databinding.ActivityTaskListBinding
import com.trello.firbase.FireStoreClass
import com.trello.models.Board
import com.trello.models.Card
import com.trello.models.Task
import com.trello.models.User
import com.trello.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentId: String
     lateinit var mAssignedMembersDetailList: ArrayList<User>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this, mBoardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQuEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardDetails(this, mBoardDocumentId)
        }else{
            Log.e("Canceled", "Canceled")
        }
    }

    fun boardDetails(board : Board){
        mBoardDetails = board
        hideProgressBarDialog()
        setUpActionBar()



        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAssignedListMembersListDetails(this, mBoardDetails.assignedTo)

    }

    fun addUpdateTaskListSuccess(){
        hideProgressBarDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String){
        val task = Task(taskListName,FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this, mBoardDetails)
    }

        fun deleteTaskList(position: Int){
            mBoardDetails.taskList.removeAt(position)
            mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().addUpdateTaskList(this, mBoardDetails)
        }

    fun updateTaskList(position: Int, listName: String, model : Task){
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position]= task
          mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?):Boolean{
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQuEST_CODE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

     fun addCardToTaskList(position: Int, cardName : String){
         mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

         val cardAssignedUsersList : ArrayList<String> = ArrayList()
         cardAssignedUsersList.add(FireStoreClass().getCurrentUserId())

         val card = Card(cardName, FireStoreClass().getCurrentUserId(), cardAssignedUsersList)
         val cardList = mBoardDetails.taskList[position].cards
         cardList.add(card)
         val task = Task(mBoardDetails.taskList[position].title,
         mBoardDetails.taskList[position].createdBy, cardList)

         mBoardDetails.taskList[position] = task
         showProgressDialog(resources.getString(R.string.please_wait))
          FireStoreClass().addUpdateTaskList(this, mBoardDetails)
     }
    private fun setUpActionBar() {
        setSupportActionBar(binding.toolbarTaskListActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }
        binding.toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int){
        val intent  = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.CARD_LIST_POSITION, cardPosition)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun boardMembersList(list: ArrayList<User>){
        mAssignedMembersDetailList = list
        hideProgressBarDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false)

        binding.rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter
    }

        fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
            mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
            mBoardDetails.taskList[taskListPosition].cards = cards

            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().addUpdateTaskList(this, mBoardDetails)
        }

    companion object{
        const val MEMBERS_REQuEST_CODE : Int = 13
        const val CARD_DETAILS_REQUEST_CODE : Int = 14

    }
}