package com.trello.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.trello.R
import com.trello.adapters.BoardItemsAdapter
import com.trello.databinding.ActivityMainBinding
import com.trello.firbase.FireStoreClass
import com.trello.models.Board
import com.trello.models.User
import com.trello.utils.Constants
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mUserName : String
    private lateinit var mSharePreferences : SharedPreferences

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARDS_REQUEST_CODE: Int = 12
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        mSharePreferences = this.getSharedPreferences(Constants.T_Preferences, Context.MODE_PRIVATE)

        val tokenUpdated = mSharePreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if(tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().loadUserData(this,true)
        }else{
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
            }
        }

        FireStoreClass().loadUserData(this, true)

        binding.includes.fabCreateBoard.setOnClickListener{
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARDS_REQUEST_CODE)
        }

    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.includes.toolbarMainActivity)
        binding.includes.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        binding.includes.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }

    }

    fun updateNavigationUserDetails(user: User, readBoardList: Boolean){
        hideProgressBarDialog()
        mUserName = user.name

         Glide.with(this)
             .load(user.image)
             .centerCrop()
             .placeholder(R.drawable.ic_user_place_holder)
             .into(iv_user_image)

            tv_username.text = user.name

        if(readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStoreClass().getBoardsList(this)
        }
    }

    private fun toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARDS_REQUEST_CODE){
            FireStoreClass().getBoardsList(this)


        }else{
            Log.e("Cancled", "Cancled")
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                startActivityForResult(intent, MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharePreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateTokenSuccess(){
        hideProgressBarDialog()
        val editor : SharedPreferences.Editor = mSharePreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().loadUserData(this, true)
    }
    private fun updateFCMToken(token :String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }

     fun populateBoardsListToUI(boardList: ArrayList<Board>){
        hideProgressBarDialog()

        if(boardList.size > 0 ){
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model :Board){
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }

    }
}