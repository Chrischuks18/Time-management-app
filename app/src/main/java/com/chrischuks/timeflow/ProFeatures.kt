package com.chrischuks.timeflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chrischuks.timeflow.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable fun MoreHub(vm:MainVm){
 val projects by vm.projects.collectAsStateWithLifecycle();val goals by vm.goals.collectAsStateWithLifecycle();val blocks by vm.blocks.collectAsStateWithLifecycle()
 var page by remember{mutableIntStateOf(0)}
 Column{Header("More","Everything you need to improve how you use your time.")
  ScrollableTabRow(page){listOf("Habits","Matrix","Projects","Goals","Insights","Review").forEachIndexed{i,s->Tab(page==i,{page=i},text={Text(s)})}}
  when(page){0->Habits(vm);1->Matrix(vm);2->Projects(vm,projects);3->Goals(vm,goals);4->Insights(vm);else->Review(vm)}
 }}
@Composable fun Planner(vm:MainVm,blocks:List<TimeBlock>){
 var title by remember{mutableStateOf("")};var duration by remember{mutableIntStateOf(60)};val scope=rememberCoroutineScope()
 LazyColumn{item{Section("Planner","Reserve your attention before the day spends it for you.");OutlinedTextField(title,{title=it},Modifier.padding(horizontal=16.dp).fillMaxWidth(),label={Text("Time block")});Row(Modifier.padding(16.dp,8.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(30,60,90,120).forEach{m->FilterChip(duration==m,{duration=m},{Text("$m min")})}};Button({if(title.isNotBlank()){val s=System.currentTimeMillis()+3600000;scope.launch{vm.db.dao().addBlock(TimeBlock(title=title,startAt=s,endAt=s+duration*60000L));title=""}}},Modifier.padding(horizontal=16.dp).fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" Add next time block")}}
  items(blocks,key={it.id}){b->Card(Modifier.padding(horizontal=16.dp,vertical=5.dp).fillMaxWidth()){ListItem(headlineContent={Text(b.title)},supportingContent={Text("${fmt(b.startAt)} – ${SimpleDateFormat("h:mm a",Locale.getDefault()).format(Date(b.endAt))}")},leadingContent={Icon(Icons.Default.CalendarMonth,null)},trailingContent={IconButton({scope.launch{vm.db.dao().deleteBlock(b)}}){Icon(Icons.Default.DeleteOutline,"Delete block")}})}}
 }
}
@Composable fun Matrix(vm:MainVm){
 val ts by vm.tasks.collectAsStateWithLifecycle()
 val groups=listOf("Do now" to ts.filter{it.urgent&&it.important},"Schedule" to ts.filter{!it.urgent&&it.important},"Delegate / simplify" to ts.filter{it.urgent&&!it.important},"Eliminate" to ts.filter{!it.urgent&&!it.important})
 LazyColumn{
  item{Section("Eisenhower Matrix","Separate urgency from importance.")}
  groups.forEach{(name,list)->
   item{Text(name,Modifier.padding(16.dp,12.dp,16.dp,4.dp),fontWeight=FontWeight.Bold)}
   items(list,key={it.id}){task->TaskRow(task,{vm.toggle(task)},{vm.del(task)})}
  }
 }
}
@Composable fun Projects(vm:MainVm,ps:List<Project>){var n by remember{mutableStateOf("")};val scope=rememberCoroutineScope();LazyColumn{item{Section("Projects","Group tasks around meaningful outcomes.");QuickAdd(n,{n=it},"New project"){if(n.isNotBlank()){scope.launch{vm.db.dao().addProject(Project(name=n))};n=""}}};items(ps){ListItem(headlineContent={Text(it.name)},supportingContent={Text(it.description.ifBlank{"Active project"})},leadingContent={Icon(Icons.Default.Folder,null)})}}}
@Composable fun Goals(vm:MainVm,gs:List<Goal>){
 var n by remember{mutableStateOf("")};val scope=rememberCoroutineScope()
 LazyColumn{item{Section("Goals","Turn long-term intentions into measurable progress.");QuickAdd(n,{n=it},"New goal"){if(n.isNotBlank()){scope.launch{vm.db.dao().addGoal(Goal(title=n))};n=""}}}
  items(gs){g->Card(Modifier.padding(horizontal=16.dp,vertical=6.dp).fillMaxWidth()){Column(Modifier.padding(14.dp)){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Flag,null);Spacer(Modifier.width(8.dp));Text(g.title,Modifier.weight(1f),fontWeight=FontWeight.Bold);Text("${g.progress}%")};Spacer(Modifier.height(8.dp));LinearProgressIndicator(progress={g.progress/100f},modifier=Modifier.fillMaxWidth());Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){IconButton({scope.launch{vm.db.dao().updateGoal(g.copy(progress=(g.progress-10).coerceAtLeast(0)))}}){Icon(Icons.Default.RemoveCircleOutline,"Decrease")};IconButton({scope.launch{vm.db.dao().updateGoal(g.copy(progress=(g.progress+10).coerceAtMost(100)))}}){Icon(Icons.Default.AddCircleOutline,"Increase")}}}}}
 }
}
@Composable fun Review(vm:MainVm){var wins by remember{mutableStateOf("")};var lessons by remember{mutableStateOf("")};var next by remember{mutableStateOf("")};val scope=rememberCoroutineScope();Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Weekly Review",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("Close the loop: celebrate, learn, then choose the next priorities.");OutlinedTextField(wins,{wins=it},Modifier.fillMaxWidth(),label={Text("Wins this week")});OutlinedTextField(lessons,{lessons=it},Modifier.fillMaxWidth(),label={Text("What did I learn?")});OutlinedTextField(next,{next=it},Modifier.fillMaxWidth(),label={Text("Top priorities next week")});Button({scope.launch{vm.db.dao().addReview(WeeklyReview(wins=wins,lessons=lessons,nextWeek=next))};wins="";lessons="";next=""},Modifier.fillMaxWidth()){Text("Save weekly review")}}}
@Composable fun Section(t:String,s:String){Column(Modifier.padding(20.dp,18.dp,20.dp,4.dp)){Text(t,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(s,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable fun QuickAdd(v:String,on:(String)->Unit,label:String,go:()->Unit){Row(Modifier.padding(16.dp)){OutlinedTextField(v,on,Modifier.weight(1f),label={Text(label)});IconButton(go){Icon(Icons.Default.AddCircle,null)}}}
fun fmt(v:Long)=SimpleDateFormat("EEE d MMM, h:mm a",Locale.getDefault()).format(Date(v))

@Composable fun PlannerScreen(vm:MainVm){val blocks by vm.blocks.collectAsStateWithLifecycle();Planner(vm,blocks)}
