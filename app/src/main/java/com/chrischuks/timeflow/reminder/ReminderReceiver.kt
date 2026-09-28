package com.chrischuks.timeflow.reminder
import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import com.chrischuks.timeflow.MainActivity
class ReminderReceiver:BroadcastReceiver(){ override fun onReceive(c:Context,i:Intent){
 val nm=c.getSystemService(NotificationManager::class.java); val id="tasks"; nm.createNotificationChannel(NotificationChannel(id,"Task reminders",NotificationManager.IMPORTANCE_HIGH))
 val pi=PendingIntent.getActivity(c,0,Intent(c,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
 nm.notify(i.getLongExtra("id",0).toInt(),NotificationCompat.Builder(c,id).setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle(i.getStringExtra("title")?:"TimeFlow reminder").setContentText("It’s time for this task.").setContentIntent(pi).setAutoCancel(true).build()) }}
object ReminderScheduler { fun schedule(c:Context,id:Long,title:String,at:Long){ if(at<=System.currentTimeMillis()) return; val am=c.getSystemService(AlarmManager::class.java); val pi=PendingIntent.getBroadcast(c,id.toInt(),Intent(c,ReminderReceiver::class.java).putExtra("id",id).putExtra("title",title),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT); if(android.os.Build.VERSION.SDK_INT>=31 && !am.canScheduleExactAlarms()) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi) else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi) } }
