package com.org.msrouter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.org.msrouter_api.MSRouter
import kotlinx.android.synthetic.main.activity_main.*

@Route(path = "/app/mainpage")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupClick()
    }

    private fun setupClick() {
        pushShopCartTextView.setOnClickListener {
            MSRouter.build(PathConst.kShopCartActivityURI).navigation()
        }

        pushProductDetailTextView.setOnClickListener {
            MSRouter.build(PathConst.kProductDetailActivityURI).withString("productId","1002").navigation()
        }

        pushAccountTextView.setOnClickListener {
            MSRouter.build(PathConst.kAccountActivityURI).navigation()
        }

    }
}
