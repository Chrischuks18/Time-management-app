package com.chrischuks.timeflow.data
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName="tasks") data class Task(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val note:String="",val dueAt:Long?=null,val priority:Int=2,val completed:Boolean=false,val category:String="Personal",val estimatedMinutes:Int=30,val createdAt:Long=System.currentTimeMillis(),val important:Boolean=false,val urgent:Boolean=false,val projectId:Long?=null,val recurrence:String="NONE")
@Entity(tableName="habits") data class Habit(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val targetPerWeek:Int=7,val streak:Int=0,val completedToday:Boolean=false)
@Entity(tableName="focus_sessions") data class FocusSession(@PrimaryKey(autoGenerate=true) val id:Long=0,val taskTitle:String,val minutes:Int,val completedAt:Long=System.currentTimeMillis())
@Entity(tableName="projects") data class Project(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val description:String="",val targetDate:Long?=null,val archived:Boolean=false)
@Entity(tableName="goals") data class Goal(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val target:String="",val progress:Int=0,val targetDate:Long?=null)
@Entity(tableName="time_blocks") data class TimeBlock(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val startAt:Long,val endAt:Long,val category:String="Work")
@Entity(tableName="reviews") data class WeeklyReview(@PrimaryKey(autoGenerate=true) val id:Long=0,val createdAt:Long=System.currentTimeMillis(),val wins:String="",val lessons:String="",val nextWeek:String="")
@Dao interface TimeDao {
 @Query("SELECT * FROM tasks ORDER BY completed ASC, CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END, dueAt ASC, priority DESC") fun tasks():Flow<List<Task>>
 @Insert suspend fun addTask(t:Task):Long
 @Update suspend fun updateTask(t:Task)
 @Delete suspend fun deleteTask(t:Task)
 @Query("SELECT * FROM habits ORDER BY id DESC") fun habits():Flow<List<Habit>>
 @Insert suspend fun addHabit(h:Habit); @Update suspend fun updateHabit(h:Habit)
 @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC") fun sessions():Flow<List<FocusSession>>
 @Insert suspend fun addSession(s:FocusSession)
 @Query("SELECT * FROM projects WHERE archived=0 ORDER BY id DESC") fun projects():Flow<List<Project>>
 @Insert suspend fun addProject(p:Project); @Update suspend fun updateProject(p:Project)
 @Query("SELECT * FROM goals ORDER BY id DESC") fun goals():Flow<List<Goal>>
 @Insert suspend fun addGoal(g:Goal); @Update suspend fun updateGoal(g:Goal)
 @Query("SELECT * FROM time_blocks ORDER BY startAt ASC") fun blocks():Flow<List<TimeBlock>>
 @Insert suspend fun addBlock(b:TimeBlock); @Delete suspend fun deleteBlock(b:TimeBlock)
 @Query("SELECT * FROM reviews ORDER BY createdAt DESC") fun reviews():Flow<List<WeeklyReview>>
 @Insert suspend fun addReview(r:WeeklyReview)
}
@Database(entities=[Task::class,Habit::class,FocusSession::class,Project::class,Goal::class,TimeBlock::class,WeeklyReview::class],version=2,exportSchema=false)
abstract class AppDatabase:RoomDatabase(){ abstract fun dao():TimeDao
 companion object{@Volatile private var I:AppDatabase?=null;fun get(c:Context)=I?:synchronized(this){I?:Room.databaseBuilder(c,AppDatabase::class.java,"timeflow.db").fallbackToDestructiveMigration().build().also{I=it}}}
}
