package com.chrischuks.timeflow.motivation
import android.app.*
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.time.*
import java.util.concurrent.TimeUnit

class DailyMotivationWorker(c:Context,p:WorkerParameters):Worker(c,p){
 override fun doWork():Result{
  val q=QuoteLibrary.today(applicationContext)
  val nm=applicationContext.getSystemService(NotificationManager::class.java)
  val channel="daily_motivation"
  nm.createNotificationChannel(NotificationChannel(channel,"Daily motivation",NotificationManager.IMPORTANCE_DEFAULT))
  nm.notify(8401,NotificationCompat.Builder(applicationContext,channel).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Your daily motivation").setContentText(q.text).setStyle(NotificationCompat.BigTextStyle().bigText(q.text)).setAutoCancel(true).build())
  return Result.success()
 }
 companion object{
  fun schedule(context:Context){
   val now=ZonedDateTime.now();var next=now.withHour(7).withMinute(0).withSecond(0).withNano(0);if(!next.isAfter(now))next=next.plusDays(1)
   val delay=Duration.between(now,next).toMillis()
   val req=PeriodicWorkRequestBuilder<DailyMotivationWorker>(24,TimeUnit.HOURS).setInitialDelay(delay,TimeUnit.MILLISECONDS).build()
   WorkManager.getInstance(context).enqueueUniquePeriodicWork("daily_motivation",ExistingPeriodicWorkPolicy.UPDATE,req)
  }
 }
}
