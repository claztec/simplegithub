package net.claztec.simplegithub.extensions

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.claztec.simplegithub.rx.AutoClearedDisposable

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)

fun runOnIoScheduler(func: () -> Unit): Disposable
    = Completable.fromCallable(func).subscribeOn(Schedulers.io()).subscribe()