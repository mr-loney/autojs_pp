package com.pp.autojs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import android.view.View
import android.widget.ImageView
import androidx.multidex.MultiDexApplication
import com.pp.app.autojs.AutoJs
import com.pp.app.receiver.DynamicBroadcastReceivers
import com.pp.app.timing.TimedTaskManager
import com.pp.app.timing.TimedTaskScheduler
import com.pp.app.util.Drawables
import com.pp.app.GlobalAppContext
import com.pp.app.ImageLoader
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Stardust on 2017/1/27.
 */

class App : MultiDexApplication() {
    lateinit var dynamicBroadcastReceivers: DynamicBroadcastReceivers
        private set

    override fun onCreate() {
        super.onCreate()
        GlobalAppContext.set(this)
        instance = WeakReference(this)
        init()
    }

    private fun init() {
        AutoJs.initInstance(this)
        setupDrawableImageLoader()
        TimedTaskScheduler.init(this)
        initDynamicBroadcastReceivers()
    }

    @SuppressLint("CheckResult")
    private fun initDynamicBroadcastReceivers() {
        dynamicBroadcastReceivers = DynamicBroadcastReceivers(this)
        val localActions = ArrayList<String>()
        val actions = ArrayList<String>()
        TimedTaskManager.getInstance().allIntentTasks
                .filter { task -> task.action != null }
                .doOnComplete {
                    if (localActions.isNotEmpty()) {
                        dynamicBroadcastReceivers.register(localActions, true)
                    }
                    if (actions.isNotEmpty()) {
                        dynamicBroadcastReceivers.register(actions, false)
                    }
//                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(
//                            DynamicBroadcastReceivers.ACTION_STARTUP
//                    ))
                }
                .subscribe({
                    if (it.isLocal) {
                        localActions.add(it.action)
                    } else {
                        actions.add(it.action)
                    }
                }, { it.printStackTrace() })


    }

    private fun setupDrawableImageLoader() {
        Drawables.setDefaultImageLoader(object : ImageLoader,
            com.pp.autojs.core.ui.inflater.ImageLoader {
            override fun loadInto(imageView: ImageView, uri: Uri) {
                Glide.with(imageView)
                        .load(uri)
                        .into(imageView)
            }

            override fun loadIntoBackground(view: View, uri: Uri) {
                Glide.with(view)
                        .load(uri)
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                view.background = resource
                            }
                        })
            }

            override fun load(view: View?, uri: Uri?): Drawable {
                throw UnsupportedOperationException()
            }

            override fun load(view: View?, uri: Uri?, callback: ImageLoader.DrawableCallback?) {
                if (view != null) {
                    Glide.with(view)
                        .load(uri)
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                callback?.onLoaded(resource)
                            }
                        })
                }
            }

            override fun load(view: View?, uri: Uri?, callback: ImageLoader.BitmapCallback?) {
                if (view != null) {
                    Glide.with(view)
                        .asBitmap()
                        .load(uri)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                callback?.onLoaded(resource)
                            }
                        })
                }
            }

            override fun load(
                view: View?,
                uri: Uri?,
                callback: com.pp.autojs.core.ui.inflater.ImageLoader.DrawableCallback?
            ) {
                if (view != null) {
                    Glide.with(view)
                        .load(uri)
                        .into(object : SimpleTarget<Drawable>() {
                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                callback?.onLoaded(resource)
                            }
                        })
                }
            }

            override fun load(
                view: View?,
                uri: Uri?,
                callback: com.pp.autojs.core.ui.inflater.ImageLoader.BitmapCallback?
            ) {
                if (view != null) {
                    Glide.with(view)
                        .asBitmap()
                        .load(uri)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                callback?.onLoaded(resource)
                            }
                        })
                }
            }
        })
    }

    companion object {

        private val TAG = "App"

        private lateinit var instance: WeakReference<App>

        val app: App
            get() = instance.get()!!
    }


}
