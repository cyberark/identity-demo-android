package com.cyberark.mfa.scenario2

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cyberark.identity.util.preferences.CyberArkPreferenceUtil
import com.cyberark.mfa.R
import com.cyberark.mfa.utils.PreferenceConstants

class TransferFundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_fund)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        title = getString(R.string.acme)
        updateUI()
    }

    /**
     * Update UI elements
     *
     */
    private fun updateUI() {
        setupHyperlink()
        val enterAmount = findViewById<TextView>(R.id.enter_amount)
        val editTextAmount = findViewById<EditText>(R.id.edit_text_amount)

        // set edit text focus change listener
        editTextAmount.onFocusChangeListener  = View.OnFocusChangeListener { view, status ->
            if (status){
                enterAmount.visibility = View.GONE
            }
        }
        findViewById<Button>(R.id.button_transfer_funds).setOnClickListener {
            if(editTextAmount.text.trim().isEmpty()) {
                enterAmount.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Invoke MFA Widget", Toast.LENGTH_SHORT).show()
            }
            editTextAmount.clearFocus()
        }

        // Get the shared preference status and update the biometrics selection
        if (!CyberArkPreferenceUtil.contains(PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL)) {
            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_APP_LAUNCH_NL,
                true
            )
            CyberArkPreferenceUtil.putBoolean(
                PreferenceConstants.INVOKE_BIOMETRICS_ON_TRANSFER_FUND_NL,
                true
            )
        }
    }

    private fun setupHyperlink() {
        val apiDocsMFAWidgetView: TextView = findViewById(R.id.api_doc_mfa_widget)
        apiDocsMFAWidgetView.movementMethod = LinkMovementMethod.getInstance()
        val apiDocsView: TextView = findViewById(R.id.api_doc)
        apiDocsView.movementMethod = LinkMovementMethod.getInstance()
    }

    // **************** Handle menu settings click action Start *********************** //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            //Start Settings activity
            val intent = Intent(this, NativeLoginSettingsActivity::class.java)
            intent.putExtra("from_activity", "TransferFundActivity")
            startActivity(intent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }
    // **************** Handle menu settings click action End *********************** //
}