package com.chrischuks.timeflow.data
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName="tasks") data class Task(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val note:String="",val dueAt:Long?=null,val priority:Int=2,val completed:Boolean=false,val category:String="Personal",val estimatedMinutes:Int=30,val createdAt:Long=System.currentTimeMillis())
@Entity(tableName="habits") data class Habit(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val targetPerWeek:Int=7,val streak:Int=0,val completedToday:Boolean=false)
@Entity(tableName="focus_sessions") data class FocusSession(@PrimaryKey(autoGenerate=true) val id:Long=0,val taskTitle:String,val minutes:Int,val completedAt:Long=System.currentTimeMillis())
@Dao interface TimeDao {
 @Query("SELECT * FROM tasks ORDER BY completed ASC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, priority DESC") fun tasks():Flow<List<Task>>
 @Insert suspend fun addTask(t:Task):Long
 @Update suspend fun updateTask(t:Task)
 @Delete suspend fun deleteTask(t:Task)
 @Query("SELECT * FROM habits ORDER BY id DESC") fun habits():Flow<List<Habit>>
 @Insert suspend fun addHabit(h:Habit)
 @Update suspend fun updateHabit(h:Habit)
 @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC") fun sessions():Flow<List<FocusSession>>
 @Insert suspend fun addSession(s:FocusSession)
}
@Database(entities=[Task::class,Habit::class,FocusSession::class],version=1,exportSchema=false) abstract class AppDatabase:RoomDatabase(){ abstract fun dao():TimeDao
 companion object { @Volatile private var I:AppDatabase?=null; fun get(c:Context)=I?: synchronized(this){ I?:Room.databaseBuilder(c,AppDatabase::class.java,"timeflow.db").build().also{I=it} } }
}
