package com.olup.notable.db

import android.content.Context
import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import com.olup.notable.TAG
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


@Entity
data class Kv(
    @PrimaryKey
    val key: String,
    val value: String
)

// DAO
@Dao
interface KvDao {
    @Query("SELECT * FROM kv WHERE key=:key")
    fun get(key: String): Kv

    @Query("SELECT * FROM kv WHERE key=:key")
    fun getLive(key: String): LiveData<Kv?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(kv: Kv)

    @Query("DELETE FROM kv WHERE key=:key")
    fun delete(key: String)

}

class KvRepository(context: Context) {
    var db = AppDatabase.getDatabase(context)?.kvDao()!!

    fun get(key: String): Kv? {
        return db.get(key)
    }

    fun getLive(key: String): LiveData<Kv?> {
        return db.getLive(key)
    }

    fun set(kv: Kv) {
        return db.set(kv)
    }

    fun delete(key: String) {
        db.delete(key)
    }

}

class KvProxy(context: Context) {
    private val kvRepository = KvRepository(context)

    fun <T> observeKv(key: String, serializer: KSerializer<T>, default: T): LiveData<T?> {
        return kvRepository.getLive(key).map {
            if (it == null) return@map default
            val jsonValue = it!!.value
            Json.decodeFromString(serializer, jsonValue)
        }
    }

    fun <T> get(key: String, serializer: KSerializer<T>): T? {
        val kv = kvRepository.get(key) ?: return null
        val jsonValue = kv!!.value
        return Json.decodeFromString(serializer, jsonValue)
    }


    fun <T> setKv(key: String, value: T, serializer: KSerializer<T>) {
        val jsonValue = Json.encodeToString(serializer, value)
        Log.i(TAG, jsonValue)
        kvRepository.set(Kv(key, jsonValue))
    }
}
fun <X, Y> LiveData<X>.map(mapFunction: Function<X, Y>): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    result.addSource(this) { x -> result.value = mapFunction.apply(x) }
    return result
}
