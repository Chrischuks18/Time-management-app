package com.chrischuks.timeflow.motivation
import android.content.Context
import java.time.LocalDate

enum class QuoteCategory(val label:String){SUCCESS("Success"),DISCIPLINE("Discipline"),FAITH("Faith"),COURAGE("Courage"),PRODUCTIVITY("Productivity"),PERSEVERANCE("Perseverance"),LEADERSHIP("Leadership"),HOPE("Hope"),GROWTH("Personal Growth")}
data class MotivationalQuote(val text:String,val category:QuoteCategory,val author:String="TimeFlow")

object MotivationPreferences{
 private const val PREF="motivation_preferences"; private const val KEY="categories"
 fun selected(c:Context):Set<QuoteCategory>{val raw=c.getSharedPreferences(PREF,0).getStringSet(KEY,null)?:return QuoteCategory.entries.toSet();return raw.mapNotNull{runCatching{QuoteCategory.valueOf(it)}.getOrNull()}.toSet().ifEmpty{QuoteCategory.entries.toSet()}}
 fun set(c:Context,values:Set<QuoteCategory>)=c.getSharedPreferences(PREF,0).edit().putStringSet(KEY,values.ifEmpty{QuoteCategory.entries.toSet()}.map{it.name}.toSet()).apply()
}

object QuoteLibrary {
 private val seeds=mapOf(
 QuoteCategory.SUCCESS to listOf("Success grows from useful work repeated","Define success by meaningful progress","Make achievement the result of service and discipline","A worthy goal deserves a worthy effort","Build results one deliberate decision at a time"),
 QuoteCategory.DISCIPLINE to listOf("Discipline protects the goals your moods may forget","Keep the promise you made to your future self","Do what matters even when it is inconvenient","Structure turns good intentions into dependable action","A disciplined hour can rescue an unfocused day"),
 QuoteCategory.FAITH to listOf("Walk faithfully even when you cannot see the whole road","Let faith give courage to today's responsibility","Pray with trust and work with diligence","Hope in God does not excuse effort; it strengthens it","Be faithful in the small duty placed before you"),
 QuoteCategory.COURAGE to listOf("Courage begins when you act despite uncertainty","Do not wait for fear to disappear before moving","Face the difficult task while it is still small","Bravery is often a quiet decision to continue","Choose the honest difficult step over the comfortable excuse"),
 QuoteCategory.PRODUCTIVITY to listOf("Protect your best hours for your most important work","A short priority list can create a powerful day","Schedule what matters before distractions fill the space","Finish one meaningful task before collecting ten new ones","Attention is a resource; spend it deliberately"),
 QuoteCategory.PERSEVERANCE to listOf("Continue when progress becomes less exciting","Slow progress still rewards persistence","A setback can interrupt the plan without ending the mission","Keep showing up long enough for effort to compound","When the road is long, shorten your attention to the next step"),
 QuoteCategory.LEADERSHIP to listOf("Lead first by the standard you live","Responsibility grows where excuses end","Good leadership makes other people stronger","Listen carefully before asking others to follow","Use influence to create clarity, courage and service"),
 QuoteCategory.HOPE to listOf("A difficult morning does not decide the whole day","There is still useful work to do and good to pursue","Hope gives tomorrow a reason to receive today's effort","Do not surrender the future to one disappointing moment","Begin again with what remains possible"),
 QuoteCategory.GROWTH to listOf("Growth often feels like practicing what is not yet natural","Learn from yesterday without living inside it","Become a little more capable with every honest attempt","Correction is useful when it becomes action","The person you become is shaped by what you repeatedly practice")
 )
 private val endings=listOf("Take one useful step now.","Give it your full attention today.","Let consistent action carry it forward.","Turn the thought into a scheduled action.","Begin with the next right thing.","Make room for it in your day.","Choose progress over perfection.","Let today's choices support tomorrow.")
 val all:List<MotivationalQuote> = buildList{seeds.forEach{(cat,ss)->ss.forEach{s->endings.forEach{e->add(MotivationalQuote("$s. $e",cat))}}}} // 360 curated combinations
 fun today(context:Context,date:LocalDate=LocalDate.now()):MotivationalQuote{val allowed=MotivationPreferences.selected(context);val pool=all.filter{it.category in allowed}.ifEmpty{all};return pool[Math.floorMod(date.toEpochDay().toInt(),pool.size)]}
}
