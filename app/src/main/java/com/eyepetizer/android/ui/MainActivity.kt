/*
 * Copyright (c) 2020. vipyinzhiwei <vipyinzhiwei@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eyepetizer.android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.eyepetizer.android.R
import com.eyepetizer.android.event.MessageEvent
import com.eyepetizer.android.event.RefreshEvent
import com.eyepetizer.android.event.SwitchPagesEvent
import com.eyepetizer.android.extension.logD
import com.eyepetizer.android.extension.setOnClickListener
import com.eyepetizer.android.extension.showToast
import com.eyepetizer.android.ui.common.ui.BaseActivity
import com.eyepetizer.android.ui.community.CommunityFragment
import com.eyepetizer.android.ui.community.commend.CommendFragment
import com.eyepetizer.android.ui.home.HomePageFragment
import com.eyepetizer.android.ui.login.LoginActivity
import com.eyepetizer.android.ui.mine.MineFragment
import com.eyepetizer.android.ui.notification.NotificationFragment
import com.eyepetizer.android.util.DialogAppraiseTipsWorker
import com.eyepetizer.android.util.GlobalUtil
import kotlinx.android.synthetic.main.layout_bottom_navigation_bar.*
import org.greenrobot.eventbus.EventBus

/**
 * Eyepetizer的主界面。
 *
 * @author vipyinzhiwei
 * @since  2020/5/29
 */
class MainActivity : BaseActivity() {

    private var backPressTime = 0L

    private var homePageFragment: HomePageFragment? = null

    private var communityFragment: CommunityFragment? = null

    private var notificationFragment: NotificationFragment? = null

    private var mineFragment: MineFragment? = null

    private val fragmentManager: FragmentManager by lazy { supportFragmentManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun setupViews() {
        observe()
        // 添加点击事件
        setOnClickListener(btnHomePage, btnCommunity, btnNotification, ivRelease, btnMine) {
            when (this) {
                btnHomePage -> {
                    notificationUiRefresh(0)
                    setTabSelection(0)
                }
                btnCommunity -> {
                    notificationUiRefresh(1)
                    setTabSelection(1)
                }
                btnNotification -> {
                    notificationUiRefresh(2)
                    setTabSelection(2)
                }
                ivRelease -> {  //  button +
                    LoginActivity.start(this@MainActivity)
                }
                btnMine -> {
                    notificationUiRefresh(3)
                    setTabSelection(3)
                }
            }
        }
        //  设置选中状态
        setTabSelection(0)  //  开启 app 的时候打开的是首页所以这里传入 0
    }

    override fun onMessageEvent(messageEvent: MessageEvent) {
        super.onMessageEvent(messageEvent)
        when {
            messageEvent is SwitchPagesEvent && CommendFragment::class.java == messageEvent.activityClass -> {
                btnCommunity.performClick()
            }
            else -> {
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            processBackPressed()
        }
    }

    private fun processBackPressed() {
        val now = System.currentTimeMillis()
        if (now - backPressTime > 2000) {
            String.format(GlobalUtil.getString(R.string.press_again_to_exit), GlobalUtil.appName).showToast()
            backPressTime = now
        } else {
            super.onBackPressed()
        }
    }

    // 设置选中状态， 被选中的按钮加载对应的 fragment
    private fun setTabSelection(index: Int) {
        clearAllSelected()  // 首先清除所有已选中状态
        fragmentManager.beginTransaction().apply {
            hideFragments(this)
            when (index) {
                0 -> {
                    ivHomePage.isSelected = true
                    tvHomePage.isSelected = true
                    if (homePageFragment == null) {
                        homePageFragment = HomePageFragment.newInstance()
                        add(R.id.homeActivityFragContainer, homePageFragment!!)     // 把homePageFragment 加载到 主页面的 homeActivityFragContainer
                    } else {
                        show(homePageFragment!!)
                    }
                }
                1 -> {
                    ivCommunity.isSelected = true
                    tvCommunity.isSelected = true
                    if (communityFragment == null) {
                        communityFragment = CommunityFragment()
                        add(R.id.homeActivityFragContainer, communityFragment!!)
                    } else {
                        show(communityFragment!!)
                    }
                }
                2 -> {
                    ivNotification.isSelected = true
                    tvNotification.isSelected = true
                    if (notificationFragment == null) {
                        notificationFragment = NotificationFragment()
                        add(R.id.homeActivityFragContainer, notificationFragment!!)
                    } else {
                        show(notificationFragment!!)
                    }
                }
                3 -> {
                    ivMine.isSelected = true
                    tvMine.isSelected = true
                    if (mineFragment == null) {
                        mineFragment = MineFragment.newInstance()
                        add(R.id.homeActivityFragContainer, mineFragment!!)
                    } else {
                        show(mineFragment!!)
                    }
                }
                else -> {
                    ivHomePage.isSelected = true
                    tvHomePage.isSelected = true
                    if (homePageFragment == null) {
                        homePageFragment = HomePageFragment.newInstance()
                        add(R.id.homeActivityFragContainer, homePageFragment!!)
                    } else {
                        show(homePageFragment!!)
                    }
                }
            }
        }.commitAllowingStateLoss()
    }

    private fun clearAllSelected() {
        ivHomePage.isSelected = false
        tvHomePage.isSelected = false
        ivCommunity.isSelected = false
        tvCommunity.isSelected = false
        ivNotification.isSelected = false
        tvNotification.isSelected = false
        ivMine.isSelected = false
        tvMine.isSelected = false
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        transaction.run {
            if (homePageFragment != null) hide(homePageFragment!!)
            if (communityFragment != null) hide(communityFragment!!)
            if (notificationFragment != null) hide(notificationFragment!!)
            if (mineFragment != null) hide(mineFragment!!)
        }
    }

    private fun notificationUiRefresh(selectionIndex: Int) {
        when (selectionIndex) {
            0 -> {
                if (ivHomePage.isSelected) EventBus.getDefault().post(RefreshEvent(HomePageFragment::class.java))
            }
            1 -> {
                if (ivCommunity.isSelected) EventBus.getDefault().post(RefreshEvent(CommunityFragment::class.java))
            }
            2 -> {
                if (ivNotification.isSelected) EventBus.getDefault().post(RefreshEvent(NotificationFragment::class.java))
            }
            3 -> {
                if (ivMine.isSelected) EventBus.getDefault().post(RefreshEvent(MineFragment::class.java))
            }
        }
    }

    private fun observe() {
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(DialogAppraiseTipsWorker.showDialogWorkRequest.id).observe(this, Observer { workInfo ->
            logD(TAG, "observe: workInfo.state = ${workInfo.state}")
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                WorkManager.getInstance(this).cancelAllWork()
            } else if (workInfo.state == WorkInfo.State.RUNNING) {
                if (isActive) {
                    DialogAppraiseTipsWorker.showDialog(this)
                    WorkManager.getInstance(this).cancelAllWork()
                }
            }
        })
    }

    companion object {

        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}