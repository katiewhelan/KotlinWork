package com.trello.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Build

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager

import com.trello.R
import com.trello.adapters.CardMembersListItemsAdapter
import com.trello.databinding.ActivityCardDetailsBinding
import com.trello.dialogs.LabelColorListDialog
import com.trello.dialogs.MembersListDialog
import com.trello.firbase.FireStoreClass
import com.trello.models.*
import com.trello.utils.Constants
import kotlinx.android.synthetic.main.activity_card_details.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    private lateinit var binding : ActivityCardDetailsBinding
    private lateinit var  mBoardDetails : Board
    private var mTaskListPosition = -1
    private var mCardListPosition = -1
    private var mSelectedColor = ""
    private var mSelectedDueDateMilliSeconds: Long = 0
    private lateinit var mAssignedMembersList : ArrayList<User>
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()
        setUpActionBar()

        binding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor

            if(mSelectedColor.isNotEmpty()){
                setColor()
            }
//        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString())
        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this@CardDetailsActivity, "Enter a card name", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.tvSelectLabelColor.setOnClickListener {
            labelColorListDialog()
        }

        binding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        setupSelectedMembersList()

        mSelectedDueDateMilliSeconds = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate

        if(mSelectedDueDateMilliSeconds > 0){
            val sdp = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
            val selectedDate = sdp.format(Date(mSelectedDueDateMilliSeconds))
            binding.tvSelectDueDate.text = selectedDate
        }

        binding.tvSelectDueDate.setOnClickListener {
            displayDatePicker()
        }
    }

    fun addUpdateTaskListSuccess(){
        hideProgressBarDialog()
        setResult(Activity.RESULT_OK)
        finish()

    }


    private fun setUpActionBar(){
        setSupportActionBar(binding.toolbarCardDetailsActivity)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name

        }
        binding.toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }

    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }

        if(intent.hasExtra(Constants.CARD_LIST_POSITION)){
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_POSITION, -1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mAssignedMembersList = intent.getParcelableArrayListExtra<User>(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun updateCardDetails(){
        val card = Card(binding.etNameCardDetails.text.toString(), mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo, mSelectedColor, mSelectedDueDateMilliSeconds)
        val taskList : ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size -1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card


        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun deleteCard(){
        val cardsList : ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardListPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size- 1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }
    private fun colorsList():ArrayList<String>{
        val colorsList :ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")
        return colorsList
    }

    private fun setColor(){
        binding.tvSelectLabelColor.text = ""
        //binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))


    }

    private fun labelColorListDialog(){
        val colorsList : ArrayList<String> = colorsList()

        val listDialog = object: LabelColorListDialog(
            this, colorsList, resources.getString(R.string.str_select_label_color), mSelectedColor) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun membersListDialog(){
        val cardMemberList  = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if(cardMemberList.size > 0 ){
            for(i in mAssignedMembersList.indices){
                for(j in cardMemberList){
                    if(mAssignedMembersList[i].id == j) {
                        mAssignedMembersList[i].selected = true
                    }
                }
            }
        }else{
            for(k in mAssignedMembersList.indices){
                mAssignedMembersList[k].selected = false
            }
        }

        val memberDialog = object: MembersListDialog(
            this,
            mAssignedMembersList,
            resources.getString(R.string.select_members)){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(user.id)

                    for(i in mAssignedMembersList.indices){
                        if(mAssignedMembersList[i].id == user.id){
                            mAssignedMembersList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()

            }
            }.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()


            for (i in mAssignedMembersList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mAssignedMembersList[i].id == j) {
                        val selectedMember = SelectedMembers(
                            mAssignedMembersList[i].id,
                            mAssignedMembersList[i].image
                        )
                        selectedMembersList.add(selectedMember)
                    }

            }
            if (selectedMembersList.size > 0) {
                selectedMembersList.add(SelectedMembers("", ""))

                binding.tvSelectMembers.visibility = View.GONE
                binding.rvSelectedMembersList.visibility = View.VISIBLE

                binding.rvSelectedMembersList.layoutManager = GridLayoutManager(
                    this, 6
                )
                val adapter = CardMembersListItemsAdapter(this, selectedMembersList, true)
                binding.rvSelectedMembersList.adapter = adapter

                adapter.setOnClickListener(
                    object : CardMembersListItemsAdapter.OnClickListener {
                        override fun onClick() {
                            membersListDialog()
                        }
                    }
                )
            }
         else {
                binding.tvSelectMembers.visibility = View.VISIBLE
                binding.rvSelectedMembersList.visibility = View.GONE
            }
        }
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(resources.getString(R.string.alert))

        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)


        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }

        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun displayDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
            val sMonthOfYear =
                if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
            val selectedDate = "$sMonthOfYear/$sDayOfMonth/$year"

            binding.tvSelectDueDate.text = selectedDate

            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)

            val theDate = sdf.parse(selectedDate)

            mSelectedDueDateMilliSeconds = theDate!!.time
        },
            year ,
            month,
            day
        ).show()
    }

}