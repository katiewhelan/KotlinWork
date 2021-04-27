package com.trello.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat.startActivityForResult

object Constants{
    const val BOARDS = "boards"
    const val USERS = "Users"
    const val IMAGE = "image"
    const val NAME = "name"
    const val MOBILE = "mobile"
    const val ASSIGNED_TO  = "assignedTo"
    const val READ_STORAGE_PERMISSION_CODE= 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID : String = "documentId"
    const val Task_List : String = "taskList"
    const val BOARD_DETAIL : String = "board_detail"
    const val SELECT: String = "select"
    const val UN_SELECT : String = "un-select"
    const val ID : String = "id"
    const val EMAIL :String ="email"
    const val TASK_LIST_ITEM_POSITION ="task list item position"
    const val CARD_LIST_POSITION = "Card LIST Position"
    const val BOARD_MEMBERS_LIST : String = "board-Members-list"
    const val T_Preferences: String = "T Preferences"
    const val FCM_TOKEN_UPDATED: String = "FCM Token Updated"
    const val FCM_TOKEN :String = "fcmToken"

    fun showImageChooser(activity: Activity){
        var galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?) :String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}