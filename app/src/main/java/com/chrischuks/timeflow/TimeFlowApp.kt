package com.chrischuks.timeflow
import android.app.Application
import com.chrischuks.timeflow.data.AppDatabase
class TimeFlowApp: Application(){ val db by lazy { AppDatabase.get(this) } }
