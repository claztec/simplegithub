package net.claztec.simplegithub.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import net.claztec.simplegithub.api.model.GithubRepo

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(repo: GithubRepo)

    @Query("select * from repositories")
    fun getHistory() : Flowable<List<GithubRepo>>

    @Query("delete from repositories")
    fun clearAll()
}