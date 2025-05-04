import Foundation
import FirebaseFirestore
import FirebaseAuth
import FirebaseAnalytics

/// ソニックRのレコード情報を保持するクラス
class SonicRRecord {
    var lapRecord: Int = 0
    var courseRecord: Int = 0
    var tagRecord: Int = 0
    var balloonRecord: Int = 0
}

/// ソニックRのバックアップデータを解析するクラス
class SonicRBackup {
    var records: [SonicRRecord] = []
    var totalTime: Int64 = 0

    init(bin: Data) {
        totalTime = 0
        for i in 0..<5 {
            let record = SonicRRecord()
            let si = i * 0x10 + 0x10

            // バイナリデータからレコード情報を抽出
            if si + 0x03 < bin.count {
                let lapRecordBytes = bin.subdata(in: si + 0x02..<si + 0x04)
                var lapRecordValue: UInt16 = 0
                _ = withUnsafeMutableBytes(of: &lapRecordValue) { lapRecordBytes.copyBytes(to: $0) }
                record.lapRecord = Int(Double(UInt16(bigEndian: lapRecordValue)) * 1.6666) * 10
            }

            if si + 0x07 < bin.count {
                let courseRecordBytes = bin.subdata(in: si + 0x06..<si + 0x08)
                var courseRecordValue: UInt16 = 0
                _ = withUnsafeMutableBytes(of: &courseRecordValue) { courseRecordBytes.copyBytes(to: $0) }
                record.courseRecord = Int(UInt16(bigEndian: courseRecordValue)) * 10
            }

            if si + 0x0B < bin.count {
                let tagRecordBytes = bin.subdata(in: si + 0x0A..<si + 0x0C)
                var tagRecordValue: UInt16 = 0
                _ = withUnsafeMutableBytes(of: &tagRecordValue) { tagRecordBytes.copyBytes(to: $0) }
                record.tagRecord = Int(UInt16(bigEndian: tagRecordValue)) * 10
            }

            if si + 0x0F < bin.count {
                let balloonRecordBytes = bin.subdata(in: si + 0x0E..<si + 0x10)
                var balloonRecordValue: UInt16 = 0
                _ = withUnsafeMutableBytes(of: &balloonRecordValue) { balloonRecordBytes.copyBytes(to: $0) }
                record.balloonRecord = Int(UInt16(bigEndian: balloonRecordValue)) * 10
            }

            records.append(record)
            totalTime += Int64(record.courseRecord)
        }
    }
}

/// Firestoreにスコアを送信する関数
func submitScoreToFirestore(
    gameId: String,
    leaderboardId: String,
    score: Int64,
    userName: String,
    onSuccess: (() -> Void)? = nil,
    onFailure: ((Error) -> Void)? = nil
) {
    let db = Firestore.firestore()
    guard let userId = Auth.auth().currentUser?.uid else {
        onFailure?(NSError(domain: "ScoreSubmission", code: 1, userInfo: [NSLocalizedDescriptionKey: "ユーザーが認証されていません"]))
        return
    }

    // ユーザーのプロフィール画像URLを取得
    let photoURL = Auth.auth().currentUser?.photoURL?.absoluteString

    let scoreData: [String: Any] = [
        "name": userName,
        "score": score,
        "timestamp": Int(Date().timeIntervalSince1970 * 1000), // ミリ秒単位のタイムスタンプ
        "photoUrl": photoURL // ユーザーのアバター画像URL（nilの場合はFirestoreではnullとして保存される）
    ]

    let scoreDocRef = db.collection("games/\(gameId)/leaderboards")
        .document(leaderboardId)
        .collection("scores")
        .document(userId)

    scoreDocRef.getDocument { document, error in
        if let error = error {
            onFailure?(error)
            return
        }

        if let document = document, document.exists,
           let currentScore = document.data()?["score"] as? Int64 {
            if score < currentScore {
                // 新記録（より短いタイム）の場合のみ上書き
                scoreDocRef.setData(scoreData) { error in
                    if let error = error {
                        onFailure?(error)
                    } else {
                        onSuccess?()
                    }
                }
            } else {
                // 記録を更新しない場合も成功扱い
                onSuccess?()
            }
        } else {
            // 初めてのスコア登録
            scoreDocRef.setData(scoreData) { error in
                if let error = error {
                    onFailure?(error)
                } else {
                    onSuccess?()
                }
            }
        }
    }
}

/// ソニックRゲームクラス
class SonicR: BaseGame {

    var gameId: String = ""

    init(gameCode: String) {
        super.init()

        // リーダーボードの初期化
        leaderBoards = [
            LeaderBoard(title: "Resort Island", id: "01"),
            LeaderBoard(title: "Radical City", id: "02"),
            LeaderBoard(title: "Regal Ruin", id: "03"),
            LeaderBoard(title: "Reactive Factory", id: "04"),
            LeaderBoard(title: "Radiant Emerald", id: "05")
        ]

        // Firestoreからゲーム情報を取得
        let db = Firestore.firestore()
        db.collection("games")
            .whereField("product_number", isEqualTo: gameCode)
            .getDocuments { [weak self] snapshot, error in
                guard let self = self, let snapshot = snapshot, !snapshot.documents.isEmpty else {
                    return
                }

                // leaderboardIdフィールドがある場合はその値を使用
                if let leaderboardId = snapshot.documents[0].get("leaderboardId") as? String {
                    self.gameId = leaderboardId
                } else {
                    // なければドキュメントIDを使用
                    self.gameId = snapshot.documents[0].documentID
                }

                // leaderboardsコレクションが空なら初期データ投入
                let leaderboardsRef = db.collection("games").document(self.gameId).collection("leaderboards")
                leaderboardsRef.getDocuments { snapshot, error in
                    guard let snapshot = snapshot else { return }

                    if snapshot.documents.isEmpty {
                        let leaderboardsData = [
                            ("01", "Resort Island"),
                            ("02", "Radical City"),
                            ("03", "Regal Ruin"),
                            ("04", "Reactive Factory"),
                            ("05", "Radiant Emerald")
                        ]

                        for (id, name) in leaderboardsData {
                            let data = ["name": name]
                            leaderboardsRef.document(id).setData(data)
                        }
                    }
                }
            }
    }

    /// テスト用のダミーデータを挿入するメソッド
    func insertDummyLeaderboardData() {
        let db = Firestore.firestore()
        let gameId = "31"
        let leaderboardId = "01"
        let scoresRef = db.collection("games").document(gameId)
            .collection("leaderboards").document(leaderboardId)
            .collection("scores")

        // 1000件分のダミーデータを作成  
        for i in 1...1000 {
            let userId = "dummy_user_\(i)"
            let name = "ダミー\(i)"
            let score = 100000 + i * 100 // 例: タイムアタックならミリ秒
            let timestamp = Int(Date().timeIntervalSince1970 * 1000 - Double(1000 * i))
            let data: [String: Any] = [
                "name": name,
                "score": score,
                "timestamp": timestamp,
                "photoUrl": "" // ダミーデータなのでphotoUrlはnilに設定
            ]
            scoresRef.document(userId).setData(data)
        }
    }

    override func onBackUpUpdated(before: Data, after: Data) {
        if gameId.isEmpty { return }
        guard let currentUser = Auth.auth().currentUser else { return }

        let beforeRecord = SonicRBackup(bin: before)
        let afterRecord = SonicRBackup(bin: after)

        for i in 0..<5 {
            if i < afterRecord.records.count && i < beforeRecord.records.count &&
               afterRecord.records[i].courseRecord < beforeRecord.records[i].courseRecord {

                let score = Int64(afterRecord.records[i].courseRecord)

                if let gid = leaderBoards?[i].id {
                    let userName = currentUser.displayName ?? "Anonymous"
                    submitScoreToFirestore(gameId: gameId, leaderboardId: gid, score: score, userName: userName)

                    // Analyticsイベントの記録
                    Analytics.logEvent(AnalyticsEventPostScore, parameters: [
                        AnalyticsParameterScore: score,
                        "leaderboard_id": gid
                    ])

                    // 新記録通知
                    self.uiEvent?.onNewRecord(leaderBoardId: gid)
                }
            }
        }
    }
}
