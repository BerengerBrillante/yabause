package org.uoyabause.yabasanshiro

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.uoyabause.android.game.submitScoreToFirestore

class SonicRTest {
    private lateinit var firestoreMock: FirebaseFirestore
    private lateinit var authMock: FirebaseAuth
    private lateinit var userMock: FirebaseUser
    private lateinit var docRefMock: DocumentReference
    private lateinit var colRefMock: CollectionReference

    @Before
    fun setUp() {
        firestoreMock = mockk(relaxed = true)
        authMock = mockk(relaxed = true)
        userMock = mockk(relaxed = true)
        docRefMock = mockk(relaxed = true)
        colRefMock = mockk(relaxed = true)

        mockkStatic(FirebaseFirestore::class)
        mockkStatic(FirebaseAuth::class)

        every { FirebaseFirestore.getInstance() } returns firestoreMock
        every { FirebaseAuth.getInstance() } returns authMock
        every { authMock.currentUser } returns userMock
        every { userMock.uid } returns "test_uid"
        every { firestoreMock.collection("leaderboards") } returns colRefMock
        every { colRefMock.document(any()) } returns docRefMock
        every { docRefMock.collection("scores") } returns colRefMock
        every { colRefMock.document(any()) } returns docRefMock
        every { docRefMock.set(any()) } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSubmitScoreToFirestore() {
        val leaderboardId = "test_leaderboard"
        val score = 12345L
        val userName = "テストユーザー"

        var successCalled = false
        var failureCalled = false
        submitScoreToFirestore(
            "GS",leaderboardId, score, userName,
            onSuccess = { successCalled = true },
            onFailure = { failureCalled = true }
        )

        verify {
            docRefMock.set(match {
                val map = it as Map<String, Any?>
                map["name"] == userName &&
                map["score"] == score &&
                map.containsKey("timestamp")
            })
        }
        assert(successCalled)
        assert(!failureCalled)

    }

    @Test
    fun testSubmitScoreToFirestore_updateOnlyIfBetterScore() {
        val leaderboardId = "test_leaderboard_update"
        val userName = "テストユーザー"
        val oldScore = 30000L
        val betterScore = 20000L
        val worseScore = 40000L

        // 既存スコアが存在し、より良いスコアで上書きされること
        val docSnapMock = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
        every { docSnapMock.getLong("score") } returns oldScore
        every { docRefMock.get() } returns mockk {
            every { addOnSuccessListener(any()) } answers {
                firstArg<(com.google.firebase.firestore.DocumentSnapshot) -> Unit>().invoke(docSnapMock)
                mockk(relaxed = true)
            }
            every { addOnFailureListener(any()) } returns mockk(relaxed = true)
        }
        every { docRefMock.set(any()) } returns mockk(relaxed = true)

        var successCalled = false
        submitScoreToFirestore(
            "",leaderboardId, betterScore, userName,
            onSuccess = { successCalled = true },
            onFailure = { }
        )
        verify {
            docRefMock.set(match {
                val map = it as Map<String, Any?>
                map["score"] == betterScore
            })
        }
        assert(successCalled)

        // 既存スコアより悪い場合はsetされない
        clearMocks(docRefMock)
        var successCalled2 = false
        submitScoreToFirestore(
            "", leaderboardId, worseScore, userName,
            onSuccess = { successCalled2 = true },
            onFailure = { }
        )
        verify(exactly = 0) { docRefMock.set(any()) }
        assert(successCalled2)
    }
}
