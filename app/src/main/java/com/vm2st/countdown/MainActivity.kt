package com.vm2st.countdown

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vm2st.countdown.data.*
import com.vm2st.countdown.ui.CountdownTheme
import com.vm2st.countdown.util.compactRemaining
import com.vm2st.countdown.widget.WidgetPinning
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){ super.onCreate(savedInstanceState); val storage=CountdownStorage(this); setContent { val theme by storage.theme.collectAsStateWithLifecycle(); CountdownTheme(theme){ App(storage) } } }
}

private val palette=listOf(0xFF892929,0xFF9C2746,0xFF3B2D51,0xFF163B52,0xFF145A4A,0xFF5A3A18,0xFF25262B,0xFF6A356A)
private val styleNames=listOf("Компактный 2×1","Широкий 3×1","Квадрат 2×2","Панорама 4×1","Карточка 3×2","Большой 4×2","Вертикальный 2×3","Постер 3×3","Экран 4×3","Максимальный 4×4")

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun App(storage:CountdownStorage){
    val data by storage.items.collectAsStateWithLifecycle(); var add by remember{mutableStateOf(false)}; var menu by remember{mutableStateOf(false)}; var remove by remember{mutableStateOf<Countdown?>(null)}; val snack=remember{SnackbarHostState()}; val scope=rememberCoroutineScope(); val context=LocalContext.current
    Scaffold(snackbarHost={SnackbarHost(snack)},topBar={TopAppBar(title={Column{Text("Бордовый отсчёт",fontWeight=FontWeight.Bold);Text("vm2 studios • @vm2_studios",style=MaterialTheme.typography.labelSmall)}},actions={IconButton(onClick={menu=true}){Icon(Icons.Default.MoreVert,"Темы")};DropdownMenu(menu,{menu=false}){ThemeItem("Светлая",AppTheme.LIGHT,storage){menu=false};ThemeItem("Тёмная",AppTheme.DARK,storage){menu=false};ThemeItem("Бордовая",AppTheme.WINE,storage){menu=false}}})},floatingActionButton={ExtendedFloatingActionButton(onClick={add=true},icon={Icon(Icons.Default.Add,null)},text={Text("Новый отсчёт")})}){ padding->
        if(data.isEmpty()) Box(Modifier.fillMaxSize().padding(padding).padding(32.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.HourglassEmpty,null,Modifier.size(64.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.height(16.dp));Text("Создайте первый отсчёт",style=MaterialTheme.typography.headlineSmall);Text("Выберите дату, стиль и цвет виджета",color=MaterialTheme.colorScheme.onSurfaceVariant)}}
        else LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp,12.dp,16.dp,96.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){items(data,key={it.id}){item->CountdownCard(item,onPin={if(WidgetPinning.request(context,item)) scope.launch{snack.showSnackbar("Подтвердите добавление на рабочий стол")} else scope.launch{snack.showSnackbar("На этом Android добавьте виджет через меню рабочего стола")}},onDelete={remove=item})}}
    }
    if(add) NewDialog(onDismiss={add=false},onCreate={storage.add(it);add=false})
    remove?.let{ item->AlertDialog(onDismissRequest={remove=null},title={Text("Удалить отсчёт?")},text={Text(item.title)},confirmButton={TextButton(onClick={storage.delete(item.id);WidgetPinning.refreshAll(context);remove=null}){Text("Удалить")}},dismissButton={TextButton(onClick={remove=null}){Text("Отмена")}}) }
}
@Composable private fun ThemeItem(text:String,theme:AppTheme,storage:CountdownStorage,close:()->Unit)=DropdownMenuItem(text={Text(text)},onClick={storage.setTheme(theme);close()})

@Composable private fun CountdownCard(item:Countdown,onPin:()->Unit,onDelete:()->Unit){
    var now by remember{mutableLongStateOf(System.currentTimeMillis())}; LaunchedEffect(Unit){while(isActive){delay(1000);now=System.currentTimeMillis()}}
    Card(colors=CardDefaults.cardColors(containerColor=Color(item.color)),shape=RoundedCornerShape(24.dp)){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(item.title,color=Color.White,fontWeight=FontWeight.SemiBold);Text(compactRemaining(item.targetMillis,now),color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(item.targetMillis)),color=Color.White.copy(.8f),style=MaterialTheme.typography.labelMedium);Text(styleNames[item.style.coerceIn(0, styleNames.lastIndex)],color=Color.White.copy(.65f),style=MaterialTheme.typography.labelSmall)};Column{IconButton(onClick=onPin){Icon(Icons.Default.AddToHomeScreen,"На рабочий стол",tint=Color.White)};IconButton(onClick=onDelete){Icon(Icons.Default.DeleteOutline,"Удалить",tint=Color.White.copy(.85f))}}}}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun NewDialog(onDismiss:()->Unit,onCreate:(Countdown)->Unit){
    val context=LocalContext.current; var title by remember{mutableStateOf("")}; var target by remember{mutableLongStateOf(System.currentTimeMillis()+86400000)}; var style by remember{mutableIntStateOf(0)}; var color by remember{mutableStateOf(palette.first())}
    fun pickDate(){val c=Calendar.getInstance().apply{timeInMillis=target};DatePickerDialog(context,{_,y,m,d->TimePickerDialog(context,{_,h,min->target=Calendar.getInstance().apply{set(y,m,d,h,min,0);set(Calendar.MILLISECOND,0)}.timeInMillis},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).apply{datePicker.minDate=System.currentTimeMillis()-1000}.show()}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Новый отсчёт")},text={Column(Modifier.fillMaxWidth()){OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("Название (необязательно)")},singleLine=true);Spacer(Modifier.height(10.dp));OutlinedButton(onClick={pickDate()},Modifier.fillMaxWidth()){Icon(Icons.Default.Event,null);Spacer(Modifier.width(8.dp));Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(target)))};Text("Стиль и размер",Modifier.padding(top=14.dp,bottom=6.dp),fontWeight=FontWeight.SemiBold);LazyVerticalGrid(GridCells.Fixed(2),Modifier.height(220.dp),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){itemsIndexed(styleNames){i,name->Surface(Modifier.fillMaxWidth().clickable{style=i},shape=RoundedCornerShape(14.dp),color=if(style==i)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant){Column(Modifier.padding(10.dp)){Text("${i+1}",fontWeight=FontWeight.Bold);Text(name,style=MaterialTheme.typography.labelMedium)}}}};Text("Цвет",Modifier.padding(top=14.dp,bottom=6.dp),fontWeight=FontWeight.SemiBold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){palette.forEach{c->Box(Modifier.size(if(color==c)34.dp else 28.dp).background(Color(c),CircleShape).clickable{color=c})}}}},confirmButton={Button(enabled=target>System.currentTimeMillis(),onClick={val fallback="До "+DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT).format(Date(target));onCreate(Countdown(UUID.randomUUID().toString(),title.trim().ifBlank{fallback},target,style,color,System.currentTimeMillis()))}){Text("Создать")}},dismissButton={TextButton(onClick=onDismiss){Text("Отмена")}})
}
