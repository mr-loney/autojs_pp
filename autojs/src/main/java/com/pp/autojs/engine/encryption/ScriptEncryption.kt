package com.pp.autojs.engine.encryption

import com.pp.util.AdvancedEncryptionStandard

object ScriptEncryption {

    private var mKey = ""
    private var mInitVector = ""

    fun decrypt(bytes: ByteArray, start: Int = 0, end: Int = bytes.size): ByteArray {
        return AdvancedEncryptionStandard(mKey.toByteArray(), mInitVector).decrypt(bytes, start, end)
    }

}