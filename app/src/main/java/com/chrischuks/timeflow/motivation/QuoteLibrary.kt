package com.chrischuks.timeflow.motivation
import java.time.LocalDate

data class MotivationalQuote(val text:String,val author:String="TimeFlow")
object QuoteLibrary {
 private val starts=listOf(
 "Begin where you are","Protect your time","Choose progress","Start before you feel ready","Do the important thing first","Give your best hour to your best work","Small steps still move you forward","Discipline makes room for freedom","Your future is shaped by today's choices","Make today count",
 "Focus on what you can control","Consistency beats occasional intensity","A clear priority creates a clear day","Action creates momentum","Finish what matters","Do not confuse movement with progress","One focused hour can change a day","Make the next decision a good one","Build the habit, then trust the habit","Your attention is valuable",
 "Plan with purpose","Work with patience","Keep promises you make to yourself","Courage often begins with one small action","Progress grows quietly","A difficult task becomes lighter once begun","Use your energy deliberately","You do not need a perfect day to make progress","Let your priorities guide your calendar","What you repeat becomes your direction",
 "Today is useful when you use it intentionally","Be faithful to the work in front of you","Make room for deep work","Your goals need time on your calendar","Do less, but do it well","A focused mind multiplies effort","Turn intention into a scheduled action","Do the next right thing","Make discipline easier by making decisions early","Give important work an appointment",
 "Your pace can be steady and still be powerful","Keep going after motivation fades","Preparation reduces procrastination","Your habits vote for the person you are becoming","A strong day begins with a clear choice","Be patient with results and persistent with effort","Time invested with purpose is never wasted","Make your minutes serve your mission","Create momentum before seeking perfection","A meaningful life is built in ordinary hours"
 )
 private val endings=listOf(
 "and take one useful step now.","because attention determines direction.","even when the step feels small.","and let consistency do the heavy lifting.","before distractions choose for you.","then give it your full attention.","and measure progress by action.","because tomorrow benefits from today's discipline."
 )
 val all:List<MotivationalQuote> = buildList {
   starts.forEachIndexed { i,s -> endings.forEachIndexed { j,e -> add(MotivationalQuote("$s, $e")) } }
 }.distinctBy{it.text}
 fun today(date:LocalDate=LocalDate.now()):MotivationalQuote = all[Math.floorMod(date.toEpochDay().toInt(),all.size)]
}
