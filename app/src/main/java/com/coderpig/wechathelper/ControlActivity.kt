package com.coderpig.wechathelper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_control.*

/**
 * 描述：辅助服务控制页
 *
 * @author CoderPig on 2018/04/12 10:50.
 */
class ControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        initView()
    }

    private fun initView() {
        ed_group_name.setText(Hawk.get(Constant.GROUP_NAME, ""))
        cb_add_friends.isChecked = Hawk.get(Constant.ADD_FRIENDS,false)
        cb_friends_square.isChecked = Hawk.get(Constant.FRIEND_SQUARE,false)
        cb_catch_red_packet.isChecked = Hawk.get(Constant.RED_PACKET,false)
        btn_sure.setOnClickListener({
            Hawk.put(Constant.GROUP_NAME, ed_group_name.text.toString())
            shortToast("群聊名称已保存！")
        })
        btn_clear.setOnClickListener({
            Hawk.put(Constant.GROUP_NAME, "")
            shortToast("群聊名称已清除！")
            ed_group_name.setText("")
        })
        btn_open_accessbility.setOnClickListener({
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        cb_add_friends.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.ADD_FRIENDS, true) else Hawk.put(Constant.ADD_FRIENDS, false)
        }
        cb_friends_square.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.FRIEND_SQUARE, true) else Hawk.put(Constant.FRIEND_SQUARE, false)
        }
        cb_catch_red_packet.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.RED_PACKET, true) else Hawk.put(Constant.RED_PACKET, false)
        }
    }


}
