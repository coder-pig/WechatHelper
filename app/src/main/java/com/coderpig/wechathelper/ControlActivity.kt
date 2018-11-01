package com.coderpig.wechathelper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import com.google.gson.Gson
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
        cb_add_friends.isChecked = Hawk.get(Constant.ADD_FRIENDS,false)
        cb_friends_square.isChecked = Hawk.get(Constant.FRIEND_SQUARE,false)
        cb_catch_red_packet.isChecked = Hawk.get(Constant.RED_PACKET,false)

        btn_write.setOnClickListener {
            val member = Gson().fromJson(ed_friends.text.toString(), Member::class.java)
            Hawk.put(Constant.MEMBER, member)
            shortToast("数据写入成功！")
        }

        btn_reset.setOnClickListener {
            Hawk.put(Constant.MEMBER, Member())
            ed_friends.setText("")
            shortToast("数据重置成功！")
        }

        btn_open_wechat.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }
        btn_open_accessbility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
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
