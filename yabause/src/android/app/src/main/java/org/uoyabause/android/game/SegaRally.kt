package org.uoyabause.android.game

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
01E0: 02 50 57 5E 64 02 52 83  55 CB 02 55 02 2F 74 02
01F0: 55 94 04 64 02 56 12 04  21 00 56 50 5E 64 02 50
0200: 07 D5 CB 02 52 33 DE 64  02 54 52 AF 74 02 58 11
0210: BF 68 03 02 64 BA 03 00  56 30 D5 CB 82 49 87 2F
0220: 74 82 52 13 5E 64 82 54  32 55 CB 82 57 91 3F 68
0230: 83 02 44 3A 03 80 56 20  2F 74 82 49 37 BA 03 82
0240: 51 63 AF 74 82 53 82 DE  64 82 57 41 BF 68 83 01
0250: 94 D5 CB 80 56 00 BA 03  22 50 57 3A 03 22 52 83
0260: 55 CB 22 55 02 2F 74 22  58 61 3F 68 23 03 14 5E
0270: 64 20 56 50 3A 03 22 50  07 AF 74 22 52 33 DE 64
0280: 22 54 52 D5 CB 22 58 11  BF 68 23 02 64 BA 03 20

01E0: best time　分
01E1: best time　秒 sec = (x>>4)*10 + (x&0x04)
01E2: best time　秒 msec = (x>>4)*10 + (x&0x04)

01F7 : ?
01F8 : ?
01F9 : fastest lap　分
01FA : fastest lap　秒 sec = (x>>4)*10 + (x&0x0F)
01FB : fastest lap　秒 msec = (x>>4)*10 + (x&0x0F)


023A : Course record 分 & 0x0F
023B : Course record 秒 sec = (x>>4)*10 + (x&0x0F)
023C : Course record 秒 msec = (x>>4)*10 + (x&0x0F)


0BA0: 00 00 00 00 00 00 00 00  00 04 32 19 18 06 1A 4B
0BB0: 12 F1 12 33 C0 CD 19 25  20 BB B5 F5 19 1B 10 E6
0BC0: 1F FE B3 21 1B E2 12 EF  13 4D 10 CE C0 16 19 E0


Desert
01F4 : totaltime　分
01F5 : totaltime　秒 sec = (x>>4)*10 + (x&0x04)
01F6 : totaltime　秒 msec = (x>>4)*10 + (x&0x04)

Forest
0BA9 : totaltime　分
0BAA : totaltime　秒 sec = (x>>4)*10 + (x&0x0F)
0BAB : totaltime　秒 msec = (x>>4)*10 + (x&0x0F)

Mountain
0E49: 04 totaltime　分
0E4A: 20 totaltime　秒 sec = (x>>4)*10 + (x&0x0F)
0E4B: 30 totaltime　秒 msec = (x>>4)*10 + (x&0x0F)


*/
class SegaRally : BaseGame {

    constructor(gameCode: String) {
        // BaseGameのinitGameIdを使用してgameIdを初期化
        CoroutineScope(Dispatchers.IO).launch {
            // gameIdの初期化を待機
            initGameId(gameCode)
        }
    }


    override fun onBackUpUpdated(before: ByteArray, after: ByteArray) {
        val tag = "SegaRally"

        // ここから追加: afterの内容を16バイトごとにHEXで出力
        var i = 0
        while (i < after.size) {
            val lineAddr = String.format("%04X:", i)
            val lineBytes = StringBuilder()
            for (j in 0 until 16) {
                if (i + j < after.size) {
                    lineBytes.append(String.format(" %02X", after[i + j]))
                } else {
                    lineBytes.append("   ") // データが足りない場合はスペース
                }
                if (j == 7) lineBytes.append(" ") // 8バイトごとにスペース
            }
            Log.d(tag, "$lineAddr${lineBytes.toString()}")
            i += 16
        }
        // ここまで追加

        val minLength = minOf(before.size, after.size)
        for (i in 0 until minLength) {
            if (before[i] != after[i]) {
                val beforeHex = String.format("%02X", before[i])
                val afterHex = String.format("%02X", after[i])
                Log.d(tag, String.format("%04X: %s, %s", i, beforeHex, afterHex))
            }
        }
        if (before.size > after.size) {
            for (i in minLength until before.size) {
                val beforeHex = String.format("%02X", before[i])
                Log.d(tag, String.format("%04X: %s, --", i, beforeHex))
            }
        } else if (after.size > before.size) {
            for (i in minLength until after.size) {
                val afterHex = String.format("%02X", after[i])
                Log.d(tag, String.format("%04X: --, %s", i, afterHex))
            }
        }
    }
}