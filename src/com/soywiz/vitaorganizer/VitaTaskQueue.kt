package com.soywiz.vitaorganizer

import com.soywiz.vitaorganizer.tasks.VitaTask
import java.util.*

class VitaTaskQueue(val vitaOrganizer: VitaOrganizer) {
	var running = false; private set
	private val tasks: Queue<() -> Unit> = LinkedList<() -> Unit>()
	val thread = Thread {
		while (vitaOrganizer.isVisible) {
			Thread.sleep(10L)
			val task = synchronized(tasks) { if (tasks.isNotEmpty()) tasks.remove() else null }
			if (task != null) {
				try {
					running = true
					task()
				} catch (t: Throwable) {
					t.printStackTrace()
				} finally {
					running = false
				}
			}
		}
	}.apply {
		isDaemon = true
		start()
	}

	fun queue(task: VitaTask) {
		try {
			task.checkBeforeQueue()
			synchronized(tasks) {
				tasks += { task.perform() }
			}
		} catch (t: Throwable) {
			t.printStackTrace()
		}
	}

	fun queue(task: () -> Unit) {
		try {
			synchronized(tasks) {
				tasks += task
			}
		} catch (t: Throwable) {
			t.printStackTrace()
		}
	}
}