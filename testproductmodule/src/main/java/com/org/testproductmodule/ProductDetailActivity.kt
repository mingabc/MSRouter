package com.org.testproductmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.org.msrouter_api.MSRouter
import kotlinx.android.synthetic.main.productdetail_activity_main.*

@Route(path= "/productdetail/main")
public class ProductDetailActivity : AppCompatActivity(){

    @JvmField
    @Autowired
    public var productId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.productdetail_activity_main)
        title = "商祥页"
        setupInjectRouter()
        setupClick()
        setupUI()
    }

    private fun setupInjectRouter() {
       MSRouter.inject(this)
    }

    private fun setupClick() {

        addShopCartBtn.setOnClickListener {
            this@ProductDetailActivity.requestAddShopCart()
        }

        goShopCartBtn.setOnClickListener {
            MSRouter.build(PathConst.kShopCartActivityURI).navigation()
        }


    }


    private fun setupUI() {
        productIdView.text = "Autowired productId: ${productId}  }"
    }


    private fun requestAddShopCart() {

        val model = AddCartParaModel()
        model.productId = productId
        MSRouter.request(PathConst.kAddCartURIPath, model).response(AddCartResponseModel::class.java){
            val txt = "code = ${it?.code} msg = ${it?.msg}"
            showToast(txt)
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }


}
