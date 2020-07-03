package com.org.testaccountmodule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

/**
 * @ClassName: AccountActivity
 * @Description: account activity
 * @Author: ms
 * @Date: 2020/7/3 12:47 AM
 */
@Route(path = "/account/main")
class AccountActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_activity_main)
        title = "我的账户"
    }
}

