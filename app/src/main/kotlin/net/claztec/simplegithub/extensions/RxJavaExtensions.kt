package net.claztec.simplegithub.extensions

import io.reactivex.disposables.Disposable
import net.claztec.simplegithub.rx.AutoClearedDisposable

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable) = this.add(disposable)