package org.uoyabause.android.game

import android.os.Bundle
import android.util.Log
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.devmiyax.yabasanshiro.R
import org.uoyabause.android.YabauseApplication

/*
  Offset: 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F
00000000: 00 00 00 01 00 00 00 00 00 00 00 00 01 01 06 11    ................
00000010: 00 00 [07 A4] 00 00 [26 B2] 02 00 [69 78] 03 00 [69 78]    ...$..&2..ix..ix
00000020: 04 00 1F 40 00 00 69 78 01 00 69 78 02 00 69 78    ...@..ix..ix..ix
00000030: 03 00 1F 40 04 00 69 78 00 00 69 78 01 00 69 78    ...@..ix..ix..ix
00000040: 02 00 1F 40 03 00 69 78 04 00 69 78 00 00 69 78    ...@..ix..ix..ix
00000050: 01 00 1F 40 02 00 69 78 03 00 69 78 04 00 69 78    ...@..ix..ix..ix
00000060: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00    ................
00000070: 00 00 00 00 00 00 00 00 00 00 00 00 01 01 06 11    ........\>
00000080: 00 00 1F 40 01 00 69 78 02 00 69 78 03 00 69 78    ...@..ix..ix..ix
00000090: 04 00 1F 40 00 00 69 78 01 00 69 78 02 00 69 78    ...@..ix..ix..ix
000000a0: 03 00 1F 40 04 00 69 78 00 00 69 78 01 00 69 78    ...@..ix..ix..ix
000000b0: 02 00 1F 40 03 00 69 78 04 00 69 78 00 00 69 78    ...@..ix..ix..ix
000000c0: 01 00 1F 40 02 00 69 78 03 00 69 78 04 00 69 78    ...@..ix..ix..ix
000000d0: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00    ................
000000e0: 00 00 00 00 00 00 00 00 00 00 00 00 01 01 06 11    ................
000000f0: 00 00 1F 40 01 00 69 78 02 00 69 78 03 00 69 78    ...@..ix..ix..ix
00000100: 04 00 1F 40 00 00 69 78 01 00 69 78 02 00 69 78    ...@..ix..ix..ix
00000110: 03 00 1F 40 04 00 69 78 00 00 69 78 01 00 69 78    ...@..ix..ix..ix
00000120: 02 00 1F 40 03 00 69 78 04 00 69 78 00 00 69 78    ...@..ix..ix..ix
00000130: 01 00 1F 40 02 00 69 78 03 00 69 78 04 00 69 78    ...@..ix..ix..ix
00000140: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00    ................
*/

class SonicRRecord {
    var lapRecord: Int = 0
    var courseRecord: Int = 0
    var tagRecord: Int = 0
    var balloonRecord: Int = 0
}

class SonicRBackup {
    var records: MutableList<SonicRRecord> = mutableListOf<SonicRRecord>()
    var totalTime: Long = 0

    constructor(bin: ByteArray) {
        totalTime = 0
        for (i in 0..4) {
            var record = SonicRRecord()
            val si = i * 0x10 + 0x10
            record.lapRecord = (((bin[si + 0x02].toInt() shl 8) or (bin[si + 0x03].toInt() and 0xFF)) * 1.6666).toInt() * 10
            record.courseRecord = (((bin[si + 0x6].toInt() shl 8) or (bin[si + 0x7].toInt() and 0xFF))) * 10
            record.tagRecord = (((bin[si + 0xA].toInt() shl 8) or (bin[si + 0xB].toInt() and 0xFF))) * 10
            record.balloonRecord = (((bin[si + 0xE].toInt() shl 8) or (bin[si + 0xF].toInt() and 0xFF))) * 10
            records.add(record)
            totalTime += record.courseRecord
        }
    }
}

fun submitScoreToFirestore(
    gameId: String,
    leaderboardId: String,
    score: Long,
    userName: String,
    onSuccess: (() -> Unit)? = null,
    onFailure: ((Exception) -> Unit)? = null
) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
        onFailure?.invoke(Exception("ユーザーが認証されていません"))
        return
    }
    val userId = currentUser.uid

    // ユーザーの画像URLを取得
    val photoUrl = currentUser.photoUrl?.toString()

    // Ensure we're using the Firebase user ID for the document ID
    // but still display the user's name (which might be from Discord)
    val scoreData = hashMapOf(
        "name" to userName,
        "score" to score,
        "timestamp" to System.currentTimeMillis(),
        "photoUrl" to photoUrl,
        "firebaseUid" to userId  // Store the Firebase UID explicitly
    )
    val scoreDocRef = db.collection("games/${gameId}/leaderboards")
        .document(leaderboardId)
        .collection("scores")
        .document(userId)

    scoreDocRef.get()
        .addOnSuccessListener { document ->
            val currentScore = document.getLong("score")
            if (currentScore == null || score < currentScore) {
                // 新記録（より短いタイム）の場合のみ上書き
                scoreDocRef.set(scoreData)
                    .addOnSuccessListener { onSuccess?.invoke() }
                    .addOnFailureListener { e -> onFailure?.invoke(e) }
            } else {
                // 記録を更新しない場合も成功扱い
                onSuccess?.invoke()
            }
        }
        .addOnFailureListener { e ->
            onFailure?.invoke(e)
        }
}


class SonicR : BaseGame {

    var gameId: String = ""

    constructor( gameCode : String ) {
    var gameId: String = ""

    constructor( gameCode : String ) {
        leaderBoards = mutableListOf<LeaderBoard>()
        leaderBoards?.add(LeaderBoard("Resort Island", "01"))
        leaderBoards?.add(LeaderBoard("Radical City", "02"))
        leaderBoards?.add(LeaderBoard("Regal Ruin", "03"))
        leaderBoards?.add(LeaderBoard("Reactive Factory", "04"))
        leaderBoards?.add(LeaderBoard("Radiant Emerald", "05"))

        val db = FirebaseFirestore.getInstance()
        db.collection("games")
            .whereEqualTo("product_number", gameCode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    gameId = documents.documents[0].id

                    //insertDummyLeaderboardData()

                    // leaderboardsコレクションが空なら初期データ投入
                    val leaderboardsRef = db.collection("games").document(gameId).collection("leaderboards")
                    leaderboardsRef.get().addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            val leaderboardsData = listOf(
                                Pair("01", "Resort Island"),
                                Pair("02", "Radical City"),
                                Pair("03", "Regal Ruin"),
                                Pair("04", "Reactive Factory"),
                                Pair("05", "Radiant Emerald")
                            )
                            leaderboardsData.forEach { (id, name) ->
                                val data = hashMapOf("name" to name)
                                leaderboardsRef.document(id).set(data)
                            }
                        }
                    }
                }
            }

    }

    fun insertDummyLeaderboardData() {
        val db = FirebaseFirestore.getInstance()
        val gameId = "31"
        val leaderboardId = "01"
        val scoresRef = db.collection("games").document(gameId)
            .collection("leaderboards").document(leaderboardId)
            .collection("scores")

        // 1000件分のダミーデータを作成
        for (i in 1..1000) {
            val userId = "dummy_user_$i"
            val name = "ダミー$i"
            val score = 100000L + i * 100 // 例: タイムアタックならミリ秒
            val timestamp = System.currentTimeMillis() - (1000L * i)
            val data = hashMapOf(
                "name" to name,
                "score" to score,
                "timestamp" to timestamp,
                "photoUrl" to "https://cdn.discordapp.com/embed/avatars/${i % 5}.png" // ダミーのアバターURL
            )
            scoresRef.document(userId).set(data)
        }
    }

    override fun onBackUpUpdated(before: ByteArray, after: ByteArray) {

        if( gameId == "" ) return
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return
        val beforeRecord = SonicRBackup(before)
        val afterRecord = SonicRBackup(after)
        if( gameId == "" ) return
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser ?: return
        val beforeRecord = SonicRBackup(before)
        val afterRecord = SonicRBackup(after)

        for (i in 0..4) {
            if (afterRecord.records[i].courseRecord < beforeRecord.records[i].courseRecord) {
                val context = YabauseApplication.appContext
                val score = afterRecord.records[i].courseRecord.toLong()
                val account = GoogleSignIn.getLastSignedInAccount(context)
                val gid = leaderBoards?.get(i)?.id
                if (gid != null) {
                    // Use the display name if available, but ensure we're using the Firebase UID for the document ID
                    val userName = currentUser.displayName ?: "Anonymous"

                    // Log the user information for debugging
                    Log.d("SonicR", "Submitting score for user: ${currentUser.uid}, display name: $userName")

                    submitScoreToFirestore(gameId, gid, score, userName)
                    val bundle = Bundle()
                    bundle.putLong(FirebaseAnalytics.Param.SCORE, score)
                    bundle.putString("leaderboard_id", leaderBoards?.get(i)?.id)
                    val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle)
                    leaderBoards?.get(i)?.id?.let { this.uievent.onNewRecord(it) }
                }
            }
        }
    }
}
