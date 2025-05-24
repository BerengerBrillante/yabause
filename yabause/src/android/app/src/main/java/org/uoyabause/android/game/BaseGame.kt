package org.uoyabause.android.game

import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LeaderBoard(val title: String, val id: String)

abstract interface GameUiEvent {
    abstract fun onNewRecord(leaderBoardId: String)
}

abstract class BaseGame {

    // gameCodeからgameIdの取得
    suspend fun initGameId(gameCode: String) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val task = db.collection("games")
            .whereEqualTo("product_number", gameCode)
            .get()
        
        // タスクが完了するまで待機
        val documents = Tasks.await(task)
        
        if (!documents.isEmpty) {
            // leaderboardIdフィールドがある場合はその値を使用
            if (documents.documents[0].get("leaderboardId") != null) {
                gameId = documents.documents[0].getString("leaderboardId")
                    ?: documents.documents[0].id
            } else {
                // なければドキュメントIDを使用
                gameId = documents.documents[0].id
            }
        }
    }

    var gameId: String = ""
    var leaderBoards: MutableList<LeaderBoard>? = null

    lateinit var uievent: GameUiEvent
    fun setUiEvent(uievent: GameUiEvent) {
        this.uievent = uievent
    }
    abstract fun onBackUpUpdated(before: ByteArray, after: ByteArray)
}
