package com.trello.firbase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.trello.activities.*
import com.trello.models.Board
import com.trello.models.Card
import com.trello.models.Task
import com.trello.models.User
import com.trello.utils.Constants
import kotlinx.android.synthetic.main.item_task.view.*

class FireStoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo : User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener { activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressBarDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun loadUserData(activity: Activity, readBoardList : Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.e("YOU ARE HERE 112123", document.toString())
                val loggedInUser = document.toObject(User::class.java)!!
                Log.e("YOU ARE HERE", "$loggedInUser.name, $loggedInUser.image, $loggedInUser.email")
                when(activity){
                    is SignInActivity ->{
                        activity.signInSuccess(loggedInUser)
                        Log.e("YOU ARE HERE", "Sign in activity")
                    }
                    is MainActivity->{
                        activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        Log.e("YOU ARE HERE", "Main Activity")
                    }
                    is MyProfileActivity->{
                        activity.setUserDataInUi(loggedInUser)
                        Log.e("YOU ARE HERE", "Profile Activity")
                    }
                }
            }
            .addOnFailureListener{ e ->
                when(activity){
                    is SignInActivity ->{
                        activity.hideProgressBarDialog()
                    }
                    is MainActivity-> {
                        activity.hideProgressBarDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressBarDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error reading Document")

            }
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                Log.i("Get Boards", document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardsListToUI(boardList)
            }
            .addOnFailureListener{
                    e ->
                activity.hideProgressBarDialog()
                Log.e("Get Board", "board failed to load", e)
            }
    }

    fun createBoard(activity : CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e("BoardActivity" ,"Board Created")
                Toast.makeText(activity, "Board Created", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccess()
            }
            .addOnFailureListener() {
                e ->
                activity.hideProgressBarDialog()
                Log.e("Create Board", "board failed to create", e)
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.e("Profile Updated", "user Profile Updated")
                Toast.makeText(activity, "UserProfile Updated", Toast.LENGTH_SHORT).show()
                if(activity is MyProfileActivity) {
                    activity.profileUpdateSuccess()
                }else if(activity is MainActivity){
                    activity.updateTokenSuccess()
            }
            }.addOnFailureListener{
                    e ->
                if(activity is MyProfileActivity) {
                    activity.hideProgressBarDialog()
                } else if(activity is MyProfileActivity){
                    activity.hideProgressBarDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error while creating profile",e)
            }
        Toast.makeText(activity, "UserProfile Error", Toast.LENGTH_SHORT).show()

    }

    fun getCurrentUserId():String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun addUpdateTaskList(activity: Activity,board: Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.Task_List] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e("TASK LIST PASS", "tasklist updated")
                if(activity is TaskListActivity){
                    activity.addUpdateTaskListSuccess()
                } else if(activity is CardDetailsActivity){
                    activity.addUpdateTaskListSuccess()
                }
            }
            .addOnFailureListener{
                e->
                if(activity is TaskListActivity){
                    activity.hideProgressBarDialog()
                } else if(activity is CardDetailsActivity){
                    activity.hideProgressBarDialog()
                }

                Log.e("TASK LIST FAIL", "tasklist failed",e)
            }
    }

    fun getAssignedListMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener{
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val userList : ArrayList<User> = ArrayList()

                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    userList.add(user)
                }
                if (activity is MembersActivity) {
                    activity.setUpMembersList(userList)
                } else if(activity is TaskListActivity){
                    activity.boardMembersList(userList)
                }
            }
            .addOnFailureListener{
                e ->
                if(activity is MembersActivity){
                    activity.hideProgressBarDialog()
                }else if(activity is TaskListActivity){
                    activity.hideProgressBarDialog()
                }

                Log.e(activity.javaClass.simpleName, "Error while creating", e)
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.i("Get Boards", document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
              activity.boardDetails(board)

            }
            .addOnFailureListener{
                    e ->
                activity.hideProgressBarDialog()
                Log.e("Get Board", "board failed to load", e)
            }

    }

    fun assignMemberTOBoard(activity: MembersActivity, board: Board, user:User){
        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {  activity.memberAssignedSuccess(user)}
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "error", e)

            }

    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if(document.documents.size > 0 ){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else {
                    activity.hideProgressBarDialog()
                    activity.showErrorSnackBar("No Member with that email")
                }
            }
            .addOnFailureListener{ e ->
                activity.hideProgressBarDialog()
                Log.e(activity.javaClass.simpleName,"Error getting user details",e)
            }

    }


}