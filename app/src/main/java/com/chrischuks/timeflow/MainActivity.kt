package com.chrischuks.timeflow
import android.Manifest
import android.os.*
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chrischuks.timeflow.data.*
import com.chrischuks.timeflow.reminder.ReminderScheduler
import com.chrischuks.timeflow.motivation.QuoteLibrary
import com.chrischuks.timeflow.motivation.QuoteCategory
import com.chrischuks.timeflow.motivation.MotivationPreferences
import com.chrischuks.timeflow.motivation.DailyMotivationWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:ComponentActivity(){
 private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){}
 override fun onCreate(b:Bundle?){super.onCreate(b);if(Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS);DailyMotivationWorker.schedule(this);setContent{TimeFlowTheme{App()}}}
}
class MainVm(val db:AppDatabase):ViewModel(){
 val tasks=db.dao().tasks().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val habits=db.dao().habits().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val sessions=db.dao().sessions().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val projects=db.dao().projects().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val goals=db.dao().goals().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val blocks=db.dao().blocks().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val reviews=db.dao().reviews().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 val dbRef get() = db
 fun add(title:String,note:String,priority:Int,mins:Int,due:Long?,important:Boolean=false,urgent:Boolean=false,after:(Long)->Unit)=viewModelScope.launch{after(db.dao().addTask(Task(title=title,note=note,priority=priority,estimatedMinutes=mins,dueAt=due,important=important,urgent=urgent)))}
 fun toggle(t:Task)=viewModelScope.launch{db.dao().updateTask(t.copy(completed=!t.completed))}
 fun del(t:Task)=viewModelScope.launch{db.dao().deleteTask(t)}
 fun habit(n:String)=viewModelScope.launch{db.dao().addHabit(Habit(name=n))}
 fun habitToggle(h:Habit)=viewModelScope.launch{db.dao().updateHabit(h.copy(completedToday=!h.completedToday,streak=(h.streak+(if(!h.completedToday)1 else -1)).coerceAtLeast(0)))}
 fun logFocus(title:String,m:Int)=viewModelScope.launch{db.dao().addSession(FocusSession(taskTitle=title,minutes=m))}
}
class VmFactory(private val db:AppDatabase):ViewModelProvider.Factory{override fun <T:ViewModel> create(c:Class<T>):T=MainVm(db) as T}
@Composable fun TimeFlowTheme(content:@Composable () -> Unit){
 val dark=androidx.compose.foundation.isSystemInDarkTheme()
 val scheme=if(dark) darkColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF9CB1FF),secondary=androidx.compose.ui.graphics.Color(0xFFC4B5FD),surface=androidx.compose.ui.graphics.Color(0xFF111318),background=androidx.compose.ui.graphics.Color(0xFF0C0E12)) else lightColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF2949C7),secondary=androidx.compose.ui.graphics.Color(0xFF6750A4),surface=androidx.compose.ui.graphics.Color(0xFFFFFBFF),background=androidx.compose.ui.graphics.Color(0xFFF7F7FC),primaryContainer=androidx.compose.ui.graphics.Color(0xFFDCE2FF),secondaryContainer=androidx.compose.ui.graphics.Color(0xFFE9DDFF))
 MaterialTheme(colorScheme=scheme,content=content)
}
@Composable fun App(){
 val a=androidx.compose.ui.platform.LocalContext.current.applicationContext as TimeFlowApp
 val vm:MainVm=viewModel(factory=VmFactory(a.db));var tab by remember{mutableIntStateOf(0)}
 Scaffold(bottomBar={NavigationBar{listOf("Today" to Icons.Default.Home,"Tasks" to Icons.Default.CheckCircle,"Focus" to Icons.Default.Timer,"Planner" to Icons.Default.CalendarMonth,"More" to Icons.Default.GridView).forEachIndexed{i,x->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(x.second,null)},label={Text(x.first)})}}}){p->Box(Modifier.padding(p)){when(tab){0->Today(vm);1->Tasks(vm);2->Focus(vm);3->PlannerScreen(vm);else->MoreHub(vm)}}}
}
@Composable fun Header(title:String,sub:String){Column(Modifier.padding(20.dp,20.dp,20.dp,8.dp)){Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(sub,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable fun Today(vm:MainVm){
 val ts by vm.tasks.collectAsStateWithLifecycle();val ss by vm.sessions.collectAsStateWithLifecycle();val hs by vm.habits.collectAsStateWithLifecycle();val done=ts.count{it.completed};val open=ts.filter{!it.completed};val focus=ss.sumOf{it.minutes};val score=if(ts.isEmpty())0 else ((done*70/ts.size)+(if(focus>0)20 else 0)+(if(hs.any{it.completedToday})10 else 0)).coerceAtMost(100)
 LazyColumn(contentPadding=PaddingValues(bottom=24.dp)){
  item{Header("Today","Make today intentional.");DailyQuoteCard();Card(Modifier.padding(20.dp,8.dp).fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Column(Modifier.padding(18.dp)){Text("Daily momentum",fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically){Text("$score",style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);Text("/100",color=MaterialTheme.colorScheme.onSurfaceVariant)};LinearProgressIndicator(progress={score/100f},modifier=Modifier.fillMaxWidth());Text(when{score>=80->"Excellent momentum. Protect your focus.";score>=50->"Good progress. Finish one important task next.";else->"Start small: complete one meaningful task and one focus session."},style=MaterialTheme.typography.bodySmall)}};Row(Modifier.padding(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){Metric("${open.size}","Open",Modifier.weight(1f));Metric("$done","Done",Modifier.weight(1f));Metric("${open.sumOf{it.estimatedMinutes}}m","Planned",Modifier.weight(1f))};Spacer(Modifier.height(18.dp));Text("Top priorities",Modifier.padding(horizontal=20.dp),fontWeight=FontWeight.Bold)}
  items(open.sortedWith(compareByDescending<Task>{it.important}.thenByDescending{it.urgent}.thenByDescending{it.priority}).take(5)){TaskRow(it,{vm.toggle(it)},{vm.del(it)})}
  if(ts.none{!it.completed})item{Empty("Your day is clear","Add a task and give your time a purpose.")}
 }
}
@Composable fun Metric(v:String,l:String,m:Modifier){Card(m){Column(Modifier.padding(14.dp)){Text(v,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(l,style=MaterialTheme.typography.bodySmall)}}}
@Composable fun Tasks(vm:MainVm){
 val ts by vm.tasks.collectAsStateWithLifecycle();var show by remember{mutableStateOf(false)};val context=androidx.compose.ui.platform.LocalContext.current;var query by remember{mutableStateOf("")};var filter by remember{mutableStateOf("Open")}
 val visible=ts.filter{(query.isBlank()||it.title.contains(query,true)||it.note.contains(query,true))&&when(filter){"Open"->!it.completed;"Done"->it.completed;"Important"->it.important;else->true}}
 Scaffold(floatingActionButton={FloatingActionButton(onClick={show=true}){Icon(Icons.Default.Add,null)}}){pad->LazyColumn(Modifier.padding(pad)){item{Header("Tasks","Capture, prioritize and finish.");OutlinedTextField(query,{query=it},Modifier.padding(horizontal=16.dp).fillMaxWidth(),leadingIcon={Icon(Icons.Default.Search,null)},placeholder={Text("Search tasks")},singleLine=true);Row(Modifier.padding(16.dp,8.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Open","Important","Done","All").forEach{x->FilterChip(filter==x,{filter=x},{Text(x)})}}};items(visible,key={it.id}){TaskRow(it,{vm.toggle(it)},{vm.del(it)})};if(visible.isEmpty())item{Empty("Nothing here","Try another filter or add a task.")}}}
 if(show)AddTaskDialog({show=false}){t,n,p,m,d,important,urgent->vm.add(t,n,p,m,d,important,urgent){id->if(d!=null)ReminderScheduler.schedule(context,id,t,d)};show=false}
}
@Composable fun TaskRow(t:Task,toggle:()->Unit,del:()->Unit){
 Card(Modifier.padding(horizontal=16.dp,vertical=5.dp).fillMaxWidth()){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(t.completed,{toggle()});Column(Modifier.weight(1f)){Text(t.title,fontWeight=FontWeight.SemiBold);Text("${t.category} • ${t.estimatedMinutes} min • ${when(t.priority){3->"High";1->"Low";else->"Medium"}}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant);t.dueAt?.let{Text(SimpleDateFormat("EEE, d MMM • h:mm a",Locale.getDefault()).format(Date(it)),style=MaterialTheme.typography.bodySmall)}};IconButton(del){Icon(Icons.Default.DeleteOutline,null)}}}
}
@Composable fun AddTaskDialog(close:()->Unit,save:(String,String,Int,Int,Long?,Boolean,Boolean)->Unit){
 var title by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var pri by remember{mutableIntStateOf(2)};var mins by remember{mutableStateOf("30")};var remind by remember{mutableStateOf(false)};var important by remember{mutableStateOf(false)};var urgent by remember{mutableStateOf(false)}
 AlertDialog(onDismissRequest=close,title={Text("Plan a task")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(title,{title=it},label={Text("Task")});OutlinedTextField(note,{note=it},label={Text("Notes")});OutlinedTextField(mins,{mins=it.filter(Char::isDigit)},label={Text("Estimated minutes")});Row{listOf(1 to "Low",2 to "Medium",3 to "High").forEach{(v,l)->FilterChip(pri==v,{pri=v},{Text(l)});Spacer(Modifier.width(4.dp))}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(important,{important=!important},{Text("Important")},leadingIcon={if(important){Icon(Icons.Default.Star,null)}});FilterChip(urgent,{urgent=!urgent},{Text("Urgent")},leadingIcon={if(urgent){Icon(Icons.Default.Bolt,null)}})};Row(verticalAlignment=Alignment.CenterVertically){Switch(remind,{remind=it});Text(" Remind me in 1 hour")}}},confirmButton={Button(onClick={if(title.isNotBlank())save(title,note,pri,mins.toIntOrNull()?:30,if(remind)System.currentTimeMillis()+3600000 else null,important,urgent)}){Text("Add task")}},dismissButton={TextButton(close){Text("Cancel")}})
}
@Composable fun Focus(vm:MainVm){
 var running by remember{mutableStateOf(false)};var minutes by remember{mutableIntStateOf(25)};var left by remember(minutes){mutableIntStateOf(minutes*60)};var title by remember{mutableStateOf("Deep work")}
 LaunchedEffect(running){while(running&&left>0){delay(1000);left--};if(running&&left==0){running=false;vm.logFocus(title,minutes)}}
 Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Header("Focus","Protect a block of undistracted time.");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf(15,25,45,60).forEach{m->FilterChip(minutes==m,{if(!running){minutes=m;left=m*60}},{Text("$m min")})}};Spacer(Modifier.height(28.dp));Text("%02d:%02d".format(left/60,left%60),style=MaterialTheme.typography.displayLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.height(20.dp));OutlinedTextField(title,{title=it},label={Text("What are you focusing on?")});Spacer(Modifier.height(20.dp));Button(onClick={running=!running},Modifier.fillMaxWidth()){Icon(if(running)Icons.Default.Pause else Icons.Default.PlayArrow,null);Text(if(running)" Pause" else " Start focus")};TextButton(onClick={running=false;left=minutes*60}){Text("Reset")};Text("$minutes-minute focus session • saved automatically when completed",style=MaterialTheme.typography.bodySmall)}
}
@Composable fun Habits(vm:MainVm){
 val hs by vm.habits.collectAsStateWithLifecycle();var n by remember{mutableStateOf("")}
 LazyColumn{item{Header("Habits","Small routines compound into better days.");Row(Modifier.padding(16.dp)){OutlinedTextField(n,{n=it},Modifier.weight(1f),label={Text("New habit")});IconButton({if(n.isNotBlank()){vm.habit(n);n=""}}){Icon(Icons.Default.AddCircle,null)}}};items(hs){h->Card(Modifier.padding(horizontal=16.dp,vertical=5.dp).fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(h.completedToday,{vm.habitToggle(h)});Column{Text(h.name,fontWeight=FontWeight.SemiBold);Text("${h.streak} day streak",style=MaterialTheme.typography.bodySmall)}}}}}
}
@Composable fun Insights(vm:MainVm){
 val ts by vm.tasks.collectAsStateWithLifecycle();val ss by vm.sessions.collectAsStateWithLifecycle();val total=ts.size;val done=ts.count{it.completed};val pct=if(total==0)0 else done*100/total
 Column{Header("Insights","Use evidence to improve your week.");Card(Modifier.padding(16.dp).fillMaxWidth()){Column(Modifier.padding(18.dp)){Text("Completion rate",fontWeight=FontWeight.Bold);Text("$pct%",style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);LinearProgressIndicator(progress={pct/100f},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(18.dp));Text("Focus time: ${ss.sumOf{it.minutes}} min");Text("Focus sessions: ${ss.size}");Text("Tasks completed: $done of $total")}};Text("Tip: plan fewer high-impact tasks, then protect time for them.",Modifier.padding(20.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)}
}
@Composable fun Empty(t:String,s:String){Column(Modifier.fillMaxWidth().padding(40.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.EventAvailable,null,Modifier.size(48.dp));Text(t,fontWeight=FontWeight.Bold);Text(s)}}

@Composable fun DailyQuoteCard(){
 val context=androidx.compose.ui.platform.LocalContext.current
 var selected by remember{mutableStateOf(MotivationPreferences.selected(context))}
 var settings by remember{mutableStateOf(false)}
 val q=remember(selected){QuoteLibrary.today(context)}
 Card(Modifier.padding(horizontal=20.dp,vertical=8.dp).fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){
  Column(Modifier.padding(18.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.FormatQuote,null);Spacer(Modifier.width(8.dp));Text("DAILY MOTIVATION",style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold)}
   Spacer(Modifier.height(10.dp));Text(q.text,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
   Spacer(Modifier.height(8.dp));Row(verticalAlignment=Alignment.CenterVertically){Text(q.category.label+" • A new thought every day",Modifier.weight(1f),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=.7f));IconButton({settings=true}){Icon(Icons.Default.Tune,"Motivation preferences")}}
  }
 }
 if(settings) AlertDialog(
  onDismissRequest={settings=false},
  title={Text("Motivation categories")},
  text={LazyColumn{items(QuoteCategory.entries){cat->Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Checkbox(cat in selected,{checked->selected=if(checked)selected+cat else selected-cat});Text(cat.label)}}}},
  confirmButton={Button({MotivationPreferences.set(context,selected);selected=MotivationPreferences.selected(context);settings=false}){Text("Save")}},
  dismissButton={TextButton({selected=QuoteCategory.entries.toSet()}){Text("Select all")}}
 )
}
