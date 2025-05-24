/*  Copyright 2019 devMiyax(smiyaxdev@gmail.com)

    This file is part of YabaSanshiro.

    YabaSanshiro is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    YabaSanshiro is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with YabaSanshiro; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/
package org.uoyabause.android.phone

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import java.util.Locale
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.frybits.harmony.getHarmonySharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.devmiyax.yabasanshiro.R
import org.uoyabause.android.GameInfo
import org.uoyabause.android.GameInfo.Companion.sigin
import org.uoyabause.android.YabauseStorage
import org.uoyabause.android.phone.GameItemAdapter.GameViewHolder
import java.io.File
import javax.sql.DataSource


class GameItemAdapter(private val originalDataSet: MutableList<GameInfo?>?) :
    RecyclerView.Adapter<GameViewHolder>(), Filterable {

    private var dataSet: MutableList<GameInfo?>? = originalDataSet?.toMutableList()
    private var currentSearchQuery: String = ""

    class GameViewHolder(var rootview: View) :
        RecyclerView.ViewHolder(rootview), View.OnCreateContextMenuListener {
        var textViewName: TextView
        var textViewVersion: TextView
        var imageViewIcon: ImageView
        var menuButton: ImageButton

        init {
            textViewName =
                rootview.findViewById<View>(R.id.textViewName) as TextView
            textViewVersion =
                rootview.findViewById<View>(R.id.textViewVersion) as TextView
            imageViewIcon =
                rootview.findViewById<View>(R.id.imageView) as ImageView

            if (Build.VERSION.SDK_INT > 23) {
                var card = rootview.findViewById<View>(R.id.card_view_main) as CardView
                card.setCardBackgroundColor(rootview.context.getColorStateList(R.color.card_view_selectable))
                card.isFocusable = true
                card.setOnCreateContextMenuListener(this)
            }

            menuButton = rootview.findViewById<View>(R.id.game_card_menu) as ImageButton



            /*
            ViewGroup.LayoutParams lp = imageViewIcon.getLayoutParams();
            lp.width = CARD_WIDTH;
            lp.height = CARD_HEIGHT;
            imageViewIcon.setLayoutParams(lp);
            */
        }

       override fun onCreateContextMenu(
           menu: ContextMenu?,
           v: View?,
           menuInfo: ContextMenu.ContextMenuInfo?,
       ) {
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cards_layout, parent, false)
        view.setOnClickListener(GameSelectFragmentPhone.myOnClickListener)


        // work here if you need to control height of your items
        // keep in mind that parent is RecyclerView in this case
        //val height = 10;
        //view.minimumHeight = height
/*
        if (parent?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            try {
                val lp: GridLayoutManager.LayoutParams =
                    view.getLayoutParams() as GridLayoutManager.LayoutParams

                val tv = TypedValue()
                if (parent.context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                        parent.resources.displayMetrics)
                    lp.height = (parent.getMeasuredHeight() - actionBarHeight - 48 )
                    view.setLayoutParams(lp)
                }

            } catch (e: Exception) {

            }
        }else{
            val tv = TypedValue()
            if (parent.context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    parent.resources.displayMetrics)
                view.minimumHeight = (parent.getMeasuredHeight() - actionBarHeight - 48 ) / 4
            }

        }
*/
        return GameViewHolder(view)
    }

    private var mListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, item: GameInfo?, v: View?)
        fun onGameRemoved(item: GameInfo?)
    }

    @SuppressLint("NotifyDataSetChanged", "CheckResult")
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val textViewName = holder.textViewName
        val textViewVersion = holder.textViewVersion
        val imageView = holder.imageViewIcon
        val ctx = holder.rootview.context
        val game = dataSet?.get(position)
        if (game != null) {
            textViewName.text = game.game_title

            // Set card appearance based on whether it's a cloud-only game
            val cardView = holder.rootview.findViewById<CardView>(R.id.card_view_main)

            if (game.isCloudOnly) {
                // Apply toned-down appearance for cloud-only games
                textViewName.alpha = 0.7f
                textViewVersion.text = ctx.getString(R.string.cloud_only_game)

                // Apply a semi-transparent overlay to the card
                cardView.alpha = 0.8f

                // Add cloud icon indicator
                textViewVersion.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.cloud_upload_48px, 0, 0, 0
                )
                textViewVersion.compoundDrawablePadding = 8
            } else {
                // Regular appearance for local games
                textViewName.alpha = 1.0f
                cardView.alpha = 1.0f
                textViewVersion.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

                // Set device information
                textViewVersion.text = ""
                if (game.device_infomation == "CD-1/1") {
                } else {
                    textViewVersion.text = game.device_infomation
                }

                // Update game state and rating
                CoroutineScope(Dispatchers.IO).launch {
                    game.updateState()
                    var rate = ""
                    for (i in 0 until game.rating) rate += "★"
                    if (game.device_infomation == "CD-1/1") {
                    } else {
                        rate += " " + game.device_infomation
                    }
                    withContext(Dispatchers.Main) {
                        textViewVersion.text = rate
                    }
                }
            }

            // Load image
            if (game.image_url != null && game.image_url != "") {
                if (game.image_url!!.startsWith("http")) {
                    var url = game.image_url
                    if (game.isCloudOnly) {
                        url += "?"+sigin
                    }
                    val glideRequest = Glide.with(imageView)
                        .load(url)


                    // Apply grayscale effect for cloud-only games
                    if (game.isCloudOnly) {
                        // Use Glide's built-in transformations
                        glideRequest.apply(RequestOptions.bitmapTransform(BlurTransformation(8)))
                    }

                    glideRequest.listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?,
                                                  target: com.bumptech.glide.request.target.Target<Drawable>?,
                                                  isFirstResource: Boolean): Boolean {
                            var mFirebaseAnalytics = FirebaseAnalytics.getInstance(ctx)
                            val bundle = Bundle()
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, game.product_number)
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, game.image_url)
                            mFirebaseAnalytics.logEvent(
                                "yab_fail_load_image", bundle
                            )
                            val ac = holder.rootview.context as Activity
                            ac?.runOnUiThread {
                                imageView.setImageDrawable(ac.getDrawable(R.drawable.missing))
                            }
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val ac = holder.rootview.context as Activity
                            ac?.runOnUiThread {
                                if (resource != null) imageView.setImageDrawable(resource)
                            }
                            return true
                        }
                    }).submit()
                } else {
                    Glide.with(holder.rootview.context)
                        .load(game.image_url?.let { File(it) })
                        .into(imageView)
                }
            }

            // Set click listener
            holder.rootview.setOnClickListener {
                if (null != mListener) {
                    mListener!!.onItemClick(position, dataSet?.get(position), null)
                }
            }
        }

        // Set menu button click listener
        holder.menuButton.setOnClickListener(View.OnClickListener {
            showPopupMenu(
                holder.menuButton,
                position
            )
        })
    }

    /**
     * Check if a game is already backed up to the cloud
     * @param gameInfo The GameInfo object to check
     * @param context The context to use for creating the GameBackupManager
     * @return true if the game is already backed up, false otherwise
     */
    private suspend fun isGameBackedUp(gameInfo: GameInfo, context: android.content.Context): Boolean {
        // Check if user is signed in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            return false
        }

        try {
            // Get the file path or URI
            val filePath = gameInfo.file_path

            // Calculate hash
            val hash = if (filePath.startsWith("content://")) {
                org.uoyabause.android.backup.GameBackupManager(context)
                    .calculateSHA256(android.net.Uri.parse(filePath))
            } else {
                org.uoyabause.android.backup.GameBackupManager(context)
                    .calculateSHA256(java.io.File(filePath))
            }

            // Check if this hash exists in the user's backups
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(auth.currentUser!!.uid)

            val existingBackup = userDocRef
                .collection("backups")
                .whereEqualTo("hash", hash)
                .get()
                .await()

            return !existingBackup.isEmpty
        } catch (e: Exception) {
            android.util.Log.e("GameItemAdapter", "Error checking if game is backed up: ${e.message}")
            return false
        }
    }

    private fun showPopupMenu(
        view: View,
        position: Int,
    ) { // inflate menu
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.getMenuInflater()

        val game = dataSet?.get(position)

        // Use different menu for cloud-only games
        if (game?.isCloudOnly == true) {
            inflater.inflate(R.menu.cloud_game_item_popup_menu, popup.getMenu())
        } else {
            inflater.inflate(R.menu.game_item_popup_menu, popup.getMenu())

            // Check if the game is already backed up to the cloud
            if (game != null) {
                // Launch coroutine to check if game is backed up
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val isBackedUp = isGameBackedUp(game, view.context)

                        // Update menu item text based on backup status
                        if (isBackedUp) {
                            popup.menu.findItem(R.id.backup_to_cloud)?.title =
                                view.context.getString(R.string.remove_from_cloud)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GameItemAdapter", "Error checking backup status: ${e.message}")
                    }
                }
            }
        }

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.download_from_cloud -> {
                    val game_info = dataSet?.get(position)!!
                    if (game_info.isCloudOnly && game_info.cloudBackupInfo != null) {
                        // Handle cloud-only game download - just click the item to download
                        if (null != mListener) {
                            mListener!!.onItemClick(position, dataSet?.get(position), null)
                        }
                        return@OnMenuItemClickListener true
                    }
                    return@OnMenuItemClickListener false
                }
                R.id.backup_to_cloud -> {
                    val game_info = dataSet?.get(position)!!

                    // Check if user is signed in
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser == null) {
                        Toast.makeText(
                            view.context,
                            "Please sign in to use cloud backup",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnMenuItemClickListener true
                    }

                    // Check if this is a backup or remove operation
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val isBackedUp = isGameBackedUp(game_info, view.context)

                            if (isBackedUp) {
                                // This is a remove operation
                                handleRemoveFromCloud(view, game_info)
                            } else {
                                // This is a backup operation
                                handleBackupToCloud(view, game_info)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("GameItemAdapter", "Error determining backup status: ${e.message}")
                            // Default to backup operation if we can't determine status
                            handleBackupToCloud(view, game_info)
                        }
                    }

                    true
                }
                R.id.detail -> {
                    val game_info = dataSet?.get(position)!!
                    // Firestoreからproduct_numberを使用してゲームのドキュメントIDを検索
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    db.collection("games")
                        .whereEqualTo("product_number", game_info.product_number)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                // ゲームが見つかった場合、そのドキュメントIDを使用してウェブページを開く
                                val gameDoc = documents.documents[0]
                                val gameId = gameDoc.id
                                val url = "https://www.yabasanshiro.com/games/${gameId}"
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                intent.data = android.net.Uri.parse(url)
                                view.context.startActivity(intent)
                            } else {
                                // ゲームが見つからない場合はエラーメッセージを表示
                                android.widget.Toast.makeText(
                                    view.context,
                                    "Game not found in database",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            // エラーが発生した場合はエラーメッセージを表示
                            android.widget.Toast.makeText(
                                view.context,
                                "Error: ${e.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            android.util.Log.e("GameItemAdapter", "Error querying games collection", e)
                        }
                }
                R.id.restore_defaults -> {
                    var game_info = dataSet?.get(position)!!
                    val gamePreference = view.context.getHarmonySharedPreferences(game_info.product_number)
                    gamePreference.edit().clear().apply()
                }
                R.id.report -> {
                    val game_info = dataSet?.get(position)!!
                    val reportDialog = org.uoyabause.android.ReportDialog( view.context, game_info.product_number )
                    val fragmentManager = (view.context as androidx.fragment.app.FragmentActivity).supportFragmentManager
                    reportDialog.show(fragmentManager, "ReportDialog")
                }
                R.id.delete -> {
/*
                    val prefs = view.context.getSharedPreferences("private", Context.MODE_PRIVATE)
                    val hasDonated = prefs.getBoolean("donated", false)

                    if (!BuildConfig.BUILD_TYPE.equals("pro") && !hasDonated) {
                        AlertDialog.Builder(view.context)
                                .setTitle(R.string.not_available)
                                .setMessage(R.string.only_pro_version)
                                .setPositiveButton(R.string.got_it) { _, _ ->
                                    val url = "https://play.google.com/store/apps/details?id=org.uoyabause.uranus.pro"
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(url)
                                    intent.setPackage("com.android.vending")
                                    view.context.startActivity(intent)
                                }.setNegativeButton(R.string.cancel) { _, _ ->
                                }
                                .show()
                    } else {

 */
                        Log.d("textext", "R.id.delete is selected")
                        AlertDialog.Builder(view.context)
                                .setTitle(R.string.delete_confirm_title)
                                .setMessage(R.string.delete_confirm)
                                .setPositiveButton(R.string.ok) { _, _ ->


                                    var game_info = dataSet?.get(position)!!

                                    GlobalScope.launch(Dispatchers.IO) {
                                        YabauseStorage.dao.delete(game_info)
                                        game_info.removeInstance()
                                        withContext(Dispatchers.Main) {
                                            dataSet.removeAt(position)
                                            mListener?.onGameRemoved(game_info)
                                            notifyItemRemoved(position)
                                        }
                                    }


                                    notifyItemRemoved(position)
                                }
                                .setNegativeButton(R.string.no) { _, _ ->
                                }
                                .show()
                    }
                else -> {
                    Log.d("textext", "Unknown value (value = $it.itemId)")
                }
            }
            true
        })
        popup.show()
    }

    override fun getItemCount(): Int {
        return dataSet!!.size
    }

    fun removeItem(id: Int) {
        val index = dataSet?.indexOfFirst({ it!!.id == id })
        if (index != null && index != -1) {
            dataSet?.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    // 検索機能
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase(Locale.getDefault()) ?: ""
                currentSearchQuery = query

                val filteredList = if (query.isEmpty()) {
                    originalDataSet?.toMutableList()
                } else {
                    originalDataSet?.filter {
                        it?.game_title?.lowercase(Locale.getDefault())?.contains(query) == true
                    }?.toMutableList()
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                dataSet = results?.values as? MutableList<GameInfo?>
                notifyDataSetChanged()
            }
        }
    }

    // ソート機能
    fun sortByName() {
        dataSet?.sortBy { it?.game_title?.lowercase(Locale.getDefault()) }
        notifyDataSetChanged()
    }

    fun sortByDate() {
        dataSet?.sortBy { it?.release_date }
        notifyDataSetChanged()
    }

    fun sortByRecentlyPlayed() {
        dataSet?.sortByDescending { it?.lastplay_date }
        notifyDataSetChanged()
    }

    // 現在のフィルタリング状態を維持したままソート
    fun applyCurrentFilter() {
        if (currentSearchQuery.isNotEmpty()) {
            val filter = filter
            filter.filter(currentSearchQuery)
        }
    }

    /**
     * Handle the "Backup to Cloud" operation
     * @param view The view that triggered the operation
     * @param gameInfo The GameInfo object to backup
     */
    private fun handleBackupToCloud(view: View, gameInfo: GameInfo) {
        // Show legal warning dialog
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle(view.context.getString(R.string.legal_warning_title))
        builder.setMessage(view.context.getString(R.string.legal_warning_message))
        builder.setPositiveButton(view.context.getString(R.string.yes)) { dialog, which ->
            // Start backup process
            val gameBackupManager = org.uoyabause.android.backup.GameBackupManager(view.context)

            // Launch coroutine to perform backup
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Perform backup
                    val result = gameBackupManager.backupGame(gameInfo)

                    // Check if backup limit was reached
                    if (!result.success && result.limitReached && result.backupList.isNotEmpty()) {
                        // Show the replacement dialog
                        org.uoyabause.android.backup.BackupReplaceDialog.show(
                            view.context,
                            result.backupList
                        ) { backupToReplace ->
                            // User selected a backup to replace
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    // Show progress dialog
                                    val replaceProgressDialog = ProgressDialog(view.context)
                                    replaceProgressDialog.setMessage("Replacing backup...")
                                    replaceProgressDialog.setCancelable(false)
                                    replaceProgressDialog.show()

                                    // Perform replacement
                                    val replaceResult = gameBackupManager.replaceBackup(gameInfo, backupToReplace)

                                    // Dismiss progress dialog
                                    replaceProgressDialog.dismiss()

                                    // Show result
                                    if (replaceResult.success) {
                                        Toast.makeText(
                                            view.context,
                                            view.context.getString(R.string.replace_backup_success),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            view.context,
                                            "${view.context.getString(R.string.replace_backup_failed)}: ${replaceResult.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        view.context,
                                        "${view.context.getString(R.string.replace_backup_failed)}: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        return@launch
                    }

                    // Show progress dialog
                    val progressDialog = ProgressDialog(view.context)
                    progressDialog.setMessage("Backing up game...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    // If we got here, either the backup was successful or there was an error
                    // that wasn't related to the backup limit

                    // Dismiss progress dialog
                    progressDialog.dismiss()

                    // Show result
                    if (result.success) {
                        Toast.makeText(
                            view.context,
                            view.context.getString(R.string.backup_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            view.context,
                            "${view.context.getString(R.string.backup_failed)}: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        view.context,
                        "${view.context.getString(R.string.backup_failed)}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        builder.setNegativeButton(view.context.getString(R.string.no)) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    /**
     * Handle the "Remove from Cloud" operation
     * @param view The view that triggered the operation
     * @param gameInfo The GameInfo object to remove from cloud
     */
    private fun handleRemoveFromCloud(view: View, gameInfo: GameInfo) {
        // Show confirmation dialog
        val builder = AlertDialog.Builder(view.context)
        builder.setTitle(view.context.getString(R.string.remove_from_cloud))
        builder.setMessage("Are you sure you want to remove this game from the cloud?")
        builder.setPositiveButton(view.context.getString(R.string.yes)) { dialog, which ->
            // Start remove process
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Show progress dialog
                    val progressDialog = ProgressDialog(view.context)
                    progressDialog.setMessage("Removing game from cloud...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    // First get the list of backed up games
                    val gameBackupManager = org.uoyabause.android.backup.GameBackupManager(view.context)
                    val backedUpGames = gameBackupManager.getBackedUpGames()

                    // Find the matching backup by product number
                    val backupToDelete = backedUpGames.find { it.productNumber == gameInfo.product_number }

                    if (backupToDelete != null) {
                        // Delete the backup
                        val result = gameBackupManager.deleteBackup(backupToDelete)

                        // Dismiss progress dialog
                        progressDialog.dismiss()

                        // Show result
                        if (result.success) {
                            Toast.makeText(
                                view.context,
                                view.context.getString(R.string.remove_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                view.context,
                                "${view.context.getString(R.string.remove_failed)}: ${result.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        // Dismiss progress dialog
                        progressDialog.dismiss()

                        // Show error
                        Toast.makeText(
                            view.context,
                            "${view.context.getString(R.string.remove_failed)}: Backup not found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        view.context,
                        "${view.context.getString(R.string.remove_failed)}: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        builder.setNegativeButton(view.context.getString(R.string.no)) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    companion object {
        private const val CARD_WIDTH = 320
        private const val CARD_HEIGHT = 224
    }
}
