package com.vm2st.countdown
import com.vm2st.countdown.util.remaining
import org.junit.Assert.*
import org.junit.Test
class TimeFormatTest { @Test fun splitsDuration(){ val r=remaining(90061000,1000); assertEquals(1,r.days); assertEquals(1,r.hours); assertEquals(1,r.minutes); assertEquals(0,r.seconds); assertFalse(r.expired) } }
